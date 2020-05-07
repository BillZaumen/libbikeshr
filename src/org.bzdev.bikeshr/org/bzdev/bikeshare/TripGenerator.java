package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.devqsim.SimulationEvent;
import org.bzdev.devqsim.SimFunctionTwo;
import org.bzdev.lang.Callable;
import org.bzdev.math.RealValuedFunctionTwo;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.rv.ExpDistrRV;

import java.io.PrintWriter;
import java.util.Vector;
import java.util.Arrays;

/**
 * Trip generator class.
 * This is the base class for trip generators.
 * It provides some abstract methods that must be implemented,
 * the method {@link TripGenerator#restart()} to restart a
 * trip generator, and the method {@link TripGenerator#stop()}
 * to stop the trip generator.
 * <P>
 * Subclasses will implement the methods {@link #getNextInterval()}
 * and {@link #action()}. The method {@link #getNextInterval()} will
 * typically return values created by a random-number generator.
 * The method {@link #action()} will schedule a sequence of events
 * associated with a trip.
 * <P>
 * The interface {@link TripDataListener}, or the class
 * {@link TripDataAdapter} that implements it, is used for
 * obtaining data about individual trips.

 * @see #getNextInterval()
 * @see #action()
 * @see TripDataListener
 * @see TripDataAdapter
 */
public abstract class TripGenerator extends Actor {

    static String errorMsg(String key, Object... args) {
	return BikeShare.errorMsg(key, args);
    }

    DramaSimulation sim;

    /*
     * Constructor.
     * @param sim the simulation
     * @param name the name of this object
     * @param intern true if this object should be interned in the
     *        simulation's name table; false otherwise
     */
    public TripGenerator(DramaSimulation sim, String name, boolean intern) {
	super(sim, name, intern);
	this.sim = sim;
	event = sim.scheduleInitCall(new Callable() {
		public void call() {
		    TripGenerator.this.sim.scheduleCall(new Callable() {
			    public void call() {
				restart();
			    }
			}, TripGenerator.this.sim.getTicks(initialDelay));
		    initialDelayFrozen = true;
		}
	    }, 0);
    }

    Callable task = null;


    RealValuedFunctionTwo pf = null;
    SimFunctionTwo sfpf = null;	// Used only by printConfiguration

    /**
     * Set the probability function.
     * This function may be used under the following circumstances:
     * <UL>
     *   <LI> the starting hub and destination hub are in the
     *        same user domain.
     *   <LI> the hubs' user domains have parent domains and
     *        those parent domains are identical.
     *   <LI> the starting and destination hubs are members of
     *        the domain that is the parent domain of each hub's
     *        user domain.
     * </UL>
     * When these circumstances exist, one may select a trip based on
     * the user domain or the common parent domain.  When the user
     * domain is selected, the trip (or one leg of a trip) uses the
     * bike-sharing system and the user domain is used to compute
     * the travel time. When the parent domain is selected, the
     * trip (or one leg of a trip) does not use the bike sharing
     * system and the parent domain is used to compute the travel time.
     * <P>
     * The probability function is defined as a real-valued
     * function f(d<sub>1</sub>,d<sub>2</sub>) whose range
     * is [0.0, 1.0] and whose domain contains non-negative
     * values of its arguments. When called, d<sub>1</sub> will
     * be the estimated delay computed using the delay table for
     * the source hub's user domain and d<sub>2</sub> will be the
     * delay computed using the delay table for the parent domain
     * of the source hub's user domain.  The probability this
     * function computes is the probability that the delay used
     * is d<sub>1</sub>. If the function is null, the d<sub>1</sub>
     * will be used if it is less than d<sub>2</sub>.  If d<sub>2</sub>
     * is used, bicycles will not be removed or added to a hub as
     * the trip is assumed to use some other service.
     * <P>
     * When the probability function is null and there is a choice
     * between domains, the user domain is chosen
     * if d<sub>1</sub> &lt; d<sub>2</sub>; otherwise the parent
     * domain is chosen.  A realistic probability function should
     * take into account that the users will sometimes guess travel
     * times incorrectly and that users may be less inclined to use
     * a bicycle as trip durations increase and the trip becomes more
     * tiring.
     * @param pf the probability function; null for a default behavior
     */
    public void setProbabilityFunction(RealValuedFunctionTwo pf) {
	this.pf = pf;
	if (pf == null) {
	    sfpf = null;
	} else {
	    sfpf = new SimFunctionTwo(sim, "<probability function>", false, pf);
	}
    }

