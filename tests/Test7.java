import org.bzdev.bikeshare.*;
import org.bzdev.drama.*;
import org.bzdev.util.units.MKS;
import org.bzdev.math.rv.*;
import org.bzdev.math.RealValuedFunctionTwo;
import org.bzdev.math.Functions;

/* 
 * No storage hub and no workers but includes an external domain
 * and use of a probability function.
 */

public class Test7 {
    public static void main(String argv[]) throws Exception {
	DramaSimulation sim = new DramaSimulation(1000.0);

	ExtDomain extDomain = new ExtDomain(sim, "extDomain", true);
						  
	UsrDomain usrDomain = new UsrDomain(sim, "usrDomain", true, extDomain);
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

	hub1.joinDomain(extDomain);
	hub2.joinDomain(extDomain);

	StdDelayTable userTable = new StdDelayTable(sim, "userTable", true);
	userTable.init(usrSpeedRV, MKS.miles(1.0),
		       4, 0.4, 30.0);
	userTable.setDistFraction(1.0);
	userTable.addToDomain(usrDomain);

	SchedDelayTable schedTable =
	    new SchedDelayTable(sim, "schedTable", true);
	schedTable.addEntry(hub1, hub2, MKS.minutes(5.0),
			    MKS.hours(8.0), MKS.minutes(10.0),
			    MKS.minutes(4.0));
	schedTable.addEntry(hub2, hub1, MKS.minutes(5.0),
			    MKS.hours(8.0), MKS.minutes(10.0),
			    MKS.minutes(4.0));
	schedTable.addToDomain(extDomain);

	schedTable.printConfiguration(System.out);

	RealValuedFunctionTwo pf = new RealValuedFunctionTwo() {
		public double valueAt(double delay1, double delay2) {
		    double diff = 0.01 * (delay2 - delay1);
		    double result = 0.5*(1.0 + Functions.erf(diff));
		    // in case of round-off errors
		    if (result < 0.0) return 0.0;
		    if (result > 1.0) return 1.0;
		    return result;
		}
	    };

	Hub[] hubs1 = {hub2};
	Hub[] hubs2 = {hub1};
	double weights[] = {1.0};
	double overflowProb[] = {0.0};
	BasicTripGenerator tgen1 = new BasicTripGenerator(sim, "tgen1", true);
	tgen1.init(hub1, MKS.minutes(6.0), 1, hubs1, weights, overflowProb);
	tgen1.setProbabilityFunction(pf);

	BasicTripGenerator tgen2 = new BasicTripGenerator(sim, "tgen2", true);
	tgen2.init(hub2, MKS.minutes(6.0), 1, hubs2, weights, overflowProb);
	tgen2.setProbabilityFunction(pf);

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

	sim.run(sim.getTicks(MKS.minutes(7*60.0)));
		
	System.exit(0);
    }
}
