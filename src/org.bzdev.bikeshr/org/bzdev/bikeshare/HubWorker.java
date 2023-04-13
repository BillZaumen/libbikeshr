package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.devqsim.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;
import java.io.PrintWriter;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

/**
 * Class representing a hub worker.
 * Hub workers either wait in queues or move between hubs
 * and adjust the number of bicycles in each one. An enumeration
 * type {@link HubWorker.Mode} determines what the workers do at
 * each node and whether they visit a sequence of nodes once or
 * repeatedly.
 * <P>
 * Each worker is assigned to a storage hub, which in turn maintains a
 * queue of available workers, some of which are preassigned to
 * perform particular tasks repeatedly and some of which are used on
 * demand.
 * <P>
 * Hub workers are started when a hub balancer calls the method
 * {@link #start(HubWorker.Mode, HubWorker.HubSorter,double,double)}.
 * For modes whose names contain the string <code>LOOP</code>,
 * the worker will loop between hubs repeatedly, trying to start
 * at fixed intervals.  Otherwise the worker will visit a series
 * of hubs and return to the worker's storage hub, placing themselves
 * back into the storage hub's worker queue.
 */
public class HubWorker extends Actor {

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
    public HubWorker(DramaSimulation sim, String name, boolean intern) {
	super(sim, name, intern);
	this.sim = sim;
    }

    private int capacity;
    private int nbikes = 0;

    /**
     * Get the current bicycle count at the preferred location.
     * @return the current bicycle count at the preferred location
     */
    public int getBikeCount() {
	return nbikes;
    }

    /**
     * Get the capacity at the preferred location.
     * The capacity is that maximum number of bicycles that can
     * be stored at the preferred location of this hub.
     * @return the capacity at the preferred location for this hub
     */
    public int getCapacity() {
	return capacity;
    }

    SysDomain domain;

    StorageHub storageHub = null;
    Hub currentHub = null;

    /**
     * Get the current hub.
     * @return the current hub; null if none has been set.
     */
    public Hub getCurrentHub() {
	return currentHub;
    }

    void setStorageHub(StorageHub shub) {
	if (currentHub == storageHub && shub == null) {
	    fireLeftHub(storageHub);
   	}
	if (currentHub != null && shub != null
	    && currentHub instanceof StorageHub) {
	    fireEnteredHub(shub);
	    currentHub = shub;
	}
	storageHub = shub;
    }

    // for use by StorageHub
    private boolean movingSH = false;
    void setMoving(boolean value) {movingSH = value;}
    private double mtime = 0.0;
    void setMoveCompletionTime(double value) {mtime = value;}

    /**
     * Determine if a hub worker is being moved between storage hubs.
     * @return true of the worker is being moved; false otherwise
     */
    public boolean isMoving() {return movingSH;}

    /**
     * Get the simulation time at which this worker completed a move
     * between storage hubs
     * @return the time in seconds from the start of the simulation
     */
    public double getMoveCompletionTime() {
	return mtime;
    }

    /**
     * Get the storage hub for a hub worker.
     * Each hub worker is assigned to a storage hub and this method
     * can be used to find that storage hub.
     * @return the storage hub for this hub worker
     */
    StorageHub getStorageHub() {return storageHub;}

    /**
     * Initialize a hub worker.
     * @param capacity the number of bicycles a hub worker can carry
     * @param shub the storage hub to which a hub worker is assigned
     * @param domain the hub worker's system domain
     * @param currentHub the hub at which the hub worker is initially
     *        located (this may be the hub worker's storage hub)
     */
    public void init(int capacity, StorageHub shub, SysDomain domain,
		     Hub currentHub)
    {
	this.capacity = capacity;
	this.domain = domain;
	// storageHub = shub;
	joinDomain(domain, false);
	shub.addWorker(this); // calls setStorageHub
	this.currentHub = currentHub;
    }

    boolean workerRunning = false;

    /**
     * Determine if a worker is running.
     * Running implies that the worker is not in a queue and is
     * actively processing various hubs.
     * @return true if the worker is running; false otherwise.
     */
    public boolean isRunning() {
	return workerRunning;
    }
    
    Hub[] currentHubs = null;

