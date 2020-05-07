import org.bzdev.bikeshare.*;
import org.bzdev.drama.*;
import org.bzdev.util.units.MKS;
import org.bzdev.math.rv.*;
import org.bzdev.devqsim.SimObject;
import org.bzdev.devqsim.TraceSet;

public class Test5 {
    public static void main(String argv[]) throws Exception {

	BikeShare.setTraceLevels(1, 2, 3, 4);

	DramaSimulation sim = new DramaSimulation(1000.0);

	TraceSet tset = new TraceSet(sim, "tset", false);
	tset.setLevel(4);
	tset.setOutput(System.out);

	UsrDomain usrDomain = new UsrDomain(sim, "usrDomain", true);
	SysDomain sysDomain = new SysDomain(sim, "sysDomain", true);

	BasicHubBalancer balancer = new BasicHubBalancer(sim, "balancer", true);
	balancer.initDomain(sysDomain);
	balancer.setQuietPeriod(MKS.minutes(30));
	balancer.setThreshold(0.75);

	/*
	System.out.println("tset added for balancer");
	balancer.addTraceSet(tset);
	*/
	DoubleRandomVariable pickupTime = new GaussianRV(MKS.minutes(4.0),
							 30.0);
	pickupTime.setMinimum(10.0, true);
	DoubleRandomVariable usrSpeedRV = new GaussianRV(MKS.mph(12.0),
							 MKS.mph(3.0));
	usrSpeedRV.setMinimum(MKS.mph(5.0), true);

	DoubleRandomVariable sysSpeedRV = new GaussianRV(MKS.mph(25.0),
							 MKS.mph(3.0));
	sysSpeedRV.setMinimum(MKS.mph(5.0), true);

	Hub hub1 = new Hub(sim, "hub1", true);
	hub1.init(10, 3, 5, 7, pickupTime,
		  5, 0,
		  0.0, 0.0, usrDomain, sysDomain);
		  
	Hub hub2 = new Hub(sim, "hub2", true);
	hub2.init(10, 3, 5, 7, pickupTime,
		  5, 0,
		  MKS.miles(1.0), 0.0, usrDomain, sysDomain);

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

	StorageHub storageHub = new StorageHub(sim, "storageHub", true);
	storageHub.init(-1, -1, -1, -1, MKS.miles(0.25), 0.0, sysDomain);
	storageHub.setInitialNumberOfWorkers(0, 0, 1,
					     0.0, 0.0, MKS.minutes(30.0));
	storageHub.addHub(hub1);
	storageHub.addHub(hub2);

	HubWorker worker1 = new HubWorker(sim, "worker1", true);
	worker1.init(5, storageHub, sysDomain, storageHub);
	worker1.addTraceSet(tset);

	Hub[] hubs1 = {hub2};
	Hub[] hubs2 = {hub1};
	double weights[] = {1.0};
	double overflowProb[] = {0.2};
	BasicTripGenerator tgen1 = new BasicTripGenerator(sim, "tgen1", true);
	tgen1.init(hub1, MKS.minutes(10.0), 1, hubs1, weights, overflowProb);

	BasicTripGenerator tgen2 = new BasicTripGenerator(sim, "tgen2", true);
	tgen2.init(hub2, MKS.minutes(10.0), 1, hubs2, weights, overflowProb);

	HubDataListener dl = new HubDataAdapter() {
		public void hubChanged(Hub hub, int bc, boolean newbc,
				       int oc, boolean newoc,
				       double time, long ticks)
		{
		    System.out.format("at %g (ticks = %d), "
				      + "Hub %s: bc = %d (%b), "
				      + "oc = %d (%b)\n",
				      time, ticks,
				      hub.getName(), bc, newbc, oc, newoc);
		}
	    };

	hub1.addHubDataListener(dl);
	hub2.addHubDataListener(dl);

	TripDataListener tl = new TripDataAdapter() {
		public void tripStarted(long tripID,
					double time, long ticks,
					Hub hub, HubDomain d)
		{
		    System.out.format("trip %d at t=%g (ticks=%d), hub %s: "
				      +"trip started (domain = %s)\n",
				      tripID, time, ticks, hub.getName(),
				      d.getName());
		}
		public void tripEnded(long tripID, double time, long ticks,
						Hub hub)
		{
		    System.out.format("trip %d at %g (ticks=%d), hub %s: "
				      +"trip ended\n",
				      tripID, time, ticks, hub.getName());
		}
		public void tripFailedAtStart(long tripID,
					      double time,
					      long ticks,
						Hub hub)
		{
		    System.out.format("trip %d at %g (ticks=%d), hub %s: "
				      +"trip could not start\n",
				      tripID, time, ticks, hub.getName());
		}
	    };

	tgen1.addTripDataListener(tl);
	tgen2.addTripDataListener(tl);

	sim.run(sim.getTicks(MKS.minutes(10*120.0)));
		
	System.exit(0);

    }
}
