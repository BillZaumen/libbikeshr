package org.bzdev.bikeshare;
import org.bzdev.devqsim.*;
import org.bzdev.drama.*;
import org.bzdev.drama.common.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;

import java.util.Set;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.io.PrintWriter;

/**
 * Base class for hub balancers.
 * A hub balancer schedules hub workers in response to changes
 * in the hub states. This class will arrange for the hub workers
 * that continually move between hubs for load balancing to start
 * automatically. Subclasses can schedule additional hub workers
 * based on changing conditions.
 * <P>
 * Two classes must be implemented by subclasses:
 * <UL>
 *   <LI><code>startAdditionalWorkers()</code>. This method will
 *      be called when hubs change the number of bicycles in
 *      the overflow area or when the number of bicycles in
 *      the preferred area changes when the number is above
 *      or below the upper and lower thresholds.
 *   <LI><code>getHubSorter(HubWorker.Mode, StorageHub,Hub[])</code>. This
 *      method determines the
 *      {@link HubWorker.HubSorter hub sorter} to use by workers when
 *      the worker is started in a particular mode.
 * </UL>
 *
 */
@DMethodContext(helper = "org.bzdev.drama.OnConditionChange",
		localHelper = "HubBalancerOnConditionChange")
public abstract class HubBalancer extends Actor {

    static {
	HubBalancerOnConditionChange.register();
    }

    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of this object
     * @param intern true if this object should be interned in the
     *        simulation's name table; false otherwise
     */
    protected HubBalancer(DramaSimulation sim, String name, boolean intern) {
	super(sim, name, intern);
	// Wait until everything is configured.
	sim.scheduleInitCall(new Callable() {
		public void call() {
		    startInitialWorkers();
		}
	    }, 0);
    }

    SysDomain sysDomain = null;

    /**
     * Get the system domain.
     * @return the system domain for this hub balancer
     */
    protected SysDomain getSysDomain() {return sysDomain;}

    /**
     * Initialize this hub balancer by configuring its system domain.
     * @param sysDomain the system domain that should be associated
     *        with this load balancer
     */
    public void initDomain(SysDomain sysDomain) {
	this.sysDomain = sysDomain;
	joinDomain(sysDomain, true);
    }

    Set<Hub> overSet = new HashSet<>();
    Set<Hub> underSet = new HashSet<>();
    Set<Hub> overflowSet = new HashSet<>();
    LinkedList<Hub> overSetQueue = new LinkedList<>();
    LinkedList<Hub> underSetQueue = new LinkedList<>();

    /**
     * Get the set of hubs whose preferred location contains more
     * bicycles than allowed by the hub's upper trigger.
     * @return the set of hubs whose preferred location contains more
     * bicycles than allowed by the hub's upper trigger
     */
    protected Set<Hub> getOverSet() {
	return Collections.unmodifiableSet(overSet);
    }

    /**
     * Get the set of hubs whose preferred location contains less
     * bicycles than allowed by the hub's lower trigger.
     * @return the set of hubs whose preferred location contains less
     * bicycles than allowed by the hub's lower trigger
     */
    public Set<Hub> getUnderSet() {
	return Collections.unmodifiableSet(underSet);
    }

    /**
     * Get the set of hubs whose overflow area contains bicycles.
     * The hubs are ones using the same user domain as this hub
     * balancer.
     * @return the set of hubs whose overflow area contains bicycles
     */
    protected Set<Hub> getOverflowSet() {
	return Collections.unmodifiableSet(overflowSet);
    }

    private boolean initialWorkersStarted = false;


    /**
     * Start the workers that loop between hubs.
     */
    protected void startInitialWorkers()
    {
	if (initialWorkersStarted) {
	    return;
	} else {
	    initialWorkersStarted = true;
	}
	trace(BikeShare.level1, "starting initial workers");
	for (StorageHub shub: sysDomain.getStorageHubs()) {
	    HubWorker.Mode mode = HubWorker.Mode.LOOP;
	    HubWorker.HubSorter sorter = null;
	    int n = shub.getInitialNumberOfWorkers(mode);
	    double interval = shub.getInterval(mode);
	    double subinterval = interval/n;
	    double offset = 0.0; // interval offset to start.
	    trace(BikeShare.level2, 
		  "starting initial workers for storage hub %s, mode %s",
		  shub.getName(), mode);
	    for (int i = 0; i < n; i++) {
		HubWorker worker = shub.pollWorkers();
		if (worker != null) {
		    trace(BikeShare.level2, "starting worker %s",
			  worker.getName());
		    sorter = getHubSorter(mode, shub,
					  shub.getHubsAsArray(mode));
		    worker.start(mode, sorter, interval, offset);
		    offset += subinterval;
		} else {
		    trace(BikeShare.level2, "could not find worker");
		}
	    }
	    mode = HubWorker.Mode.LOOP_WITH_PICKUP;
	    n = shub.getInitialNumberOfWorkers(mode);
	    interval = shub.getInterval(mode);
	    subinterval = interval/n;
	    offset = 0.0; // interval offset to start.
	    trace(BikeShare.level2, 
		  "starting initial workers for storage hub %s, mode %s",
		  shub.getName(), mode);
	    for (int i = 0; i < n; i++) {
		HubWorker worker = shub.pollWorkers();
		if (worker != null) {
		    trace(BikeShare.level2, "starting worker %s",
			  worker.getName());
		    sorter = getHubSorter(mode, shub,
					  shub.getHubsAsArray(mode));
		    worker.start(mode, sorter, interval, offset);
		    offset += subinterval;
		} else {
		    trace(BikeShare.level2, "could not find worker");
		}
	    }
	    mode = HubWorker.Mode.LOOP_TO_FIX_OVERFLOWS;
	    n = shub.getInitialNumberOfWorkers(mode);
	    interval = shub.getInterval(mode);
	    subinterval = interval/n;
	    offset = 0.0; // interval offset to start.
	    trace(BikeShare.level2, 
		  "starting initial workers for storage hub %s, mode %s",
		  shub.getName(), mode);
	    for (int i = 0; i < n; i++) {
		HubWorker worker = shub.pollWorkers();
		if (worker != null) {
		    trace(BikeShare.level2, "starting worker %s",
			  worker.getName());
		    sorter = getHubSorter(mode, shub,
					  shub.getHubsAsArray(mode));
		    worker.start(mode, sorter, interval, offset);
		    offset += subinterval;
		} else {
		    trace(BikeShare.level2, "could not find worker");
		}
	    }
	}
    }