    /**
     * Enumeration listing the Worker modes.
     * The class {@link HubWorker} uses this enumeration as an
     * argument for the worker's method
     * {@link HubWorker#start(Mode,HubSorter,double,double)}, and essentially
     * lists the operations that a worker can perform. Similarly, the
     * {@link HubBalancer} class provides a method
     * {@link HubBalancer#getHubSorter(HubWorker.Mode,StorageHub,Hub[])} that
     * will provide a hub sorter for the specified mode, given the storage
     * hub used to get a worker and an
     * array listing the hubs that the sorter should consider.  The order
     * in which hubs are visited and which hubs of that set are visited
     * is a policy decision set by the hub balancer.
     */
    public static enum Mode {
	/**
	 * Loop between nodes and balance storage
	 */
	LOOP,
	/**
	 * Visit a specific set of nodes and balance storage
	 */
	VISIT,
	/**
	 * Loop between nodes, pick up overflows, and balance storage
	 */
	LOOP_WITH_PICKUP,
	/**
	 * Visit a specific set of nodes, pick up overflows, and
	 * balance storage
	 */
	VISIT_WITH_PICKUP,
	/**
	 * Loop between nodes and at each node, move overflows to
	 * storage when possible
	 */
	LOOP_TO_FIX_OVERFLOWS,
	/**
	 * Visit a specific set of nodes and at each node, move
	 * overflows to storage when possible
	 */
	VISIT_TO_FIX_OVERFLOWS
    }

    Mode currentMode = null;

    private Vector<HubWorkerListener> hubWorkerListenerList = new Vector<>();

    /**
     * Add a hub-worker listener to the current object.
     * @param listener the listener to add
     */
    public void addHubWorkerListener(HubWorkerListener listener) {
	hubWorkerListenerList.add(listener);
    }

    /**
     * Remove a hub-worker listener from the current object.
     * @param listener the listener to remove
     */
    public void removeHubWorkerListener(HubWorkerListener listener) {
	hubWorkerListenerList.remove(listener);
    }

    /**
     * Indicate that a worker has left its queue.
     * Note: visible only in this package because used by StorageHub alone.
     * @param hub the hub at which this event occurred
     */
    void fireDequeued(Hub hub) {
	double time = sim.currentTime();
	long ticks = sim.currentTicks();
	for (HubWorkerListener listener: hubWorkerListenerList) {
	    listener.dequeued(this, time, ticks, hub);
	}
    }

    /**
     * Indicate that a worker has entered a hub.
     * @param hub the hub at which this event occurred
     */
    protected void fireEnteredHub(Hub hub) {
	double time = sim.currentTime();
	long ticks = sim.currentTicks();
	for (HubWorkerListener listener: hubWorkerListenerList) {
	    listener.enteredHub(this, time, ticks, hub);
	}
    }

    /**
     * Indicate that a worker has started to fix the overflow area.
     * @param hub the hub at which this event occurred
     */
    protected void fireFixingOverflows(Hub hub) {
	double time = sim.currentTime();
	long ticks = sim.currentTicks();
	for (HubWorkerListener listener: hubWorkerListenerList) {
	    listener.fixingOverflows(this, time, ticks, hub);
	}
    }

    /**
     * Indicate that a worker has started to fix the preferred area.
     * @param hub the hub at which this event occurred
     */
    protected void fireFixingPreferred(Hub hub) {
	double time = sim.currentTime();
	long ticks = sim.currentTicks();
	for (HubWorkerListener listener: hubWorkerListenerList) {
	    listener.fixingPreferred(this, time, ticks, hub);
	}
    }

    /**
     * Indicate that a worker has left a hub.
     * Note: visible only in this package because used by StorageHub alone.
     * @param hub the hub at which this event occurred
     */
    void fireLeftHub(Hub hub) {
	double time = sim.currentTime();
	long ticks = sim.currentTicks();
	for (HubWorkerListener listener: hubWorkerListenerList) {
	    listener.leftHub(this, time, ticks, hub);
	}
    }

    /**
     * Indicate that a worker has been queued.
     * @param hub the hub at which this event occurred
     */
    protected void fireQueued(Hub hub) {
	double time = sim.currentTime();
	long ticks = sim.currentTicks();
	for (HubWorkerListener listener: hubWorkerListenerList) {
	    listener.queued(this, time, ticks, hub);
	}
    }

    /**
     * Indicate that a worker's bicycle count has changed.
     * @param hub the hub at which this event occurred
     * @param oldCount the old value of the bicycle count
     * @param newCount the new value fo the bicycle count
     */
    protected void fireChangedCount(Hub hub, int oldCount, int newCount) {
	double time = sim.currentTime();
	long ticks = sim.currentTicks();
	for (HubWorkerListener listener: hubWorkerListenerList) {
	    listener.changedCount(this, time, ticks, hub, oldCount, newCount);
	}
    }