    /**
     * Set the probability function using a simulation function.
     * A simulation function allows a function to be specified as a
     * simulation object that can be created using a factory.  The
     * probability function may be used under the following
     * circumstances:
     * <UL>
     *   <LI> the starting hub and destination hub are in the
     *        same user domain.
     *   <LI> the hubs' user domains have parent domains and
     *        those parent domains are identical.
     *   <LI> the starting and destination hubs are members of
     *        the domain that is the parent domain of each hub's
     *        user domain.
     * </UL>
     * When these circumstances exist, one may select a trip based on
     * the user domain or the common parent domain.  When the user
     * domain is selected, the trip (or one leg of a trip) uses the
     * bike-sharing system and the user domain is used to compute
     * the travel time. When the parent domain is selected, the
     * trip (or one leg of a trip) does not use the bike sharing
     * system and the parent domain is used to compute the travel time.
     * <P>
     * The probability function is defined as a real-valued
     * function f(d<sub>1</sub>,d<sub>2</sub>) whose range
     * is [0.0, 1.0] and whose domain contains non-negative
     * values of its arguments. When called, d<sub>1</sub> will
     * be the estimated delay computed using the delay table for
     * the source hub's user domain and d<sub>2</sub> will be the
     * delay computed using the delay table for the parent domain
     * of the source hub's user domain.  The probability this
     * function computes is the probability that the delay used
     * is d<sub>1</sub>. If the function is null, the d<sub>1</sub>
     * will be used if it is less than d<sub>2</sub>.  If d<sub>2</sub>
     * is used, bicycles will not be removed or added to a hub as
     * the trip is assumed to use some other service.
     * <P>
     * When the probability function is null and there is a choice
     * between domains, the user domain is chosen
     * if d<sub>1</sub> &lt; d<sub>2</sub>; otherwise the parent
     * domain is chosen.  A realistic probability function should
     * take into account that the users will sometimes guess travel
     * times incorrectly and that users may be less inclined to use
     * a bicycle as trip durations increase and the trip becomes more
     * tiring.
     * @param sfpf the probability function; null for a default behavior
     */
    public void setProbabilitySimFunction(SimFunctionTwo sfpf) {
	this.sfpf = sfpf;
	if (sfpf == null) {
	    pf = null;
	} else {
	    pf = sfpf.getFunction();
	}
    }

    /**
     * Get the probability function.
     * The probability function determines which of two domains
     * to use for computing trip times and whether or not the
     * bike-sharing system is used for a trip or portion of a
     * trip.
     * This function's definition is provided by the documentation
     * for {@link #setProbabilityFunction(RealValuedFunctionTwo)}.
     * @return the probability function
     * @see #setProbabilityFunction(RealValuedFunctionTwo)
     * @see #setProbabilitySimFunction(SimFunctionTwo)
     */
    protected RealValuedFunctionTwo getProbabilityFunction() {
	return pf;
    }


    /**
     * Get the time interval to wait before scheduling the next trip.
     * @return the next trip interarrival time
     */
    protected abstract double getNextInterval();

    boolean initialDelayFrozen = false;
    private double initialDelay = 0.0;

    /**
     * Get an initial delay.
     * The initial delay is the delay before the trip generator
     * is started.  The default value is 0.0. After this initial
     * delay is passed, the method {@link #getNextInterval()} determines
     * when a trip will start.  As a result, the starting time for
     * the first trip is the value getInitialDelay() + getNextInterval().
     * @return the initial delay in seconds
     */
    protected double getInitialDelay() {
	return initialDelay;
    }

    /**
     * Set the initial delay.
     * The initial delay is the simulation time to wait from the time
     * a trip generator is created to the time the trip generator starts.
     * This method may be called before the simulation is run (or restarted)
     * and must not be called after the simulation is run (or restarted).
     * @param delay the initial delay in seconds
     * @exception IllegalArgumentException the delay was negative.
     * @exception IllegalStateException an attempt to set the delay was
     *            made after the simulation had started.
     */
    public void setInitialDelay(double delay) {
	if (initialDelayFrozen) {
	    throw new IllegalStateException
		(errorMsg("tripGeneratorFrozen", getName()));
	}
	if (delay < 0.0) {
	    throw new IllegalArgumentException
		(errorMsg("argNegative", delay));
	}
	initialDelay = delay;
    }


    private static long currentTripID = 0;

    /**
     * Create a new trip ID.
     * Trip IDs must be created using this method to avoid duplicating an ID.
     * @return a new trip ID.
     */
    protected long createTripID() {
	return ++currentTripID;
    }

