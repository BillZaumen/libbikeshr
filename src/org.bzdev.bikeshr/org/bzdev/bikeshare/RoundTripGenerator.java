package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.lang.Callable;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.ExpDistrRV;
import java.util.Arrays;

import java.io.PrintWriter;

/**
 * Trip generator trips between two hubs given Poisson traffic,
 * A specified number of bicycles per trip, a starting hub and
 * a set of destination hubs, the probabilities for using a
 * given destination, and the probabilities for each destination
 * of using the overflow area.  There is additionally a random
 * variable providing the waiting time before the return trip.
 * <P>
 * Many trips in practice will be individual trips, but some trips may
 * be made a groups of people traveling together.  To account for
 * these trips, RoundTripGenerator allows one to specify the number of
 * bicycles taken for a trip (one can create different trip generators
 * for each number of bicycles that one expects to see).  The
 * generator assumes that bicycles are picked up at a hub's preferred
 * location, and that a fraction of the trips will always use a hub's
 * overflow area.
 */
public class RoundTripGenerator extends TripGenerator {

    static String errorMsg(String key, Object... args) {
	return BikeShare.errorMsg(key, args);
    }

    DramaSimulation sim;


    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of this object
     * @param intern true if this object should be interned in the
     *        simulation's name table; false otherwise
     */
    public RoundTripGenerator(DramaSimulation sim,
			      String name,
			      boolean intern)
    {
	super(sim, name, intern);
	this.sim = sim;
    }

    static class Dest implements Comparable<Dest> {
	public Hub hub;
	public double cvalue;
	public double oprob;

	private long instance;
	static long instanceCounter = 0;

	Dest(Hub hub, double cvalue, double oprob) {
	    this.hub = hub;
	    this.cvalue = cvalue;
	    this.oprob = oprob;
	    this.instance = (instanceCounter++);
	}
	
	public int compareTo(Dest other) {
	    if (cvalue < other.cvalue) return -1;
	    if (cvalue > other.cvalue) return 1;
	    if (instance > other.instance) return -1;
	    if (instance < other.instance) return 1;
	    else return 0;
	}
    }



    Hub hub;
    DoubleRandomVariable rv;
    double mean = 0.0;
    DoubleRandomVariable waitrv;
    double oprob;
    Dest[] dests;
    int nbikes;

    /**
     * Set the mean interarrival time for trips.
     * This method will stop the traffic generator, change
     * the mean interarrival time, and restart the traffic
     * generator.  Correct operation is dependent on Poission
     * traffic being memoryless: calling stop() followed by
     * restart() has no effect on the statisitical distribution
     * of the trips' starting times.
     * @param mean the mean interarrival time in seconds
     */
    public void setMean(double mean) {
	if (mean < 0.0) {
	    throw new IllegalArgumentException();
	}
	if (this.mean != mean) {
	    boolean wasRunning = isRunning();
	    if (wasRunning) stop();
	    this.mean = mean;
	    rv = new ExpDistrRV(mean);
	    if (wasRunning) restart();

	}
    }

    /**
     * Get the mean interarrival time for trips.
     * @return the mean interarrival time in seconds
     */
    public double getMean() {
	return mean;
    }

    /**
     * Set the time interval between arriving at a destination and
     * starting a return trip.
     *  @param waitrv a random variable giving the time interval in seconds
     */
    public void setWaitRV(DoubleRandomVariable waitrv) {
	this.waitrv = waitrv;
    }

    /**
     * Set the probability of using the starting hub's overflow area
     * at the end of a return trip.
     * @param p the probability
     */
    public void setReturnOverflowProb(double p) {
	this.oprob = p;
    }
    