    /**
     * Interface for classes that generate a traversal order for hubs.
     * Each instance of this class represents a choice of the traversal
     * order for a set of hubs. That order is determined by a subclass
     * of {@link HubBalancer}.
     */
    public static interface  HubSorter {
	/**
	 * Put the hubs associated with this object into its traversal order.
	 */
	public void sort();
	/**
	 * Get the hubs for this hub worker, listed in the
	 * traversal order for this hub.
	 * @return the hubs
	 */
	public abstract Hub[] getHubs();
	/**
	 * Get the the hubs whose bicycle count in the preferred area of the
	 * hub is larger than the nominal value for  that hub.
	 * @return an array of hubs sorted into traversal order
	 */
	public abstract Hub[] getOverNominal();
	/**
	 * The the hubs whose bicycle count in the preferred area of the
	 * hub is lower than the nominal value for  that hub.
	 * @return an array of hubs sorted into traversal order
	 */
	public abstract Hub[] getUnderNominal();

	/**
	 * Get the desired initial bicycle count for a worker.
	 * This is the number of bicycles given the state of hubs
	 * being serviced that would result in the worker having zero
	 * bicycles left at the end of a traversal, ignoring capacity
	 * limits for the worker.  This is really an estimate as the
	 * hubs may change before the worker arrives. The value returned
	 * can be negative, which indicates that there are more than
	 * the expected number of bicycles at the hubs.
	 * @return the desired initial bicycle count
	 */
	public abstract int getInitialCountEstimate();
    }

    /**
     * Start a hub worker.
     * The worker's behavior depends on worker mode as
     * described by the documentation for {@link HubWorker.Mode}.
     * The second argument is an array. Hubs will be "visited" in
     * the order provided by the array. For worker mode's whose
     * name contains the string LOOP, the hubs in the array will
     * be visited an unlimited number of times.
     * <P>
     * The specified wait will be ignore if the time needed to traverse
     * a loop is more than the wait time.
     * @param mode the worker mode to use
     * @param hubSorter the hub sorter
     * @param wait the desired time in seconds to wait after starting
     *        one loop before starting the next (used for modes that loop)
     * @param offset an initial offset in seconds before starting a loop
     *        (ignored for on-demand workers)
     */
    public void start(Mode mode, HubSorter hubSorter, double wait,
		      double offset) {
	if (mode == null) mode = Mode.LOOP;
	currentMode = mode;
	workerRunning = true;
	currentHubs = hubSorter.getHubs();
	long lwait = sim.getTicks(wait);
	long loffset = sim.getTicks(offset);
	trace(BikeShare.level2, "successfully started, servicing %d hubs",
	      currentHubs.length);
	switch(mode) {
	case LOOP:
	    loop(hubSorter, lwait, loffset);
	    break;
	case VISIT:
	    visit(hubSorter);
	    break;
	case LOOP_WITH_PICKUP:
	    loopWithPickup(hubSorter, lwait, loffset);
	    break;
	case VISIT_WITH_PICKUP:
	    visitWithPickup(hubSorter);
	    break;
	case LOOP_TO_FIX_OVERFLOWS:
	    loopToFixOverflows(hubSorter, lwait, loffset);
	    break;
	case VISIT_TO_FIX_OVERFLOWS:
	    visitToFixOverflows(hubSorter);
	    break;
	}
    }

    private void loadBikes(HubSorter hubSorter) {
	int m = hubSorter.getInitialCountEstimate();
	int n = storageHub.getBikeCount() - storageHub.getNominal();
	if (n < 0) n = 0;
	if (m < 0) {
	    n = 0;
	} else if (m < n) {
	    n = m;
	}
	if (n + nbikes > capacity) {
	    n = capacity - nbikes;
	}
	int old = nbikes;
	nbikes += storageHub.decrBikeCount(n);
	fireChangedCount(storageHub, old, nbikes);
	trace(BikeShare.level3, "took %d bicycles from storage hub \"%s\""
	      + ", worker bicycle count = %d",
	      n, storageHub.getName(), nbikes);
    }

    private void storeBikes() {
	int change = storageHub.incrBikeCount(nbikes);
	int old = nbikes;
	nbikes -= change;
	fireChangedCount(storageHub, old, nbikes);
	trace(BikeShare.level3, "added %d bicycles to storage hub \"%s\""
	      + ", worker bicycle count = %d",
	      change, storageHub.getName(), nbikes);
    }