    /**
     * Perform an action that generates a trip.
     * Implementations of this method will typically call the
     * {@link Hub} method {@link Hub#sendUsers(Hub,int,boolean,Callable)} or
     * {@link Hub#sendUsers(Hub,int,boolean,RealValuedFunctionTwo,Callable)}.
     * The {@link Callable} argument handles multi-hop trips and also
     * a final call to {@link #fireTripEnded(long,Hub)} for successfully
     * completed trips. The return value for
     * {@link Hub#sendUsers(Hub,int,boolean,Callable) sendUsers} or
     * {@link Hub#sendUsers(Hub,int,boolean,RealValuedFunctionTwo,Callable) sendUsers}
     * can be used to determine whether
     * {@link #fireTripStarted(long,Hub,HubDomain)} or
     * {@link #fireTripFailedAtStart(long,Hub)} should be called.
     * If {@link Hub#sendUsers(Hub,int,boolean,Callable) sendUsers} or
     * {@link Hub#sendUsers(Hub,int,boolean,RealValuedFunctionTwo,Callable) sendUsers}
     * is used mid-trip, a failure is noted by calling
     * {@link #fireTripFailedMidstream(long,Hub)} (which should not be
     * called except for multi-hop trips). For multi-hop trips, the
     * {@link #fireTripPauseStart(long,Hub)} should be called to note that
     * a trip has temporarily paused at an intermediate hub and
     * {@link #fireTripPauseEnd(long,Hub,HubDomain)} to indicate that the trip
     * is continuing after a pause.
     * <P>
     * Note: when this method returns false, the traffic generator cannot
     * be restarted.
     * @return true if more trips can be generated; false otherwise
     */
    protected abstract boolean action();

    private boolean started = false;
    SimulationEvent event = null;

    /**
     * Stop the trip generator.
     * If this method is called when a trip generator is not
     * running, it will have no effect. After {@link #stop()} is
     * called, the traffic generator may be restarted. If one stops
     * and restarts before the event associated with the initial delay
     * is processed, the initial delay will be ignored.
     * @see #restart()
     */
    public void stop() {
	if (event != null) {
	    event.cancel();
	    event = null;
	    started = false;
	    trace(BikeShare.level1, "trip generator stopped");
	}
    }

    /**
     * Determine if the trip generator is running.
     * @return true if the trip generator is running; false if it
     *         is not running
     */
    public boolean isRunning() {
	return (event != null);
    }

    /**
     * Restart the trip generator after it has been stopped.
     * If the trip generator is already
     * running, calling {@link #restart()} will have no effect.
     * @see #stop()
     */
    public void restart() {
	if (started) return;
	trace(BikeShare.level1, "trip generator started");
	started = true;
	task = new Callable() {
		public void call() {
		    if (action()) {
			double interval = getNextInterval();
			if (interval >= 0.0) {
			    event = sim.scheduleCall(task,
						     sim.getTicks(interval));
			}
		    } else {
			// started is still true so we cannot restart.
			event = null;
		    }
		}
	    };
	double interval = getNextInterval();
	if (interval >= 0.0) {
	    event = sim.scheduleCall(task, sim.getTicks(interval));
	}
    }

    private Vector<TripDataListener> tripDataListenerList = new Vector<>();

    /**
     * Add a trip data listener to the current object.
     * @param listener the listener to add
     */
    public void addTripDataListener(TripDataListener listener) {
	tripDataListenerList.add(listener);
    }

    /**
     * Remove a trip data listener from the current object.
     * @param listener the listener to remove
     */
    public void removeTripDataListener(TripDataListener listener) {
	tripDataListenerList.remove(listener);
    }

    /**
     * Notify each trip data listener that a trip has started.
     * @param tripID the ID for this trip
     * @param hub the hub at which this event occurred
     * @param d the HubDomain used to determine the time the trip takes
     *        for the first hop.
     */
    protected void fireTripStarted(long tripID, Hub hub, HubDomain d)
    {
	double time = sim.currentTime();
	long ticks = sim.currentTicks();
	for (TripDataListener listener: tripDataListenerList) {
	    listener.tripStarted(tripID, time, ticks, hub, d);
	}
    }

    /**
     * Notify each trip data listener that a trip has just paused at an
     * intermediate hub.
     * @param tripID the ID for this trip
     * @param hub the hub at which this event occurred
     */
    protected void fireTripPauseStart(long tripID, Hub hub)
    {
	double time = sim.currentTime();
	long ticks = sim.currentTicks();
	for (TripDataListener listener: tripDataListenerList) {
	    listener.tripPauseStart(tripID, time, ticks, hub);
	}
    }

    /**
     * Notify each trip data listener that a trip is continuing
     * after a pause.
     * @param tripID the ID for this trip
     * @param hub the hub at which this event occurred
     */
    protected void fireTripPauseEnd(long tripID, Hub hub, HubDomain d)
    {
	double time = sim.currentTime();
	long ticks = sim.currentTicks();
	for (TripDataListener listener: tripDataListenerList) {
	    listener.tripPauseEnd(tripID, time, ticks, hub, d);
	}
    }