    /**
     * Start additional works on an as-needed basis.
     * This method may be called in response to a condition change.
     * When a subclass implements this method, it is the
     * responsibility of the subclass to determine the number of
     * workers that should be used. This number may be zero. The
     * class {@link BasicHubBalancer}, for example, uses an
     * implementation where there is a threshold for the number of
     * hubs that can use an extra worker and a quiet period that
     * prevents a large number of workers from being scheduled at
     * once.
     */
    protected abstract void startAdditionalWorkers();

    /**
     * Get a hub sorter.
     * @param mode the worker mode for this hub sorter
     * @param shub the storage hub used to obtain a worker.
     * @param hubs a list of hubs to sort.
     * @return a hub sorter.
     * @see org.bzdev.bikeshare.HubWorker.HubSorter
     */
    public abstract HubWorker.HubSorter getHubSorter(HubWorker.Mode mode,
						     StorageHub shub,
						     Hub[] hubs);

    /**
     * Respond to a change in a hub condition.
     * This implementation will ignore all condition modes other
     * than OBSERVER_NOTIFIED.
     * @param c the hub condition representing a change
     * @param mode the type of condition change
     * @param source the object that caused the change.
     */
    @DMethodImpl("org.bzdev.drama.OnConditionChange")
    protected void onConditionChangeImpl(HubCondition c,
					 ConditionMode mode,
					 SimObject source)
    {
	if (mode == ConditionMode.OBSERVER_NOTIFIED) {
	    Hub hub = c.getChangedHub();
	    if (c.getInOverSet()) {
		overSet.add(hub);
	    } else {
		overSet.remove(hub);
	    }
	    if (c.getInUnderSet()) {
		underSet.add(hub);
	    } else {
		underSet.remove(hub);
	    }
	    if (c.getInOverflowSet()) {
		overflowSet.add(hub);
	    } else {
		overflowSet.remove(hub);
	    }
	    trace(BikeShare.level2, "trying to start additional workers");
	    startAdditionalWorkers();
	    return;
	}
    }

     /**
     * Print the configuration for an instance of HubBalancer.
     * The documentation for method
     * {@link org.bzdev.devqsim.SimObject#printConfiguration(String,String,boolean,java.io.PrintWriter)}
     * contains a description of how this method is used and how to
     * override it. The method
     * {@link org.bzdev.drama.Actor#printConfiguration(String,String,boolean,java.io.PrintWriter)}
     * describes the data that will be printed for the
     * superclass of this class. The data that will be printed
     * when this method is called are the following.
     * <P>
     * For class {@link HubBalancer}:
     * <UL>
     *   <LI> (No data associated with the HubBalancer class is printed.)
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
    }

     /**
     * Print the state for an instance of HubBalancer.
     * The documentation for method
     * {@link org.bzdev.devqsim.SimObject#printState(String,String,boolean,java.io.PrintWriter)}
     * contains a description of how this method is used and how to
     * override it. The method
     * {@link org.bzdev.drama.Actor#printState(String,String,boolean,java.io.PrintWriter)}
     * describes the data that will be printed for the
     * superclass of this class. The data that will be printed
     * when this method is called are the following.
     * <P>
     * For class {@link HubBalancer}:
     * <UL>
     *   <LI> the over-trigger set (the hubs whose bicycle count is
     *        larger than its upper-trigger value).
     *   <LI> the under-trigger set (the hubs whose bicycle count is
     *        below its under-trigger value).
     *   <LI> the overflow set (the hubs that have bicycles in their
     *        overflow areas).
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
	if (overSet.isEmpty()) {
	    out.println(prefix + "over-trigger set: <empty>");
	} else {
	    out.println(prefix + "over-trigger set:");
	    for (Hub hub: overSet) {
		out.println(prefix + "    " + hub.getName());
	    }
	}

	if (underSet.isEmpty()) {
	    out.println(prefix + "under-trigger set: <empty>");
	} else {
	    out.println(prefix + "under-trigger set:");
	    for (Hub hub: underSet) {
		out.println(prefix + "    " + hub.getName());
	    }
	}

	if (overflowSet.isEmpty()) {
	    out.println(prefix + "overflow set: <empty>");
	} else {
	    out.println(prefix + "overflow set:");
	    for (Hub hub: overflowSet) {
		out.println(prefix + "    " + hub.getName());
	    }
	}
    }
}

//  LocalWords:  balancers balancer startAdditionalWorkers HubWorker
//  LocalWords:  getHubSorter StorageHub HubSorter sim HubBalancer
//  LocalWords:  HubBalancerOnConditionChange startInitialWorkers
//  LocalWords:  sysDomain BasicHubBalancer shub printConfiguration
//  LocalWords:  boolean superclass iPrefix printName whitespace
//  LocalWords:  printState