    private void visitAux(String action, HubSorter hubSorter) {
	trace(BikeShare.level2, "started %s, nbikes = %d",
	      action, nbikes);
	hubSorter.sort();
	Hub[] harray1 = hubSorter.getOverNominal();
	Hub[] harray2 = hubSorter.getUnderNominal();
	int index1 = 0;
	int index2 = 0;
	boolean tmode = true;
	while (index1 < harray1.length ||
	       index2 < harray2.length) {
	    Hub hub1 = ((index1 < harray1.length)?
			harray1[index1]: null);
	    Hub hub2 = ((index2 < harray2.length)?
			harray2[index2]: null);
	    Hub hub = (tmode)? hub1: hub2;
	    trace(BikeShare.level4, "choosing between %s and %s",
		  ((hub1 == null)? "null": hub1.getName()),
		  ((hub2 == null)? "null": hub2.getName()));
	    if (hub == null) {
		tmode = !tmode;
		continue;
	    }
	    int take = hub.getBikeCount() - hub.getNominal();
	    trace(BikeShare.level4, "chose %s, take = %d, tmode = %b",
		  hub.getName(), take, tmode);
	    if (tmode) {
		if (take > 0) {
		    int delta = capacity - nbikes;
		    trace(BikeShare.level4, "delta = %d", delta);
		    if (delta == 0) {
			if (index2 == harray2.length) {
			    return;
			} else {
			    tmode = !tmode;
			    continue;
			}
		    }
		    if (take > delta) {
			take = delta;
			trace(BikeShare.level4, "changing take to %d", take);

		    }
		} else {
		    index1++;
		    trace(BikeShare.level4, "incrementing index1 to %d",index1);
		    if (index1 == harray1.length) {
			tmode = !tmode;
			trace(BikeShare.level4, "changing tmode to %b",tmode);
		    }
		    continue;
		}
	    } else {
		if (take < 0) {
		    if (-take > nbikes) {
			take = -nbikes;
			trace(BikeShare.level4, "changing take to %d",take);
					
		    }
		} else {
		    index2++;
		    trace(BikeShare.level4, "incrementing index2 to %d",index2);
		    if (index2 == harray2.length) {
			index1 = harray1.length;
			trace(BikeShare.level4, "setting index1 to %d", index1);
		    }
		    continue;
		}
	    }
	    boolean moving = currentHub != hub;
	    if (moving) fireLeftHub(currentHub);
	    double delay = domain.getDelay(currentHub, hub, 1);
	    trace(BikeShare.level3, "moving from %s to %s, delay = %g",
		  currentHub.getName(), hub.getName(), delay);
	    long ldelay = sim.getTicks(delay);
	    if (ldelay > 0) {
		TaskThread.pause(sim.getTicks(delay));
	    }
	    currentHub = hub;
	    if (moving) fireEnteredHub(currentHub);
	    take = currentHub.getBikeCount() - currentHub.getNominal();
	    if (take > 0) {
		int delta = capacity - nbikes;
		if (take > delta) {
		    take = delta;
		}
	    } else {
		if (-take > nbikes) {
		    take = -nbikes;
		}
	    }
	    fireFixingPreferred(currentHub);
	    int old = nbikes;
	    nbikes += take;
	    fireChangedCount(currentHub, old, nbikes);
	    trace(BikeShare.level3, "at hub %s, bikes before change = %d",
		  currentHub.getName(), currentHub.getBikeCount());
	    currentHub.decrBikeCount(take);
	    trace(BikeShare.level3,
		  "at hub %s after change, worker bikes = %d, hub bikes = %d",
		  currentHub.getName(), nbikes, currentHub.getBikeCount());
	    if (tmode) {
		index1++;
		if (index1 == harray1.length) tmode = !tmode;
	    } else {
		index2++;
		if (index2 == harray2.length) tmode = !tmode;
	    }
	    trace(BikeShare.level4, "index1 = %d, index2 = %d, tmode = %b",
		  index1, index2, tmode);
	}
	trace(BikeShare.level2, "completed %s, nbikes = %d",
	      action, nbikes);
    }


    /**
     * Visit a sequence of hubs repeatedly, adjusting the number of
     * bicycles in the preferred area.
     * @param hubSorter the hub sorter
     * @param period the period in simulation ticks
     * @param loffset the initial wait in simulation ticks before starting the
     *        first loop
     */
    protected void loop(final HubSorter hubSorter, final long period,
			final long loffset)
    {
	// final Hub[] xhubs = hubs.clone();
	Runnable runnable = new Runnable() {
		public void run() {
		    TaskThread.pause(loffset);
		    
		    // Hub current = storageHub;
		    if (currentHub == storageHub) {
			loadBikes(hubSorter);
		    }
		    // fireLeftHub(currentHub);
		    for (;;) {
			long startingTime = sim.currentTicks();
			visitAux("loop",  hubSorter);
			if (currentHub != storageHub) {
			    double delay =
				domain.getDelay(currentHub, storageHub, 1);
			    fireLeftHub(currentHub);
			    TaskThread.pause(sim.getTicks(delay));
			    currentHub = storageHub;
			    fireEnteredHub(storageHub);
			    trace(BikeShare.level3, "at storage hub %s",
				  storageHub.getName());
			} else {
			    trace(BikeShare.level3, "already at storage hub %s",
				  storageHub.getName());
			}
			long interval = sim.currentTicks() - startingTime;
			if (period > interval) {
			    storeBikes();
			    TaskThread.pause(period - interval);
			    loadBikes(hubSorter);
			    fireLeftHub(currentHub);
			}
		    }
		}
	    };
	sim.scheduleTask(runnable, period);
    }


