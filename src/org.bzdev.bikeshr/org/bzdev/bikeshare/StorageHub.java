
package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.lang.Callable;
import org.bzdev.math.rv.DoubleRandomVariable;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.Collection;
import java.io.PrintWriter;

/**
 * Class representing a storage hub.
 * A storage hub has a set of workers associated with it, a worker queue
 * for idle hub workers, and a table that associates a set of hubs with
 * a worker mode.  The table's key is an enum constant whose type is
 * {@link HubWorker.Mode}. A storage hub is initialized by providing its
 * (X,Y) coordinates and its system domain.  One will also set the
 * the number of workers that loop between sets of hubs repeatedly.
 * The modes for these loops are
 * {@link HubWorker.Mode#LOOP},
 * {@link HubWorker.Mode#LOOP_WITH_PICKUP}, and
 * {@link HubWorker.Mode#LOOP_TO_FIX_OVERFLOWS}.
 * One will also provide the storage hub with the hubs that can be
 * visited by hub workers for each mode (all modes, not just the ones
 * associated with loops). There should be at least as many workers
 * as are allocated to the three modes listed above. Additional workers
 * will be used on demand (e.g., when a condition requiring an immediate
 * adjustment is detected). While a storage hub specifies allocations,
 * workers are actually started by hub balancers.
 * @see HubWorker
 * @see HubWorker.Mode
 * @see HubBalancer
 */
public class StorageHub extends Hub {

    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of this object
     * @param intern true if this object should be interned in the
     *        simulation's name table; false otherwise
     */
    public StorageHub(DramaSimulation sim, String name, boolean intern) {
	super(sim, name, intern);
    }

    /**
     * Initialize this object.  Each instance of this object has an
     * X and Y coordinate and a system domain, but no user domain.
     * <P>
     * As with the Hub class, the X and Y coordinates are used to
     * determine the delays for traveling between two hubs. As a storage
     * domain, however, the only objects traveling to or from a
     * storage domain are hub workers, and the travel time for these
     * is determined by a system domain.
     * @param count the initial number of bicycles stored at this hub; -1
     *        to use the nominal value
     * @param lowerTrigger the lower-trigger value; -1 for the default (0)
     * @param nominal the nominal value; -1 for the default (0)
     * @param upperTrigger the upper-trigger value; -1 for the default
     *        (Integer.MAX_VALUE)
     * @param x the X coordinate in meters
     * @param y the Y coordinate in meters
     * @param domain the system domain for this storage hub
     */
    public void init(int upperTrigger, int nominal, int lowerTrigger,
		     int count, double x, double y,
		     SysDomain domain)
    {
	if (lowerTrigger == -1) lowerTrigger = 0;
	if (nominal == -1) nominal = 0;
	if (upperTrigger == -1) upperTrigger = Integer.MAX_VALUE;
	if (count == -1) count = nominal;
	// because we override init with these arguments so the init
	// method with this signature does nothing.
	super.init(Integer.MAX_VALUE, lowerTrigger, nominal, upperTrigger,
		   null, count, 0, x, y, null, domain);
    }

    /**
     * Superclass initializer.
     * This is overridden to disable the method. It should not
     * be called except in factories, as it will do nothing.
     * Use {@link #init(int,int,int,int,double,double,SysDomain)} instead.
     */
    @Override
    public void init(int capacity,
		     int lowerTrigger, int nominal, int upperTrigger,
		     DoubleRandomVariable pickupTime,
		     int count, int overCount,
		     double x, double y,
		     UsrDomain usrDomain, SysDomain sysDomain)
    {
	return;
    }