    /**
     * Initialization.
     * @param hub the hub from which trips originate
     * @param mean the mean interarrival time for the starting times
     *        of trips created by this trip generator
     * @param nbikes the number of bicycles per trip
     * @param waitrv a random variable providing the time spent at a
     *        destination hub before the return trip
     * @param returnOverflowProb the probability of using the overflow area
     *        after returning to the starting point
     * @param destHubs the destination hubs
     * @param weights the probabilities that the destination is a
     *        particular hub
     * @param overflowProb the probability for a destination hub
     *        that the overflow area is used regardless of whether
     *        or not there is excess capacity in the preferred location
     * 
     */
    public void init(Hub hub, double mean,
		     int nbikes,
		     DoubleRandomVariable waitrv,
		     Double returnOverflowProb,
		     Hub[] destHubs,
		     double[] weights,
		     double[] overflowProb)
    {
	this.hub = hub;
	rv = new ExpDistrRV(mean);
	this.mean = mean;
	this.nbikes = nbikes;
	this.waitrv = waitrv;
	this.oprob = returnOverflowProb;

	double sum = 0.0;
	weights = weights.clone();
	for (int i = 0; i < destHubs.length; i++) {
	    if (weights[i] < 0.0) {
		throw new IllegalArgumentException
		    (errorMsg("weights", weights[i]));
	    }
	    sum += weights[i];
	}
	for (int i = 0; i < destHubs.length; i++) {
	    weights[i] /= sum;
	}
	
	double total = 0.0;
	double last = 0.0;
	dests = new Dest[weights.length];
	for (int i = 0; i < destHubs.length; i++) {
	    total += weights[i];
	    dests[i] = new Dest(destHubs[i], total, overflowProb[i]);
	    last = total;
	}
	trace(BikeShare.level1, "trip generator configured");
    }

    Dest chooseDest() {
	double rval = StaticRandom.nextDouble();
	while (rval == 1.0) rval = StaticRandom.nextDouble();
	Dest key = new Dest(null, rval, 0.0);
	int index = Arrays.binarySearch(dests, key);
	if (index < 0) index = -index - 1;
	return dests[index];
    }

    @Override
    protected double getNextInterval() {
	return rv.next();
    }


    Callable task = null;

    @Override
    protected boolean action() {
	final Dest dest = chooseDest();
	final long tripID = createTripID();
	final double wait = waitrv.next();
	boolean willOverflow =
	    StaticRandom.nextDouble() < dest.oprob;
	final boolean willOverflowR =
	    StaticRandom.nextDouble() < oprob;
	trace(BikeShare.level4,
	      "sending %d bike-share users from %s to %s, "
	      + "intending to use the %s",
	      nbikes, hub, dest.hub,
	      (willOverflow? "overflow area": "preferred area"));
	final Callable nextContinuation = new Callable() {
		public void call() {
		    fireTripEnded(tripID, hub);
		}
	    };

	final Callable nextCall = new Callable() {
		public void call() {
		    HubDomain rd = dest.hub.sendUsers(hub, nbikes,
						      willOverflowR,
						      getProbabilityFunction(),
						      nextContinuation);
		    if (rd != null) {
			fireTripPauseEnd(tripID, dest.hub, rd);
		    } else {
			fireTripFailedMidstream(tripID, dest.hub);
		    }
		}
	    };

	Callable continuation = new Callable() {
		public void call() {
		    fireTripPauseStart(tripID, dest.hub);
		    sim.scheduleCall(nextCall, sim.getTicks(wait));
		}
	    };
	HubDomain d = hub.sendUsers(dest.hub, nbikes, willOverflow,
				    getProbabilityFunction(), continuation);
	if (d != null) {
	    fireTripStarted(tripID, hub, d);
	} else {
	    fireTripFailedAtStart(tripID, hub);
	}
	return true;
    }

     /**
     * {@inheritDoc}
     * <P>
     * Defined for class RoundTripGenerator:
     * <UL>
     *  <LI> the mean interarrival time in seconds for trips.
     *  <LI> the number of bicycles per trip.
     *  <LI> the starting hub.
     *  <LI> the destination hubs. For each destination, the following
     *       are printed:
     *       <UL>
     *         <LI> the name of a destination hub.
     *         <LI> the probability of choosing this hub.
     *         <LI> the probability of using the overflow area.
     *       </UL>
     * </UL>
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printConfiguration(String iPrefix, String prefix,
				   boolean printName,
				   PrintWriter out)
    {
	super.printConfiguration(iPrefix, prefix, printName, out);
	out.println(prefix + "mean interarrival time: " + mean);
	out.println(prefix + "number of bicycles per trip: " + nbikes);
	out.println(prefix + "starting hub: " + hub.getName());
	out.println(prefix + "destination hubs:");
	out.println(prefix + "probability function: "
		    + ((sfpf == null)? "<none>": sfpf.getName()));
	double last = 0;
	for (Dest dest: dests) {
	    out.println(prefix + "    " + dest.hub.getName() + ":");
	    out.println(prefix + "        probability: "
			+ (dest.cvalue - last));
	    last = dest.cvalue;
	    out.println(prefix + "        probability of using overflow area: "
			+ dest.oprob);
	}
    }
}

//  LocalWords:  RoundTripGenerator printConfiguration sim sfpf
//  LocalWords:  interarrival nbikes destHubs overflowProb iPrefix
//  LocalWords:  printName