    /**
     * Visit a sequence of hubs, adjusting the number of
     * bicycles in the preferred area.
     * @param hubSorter the hub seror
     */
    protected void visit(final HubSorter hubSorter) {
	// final Hub[] xhubs = hubs.clone();
	Runnable runnable = new Runnable() {
		public void run() {
		    if (currentHub == storageHub) {
			loadBikes(hubSorter);
		    }
		    // fireLeftHub(currentHub);
		    visitAux("visit",  hubSorter);
		    if (currentHub != storageHub) {
			fireLeftHub(currentHub);
			double delay =
			    domain.getDelay(currentHub, storageHub, 1);
			TaskThread.pause(sim.getTicks(delay));
			currentHub = storageHub;
			fireEnteredHub(currentHub);
			trace(BikeShare.level3, "at storage hub %s",
			      storageHub.getName());
		    } else {
			trace(BikeShare.level3, "already at storage hub %s",
			      storageHub.getName());
		    }
		    storeBikes();
		    storageHub.queueWorker(HubWorker.this);
		    workerRunning = false;
		    currentMode = null;
		    currentHubs = null;
		}
	    };
	sim.scheduleTask(runnable);
    }

    private void visitWithPickupAux(String action, HubSorter hubSorter)
    {
	trace(BikeShare.level2, "started %s, nbikes = %d", action, nbikes);
	hubSorter.sort();
	Hub[] harray1 = hubSorter.getOverNominal();
	Hub[] harray2 = hubSorter.getUnderNominal();
	trace(BikeShare.level4, "harray1.length = %d, harray2.length = %d",
	      harray1.length, harray2.length);
	int index1 = 0;
	int index2 = 0;
	boolean tmode = true;
	while (index1 < harray1.length || index2 < harray2.length) {
	    Hub hub1 = ((index1 < harray1.length)? harray1[index1]: null);
	    Hub hub2 = ((index2 < harray2.length)? harray2[index2]: null);
	    Hub hub = (tmode)? hub1: hub2;
	    trace(BikeShare.level4, "choosing between %s and %s",
		  ((hub1 == null)? "null": hub1.getName()),
		  ((hub2 == null)? "null": hub2.getName()));
	    if (hub == null) {
		tmode = !tmode;
		continue;
	    }
	    int take = hub.getBikeCount() - hub.getNominal();
	    if (tmode) take += hub.getOverflow();
	    trace(BikeShare.level4,
		  "chose %s, take = %d, tmode = %b",
		  hub.getName(), take, tmode);
	    if (tmode) {
		if (take > 0) {
		    int delta = capacity - nbikes;
		    trace(BikeShare.level4, "delta = %d", delta);
		    if (delta == 0) {
			if (index2 == harray2.length) {
			    return;
			} else {
			    tmode = !tmode;
			    continue;
			}
		    }
		    if (take > delta) {
			take = delta;
			trace(BikeShare.level4, "changing take to %d", take);

		    }
		} else {
		    index1++;
		    trace(BikeShare.level4, "incrementing index1 to %d",index1);
		    if (index1 == harray1.length) {
			tmode = !tmode;
			trace(BikeShare.level4, "changing tmode to %b",tmode);
		    }
		    continue;
		}
	    } else {
		if (take < 0) {
		    if (-take > nbikes) {
			take = -nbikes;
			trace(BikeShare.level4, "changing take to %d",take);
					
		    }
		} else {
		    index2++;
		    trace(BikeShare.level4, "incrementing index2 to %d",index2);
		    if (index2 == harray2.length) {
			index1 = harray1.length;
			trace(BikeShare.level4, "setting index1 to %d", index1);
		    }
		    continue;
		}
	    }
	    boolean moving = currentHub != hub;
	    if (moving) fireLeftHub(currentHub);
	    double delay = domain.getDelay(currentHub, hub, 1);
	    trace(BikeShare.level3, "moving from %s to %s, delay = %g",
		  currentHub.getName(), hub.getName(), delay);
	    long ldelay = sim.getTicks(delay);
	    if (ldelay > 0) {
		TaskThread.pause(sim.getTicks(delay));
	    }
	    currentHub = hub;
	    if (moving) fireEnteredHub(currentHub);
	    int n = currentHub.getOverflow();
	    int nr = 0;
	    if (n > (capacity - nbikes)) {
		nr = n;
		n = capacity - nbikes;
		nr -= n;
	    }
	    if (n > 0) {
		fireFixingOverflows(currentHub);
		int old = nbikes;
		nbikes += n;
		fireChangedCount(currentHub, old, nbikes);
		trace(BikeShare.level3,
		      "at hub %s, picking up %d bicycles from "
		      + "the overflow area",
		      currentHub.getName(), n);
		TaskThread.pause(hub.pickupOverflow(n));
	    }
	    take = currentHub.getBikeCount() - currentHub.getNominal();
	    if (take > 0) {
		int delta = capacity - nbikes;
		if (take > delta) {
		    take = delta;
		}
	    } else {
		if (-take > nbikes) {
		    take = -nbikes;
		}
	    }
	    fireFixingPreferred(currentHub);
	    int old1 = nbikes;
	    nbikes += take;
	    fireChangedCount(currentHub, old1, nbikes);
	    trace(BikeShare.level3, "at hub %s, bikes before change = %d",
		  currentHub.getName(), currentHub.getBikeCount());
	    currentHub.decrBikeCount(take);
	    trace(BikeShare.level3,
		  "at hub %s after change, worker bikes = %d, hub bikes = %d",
		  currentHub.getName(), nbikes,
		  currentHub.getBikeCount());
	    if (nr > 0) {
		if (nr > (capacity - nbikes)) {
		    nr = capacity - nbikes;
		}
		if (nr > 0) {
		    fireFixingOverflows(currentHub);
		    trace(BikeShare.level3, "at hub %s, picking up %d "
			  + "more bicycles from overflow",
			  currentHub.getName(), nr);
		    int old = nbikes;
		    nbikes += nr;
		    fireChangedCount(currentHub, old, nbikes);
		    TaskThread.pause(hub.pickupOverflow(nr));
		}
	    }
	    if (tmode) {
		index1++;
		if (index1 == harray1.length) tmode = !tmode;
	    } else {
		index2++;
		if (index2 == harray2.length) tmode = !tmode;
	    }
	    trace(BikeShare.level4, "index1 = %d, index2 = %d, tmode = %b",
		  index1, index2, tmode);
	}
	trace(BikeShare.level2, "completed %s, nbikes = %d", action, nbikes);
    }

