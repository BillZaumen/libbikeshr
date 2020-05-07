package org.bzdev.bikeshare;
import org.bzdev.devqsim.SimulationEvent;
import org.bzdev.drama.*;
import org.bzdev.lang.Callable;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.rv.IntegerRandomVariable;

import java.io.PrintWriter;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.LinkedList;

public class BurstTripGenerator extends TripGenerator {

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
    public BurstTripGenerator(DramaSimulation sim,
			      String name,
			      boolean intern)
    {
	super(sim, name, intern);
	this.sim = sim;
    }

    Hub hub;
    double initialTime = 0.0;
    double time = 0.0;
    int nbikes = 0;
    boolean fanIn = false;
    double burstTime;

    int estimationCount = 25;
    double estimationFactor = 1.0;
    double estimationOffset = 0.0;

    /**
     * Set parameters for the fan-in case.
     * The estimation count is used to compute an estimated delay
     * between two hubs, assuming that <code>estimationCount</code>
     * is the number of users traveling together (the delay is the
     * mean delay for the slowest of a set of <code>estimationCount</code>
     * users). This estimated delay is then multiplied by
     * <code>estimationFactor</code>. Finally a  fixed number
     * <code>estimationOffset</code> is added to that delay to compute
     * the delay that will be assumed.
     * @param estimationCount the number of users to assume in a delay
     *        estimate
     * @param estimationFactor the factor by which to scale the delay
     * @param estimationOffset an offset (in seconds) to add to the delay
     */
    public void setFanInParameters(int estimationCount,
				   double estimationFactor,
				   double estimationOffset)
    {
	this.estimationCount = estimationCount;
	this.estimationFactor = estimationFactor;
	this.estimationOffset = estimationOffset;
    }

    /**
     * Get the estimation-count parameter.
     * @return the estimation count
     * @see #setFanInParameters(int,double,double)
     */
    public int getEstimationCount() {
	return estimationCount;
    }

    /**
     * Get the estimation-factor parameter.
     * @return the estimation factor
     * @see #setFanInParameters(int,double,double)
     */
    public double getEstimationFactor() {
	return estimationFactor;
    }

    /**
     * Get the estimation-offset parameter.
     * @return the estimation offset
     * @see #setFanInParameters(int,double,double)
     */
    public double getEstimationOffset() {
	return estimationOffset;
    }

    static class Other implements Comparable<Other> {
	public Hub hub;
	public double cvalue;
	public double oprob;

	private long instance;
	static long instanceCounter = 0;

	Other(Hub hub, double cvalue, double oprob) {
	    this.hub = hub;
	    this.cvalue = cvalue;
	    this.oprob = oprob;
	    this.instance = (instanceCounter++);
	}
	
	public int compareTo(Other other) {
	    if (cvalue < other.cvalue) return -1;
	    if (cvalue > other.cvalue) return 1;
	    if (instance > other.instance) return -1;
	    if (instance < other.instance) return 1;
	    else return 0;
	}
    }

    Other[] others;

    /**
     * Initialization.
     * @param hub the hub from which trips originate
     * @param time the time at which trips are generated
     * @param nbikes is the number of
     * @param otherHubs the destination hubs
     * @param weights the probabilities that the destination is a
     *        particular hub
     * @param overflowProb the probability for a destination hub
     *        that the overflow area is used regardless of whether
     *        or not there is excess capacity in the preferred location
     * @param fanIn true of traffic goes in the reverse direction (i.e.,
     *        towards Hub hub); false if the traffic starts at Hub hub
     */
    public void init(Hub hub, double time,
		     int nbikes,
		     Hub[] otherHubs,
		     double[] weights,
		     double[] overflowProb,
		     boolean fanIn)
    {
	this.hub = hub;

	initialTime = sim.currentTime();
	double idelay = time - initialTime;
	setInitialDelay(fanIn? 0.0: idelay);
	this.nbikes = nbikes;
	burstTime = time;
	this.fanIn = fanIn;

	double sum = 0.0;
	weights = weights.clone();
	for (int i = 0; i < otherHubs.length; i++) {
	    if (weights[i] < 0.0) {
		throw new IllegalArgumentException
		    (errorMsg("weights", weights[i]));
	    }
	    sum += weights[i];
	}

	for (int i = 0; i < otherHubs.length; i++) {
	    weights[i] /= sum;
	}
	
	double total = 0.0;
	double last = 0.0;
	others = new Other[weights.length];
	for (int i = 0; i < otherHubs.length; i++) {
	    total += weights[i];
	    others[i] = new Other(otherHubs[i], total, overflowProb[i]);
	    last = total;
	}
	trace(BikeShare.level1, "trip generator configured");
    }

    Other chooseOther() {
	double rval = StaticRandom.nextDouble();
	while (rval == 1.0) rval = StaticRandom.nextDouble();
	Other key = new Other(null, rval, 0.0);
	int index = Arrays.binarySearch(others, key);
	if (index < 0) index = -index - 1;
	return others[index];
    }

    @Override
    protected double getNextInterval() {
	return getInitialDelay() + initialTime - sim.currentTime();
    }

    LinkedList<WeakReference<SimulationEvent>> wrlist =
	new LinkedList<WeakReference<SimulationEvent>>();

