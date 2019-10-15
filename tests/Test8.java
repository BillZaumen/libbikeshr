import org.bzdev.bikeshare.*;
import org.bzdev.drama.*;
import org.bzdev.util.units.MKS;
import org.bzdev.util.rv.*;

/* 
 * No storage hub and no workers.
 * Test of RoundTripGenerator
 */

public class Test8 {
    public static void main(String argv[]) throws Exception {
	DramaSimulation sim = new DramaSimulation(1000.0);

	UsrDomain usrDomain = new UsrDomain(sim, "usrDomain", true);
	SysDomain sysDomain = new SysDomain(sim, "sysDomain", true);

	BasicHubBalancer balancer = new BasicHubBalancer(sim, "balancer", true);
	balancer.initDomain(sysDomain);
	balancer.setQuietPeriod(MKS.minutes(30));
	balancer.setThreshold(0.75);

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
	Hub[] hubs = {hub2};
	double weights[] = {1.0};
	double overflowProb[] = {0.2};
	RoundTripGenerator tgen = new RoundTripGenerator(sim, "tgen", true);
	tgen.init(hub1, MKS.minutes(10.0), 1,
		  new FixedDoubleRV(5.0), 0.2,
		  hubs, weights, overflowProb);

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
		@Override
		public void tripStarted(long tripID,
					double time, long ticks,
					Hub hub, HubDomain d)
		{
		    System.out.format("trip %d at t=%g (ticks=%d), hub %s: "
				      +"trip started (domain = %s)\n",
				      tripID, time, ticks, hub.getName(),
				      d.getName());
		}

		@Override
		public void tripPauseStart(long tripID, double time, long ticks,
					   Hub hub)
		{
		    System.out.format("trip %d at %g (ticks=%d), hub %s: "
				      +"trip paused\n",
				      tripID, time, ticks, hub.getName());
		}

		@Override
		public void tripPauseEnd(long tripID, double time, long ticks,
					 Hub hub, HubDomain d)
		{
		    System.out.format("trip %d at %g (ticks=%d), hub %s: "
				      +"trip continued (domain = %s)\n",
				      tripID, time, ticks, hub.getName(),
				      d.getName());
		}

		@Override
		public void tripEnded(long tripID, double time, long ticks,
						Hub hub)
		{
		    System.out.format("trip %d at %g (ticks=%d), hub %s: "
				      +"trip ended\n",
				      tripID, time, ticks, hub.getName());
		}

		@Override
		public void tripFailedAtStart(long tripID,
					      double time,
					      long ticks,
						Hub hub)
		{
		    System.out.format("trip %d at %g (ticks=%d), hub %s: "
				      +"trip could not start\n",
				      tripID, time, ticks, hub.getName());
		}

		@Override
		public void tripFailedMidstream(long tripID,
					      double time,
					      long ticks,
						Hub hub)
		{
		    System.out.format("trip %d at %g (ticks=%d), hub %s: "
				      +"trip failed midstream\n",
				      tripID, time, ticks, hub.getName());
		}

	    };

	tgen.addTripDataListener(tl);

	sim.run(sim.getTicks(MKS.minutes(10*120.0)));
		
	System.exit(0);
    }
}