    /**
     * Visit a sequence of hubs repeatedly, picking up bicycles
     * from the overflow area and adjusting the number of
     * bicycles in the preferred area.
     * @param hubSorter the hub seror
     * @param period the period in simulation ticks
     * @param loffset the initial wait in simulation ticks before starting the
     *        first loop
     */
    protected void loopWithPickup(final HubSorter hubSorter,
				  final long period, final long loffset)
    {
	// final Hub[] xhubs = hubs.clone();
	Runnable runnable = new Runnable() {
		public void run() {
		    TaskThread.pause(loffset);
		    if (currentHub == storageHub) {
			loadBikes(hubSorter);
		    }
		    // fireLeftHub(currentHub);
		    for (;;) {
			long startingTime = sim.currentTicks();
			visitWithPickupAux("loop with pickup", hubSorter);
			if (currentHub != storageHub) {
			    double delay =
				domain.getDelay(currentHub, storageHub, 1);
			    fireLeftHub(currentHub);
			    TaskThread.pause(sim.getTicks(delay));
			    currentHub = storageHub;
			    fireEnteredHub(currentHub);
			    trace(BikeShare.level3, "at storage hub %s",
				  storageHub.getName());
			} else {
			    trace(BikeShare.level3, "already at storage hub %s",
				  storageHub.getName());
			}
			long interval = sim.currentTicks() - startingTime;
			if (period > interval) {
			    storeBikes();
			    TaskThread.pause(period - interval);
			    loadBikes(hubSorter);
			    fireLeftHub(currentHub);
			}
		    }
		}
	    };
	sim.scheduleTask(runnable, period);
    }

