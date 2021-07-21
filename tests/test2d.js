// Set up an output stream.
var output = scripting.getWriter();

// Import the classes we will explicitly need from the
// bike-share library
scripting.importClasses("org.bzdev.bikeshare",
			["HubDataAdapter", "TripDataAdapter"]);

// Import the classes we will explicitly need from the
// bzdev class library
scripting.importClass("org.bzdev.drama.DramaSimulation");
scripting.importClass("org.bzdev.gio.OutputStreamGraphics");
scripting.importClasses("org.bzdev.graphs",
			["Graph", "AxisBuilder"]);
scripting.importClass("org.bzdev.math.rv.GaussianRV");
scripting.importClass("org.bzdev.util.units.MKS");
scripting.importClass("org.bzdev.util.VarArgsFormatter");

// Import Java classes
scripting.importClass("java.awt.geom.Path2D");
scripting.importClass("java.awt.Color");
scripting.importClass("java.awt.BasicStroke");

var runningTime;
if (typeof runningTime == "undefined") {
    runningTime = MKS.minutes(40.0);
}
if (typeof gtype == "undefined") {
    gtype = "png";
}

var g2d = null;
line1 = new Path2D.Double();
line2 = new Path2D.Double();
line3 = new Path2D.Double();
var gout;
if (typeof gout == undefined) {
    gout = null;
} else {
    // Set up a graph so that it is written to the output stream named
    // gout.
    osg = OutputStreamGraphics.newInstance(gout, 800, 600, gtype);
    graph = new Graph(osg);
    graph.setRanges(0.0, runningTime, 0.0, 20.0);
    graph.setOffsets(100, 50, 100, 50);
    xab = new AxisBuilder.Linear(graph, 0.0, 0.0, runningTime, true,
				"Time (Seconds)");
    xab.setMaximumExponent(3);
    xab.addTickSpec(0, 0, true, "%4.0f");
    xab.addTickSpec(2, 1, false, null);
    graph.draw(xab.createAxis());
    yab = new AxisBuilder.Linear(graph, 0.0, 0.0, 20.0, false, "Count");
    yab.setMaximumExponent(1);
    yab.addTickSpec(0, 0, true, "%3.0f", "%3.0f");
    yab.addTickSpec(2, 1, false, null);
    graph.draw(yab.createAxis());
    g2d = graph.createGraphics();
}


// Used due to Java 7 (Rhino ECMAScript implementation), which has
// trouble with variable numbers of arguments
var foutput = new VarArgsFormatter(output);

// configure the simulation so there are 1000 simulation ticks per
// second.
var sim = new DramaSimulation(scripting, 1000.0);

// Create the factories we'll use.
sim.createFactories("org.bzdev.bikeshare", {
    udf: "UsrDomainFactory",
    sdf: "SysDomainFactory",
    hbf: "BasicHubBalancerFactory",
    hf: "HubFactory",
    shf: "StorageHubFactory",
    dtf: "StdDelayTableFactory",
    btgf: "BurstTripGenFactory"
});

// Set up domains.
var usrDomain = udf.createObject("usrDomain");
var sysDomain = sdf.createObject("sysDomain");

// Set up a load balancer.
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
    "capacity": 20, "lowerTrigger": 3, "nominal": 5, "upperTrigger": 7,
    "pickupTime": pickupTime,
    "count": 10, "overCount": 0,
    "usrDomain": usrDomain, "sysDomain": sysDomain});

var hub2 = hf.createObject("hub2", {
    "x": MKS.miles(0.5), "y": 0.0,
    "capacity": 20, "lowerTrigger": 3, "nominal": 5, "upperTrigger": 7,
    "pickupTime": pickupTime,
    "count": 10, "overCount": 0,
    "usrDomain": usrDomain, "sysDomain": sysDomain});

var hub3 = hf.createObject("hub3", {
    "x": MKS.miles(1.0), "y": 0.0,
    "capacity": 20, "lowerTrigger": 3, "nominal": 5, "upperTrigger": 7,
    "pickupTime": pickupTime,
    "count": 10, "overCount": 0,
    "usrDomain": usrDomain, "sysDomain": sysDomain});