    /**
     * Decrement the number of bicycles at a storage hub.
     * The value returned may differ from the argument as the number
     * of bicycles at the current location cannot be negative.
     * @param decr the decrement in the number of bicycles
     * @return the number of bicycles actually removed from the
     *         preferred location
     */
    @Override
    public int decrBikeCount(int decr) {
	int oldBikeCount = bikeCount;
	if (decr > bikeCount) {
	    bikeCount = 0;
	    decr = bikeCount;
	} else {
	    int oldCount = bikeCount;
	    bikeCount -= decr;
	    // The following is there just in case, as in practice
	    // we should never be close to the limit.
	    if (bikeCount < 0) {
		bikeCount = Integer.MAX_VALUE;
		decr = oldCount - bikeCount;
	    }
	}
	if (decr != 0) {
	    fireHubDataListeners(bikeCount, (oldBikeCount != bikeCount),
				 overflow, false);
	}
	return decr;
    }

    private int nworkersNP = 0;
    private int nworkersWP = 0;
    private int nworkersFO = 0;

    private double intervalNP = 0.0;
    private double intervalWP = 0.0;
    private double intervalFO = 0.0;

    int totalPreallocatedWorkers = 0;

    /**
     * Set the initial worker allocations.
     * The initial allocations are desired values.  Enough workers must
     * be available for these desired values to be met or the actual
     * number allocated will be less.
     * @param nworkersNP the number of workers that will loop between hubs,
     *        ignoring the overflow areas but handling the preferred areas,
     *        corresponding to the worker mode LOOP_NO_PICKUP
     * @param nworkersWP the number of workers that will loop between hubs,
     *        handling the overflow areas as well as the preferred areas,
     *        corresponding to the worker mode LOOP_WITH_PICKUP
     * @param nworkersFO the number of workers that will pickup bicycles
     *        from the overflow areas and deposit these bicycles in the
     *        preferred areas, but that will not remove bicycles from
     *        the preferred area, corresponding to the worker mode
     *        LOOP_TO_FIX_OVERFLOWS
     * @param intervalNP the desired time interval in units of seconds
     *        from the start of one loop to the start of the next
     *        for the nworkersNP case (worker mode LOOP_NO_PICKUP)
     * @param intervalWP the desired time interval in units of
     *        seconds from the start of one loop to the start of the next
     *        for the nworkersWP case (worker mode LOOP_WITH_PICKUP)
     * @param intervalFO the desired time interval in units of
     *        seconds from the start of one loop to the start of the next
     *        for the nworkersFO case (worker mode LOOP_TO_FIX_OVERFLOWS)
     */
    public void setInitialNumberOfWorkers(int nworkersNP, int nworkersWP,
					  int nworkersFO,
					  double intervalNP,
					  double intervalWP,
					  double intervalFO)
    {
	this.nworkersNP = nworkersNP;
	this.nworkersWP = nworkersWP;
	this.nworkersFO = nworkersFO;
	this.intervalNP = intervalNP;
	this.intervalWP = intervalWP;
	this.intervalFO = intervalFO;
	totalPreallocatedWorkers = nworkersNP + nworkersWP + nworkersFO;
    }

    /**
     * Get the initial number of workers that were requested.
     * The worker modes that produce non-zero values are LOOP,
     * LOOP_WITH_PICKUP, and LOOP_TO_FIX_OVERFLOWS, and correspond
     * to arguments of the method
     * {@link #setInitialNumberOfWorkers(int,int,int,double,double,double)}.
     * @param mode the worker mode for the allocation
     * @return the initial number of workers that were requested for
     *         the given mode
     */
    public int getInitialNumberOfWorkers(HubWorker.Mode mode) {
	switch(mode) {
	case LOOP:
	    return nworkersNP;
	case LOOP_WITH_PICKUP:
	    return nworkersWP;
	case LOOP_TO_FIX_OVERFLOWS:
	    return nworkersFO;
	default:
	    return 0;
	}
    }

    /**
     * Get the desired time interval interval from the start of one loop
     * to the start of the next for hub workers with various modes.
     * @param mode the mode
     * @return the interval in seconds
     */
    public double getInterval(HubWorker.Mode mode) {
	switch(mode) {
	case LOOP:
	    return intervalNP;
	case LOOP_WITH_PICKUP:
	    return intervalWP;
	case LOOP_TO_FIX_OVERFLOWS:
	    return intervalFO;
	default:
	    return 0;
	}
    }

