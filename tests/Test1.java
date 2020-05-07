import org.bzdev.bikeshare.*;
import org.bzdev.drama.*;
import org.bzdev.lang.Callable;
import org.bzdev.util.units.MKS;
import org.bzdev.math.rv.*;
import org.bzdev.devqsim.SimObject;

import java.util.Arrays;
import java.util.Comparator;

public class Test1 {
     static Comparator<Hub> loopComparator1 = new Comparator<Hub>() {
	    public int compare(Hub x, Hub y) {
		int xtake = x.getBikeCount() - x.getNominal();
		int ytake = y.getBikeCount() - y.getNominal();
		return ytake - xtake;
	    }
	};

    static Comparator<Hub> loopComparator2 = new Comparator<Hub>() {
	    public int compare(Hub x, Hub y) {
		int xtake = x.getBikeCount() - x.getNominal();
		int ytake = y.getBikeCount() - y.getNominal();
		return (xtake - ytake);
	    }
	};


   public static void main(String argv[]) throws Exception {
	DramaSimulation sim = new DramaSimulation(1000.0);

	DramaSimulation fsim = new DramaSimulation(1000.0);

	UsrDomain usrDomain = new UsrDomain(sim, "usrDomain", true);
	SysDomain sysDomain = new SysDomain(sim, "sysDomain", true);

	UsrDomainFactory udf = new UsrDomainFactory(fsim);
	SysDomainFactory sdf = new SysDomainFactory(fsim);

	UsrDomain fusrDomain = udf.createObject("usrDomain");
	SysDomain fsysDomain = sdf.createObject("sysDomain");

	BasicHubBalancer balancer = new BasicHubBalancer(sim, "balancer", true);
	balancer.initDomain(sysDomain);
	balancer.setQuietPeriod(MKS.minutes(30));
	balancer.setThreshold(0.75);

	BasicHubBalancerFactory hbf = new BasicHubBalancerFactory(fsim);
	hbf.set("sysDomain", fsysDomain);
	hbf.set("quietPeriod", MKS.minutes(30));
	hbf.set("threshold", 0.75);
	BasicHubBalancer fhb = hbf.createObject("balancer");

	DoubleRandomVariable pickupTime = new GaussianRV(MKS.minutes(4.0),
							 30.0);
	pickupTime.setMinimum(10.0, true);
	DoubleRandomVariable usrSpeedRV = new GaussianRV(MKS.mph(12.0),
							 MKS.mph(3.0));
	usrSpeedRV.setMinimum(MKS.mph(5.0), true);

	DoubleRandomVariable sysSpeedRV = new GaussianRV(MKS.mph(25.0),
							 MKS.mph(3.0));
	sysSpeedRV.setMinimum(MKS.mph(5.0), true);

	HubFactory hf = new HubFactory(fsim);
	hf.set("usrDomain", fusrDomain);
	hf.set("sysDomain", fsysDomain);
	hf.set("capacity", 10);
	hf.set("lowerTrigger", 3);
	hf.set("nominal", 5);
	hf.set("upperTrigger", 7);
	hf.set("pickupTime", pickupTime);
	hf.set("count", 5);
	hf.set("overCount", 0);

	Hub hub1 = new Hub(sim, "hub1", true);
	hub1.init(10, 3, 5, 7, pickupTime,
		  5, 0,
		  0.0, 0.0, usrDomain, sysDomain);
	
	hf.set("x", 0.0);
	hf.set("y", 0.0);
	Hub fhub1 = hf.createObject("hub1");

	Hub hub2 = new Hub(sim, "hub2", true);
	hub2.init(10, 3, 5, 7, pickupTime,
		  5, 0,
		  MKS.miles(1.0), 0.0, usrDomain, sysDomain);

	hf.set("x", MKS.miles(1.0));
	hf.set("y", 0.0);
	Hub fhub2 = hf.createObject("hub2");

	StdDelayTable userTable = new StdDelayTable(sim, "userTable", true);
	userTable.init(usrSpeedRV, MKS.miles(1.0),
		       4, 0.4, 30.0);
	userTable.setDistFraction(1.0);
	userTable.addToDomain(usrDomain);
	StdDelayTable sysTable = new StdDelayTable(sim, "sysTable", true);
	sysTable.init(sysSpeedRV, MKS.miles(1.0),
		      4, 0.4, 30.0);
	sysTable.addToDomain(sysDomain);
	sysTable.addEntry(hub1, hub2, MKS.miles(1.0),
			  5, 0.4, 30.0);

	StdDelayTableFactory dtf = new StdDelayTableFactory(fsim);
	dtf.set("speedRV",sysSpeedRV);
	dtf.set("dist", MKS.miles(1.0));
	dtf.set("nStops", 4);
	dtf.set("stopProbability", 0.4);
	dtf.set("maxWait", 30.0);
	dtf.set("distFraction", 1.0);
	dtf.add("domains", fusrDomain);
	DelayTable fuserTable = dtf.createObject("userTable");
	dtf.remove("domains", fusrDomain);
	dtf.unset("distFraction");
	dtf.set("entry.origin", 1, fhub1);
	dtf.set("entry.dest", 1, fhub2);
	dtf.set("entry.distance", 1,  MKS.miles(1.0));
	dtf.set("entry.stops", 1, 5);
	dtf.set("entry.stopProbability",1 , 0.4);
	dtf.set("entry.maxWait", 1, 30.0);
	dtf.add("domains", fsysDomain);

	DelayTable fsysTable = dtf.createObject("sysTable");


	final StorageHub storageHub = new StorageHub(sim, "storageHub", true);
	storageHub.init(-1, -1, -1, -1, MKS.miles(0.25), 0.0, sysDomain);
	storageHub.setInitialNumberOfWorkers(0, 1, 0,
					     MKS.hours(1.0),
					     MKS.hours(1.1),
					     MKS.hours(2.2));
	storageHub.addHub(HubWorker.Mode.LOOP, hub1);
	storageHub.addHub(HubWorker.Mode.LOOP, hub2);

	StorageHubFactory shf = new StorageHubFactory(fsim);
	shf.set("sysDomain", fsysDomain);
	shf.set("nworkersNoPickup", 0);
	shf.set("nworkersWithPickup", 1);
	shf.set("nworkersFixOnly", 0);
	shf.set("intervalNoPickup", MKS.hours(1.0));
	shf.set("intervalWithPickup", MKS.hours(1.1));
	shf.set("intervalFixOnly", MKS.hours(1.2));
	shf.set("hubTable.mode", 0, "LOOP");
	shf.set("hubTable.hub", 0, fhub1);
	shf.set("hubTable.mode", 1, "LOOP");
	shf.set("hubTable.hub", 1, fhub2);
	shf.set("x", MKS.miles(0.25));
	shf.set("y", 0.0);

	StorageHub fstorageHub = shf.createObject("storageHub");

	HubWorker worker1 = new HubWorker(sim, "worker1", true);
	worker1.init(5, storageHub, sysDomain, storageHub);

	HubWorker worker2 = new HubWorker(sim, "worker2", true);
	worker2.init(5, storageHub, sysDomain, storageHub);
	
	HubWorkerFactory hwf = new HubWorkerFactory(fsim);
	hwf.set("sysDomain", fsysDomain);
	hwf.set("capacity", 5);
	hwf.set("storageHub", fstorageHub);
	HubWorker fworker1 = hwf.createObject("worker1");
	HubWorker fworker2 = hwf.createObject("worker2");


	Hub[] hubs1 = {hub2};
	Hub[] hubs2 = {hub1};
	double weights[] = {1.0};
	double overflowProb[] = {0.2};
	BasicTripGenerator tgen1 = new BasicTripGenerator(sim, "tgen1", true);
	tgen1.init(hub1, MKS.minutes(10.0), 1, hubs1, weights, overflowProb);

	BasicTripGenerator tgen2 = new BasicTripGenerator(sim, "tgen2", true);
	tgen2.init(hub2, MKS.minutes(10.0), 1, hubs2, weights, overflowProb);

	BasicTripGenFactory tgf = new BasicTripGenFactory(fsim);
	tgf.set("startingHub", fhub1);
	tgf.set("meanIATime", MKS.minutes(10.0));
	tgf.set("nBicycles", 1);
	tgf.set("dest.prob", fhub2, 1.0);
	tgf.set("dest.overflowProb", fhub2, 0.2);
	BasicTripGenerator ftgen1 = tgf.createObject("tgen1");
	tgf.set("startingHub", fhub2);
	tgf.clear("dest");
	tgf.set("dest.prob", fhub1, 1.0);
	tgf.set("dest.overflowProb", hub1, 0.2);

	System.out.println("starting timeline configuration tor tgf");
	tgf.set("timeline.time", 0, MKS.hours(3));
	tgf.set("timeline.running", 0, false);
	tgf.set("timeline.time", 1, MKS.hours(6));
	tgf.set("timeline.running", 1, true);

	System.out.println("creating tgen2");
	BasicTripGenerator ftgen2 = tgf.createObject("tgen2");

	System.out.println("------------------------");
	for (SimObject object: sim.getObjects()) {
	    object.printConfiguration(System.out);
	    object.printState(System.out);
	    System.out.println("........");
	}

	if (argv.length > 0 && argv[0].equals("printOnly")) {
	    System.out.print("\f");
	}

	System.out.println("**** REPEAT USING FSIM ****"); 

	for (SimObject object: fsim.getObjects()) {
	    object.printConfiguration(System.out);
	    object.printState(System.out);
	    System.out.println("........");
	}
	System.out.println("------------------------");

	if (argv.length > 0 && argv[0].equals("printOnly")) {
	    System.exit(0);
	}

	System.out.println("Decrement bike count for hub1 by 2:");
	System.out.println("... old count = " + hub1.getBikeCount());
	System.out.println("... hub1.decrBikeCount(2) = "
			   + hub1.decrBikeCount(2));
	System.out.println("... new count = " + hub1.getBikeCount());
	System.out.println("Increment bike count for hub1 by 10");
	System.out.println ("... hub1.incrBikeCount(10) = "
			    + hub1.incrBikeCount(10));
	System.out.println("... new bike count for hub1 = "
			   + hub1.getBikeCount());
	System.out.println("... new overflow count for hub1 = "
			   + hub1.getOverflow());
	
	
	System.out.println("Pickup 3 bicycles from overflow for hub1");
	System.out.println("(mean value of 720, sdev = 51)");
	System.out.println(" ... hub1.pickupOverflow = "
			   + hub1.pickupOverflow(3) + " simulation ticks");
	System.out.println("... new bike count for hub1 = "
			   + hub1.getBikeCount());
	System.out.println("... new overflow count for hub1 = "
			   + hub1.getOverflow());

	System.out.println("Decrement bike count by 5 = (10 - 3 - 2)"
			   + " for hub1:");
	System.out.println("hub1.decrBikeCount(5) = " + hub1.decrBikeCount(5));
	System.out.println("... new bike count for hub1 = "
			   + hub1.getBikeCount());
	System.out.println("... new overflow count for hub1 = "
			   + hub1.getOverflow());
	System.out.println("---------------------------------");
	System.out.println("DelayTable Test:");
	userTable.printConfiguration(System.out);
	double delay = 0.0;
	for (int i = 0; i < 100; i++) {
	    delay += userTable.getDelay(hub1, hub2, 1);
	}
	delay /= 100;
	System.out.println("userTable.delay(hub1, hub2) = " + delay);
	System.out.println("... add entry");
	    userTable.addEntry(hub1, hub2, MKS.miles(1.0), 4, 0.3, 35.0);
	userTable.printConfiguration(System.out);
	delay = 0.0;
	for (int i = 0; i < 100; i++) {
	    delay += userTable.getDelay(hub1, hub2, 1);
	}
	delay /= 100;
	System.out.println("userTable.delay(hub1, hub2) = " + delay);

	System.out.println("----------------------------------");
	System.out.println("HubListener test");
	HubListener hl = new HubListener() {
		public void hubChanged(Hub hub,
				       int need, int excess,
				       int overflow)
		{
		    System.out.format(".......... Listener: hub = %s, "
				      + "need = %d, excess = %d, "
				      + "overflow = %d\n",
				      hub.getName(), need, excess, overflow);
		}
	    };

	HubDataListener dl = new HubDataListener() {
		public void hubChanged(Hub hub, int bc, boolean nbc,
				       int oc, boolean newoc,
				       double time, long ticks)
		{
		    System.out.format("at %g, Hub %s: bc = %d (%b), "
				      + "oc = %d (%b)\n",
				      time,
				      hub.getName(), bc, nbc, oc, newoc);
		}
	    };

	hub1.addHubListener(hl);
	hub1.addHubDataListener(dl);
		
	System.out.println("... upper trigger test");
	hub1.incrBikeCount(4);
	System.out.println("... excess bicycles = " + hub1.excessBikes());
	balancer.printState(System.out);

	System.out.println("... lower trigger test");
	hub1.decrBikeCount(4);
	hub1.decrBikeCount(4);
	balancer.printState(System.out);
	System.out.println("... need bicycles = " +hub1.needBikes());
	hub1.incrBikeCount(4);
	balancer.printState(System.out);
	
	hub1.removeHubListener(hl);
	hub1.removeHubDataListener(dl);
	System.out.println("test hub listener removed");
	System.out.println("... upper trigger test");
	hub1.incrBikeCount(4);
	System.out.println("... excess bicycles = " + hub1.excessBikes());

	System.out.println("... lower trigger test");
	hub1.decrBikeCount(4);
	hub1.decrBikeCount(4);
	System.out.println("... need bicycles = " +hub1.needBikes());
	hub1.incrBikeCount(4);

	System.out.println ("-------------------------------");
	System.out.println("StorageHub test");
	System.out.println("hubs for mode LOOP (expecting hub1, hub2):");
	for (Hub hub: storageHub.getHubs(HubWorker.Mode.LOOP)) {
	    System.out.println("     " + hub.getName());
	}
	HubWorker w1 = storageHub.pollWorkers();
	HubWorker w2 = storageHub.pollWorkers();
	HubWorker w3 = storageHub.pollWorkers();
	System.out.format("unqueued workers: %s, %s \n",
			  ((w1 == null)? "null": w1.getName()),
			  ((w2 == null)? "null": w2.getName()));
	if (w3 != null) { 
	    System.out.println("w3 should have been null");
	    System.exit(1);
	}
	storageHub.queueWorker(w1);
	storageHub.queueWorker(w2);
	w1 = storageHub.pollWorkers();
	storageHub.addOnQueueCallable(new Callable() {
		public void call() {
		    HubWorker w = storageHub.pollWorkers();
		    System.out.println("... polled & got worker "
				       + w.getName());
		    storageHub.queueWorker(w);
		}
	    });
	storageHub.queueWorker(w1);
	w1 = storageHub.pollWorkers();
	w2 = storageHub.pollWorkers();
	System.out.format("unqueued workers: %s, %s \n",
			  ((w1 == null)? "null": w1.getName()),
			  ((w2 == null)? "null": w2.getName()));
	storageHub.queueWorker(w1);
	storageHub.queueWorker(w2);
 
	Hub array[] = new Hub[2];
	array[0] = hub1;
	array[1] = hub2;
	System.out.println("test loopComparator1");
	hub1.incrBikeCount(8 - hub1.getBikeCount());
	hub2.incrBikeCount(9 - hub2.getBikeCount());
	Arrays.sort(array, loopComparator1);
	for (Hub h: array) {
	    System.out.println("   " + h.getName());
	}
	System.out.println("test loopComparator2");
	Arrays.sort(array, loopComparator2);
	for (Hub h: array) {
	    System.out.println("    " + h.getName());
	}
	System.out.println("test SchedDelayTable");
	SchedDelayTable st = new SchedDelayTable(sim, "schedDT", true);
	st.addEntry(hub1, hub2, 20.0, 50.0);
	st.addEntry(hub1, hub2, 25.0, 48.0);
	st.addEntry(hub1, hub2, 50.0, 60.0);
	st.addEntry(hub1, hub2, 55.0, 65.0);

	st.addEntry(hub1, hub2, 100.0, 120.0);
	st.addEntry(hub1, hub2, 110.0, 120.0);
	
	st.addEntry(hub2, hub1, 100.0, 200.1, 20.0, 30.0);

	st.printConfiguration(System.out);

	SchedDelayTableFactory stf = new SchedDelayTableFactory(fsim);
	stf.set("entry.origin", 1, hub1);
	stf.set("entry.dest", 1, hub2);
	stf.set("entry.initialTime", 1, 20.0);
	stf.set("entry.duration", 1, 30.0);
	stf.set("entry.origin", 2, hub1);
	stf.set("entry.dest", 2, hub2);
	stf.set("entry.initialTime", 2, 25.0);
	stf.set("entry.duration", 2, 23.0);
	stf.set("entry.origin", 3, hub1);
	stf.set("entry.dest", 3, hub2);
	stf.set("entry.initialTime", 3, 50.0);
	stf.set("entry.duration", 4, 10.0);
	stf.set("entry.origin", 4, hub1);
	stf.set("entry.dest", 4, hub2);
	stf.set("entry.initialTime", 4, 55.0);
	stf.set("entry.duration", 4, 10.0);
	stf.set("entry.origin", 5, hub1);
	stf.set("entry.dest", 5, hub2);
	stf.set("entry.initialTime", 5, 100.0);
	stf.set("entry.duration", 5, 20.0);
	stf.set("entry.origin", 6, hub1);
	stf.set("entry.dest", 6, hub2);
	stf.set("entry.initialTime", 6, 110.0);
	stf.set("entry.duration", 6, 10.0);

	stf.set("entry.origin", 7, hub2);
	stf.set("entry.dest", 7, hub1);
	stf.set("entry.initialTime", 7, 100.0);
	stf.set("entry.cutoffTime", 7, 200.1);
	stf.set("entry.period", 7, 20.0);
	stf.set("entry.duration", 7, 30.0);

	SchedDelayTable fst = stf.createObject("SchedDTf");

	fst.printConfiguration(System.out);

	double stdata[] = {
	    10.0, 15.0, 20.0, 24.0, 25.0, 40.0, 50.0, 52.0, 90.0
	};

	double stvals[] = {
	    48.0 - 10.0,
	    48.0 - 15.0,
	    48.0 - 20.0,
	    48.0 - 24.0,
	    48.0 - 25.0,
	    60.0 - 40.0,
	    60.0 - 50.0,
	    65.0 - 52.0,
	    120.0 - 90.0
	};

	double stsvals[] = {
	    25.0, 25.0, 25.0, 25.0, 25.0, 50.0, 50.0, 55.0, 110.0
	};

	for (int i = 0; i < stdata.length; i++) {
	    double xdelay = st.getDelay(stdata[i], hub1, hub2, 1);
	    System.out.format("for t = %g, delay = %g, expecting %g \n",
			      stdata[i], xdelay, stvals[i]);
	    if (Math.abs(xdelay - stvals[i]) > 1.e-10) {
		throw new Exception("unexpected value: " + xdelay
				    + " != " + stvals[i]);
	    }
	}
	if (st.getDelay(170.0, hub1, hub2, 1) != Double.POSITIVE_INFINITY) {
	    System.out.println("time 170.0 - delay = "
			       +st.getDelay(170.0, hub1, hub2, 1));
	    System.exit(1);
	}
	for (int i = 0; i < stdata.length; i++) {
	    double stime = st.latestStartingTime(stdata[i], hub1, hub2);
	    System.out.format("for t = %g, latest start = %g, expecting %g \n",
			      stdata[i],  stime, stsvals[i]);
	    if (Math.abs(stime - stsvals[i]) > 1.e-10) {
		throw new Exception("unexpected value: " + stime
				    + " != " + stsvals[i]);
	    }
	}

	double rstdata[] = {90.0, 100.0, 105.0, 141.0};
	double rstvals[] = {
	    130.0 - 90.0,
	    130.0 - 100.0,
	    150.0 - 105.0,
	    190.0 - 141.0
	};
	
	for (int i = 0; i < rstdata.length; i++) {
	    double xdelay = st.getDelay(rstdata[i], hub2, hub1, 1);
	    System.out.format("for t = %g, delay = %g, expecting %g \n",
			      rstdata[i], xdelay, rstvals[i]);
	    if (Math.abs(xdelay - rstvals[i]) > 1.e-10) {
		throw new Exception("unexpected value: " + xdelay
				    + " != " + rstvals[i]);
	    }
	}

	double rstsvals[] = {
	    100.0,
	    100.0,
	    120.0,
	    160.0
	};

	for (int i = 0; i < rstdata.length; i++) {
	    double stime = st.latestStartingTime(rstdata[i], hub2, hub1);
	    System.out.format("for t = %g, latest start = %g, expecting %g \n",
			      rstdata[i],  stime, rstsvals[i]);
	    if (Math.abs(stime - rstsvals[i]) > 1.e-10) {
		throw new Exception("unexpected value: " + stime
				    + " != " + rstsvals[i]);
	    }
	}

	System.exit(0);
   }
}