// create a delay table

var userTable = dtf.createObject("userTable", {
    "speedRV": usrSpeedRV, "dist": MKS.miles(1.0),
    "nStops": 4, "stopProbability": 0.4, "maxWait": 30.0,
    "distFraction": 1.0,
    "domains": [usrDomain]});

// create traffic generators
var tgen1 = btgf.createObject("tgen1", [
    {"centralHub": hub1, "burstTime": MKS.minutes(20), "burstSize": 10,
     "fanIn": true},
    {withPrefix: "other",
     config: [
	 {withKey: hub2, "prob": 0.5, "overflowProb": 0.0},
	 {withKey: hub3, "prob": 0.5, "overflowProb": 0.0}
     ]}
]);

var tgen2 = btgf.createObject("tgen2", [
    {"centralHub": hub1, "burstTime": MKS.minutes(25), "burstSize": 10,
     "fanIn": false},
    {withPrefix: "other",
     config: [
	 {withKey: hub2, "prob": 0.5, "overflowProb": 0.0},
	 {withKey: hub3, "prob": 0.5, "overflowProb": 0.0}
     ]}
]);

// print out the configuration of all the objects we created.
for each (var i in [usrDomain, sysDomain, balancer, hub1, hub2,
		    userTable, tgen1, tgen2]) {
    i.printConfiguration(output);
    output.println("---------------------");
}

// Track changes to each hub.
//
var last1 = hub1.getBikeCount();
var last2 = hub2.getBikeCount();
var last3 = hub3.getBikeCount();
line1.moveTo(0.0, last1);
line2.moveTo(0.0, last2);
line3.moveTo(0.0, last3);

var dl = new HubDataAdapter(scripting, {
    hubChanged: function(hub, bc, newbc, oc, newoc, time, ticks) {
	if (hub == hub1) {
	    line1.lineTo(time, last1);
	    if (newbc) {
		last1 = bc;
		line1.lineTo(time, last1);
	    }

	}
	if (hub == hub2) {
	    line2.lineTo(time, last2);
	    if (newbc) {
		last2 = bc;
		line2.lineTo(time, last2);
	    }
	}
	if (hub == hub3) {
	    line3.lineTo(time, last3);
	    if (newbc) {
		last3 = bc;
		line3.lineTo(time, last3);
	    }
	}
	/*
	foutput.format("at %g (ticks = %d), Hub %s: bc = %d (%b), "
		      +"oc = %d (%b)\n",
		      time, ticks, hub.getName(), bc, newbc, oc, newoc);
	foutput.flush();
        */
    }
});

hub1.addHubDataListener(dl);
hub2.addHubDataListener(dl);
hub3.addHubDataListener(dl);

// Track status of each trip

var tripStarts = 0;
var tripEnds = 0;
var tripFails = 0;

var tl = new TripDataAdapter(scripting, {
    tripStarted: function(tripID, time, ticks, hub, domain) {
	tripStarts++;
    },
    tripEnded: function (tripID, time, ticks, hub) {
	tripEnds++;
    },
    tripFailedAtStart: function(tripID, time, ticks, hub) {
	tripFails++;
    }
});

tgen1.addTripDataListener(tl);
tgen2.addTripDataListener(tl);

// run the simulation for runnintTime seconds of simulation time

sim.run(sim.getTicks(runningTime));

output.println("number of intitiated trips = " + tripStarts);
output.println("number of completed trips = " + tripEnds);
output.println("number of trips that failed to start = " + tripFails);

if (gout != null) {
    line1.lineTo(runningTime, last1);
    line2.lineTo(runningTime, last2);
    line3.lineTo(runningTime, last3);

    g2d.setStroke(new BasicStroke(1.5));
    g2d.setColor(Color.BLUE);
    graph.draw(g2d, line2);
    g2d.setColor(Color.RED);
    graph.draw(g2d, line3);
    g2d.setColor(Color.GREEN);
    graph.draw(g2d, line1);
    graph.write();
    osg.close();
}