    private Map<HubWorker.Mode,LinkedList<Hub>>
	hublistMap = new HashMap<>();
       
    /**
     * Add a hub to the hub table for all modes.
     * If the hub is a storage hub, the hub will not be added
     * @param hub the hub to add
     */
    public void addHub(Hub hub) {
	for (HubWorker.Mode mode: HubWorker.Mode.class.getEnumConstants()) {
	    addHub(mode, hub);
	}
    }

    /**
     * Remove a hub from the hub table for all modes.
     * @param hub the hub to remove
     */
    public void removeHub(Hub hub) {
	for (HubWorker.Mode mode: HubWorker.Mode.class.getEnumConstants()) {
	    removeHub(mode, hub);
	}
    }

    /**
     * Add a hub to the hub table.
     * The hub table assigns an ordered set of hubs to a
     * worker mode. The set is ordered by the order of insertion
     * and indicates the order in which hubs will be visited for
     * each worker mode.
     * <P>
     * Storage hubs cannot be added to the hub table.
     * @param mode the worker mode; null for all modes
     * @param hub the hub to add
     * @return true if the hub was added sucessfully and was not
     *         previously in the table; false otherwise.
     */
    public boolean addHub(HubWorker.Mode mode, Hub hub) {
	if (hub instanceof StorageHub) return false;
	if (mode == null) addHub(hub);
	LinkedList<Hub> hubs = hublistMap.get(mode);
	if (hubs == null) {
	    hubs = new LinkedList<Hub>();
	    hublistMap.put(mode, hubs);
	}
	return hubs.add(hub);
    }
    
    /**
     * Remove a hub from the hub table.
     * The hub table assigns an ordered set of hubs to a
     * worker mode. The set is ordered by the order of insertion
     * and indicates the order in which hubs will be visited for
     * each worker mode.
     * @param mode the worker mode
     * @param hub the hub to add
     * @return true if the hub was removed; false if it could not
     *         be removed or was already removed
     */
    public boolean removeHub(HubWorker.Mode mode, Hub hub) {
	if (hub instanceof StorageHub) return false;
	LinkedList<Hub> hubs = hublistMap.get(mode);
	if (hubs == null) {
	    return false;
	}
	boolean result = hubs.removeLastOccurrence(hub);
	if (hubs.isEmpty()) {
	    hublistMap.remove(mode);
	}
	return result;
    }

    static private LinkedList<Hub> emptyList = new LinkedList<Hub>() {
	public boolean add(Hub e) {return false;}
	public void add(int index, Hub e) {return;}
	public boolean addAll(Collection<? extends Hub> collection)
	{
	    return false;
	}
	public boolean addAll(int index, Collection<? extends Hub> collection)
	{
	    return false;
	}
	public void addFirst(Hub e) {return;}
	public void addLast(Hub e) {return;}
	public boolean offer(Hub e) {return false;}
	public boolean offerFirst(Hub e) {return false;}
	public boolean offerLast(Hub e) {return false;}
	public void push(Hub e) {return;}
    };

    /**
     * Get a set of the hubs for a specified worker mode.
     * These are the hubs that a worker will "visit" for the specified
     * mode. 
     * @param mode the worker mode
     * @return a list of the corresponding hubs, ordered by the
     *         sequence in which they were inserted into the hub table
     */
    public List<Hub> getHubs(HubWorker.Mode mode) {
	LinkedList<Hub> hubs = hublistMap.get(mode);
	if (hubs == null) hubs = emptyList;
	return Collections.unmodifiableList(hubs);
    }