    /**
     * Notify each trip data listener that a trip has started.
     * @param tripID the ID for this trip
     * @param hub the hub at which this event occurred
     */
    protected void fireTripEnded(long tripID, Hub hub)
    {
	double time = sim.currentTime();
	long ticks = sim.currentTicks();
	for (TripDataListener listener: tripDataListenerList) {
	    listener.tripEnded(tripID, time, ticks, hub);
	}
    }

    /**
     * Notify each trip data listener that a trip failed to start.
     * @param tripID the ID for this trip
     * @param hub the hub at which this event occurred
     */
    protected void fireTripFailedAtStart(long tripID, Hub hub)
    {
	double time = sim.currentTime();
	long ticks = sim.currentTicks();
	for (TripDataListener listener: tripDataListenerList) {
	    listener.tripFailedAtStart(tripID, time, ticks, hub);
	}
    }

    /**
     * Notify each trip data listener that a trip failed midstream.
     * @param tripID the ID for this trip
     * @param hub the hub at which this event occurred
     */
    protected void fireTripFailedMidstream(long tripID, Hub hub)
    {
	double time = sim.currentTime();
	long ticks = sim.currentTicks();
	for (TripDataListener listener: tripDataListenerList) {
	    listener.tripFailedMidstream(tripID, time, ticks, hub);
	}
    }

     /**
     * Print the configuration for an instance of TripGenerator.
     * The documentation for method
     * {@link org.bzdev.devqsim.SimObject#printConfiguration(String,String,boolean,java.io.PrintWriter)}
     * contains a description of how this method is used and how to
     * override it. The method
     * {@link org.bzdev.drama.Actor#printConfiguration(String,String,boolean,java.io.PrintWriter)}
     * describes this method as it applies to all actors .
     * The data that will be printed when this method is called are
     * the following.
     * <P>
     * For class {@link TripGenerator}:
     * <UL>
     *    <LI> the initial delay - the time to wait before trips are
     *         generated.
     * </UL>
     * @param iPrefix the prefix to use for an initial line when printName is
     *        true with null treated as an empty string
     * @param prefix a prefix string (typically whitespace) to put at
     *        the start of each line other than the initial line that is
     *        printed when printName is true
     * @param printName requests printing the name of an object
     * @param out the output print writer
     */
    @Override
    public void printConfiguration(String iPrefix, String prefix,
				   boolean printName,
				   PrintWriter out)
    {
	super.printConfiguration(iPrefix, prefix, printName, out);
	out.println(prefix + "initial delay: " + getInitialDelay());
	out.println(prefix + "probability function: "
		    + ((sfpf == null)? "<none>": sfpf.getName()));
	
    }

     /**
     * Print the configuration for an instance of TripGenerator.
     * The documentation for method
     * {@link org.bzdev.devqsim.SimObject#printState(String,String,boolean,java.io.PrintWriter)}
     * contains a description of how this method is used and how to
     * override it. The method
     * {@link org.bzdev.drama.Actor#printState(String,String,boolean,java.io.PrintWriter)}
     * describes this method as it applies to all actors. In addition,
     * the state that is printed includes the following items.
     * <P>
     * Defined in {@link TripGenerator}:
     * <UL>
     *   <LI> whether or not the trip generator is running and the next
     *        time an action will be run if the trip generator is running.
     * </UL>
     * @param iPrefix the prefix to use for an initial line when printName is
     *        true with null treated as an empty string
     * @param prefix a prefix string (typically whitespace) to put at
     *        the start of each line other than the initial line that is
     *        printed when printName is true
     * @param printName requests printing the name of an object
     * @param out the output print writer
     */
    @Override
    public void printState(String iPrefix, String prefix,
			   boolean printName, PrintWriter out)
    {
	super.printState(iPrefix, prefix, printName, out);
	boolean r = isRunning();
	if (r) {
	    out.println(prefix + "running: " + r);
	} else {
	    out.println(prefix + "running: " + r + ", next action at "
			+ sim.getTime(event.getTime()) + " sec");
	}
    }
}

//  LocalWords:  TripGenerator getNextInterval TripDataListener sim
//  LocalWords:  TripDataAdapter interarrival getInitialDelay boolean
//  LocalWords:  IllegalArgumentException IllegalStateException multi
//  LocalWords:  tripGeneratorFrozen argNegative sendUsers HubDomain
//  LocalWords:  RealValuedFunctionTwo fireTripEnded fireTripStarted
//  LocalWords:  fireTripFailedAtStart fireTripFailedMidstream tripID
//  LocalWords:  fireTripPauseStart fireTripPauseEnd superclass
//  LocalWords:  printConfiguration iPrefix printName whitespace
//  LocalWords:  printState