    @Override
    public void restart() {
	super.restart();
	if (fanIn) {
	    for (int i = 0; i < nbikes; i++) {
		final Hub other = chooseOther().hub;
		HubDomain d = hub.getUsrDomain();
		double delay = d.estimateDelay(other, hub, estimationCount);
		delay *= estimationFactor;
		delay += estimationOffset;
		double st = burstTime - delay - sim.currentTime();
		Callable callable = new Callable() {
			public void call() {
			    startOneTrip(other, false, true);
			}
		    };
		SimulationEvent event = sim.scheduleCall(callable,
							 sim.getTicks(st));
		wrlist.add(new WeakReference<SimulationEvent>(event));
	    }
	}
    }

    @Override
    public void stop() {
	super.stop();
	for (WeakReference<SimulationEvent> eref: wrlist) {
	    SimulationEvent event = eref.get();
	    if (event != null && event.isPending()) event.cancel();
	}
	wrlist.clear();
    }

    int tripsTried = 0;
    int tripsInProgress = 0;
    int tripsFailed = 0;

    private void startOneTrip(Hub other,
			      boolean willOverflow,
			      boolean reverse) {
	// final Other dest = chooseOther();
	final long tripID = createTripID();
	final Hub src = reverse? other: hub;
	final Hub dest = reverse? hub: other;
	/*
	boolean willOverflow =
	    StaticRandom.nextDouble() < dest.oprob;
	*/
	trace(BikeShare.level4,
	      "sending %d bike-share users from %s to %s, "
	      + "intending to use the %s",
	      1, src, dest,
	      (willOverflow? "overflow area": "preferred area"));
	tripsTried++;
	Callable continuation = new Callable() {
		public void call() {
		    tripsInProgress--;
		    fireTripEnded(tripID, dest);
		}
	    };
	HubDomain d = src.sendUsers(dest, 1, willOverflow,
				    getProbabilityFunction(), continuation);
	if (d != null) {
	    tripsInProgress++;
	    fireTripStarted(tripID, src, d);
	} else {
	    tripsFailed++;
	    fireTripFailedAtStart(tripID, src);
	}
    }

    @Override
    protected boolean action() {
	if (!fanIn) {
	    for (int i = 0; i < nbikes; i++) {
		final Other other = chooseOther();
		boolean willOverflow =
		    StaticRandom.nextDouble() < other.oprob;
		startOneTrip(other.hub, willOverflow, false);
	    }
	} else {
	    wrlist.clear();
	}
	return false;
    }

    /**
     * {@inheritDoc}
     * <P>
     * Defined in {@link BurstTripGenerator}:
     * <UL>
     *   <LI> the burst time - he time at which trips are secheduled to
     *        leave from or arrive at the central hub. This is the
     *        exact time for the "fan-in" case and an estimate for
     *        the fan-out case.
     *   <LI> the burst size - the number of trips that will be generated.
     *   <LI> the fan-in flag - true if trips start at the central hub; false if
     *        trips end at the central hub.
     *   <LI> the central hub - the destination hub when the fan-in value is
     *        true; the originating hub when the fan-in value is false.
     *   <LI> the other-hub table - A table of hubs other than the central
     *        hub. Each entry contains the following:
     *        <UL>
     *           <LI> the hub - a destination hub when the fan-in value is
     *                false; an originating hub when the fan-in value is true.
     *           <LI> the branching probability - the probability that a trip
     *                uses the specified hub.
     *           <LI> the overflow probability - the probability that the
     *                overflow area is chosen regardless of the hub state.
     *                This value is ignored when the fan-in value is true
     *                and is not printed for this case.
     *        </UL>
     * </UL>
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printConfiguration(String iPrefix, String prefix,
			   boolean printName, PrintWriter out)
    {
	super.printConfiguration(iPrefix, prefix, printName, out);
	out.println(prefix + "burst time:" + burstTime);
	out.println(prefix + "burst size:" + nbikes);
	out.println(prefix + "fan in: " + fanIn);
	out.println(prefix + "central hub: " + hub.getName());
	if (others.length == 0) {
	    out.println(prefix + "[no other hubs]");
	} else {
	    out.println(prefix + "Other hubs:");
	    out.println(prefix + "    Hub" + others[0].hub.getName()
			+ " -- branching probability = " + others[0].cvalue
			+ ", overflow probability = " + others[0].oprob);
	    for (int i = 1; i < others.length; i++) {
		if (fanIn) {
		    out.println(prefix + "    Hub" + others[i].hub.getName()
				+ " -- branching probability = "
				+ (others[i].cvalue - others[i-1].cvalue));
		} else {
		    out.println(prefix + "    Hub" + others[i].hub.getName()
				+ " -- branching probability = "
				+ (others[i].cvalue - others[i-1].cvalue)
				+ ", overflow probability = "
				+ others[i].oprob);
		}
	    }
	}
    }

    /**
     * {@inheritDoc}
     * <P>
     * Defined for class BurstTripGenerator:
     * <UL>
     * </UL>
     */
    public void printState(String iPrefix, String prefix,
			   boolean printName, PrintWriter out)
    {
	super.printConfiguration(iPrefix, prefix, printName, out);

	boolean tripsPending = (tripsTried != nbikes);
	boolean tripsCompleted = (tripsPending == false) && (tripsInProgress == 0);
	
	out.println(prefix + "trips pending: " + tripsPending);
	out.println(prefix + "trips completed: " + tripsCompleted);
	out.println(prefix + "trips in progress:" + tripsInProgress);
	out.println(prefix + "trips failed: " + tripsFailed);

    }
}