    /**
     * Get an array of the hubs for a specified worker mode.
     * These are the hubs that a worker will "visit" for the specified
     * mode. 
     * @param mode the worker mode
     * @return an array of the corresponding hubs, ordered by the
     *         sequence in which they were inserted into the hub table
     */
    public Hub[] getHubsAsArray(HubWorker.Mode mode) {
	LinkedList<Hub> hubs = hublistMap.get(mode);
	if (hubs == null) return new Hub[0];
	Hub[] array = new Hub[hubs.size()];
	return hubs.toArray(array);
    }

    Set<HubWorker> workers = new LinkedHashSet<>();
    LinkedList<HubWorker> workerQueue = new LinkedList<>();

    boolean moving = false;

    /**
     * Add a worker to this storage hub.
     * This method also inserts the worker into this storage hub's
     * worker queue.
     * <P>
     * Note: when a worker is initialized, it will automatically be
     * added to its storage hub. This method would only be used
     * explicitly in the case where a worker is moved from one
     * storage hub to another.  In that case, the worker must be
     * queued - not running and not in the process of being moved
     * from one hub to another
     * @param worker the worker to add
     * @exception IllegalStateException addWorker was called before
     *         a previously scheduled addWorker call could complete
     *         or if an addWorker call was made while a worker was
     *         running
     * @see HubWorker#init(int,StorageHub,SysDomain,Hub)
     * @see HubWorker#isRunning()
     * @see HubWorker#isMoving()
     * @see HubWorker#getMoveCompletionTime()
     */
    public void addWorker(final HubWorker worker) 
    {
	if (worker == null) throw new NullPointerException();
	if (worker.isMoving() || worker.isRunning()) {
	    throw new IllegalStateException();
	}
        StorageHub old = worker.getStorageHub();
	if (old != null) old.removeWorker(worker);
	Hub hub = worker.getCurrentHub();
	if (hub == null || hub == this  || !(hub instanceof StorageHub)) {
	    workers.add(worker);
	    if (!worker.isRunning()) {
		workerQueue.add(worker);
	    }
	    worker.setStorageHub(this);
	} else {
	    // need to delay this operation to allow for the time it
	    // takes to move from one storage hub to another.
	    double moveInterval = getSysDomain().getDelay(hub, this, 1);
	    worker.setMoving(true);
	    worker.setMoveCompletionTime(sim.currentTime() + moveInterval);
	    sim.scheduleCall(new Callable() {
		    public void call() {
			workers.add(worker);
			workerQueue.add(worker);
			worker.setStorageHub(StorageHub.this);
			worker.setMoving(false);
		    }
		}, sim.getTicks(moveInterval));
	}
    }

    /**
     * Remove a worker from this storage hub.
     * This method also removes the worker from this storage hub's worker
     * queue.
     * @param worker the worker to remove
     */
    public void removeWorker(HubWorker worker) {
	workers.remove(worker);
	while(workerQueue.remove(worker));
	worker.setStorageHub(null);
    }

    /**
     * Determine if the worker queue is not usable.
     * A worker queue is not use-able if all its entries are in
     * permanent use. I.e., they were started with a worker mode
     * that causes the worker to loop indefinitely.
     * @return true if the queue is not use-able; false otherwise
     */
    public boolean workerQueueNotUseable() {
	return (workers.size() - totalPreallocatedWorkers <= 0);
    }

    LinkedList<Callable> onQueueCallableList = new LinkedList<>();

    /**
     * Add a callable to a list whose first element will be
     * removed and called when a new worker is queued.
     * <P>
     * A typical use of this is to delay some action that requires a
     * worker. In this case, the Callable's call method will call
     * {@link #pollWorkers()} to get a worker and then perform some
     * action with that worker.
     * @param c the Callable to add.
     */
    public void addOnQueueCallable(Callable c) {
	trace(BikeShare.level2, "action queued until worker available"); 
	onQueueCallableList.add(c);
    }

    /**
     * Take a worker off of the worker queue.
     * @return a worker; null if no worker is available
     */
    public HubWorker pollWorkers() {
	HubWorker worker = workerQueue.poll();
	if (worker != null) {
	    trace(BikeShare.level2, "worker %s unqueue",
		  worker.getName());
	    worker.fireDequeued(this);
	} else {
	    trace(BikeShare.level2, "no workers available");
	}
	return worker;
    }