    /**
     * Visit a sequence of hubs, picking up bicycles
     * from the overflow area and adjusting the number of
     * bicycles in the preferred area.
     * @param hubSorter the hub seror
     */
    protected void visitWithPickup(final HubSorter hubSorter) {
	// final Hub[] xhubs = hubs.clone();
	Runnable runnable = new Runnable() {
		public void run() {
		    if (currentHub == storageHub) {
			loadBikes(hubSorter);
		    }
		    // fireLeftHub(currentHub);
		    visitWithPickupAux("visit with pickup", hubSorter);
		    if (currentHub != storageHub) {
			double delay =
			    domain.getDelay(currentHub, storageHub, 1);
			fireLeftHub(currentHub);
			TaskThread.pause(sim.getTicks(delay));
			currentHub = storageHub;
			fireEnteredHub(currentHub);
			trace(BikeShare.level3, "at storage hub %s",
			      storageHub.getName());
		    } else {
			trace(BikeShare.level3, "already at storage hub %s",
			      storageHub.getName());
		    }
		    storeBikes();
		    storageHub.queueWorker(HubWorker.this);
		    workerRunning = false;
		    currentMode = null;
		    currentHubs = null;
		}
	    };
	sim.scheduleTask(runnable);
    }

    private void visitToFixOverflowsAux(String action, HubSorter hubSorter)
    {
	trace(BikeShare.level2, "started %s, number of bicycles = %d",
	      action, nbikes);
	hubSorter.sort();
	Hub[] hubs = hubSorter.getHubs();
	for (Hub hub: hubs) {
	    if (hub instanceof StorageHub) {
		throw new RuntimeException
		    (errorMsg("storageHub", hub.getName()));
	    }
	    trace(BikeShare.level4, "checking hub %s", hub.getName());
	    // first check what would happen and skip a
	    // hub if there is nothing to do. We assume
	    // there are communication links that can
	    // provide this information.
	    int n = hub.getOverflow();
	    if (n > capacity - nbikes) n = capacity - nbikes;
	    int freespace =
		hub.getCapacity() - hub.getBikeCount();
	    if (freespace < n) {
		n = freespace;
	    }
	    if(n <= 0) {
		trace(BikeShare.level3, "nothing to do for hub %s, n=%d",
		      hub.getName(), n);
		continue;
	    }
	    boolean moving = currentHub != hub;
	    if (moving) fireLeftHub(currentHub);
	    double delay = domain.getDelay(currentHub, hub, 1);
	    trace(BikeShare.level3, "moving from %s to %s, delay = %g",
		  currentHub.getName(), hub.getName(), delay);
	    if (delay > 0.0) {
		TaskThread.pause(sim.getTicks(delay));
	    }
	    if (moving) fireEnteredHub(hub);
	    // Now recompute, in case something changed,
	    // and actually make the required changes.
	    n = hub.getOverflow();
	    if (n > capacity - nbikes) n = capacity - nbikes;
	    freespace = hub.getCapacity() - hub.getBikeCount();
	    if (freespace < n) {
		n = freespace;
	    }
	    currentHub = hub;
	    if(n <= 0) {
		trace(BikeShare.level3, "at hub %s, no change",
		      currentHub.getName());
		continue;
	    }
	    fireFixingOverflows(currentHub);
	    trace(BikeShare.level3, 
		  "at hub %s, picking up %d bicycles from overflow area",
		  hub.getName(), n);
	    currentHub.incrBikeCount(n);
	    fireChangedCount(currentHub, nbikes, nbikes+n);
	    TaskThread.pause(currentHub.pickupOverflow(n));
	    fireFixingPreferred(currentHub);
	    fireChangedCount(currentHub, nbikes+n, nbikes);
	    trace(BikeShare.level3,
		  "number of worker bikes = %d, at hub %s overflow = %d",
		  nbikes, currentHub.getName(), currentHub.getOverflow());
	}
	trace(BikeShare.level2, "ended %s, number of bicycles = %d",
	      action, nbikes);
    }

    /**
     * Visit a sequence of hubs repeatedly, picking up bicycles
     * from the overflow area and putting as many as possible
     * into the preferred area.
     * @param hubSorter the hub seror
     * @param period the period in simulation ticks
     * @param loffset the initial wait in simulation ticks before starting the
     *        first loop
     */
    protected void loopToFixOverflows( final HubSorter hubSorter,
				       final long period, final long loffset)
    {
	// final Hub[] xhubs = hubs.clone();
	Runnable runnable = new Runnable() {
		public void run() {
		    TaskThread.pause(loffset);
		    if (currentHub == storageHub) {
			loadBikes(hubSorter);
		    }
		    // fireLeftHub(currentHub);
		    for (;;) {
			long startingTime = sim.currentTicks();
			visitToFixOverflowsAux("loop to fix overflows",
					       hubSorter);
			if (currentHub != storageHub) {
			    double delay =
				domain.getDelay(currentHub, storageHub, 1);
			    trace(BikeShare.level3,
				  "moving from %s to storageHub %s, delay=%g",
				  currentHub.getName(), storageHub.getName(),
				  delay);
			    TaskThread.pause(sim.getTicks(delay));
			    currentHub = storageHub;
			    trace(BikeShare.level3, "at storage hub %s",
				  storageHub.getName());
			} else {
			    trace(BikeShare.level3, "already at storage hub %s",
				  storageHub.getName());
			}
			long interval = sim.currentTicks() - startingTime;
			if (period > interval) {
			    storeBikes();
			    TaskThread.pause(period - interval);
			    loadBikes(hubSorter);
			    fireLeftHub(currentHub);
			}
		    }
		}
	    };
	sim.scheduleTask(runnable, period);
    }

