package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.lang.Callable;

import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.io.PrintWriter;

import java.util.Comparator;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Basic hub balancer class.
 * This class provides a simple implementation of a hub
 * balancer in which additional workers are scheduled based on
 * the fraction of hubs that are in the hub balancer's user domain
 * and that (1) have bicycles in their overflow areas, (2) have
 * more bicycles in their preferred areas than the value provided
 * by an upper trigger, or (3) have fewer bicycles in their preferred
 * areas than the value provided by a lower trigger.  When this
 * fraction is larger than a threshold, an additional worker is
 * scheduled from each storage hub in the hub balancer's system domain.
 * In addition, a timer is started (the interval is called the quiet
 * period and is configurable). Additional workers will not be
 * scheduled until this timer expires.
 * <P>
 * Subclasses that change the behavior of this class only by
 * overriding
 * {@link HubBalancer#getHubSorter(HubWorker.Mode,StorageHub,Hub[])} are
 * relatively simple extensions of this class. For more complex
 * changes, one will want to override {@link #startAdditionalWorkers()}
 * as well, in which case it might be better to create a subclass
 * of {@link HubBalancer} itself.
 */
public class BasicHubBalancer extends HubBalancer {

    static String errorMsg(String key, Object... args) {
	return BikeShare.errorMsg(key, args);
    }

    DramaSimulation sim;

    Set<Hub> overSet;
    Set<Hub> underSet;
    Set<Hub> overflowSet;

    long lastTime = 0;
    long initialLastTime = 0;

    long quietPeriod = 0;
    boolean quiet = false;

    int initialBikeCount = 0;
    int nominalCount = 0;
    int numberOfHubs = 0;

    double threshold = 0.25;

    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of this object
     * @param intern true if this object should be interned in the
     *        simulation's name table; false otherwise
     */
    public BasicHubBalancer(DramaSimulation sim, String name, boolean intern) {
	super(sim, name, intern);
	overSet = getOverSet();
	underSet = getUnderSet();
	overflowSet = getOverflowSet();
	this.sim = sim;
	sim.scheduleInitCall(new Callable() {
		public void call() {
		    lastTime = BasicHubBalancer.this.sim.currentTicks();
		    initialLastTime = lastTime;
		    for (Actor actor: getSysDomain().actorSet()) {
			if (actor instanceof Hub
			    &&!(actor instanceof StorageHub)) {
			    Hub hub = (Hub) actor;
			    initialBikeCount += hub.getInitialBikeCount();
			    nominalCount += hub.getNominal();
			    numberOfHubs++;
			}
		    }
		}
	    }, 0);
    }

    /**
     * Set the quiet period.
     * If additional workers are scheduled at some simulation time t, the
     * quiet period &delta; indicates that no more workers should
     * be scheduled until the simulation time reaches t + &delta;.
     * @param value the quiet period in seconds
     */
    public void setQuietPeriod(double value) {
	quietPeriod = sim.getTicksCeil(value);
    }

    /**
     * Get the quiet period.
     * If additional workers are scheduled at some simulation time t, the
     * quiet period &delta; indicates that no more workers should
     * be scheduled until the simulation time reaches t + &delta;.
     * @return the quite period in units of simulation ticks
     */
    public long getQuietPeriod() {return quietPeriod;}

    /**
     * Set the fraction of the number of hubs beyond which a worker may
     * be scheduled.
     * If the fraction of the number of hubs in the sets returned by
     * the methods {@link #getUnderSet()}, {@link #getOverSet()}, or
     * {@link #getOverflowSet()}, when compared to the total number of
     * hubs, exceeds the threshold, one or more worker may be
     * scheduled.
     * @param value the value of the threshold, which must be
     *        in the range [0, 1].
     */
    public void setThreshold(double value) {
	threshold = value;
    }

    /**
     * Get the fraction of the number of hubs beyond which a worker may
     * be scheduled.
     * If the fraction of the number of hubs in the sets returned by
     * the methods {@link #getUnderSet()}, {@link #getOverSet()}, or
     * {@link #getOverflowSet()}, when compared to the total number of
     * hubs, exceeds the threshold, one or more worker may be
     * scheduled.
     * @return the current value of the threshold
     * @see #setThreshold(double)
     */
    public double getThreshold() {
	return threshold;
    }

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

    static class HubSorter implements HubWorker.HubSorter {
	Hub[] hubs;
	Hub[] harray1;
	Hub[] harray2;
	HubWorker.Mode mode;

	int initialCount = 0;

	HubSorter(HubWorker.Mode mode, Hub[] hubs) {
	    this.hubs = hubs.clone();
	    this.mode = mode;
	}

	public void sort() {
	    ArrayList<Hub>list1 = new ArrayList<>(hubs.length);
	    ArrayList<Hub>list2 = new ArrayList<>(hubs.length);
	    if (mode == HubWorker.Mode.LOOP_TO_FIX_OVERFLOWS || 
		mode == HubWorker.Mode.VISIT_TO_FIX_OVERFLOWS) {
		harray1 = null;
		harray2 = null;
		return;
	    }
	    for (Hub hub: hubs) {
		if (hub instanceof StorageHub) {
		    throw new RuntimeException
			(errorMsg("storageHub", hub.getName()));
		}
		int take;
		int ptake;
		switch(mode) {
		case LOOP:
		case VISIT:
		    take = hub.getBikeCount() - hub.getNominal();
		    initialCount -= take;
		    if (take > 0) {
			list1.add(hub);
		    } else if (take < 0) {
			list2.add(hub);
		    }
		    break;
		case LOOP_WITH_PICKUP:
		case VISIT_WITH_PICKUP:
		    take = hub.getBikeCount() - hub.getNominal();
		    ptake = take + hub.getOverflow();
		    initialCount -= ptake;
		    if (take > 0) {
			list1.add(hub);
		    } else if (take < 0) {
			if (ptake > 0) {
			    list1.add(hub);
			} else {
			    list2.add(hub);
			}
		    } else if (hub.getOverflow() > 0) {
			list1.add(hub);
		    }
		}
	    }
	    harray1 = new Hub[list1.size()];
	    harray2 = new Hub[list2.size()];
	    list1.toArray(harray1);
	    list2.toArray(harray2);
	    Arrays.sort(harray1, loopComparator1);
	    Arrays.sort(harray2, loopComparator2);
	}

	public Hub[] getHubs() { return hubs;}
	public Hub[] getOverNominal() {
	    return harray1;
	}
	public Hub[] getUnderNominal() {
	    return harray2;
	}
	public int getInitialCountEstimate() {
	    return initialCount;
	}
    }

    @Override
    public HubWorker.HubSorter getHubSorter(HubWorker.Mode mode,
					    StorageHub shub,
					    Hub[] hubs)
    {
	return new BasicHubBalancer.HubSorter(mode, hubs); 
    }

    boolean needStart = false;

    /**
     * {@inheritDoc}
     * <P>
     * This method uses a "quiet period" to prevent multiple workers from
     * responding to the same event. It also uses a threshold that
     * looks at the ratio of three counts to the total number of hubs for
     * this hub balancer. These counts are:
     * <UL>
     *    <LI> the number of hubs for which the number of bicycles stored
     *         the preferred area exceeds the upper limit.
     *    <LI> the number of hubs for which the number of bicycles stored
     *         the preferred area is below the lower limit.
     *    <LI> the number of hubs with bicycles in their overflow areas.
     * </UL>
     * For an additional worker to be scheduled, one of these counts must
     * exceed the threshold multiplied by the total number of hubs for this
     * hub balancer.
     */
    @Override
    protected void startAdditionalWorkers()
    {
	if (quiet) {
	    trace(BikeShare.level2,
		  "start of additional workers delayed - in quiet period");
	    needStart = true;
	    return;
	} else {
	    trace(BikeShare.level2, "additional workers starting as needed");
	}

	boolean haveAdditionalWork = false;
	boolean overSetEmpty = overSet.isEmpty();
	boolean underSetEmpty = underSet.isEmpty();
	boolean overflowSetEmpty = overflowSet.isEmpty();

	int limit = (int)Math.floor(threshold * numberOfHubs);
	boolean overSetTriggered = overSet.size() >= limit;
	boolean underSetTriggered = underSet.size() >= limit;
	boolean overflowSetTriggered = overflowSet.size() >= limit;

	if (overSetTriggered || underSetTriggered || overflowSetTriggered) {
	    int osz = overSet.size();
	    int ofsz = overflowSet.size();
	    int usz = underSet.size();
	    int sz = osz + usz + ofsz;
	    HubWorker.Mode mode = HubWorker.Mode.VISIT_WITH_PICKUP;
	    if (osz == 0) {
		if (ofsz == 0) {
		    // under set is low, so just visit to fix
		    mode = HubWorker.Mode.VISIT;
		} else {
		    if (usz == 0) {
			mode = HubWorker.Mode.VISIT_TO_FIX_OVERFLOWS;
		    }
		}
	    } else {
		if (ofsz == 0) {
		    // there is nothing to pick up.
		    mode = HubWorker.Mode.VISIT;
		}
	    }
	    trace(BikeShare.level2, "will use mode %s", mode);
	    for (StorageHub shub: getSysDomain().getStorageHubs()) {
		int n = 0;
		ArrayList<Hub> hublist = new ArrayList<>(sz);
		for (Hub hub: shub.getHubs(mode)) {
		    if (overSet.contains(hub) || overflowSet.contains(hub)) {
			hublist.add(hub);
			n++;
		    }
		}
		for (Hub hub: shub.getHubs(mode)) {
		    if (underSet.contains(hub)) {
			hublist.add(hub);
			n++;
		    }
		}
		if (n > 0) {
		    long ctime = sim.currentTicks();
		    trace(BikeShare.level2,
			  "startAdditionalWorkers --- n = %d "
			  + "last time = %d (ticks)", n , lastTime);
		    lastTime = ctime;
		    if (shub.workerQueueNotUseable()) {
			trace(BikeShare.level2,
			      "insufficient workers for storage hub");
			continue;
		    }
		    haveAdditionalWork = true;
		    HubWorker worker = shub.pollWorkers();
		    if (worker != null) {
			trace(BikeShare.level2,
			      "starting worker %s for %d hubs, mode %s",
			      worker.getName(), n, mode);
			HubWorker.HubSorter hsorter = 
			    getHubSorter(mode, shub,
					 hublist.toArray(new Hub[n]));
			worker.start(mode, hsorter , 0.0, 0.0);
		    } else {
			trace(BikeShare.level2,
			      "queuing request for a worker, "
			      + "n = %d, mode = %s", n, mode);
			final StorageHub xshub = shub;
			final int xn = n;
			final ArrayList<Hub> xhublist = hublist;
			final HubWorker.Mode xmode = mode;
			shub.addOnQueueCallable(new Callable() {
				public void call() {
				    HubWorker w = xshub.pollWorkers();
				    trace(BikeShare.level2,
					  "worker %s available: n=%d, mode=%s", 
					  w.getName(), xn, xmode);
				    HubWorker.HubSorter hsorter =
					getHubSorter(xmode, xshub,
						     xhublist.toArray
						     (new Hub[xn]));
				    w.start(xmode, hsorter, 0.0, 0.0);
				}
			    });
		    }
		}
	    }
	} else {
	    trace(BikeShare.level4,
		  "startAdditionalWorkers: nothing to do (%d, %d, %d), "
		  + "limit =%d",
		  underSet.size(), overSet.size(), overflowSet.size(),
		  limit);
	}
	if (haveAdditionalWork) {
	    sim.scheduleCall(new Callable() {
		    public void call() {
			quiet = false;
			if (needStart) {
			    needStart = false;
			    startAdditionalWorkers();
			}
		    }
		}, quietPeriod);
	    quiet = true;
	}
	return;
    }

    /**
     * {@inheritDoc}
     * Defined for class BasicHubBalancer:
     * <UL>
     *   <LI> the quiet period in seconds.
     *   <LI> the threshold (the fraction of the number of hubs
     *        beyond which a worker may be scheduled).
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
	out.println(prefix + "quietPeriod: " + quietPeriod);
	out.println(prefix + "threshold: " + threshold);
    }

    /**
     * {@inheritDoc}
     * Defined for class BasicHubBalancer:
     * <UL>
     *   <LI> parameters set as the simulation starts:
     *        <UL>
     *           <LI> initial bicycle count.
     *           <LI> nominal bicycle count.
     *           <LI> the number of hubs.
     *        </UL>
     * </UL>
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printState(String iPrefix, String prefix,
				   boolean printName, PrintWriter out)
    {
	super.printState(iPrefix, prefix, printName, out);
	out.println(prefix + "parameters set at simulation start:");
	out.println(prefix + "    initial bicycle count: " + initialBikeCount);
	out.println(prefix + "    nominal bicycle count: " + nominalCount);
	out.println(prefix + "    number of hubs: " + numberOfHubs);
	if (lastTime != initialLastTime) {
	    out.println(prefix + "last time non-looping workers were started: "
			+ sim.getTime(lastTime) + " (seconds)");
	} else {
	    out.println(prefix + "non-looping workers not yet started");
	}
    }
}

//  LocalWords:  balancer balancer's HubBalancer getHubSorter sim
//  LocalWords:  HubWorker StorageHub startAdditionalWorkers iPrefix
//  LocalWords:  getUnderSet getOverSet getOverflowSet setThreshold
//  LocalWords:  storageHub BasicHubBalancer printName quietPeriod