    /**
     * Put a worker onto the worker queue.
     * @param worker the worker to insert into the worker queue
     */
    public void queueWorker(HubWorker worker) {
	if (workers.contains(worker)) {
	    workerQueue.add(worker);
	    trace(BikeShare.level2, "worker %s queued", worker.getName());
	    worker.fireQueued(this);
	    if (!onQueueCallableList.isEmpty()) {
		onQueueCallableList.poll().call();
	    }
	}
    }

    /**
     * {@inheritDoc}
     * Defined for class StorageHub:
     * <UL>
     *   <LI> the permanent worker allocations. These include:
     *        <UL>
     *           <LI> the number of workers that should loop, only
     *                visiting a hub's preferred area, and the period
     *                in seconds at which successive loops are run.
     *           <LI> the number of workers that should loop, picking
     *                up bicycles from the overflow area and visiting
     *                the preferred area, , and the period
     *                in seconds at which successive loops are run.
     *           <LI> the number of workers that should loop, picking
     *                up bicycles in the overflow area and moving them
     *                to the same hub's preferred area, , and the period
     *                in seconds at which successive loops are run.
     *        </UL>
     *   <LI> The number of assigned workers plus a list of those workers,
     *        listed by name.
     *   <LI> The hub table. Each "row" in this table consists of a
     *        the worker mode and a list of the hubs that a worker with
     *        that mode will visit, with the list in the order in which
              hubs are visited.
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
	out.println(prefix + "permanent worker allocations:");
	out.println(prefix + "    number of workers without overflow pickup: "
		    + nworkersNP  + ", desired period per loop = "
		    + intervalNP + " seconds");
	out.println(prefix + "    number of workers with overflow pickup: "
		    + nworkersWP
		    + ", desired period per loop = " + intervalWP
		    + " seconds");
	out.println(prefix
		    + "    number of workers to just fix overflows: "
		    + nworkersFO + ", desired period per loop = "
		    + intervalFO + " seconds");
	out.println(prefix + "assigned workers (total = "
		    + workers.size() + "):");
	for (HubWorker worker: workers) {
	    out.println(prefix + "    " + worker.getName());
	}
	out.println(prefix + "hub table:");
	for (Map.Entry<HubWorker.Mode,LinkedList<Hub>> entry: 
		 hublistMap.entrySet()) {
	    HubWorker.Mode mode = entry.getKey();
	    LinkedList<Hub> hublist = entry.getValue();
	    if (hublist.size() > 0) {
		out.println(prefix + "    mode: " + mode + "; hubs:");
		for (Hub hub: hublist) {
		    out.println(prefix + "        " + hub.getName());
		}
	    }
	}
    }

    /**
     * {@inheritDoc}
     * Defined for class StorageHub:
     * <UL>
     *   <LI> the worker-queue and its length, with workers in the queue
     *        listed by name.
     * </UL>
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printState(String iPrefix, String prefix,
			   boolean printName, PrintWriter out) {
	super.printState(iPrefix, prefix, printName, out);
	out.println(prefix + "worker queue (length = " + workerQueue.size()
		    + "):");
	for (HubWorker worker: workerQueue) {
	    out.println(prefix + "    " + worker.getName());
	}
    }
}

//  LocalWords:  enum HubWorker balancers HubBalancer sim init decr
//  LocalWords:  lowerTrigger upperTrigger Superclass initializer
//  LocalWords:  SysDomain nworkersNP nworkersWP nworkersFO unqueue
//  LocalWords:  intervalNP intervalWP intervalFO StorageHub iPrefix
//  LocalWords:  setInitialNumberOfWorkers Callable's pollWorkers
//  LocalWords:  printName IllegalStateException addWorker isRunning
//  LocalWords:  isMoving getMoveCompletionTime