    /**
     * Visit a sequence of hubs, picking up bicycles
     * from the overflow area and putting as many as possible
     * into the preferred area.
     * @param hubSorter the hub seror
     */
    protected void visitToFixOverflows(final HubSorter hubSorter) {
	// final Hub[] xhubs = hubs.clone();
	Runnable runnable = new Runnable() {
		public void run() {
		    if (currentHub == storageHub) {
			loadBikes(hubSorter);
		    }
		    // fireLeftHub(currentHub);
		    visitToFixOverflowsAux("visit to fix overflows", hubSorter);
		    if (currentHub != storageHub) {
			double delay =
			    domain.getDelay(currentHub, storageHub, 1);
			trace(BikeShare.level3,
			      "moving from %s to storageHub %s, delay=%g",
			      currentHub.getName(), storageHub.getName(),
			      delay);
			TaskThread.pause(sim.getTicks(delay));
			currentHub = storageHub;
			trace(BikeShare.level3, "at storage hub %s",
			      storageHub.getName());
		    } else {
			trace(BikeShare.level3, "already at storage hub %s",
			      storageHub.getName());
		    }
		    storeBikes();
		    storageHub.queueWorker(HubWorker.this);
		    workerRunning = false;
		    currentMode = null;
		    currentHubs = null;
		}
	    };
	sim.scheduleTask(runnable);
    }

     /**
     * Print the configuration for an instance of HubWorker.
     * The documentation for method
     * {@link org.bzdev.devqsim.SimObject#printState(String,String,boolean,java.io.PrintWriter)}
     * contains a description of how this method is used and how to
     * override it. The method
     * {@link org.bzdev.drama.Actor#printState(String,String,boolean,java.io.PrintWriter)}
     * describes the data that will be printed for the
     * superclass of this class. The data that will be printed
     * when this method is called are the following.
     * <P>
     * For class {@link HubWorker}:
     * <UL>
     *   <LI> the capacity of this worker (the number of bicycles the
     *        worker can move at one time).
     *   <LI> the storage hub to which this worker is assigned.
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
	// out.println(prefix + "system domain: " + domain.getName());
	out.println(prefix + "capacity: " + capacity);
	out.println(prefix + "storage hub: " + storageHub.getName());
    }

     /**
     * Print the state for an instance of HubWorker.
     * The documentation for method
     * {@link org.bzdev.devqsim.SimObject#printConfiguration(String,String,boolean,java.io.PrintWriter)}
     * contains a description of how this method is used and how to
     * override it. The method
     * {@link org.bzdev.drama.Actor#printConfiguration(String,String,boolean,java.io.PrintWriter)}
     * describes the data that will be printed for the
     * superclass of this class. The data that will be printed
     * when this method is called are the following.
     * <P>
     * For class {@link HubWorker}:
     * <UL>
     *   <LI> the number of bicycles this worker is carrying.
     *   <LI> the hub this worker is currently visiting.
     *   <LI> whether or not the worker is running.
     *   <LI> the current value of the worker mode for this worker.
     *   <LI> when the worker is running, a list of the hubs being visited.
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
	out.println(prefix + "number of bicycles: " + nbikes);
	if (currentHub != null) {
	    out.println(prefix + "current hub: " + currentHub.getName());
	} else {
	    out.println(prefix + "current hub: [null]");
	}
	out.println(prefix + "worker running: " + workerRunning);
	out.println(prefix + "current worker mode: " + currentMode);
	if (currentHubs == null) {
	    out.println(prefix + "no hubs being visited");
	} else {
	    out.println(prefix + "hubs being visited:");
	    for (Hub hub: currentHubs) {
		if (hub != storageHub) {
		    out.println(prefix +"    " + hub.getName());
		}
	    }
	}
    }
}

//  LocalWords:  HubWorker balancer HubSorter sim shub currentHub
//  LocalWords:  setStorageHub HubBalancer getHubSorter StorageHub
//  LocalWords:  hubSorter param storageHub nbikes tmode loffset
