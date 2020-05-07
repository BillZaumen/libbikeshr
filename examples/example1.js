// Set up default values for variables that may
// be set from the command line.
//
// rtime: the length of the simulation in hours of simulation time
//        (the default is 2.0 hours)
//
// iat12: the interarriaval time in minutes for trips going from
//        hub1 to hub2 (the default is 10 minutes).
//
// iat12: the interarriaval time in minutes for trips going from
//        hub2 to hub1 (the default is 10 minutes).

// Set up an output stream.
var output = scripting.getWriter();

// Import the classes we will explicitly need from the
// bike-share library
scripting.importClasses("org.bzdev.bikeshare",
			["HubDataAdapter", "TripDataAdapter"]);

// Import the classes we will explicitly need from the
// bzdev class library
scripting.importClass("org.bzdev.drama.DramaSimulation");
scripting.importClass("org.bzdev.util.units.MKS");
scripting.importClass("org.bzdev.math.rv.GaussianRV");
scripting.importClass("org.bzdev.util.VarArgsFormatter");

// Set default values for comand-line variable that were not defined
if (typeof(rtime) == "undefined") rtime = 2.0;
if (typeof (iat12) == "undefined") iat12 = 10.0;
if (typeof (iat21) == "undefined") iat21 = 10.0;

// change command-line variables to standard units:
rtime = MKS.hours(rtime);
iat12 = MKS.minutes(iat12);
iat21 = MKS.minutes(iat21);

// Used due to Java 7 (Rhino ECMAScript implementation), which has
// trouble with variable numbers of arguments
var foutput = new VarArgsFormatter(output);

// configure the simulation so there are 1000 simulation ticks per
// second.
var sim = new DramaSimulation(scripting, 1000.0);

// create the factories we'll use
sim.createFactories("org.bzdev.bikeshare", {
    udf: "UsrDomainFactory",
    sdf: "SysDomainFactory",
    hbf: "BasicHubBalancerFactory",
    hf: "HubFactory",
    shf: "StorageHubFactory",
    dtf: "StdDelayTableFactory",
    btgf: "BasicTripGenFactory"
});

// Set up domains.
var usrDomain = udf.createObject("usrDomain");
var sysDomain = sdf.createObject("sysDomain");

// set up a load balancer
var balancer = hbf.createObject("balancer", {
    "quietPeriod": MKS.minutes(30), "threshold": 0.75, "sysDomain": sysDomain
});

// set up random variables.
var pickupTime = new GaussianRV(MKS.minutes(4.0), 30.0);
pickupTime.setMinimum(10.0, true);

var usrSpeedRV = new GaussianRV(MKS.mph(12.0), MKS.mph(3.0));
usrSpeedRV.setMinimum(MKS.mph(5.0), true);

var sysSpeedRV = new GaussianRV(MKS.mph(25.0), MKS.mph(3.0));
sysSpeedRV.setMinimum(MKS.mph(5.0), true);

// create hubs.
var hub1 = hf.createObject("hub1", {
    "x": 0.0, "y": 0.0,
    "capacity": 10, "lowerTrigger": 3, "nominal": 5, "upperTrigger": 7,
    "pickupTime": pickupTime,
    "count": 5, "overCount": 0,
    "usrDomain": usrDomain, "sysDomain": sysDomain});

var hub2 = hf.createObject("hub2", {
    "x": MKS.miles(1.0), "y": 0.0,
    "capacity": 10, "lowerTrigger": 3, "nominal": 5, "upperTrigger": 7,
    "pickupTime": pickupTime,
    "count": 5, "overCount": 0,
    "usrDomain": usrDomain, "sysDomain": sysDomain});

// create a delay table

var userTable = dtf.createObject("userTable", {
    "speedRV": usrSpeedRV, "dist": MKS.miles(1.0),
    "nStops": 4, "stopProbability": 0.4, "maxWait": 30.0,
    "distFraction": 1.0,
    "domains": [usrDomain]});

// create traffic generators
var tgen1 = btgf.createObject("tgen1", [
    {"startingHub": hub1, "meanIATime": iat12, "nBicycles": 1},
     {withPrefix: "dest",
      withKey: hub2, config: {"prob": 1.0, "overflowProb": 0.0}}]);

var tgen2 = btgf.createObject("tgen2", [
    {"startingHub": hub2, "meanIATime": iat21, "nBicycles": 1},
     {withPrefix: "dest",
      withKey: hub1,
      config: {"prob": 1.0, "overflowProb": 0.0}}]);

// print out the configuration of all the objects we created.
for each (var i in [usrDomain, sysDomain, balancer, hub1, hub2,
		    userTable, tgen1, tgen2]) {
    i.printConfiguration(output);
    output.println("---------------------");
}

// Track changes to each hub.
//
var dl = new HubDataAdapter(scripting, {
    hubChanged: function(hub, bc, newbc, oc, newoc, time, ticks) {
	foutput.format("at %g (ticks = %d), Hub %s: bc = %d (%b), "
		      +"oc = %d (%b)\n",
		      time, ticks, hub.getName(), bc, newbc, oc, newoc);
	foutput.flush();
    }
});

hub1.addHubDataListener(dl);
hub2.addHubDataListener(dl);

// Track status of each trip

var tl = new TripDataAdapter(scripting, {
    tripStarted: function(tripID, time, ticks, hub, domain) {
	foutput.format("trip %d at t=%g (ticks=%d), hub %s: trip started "
		       + "(domain = %s)\n",
		       tripID, time, ticks, hub.getName(), domain.getName());
	foutput.flush();
    },
    tripEnded: function (tripID, time, ticks, hub) {
	foutput.format("trip %d at %g (ticks=%d), hub %s: trip ended\n",
		      tripID, time, ticks, hub.getName());
	foutput.flush();
    },
    tripFailedAtStart: function(tripID, time, ticks, hub) {
	foutput.format("trip %d at %g (ticks=%d), hub %s: "
		      + "trip could not start\n",
		      tripID, time, ticks, hub.getName());
	foutput.flush();
    }
});

tgen1.addTripDataListener(tl);
tgen2.addTripDataListener(tl);

// run the simulation for 120 minutes of simulation time

sim.run(sim.getTicks(rtime));
