package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import java.util.*;
import java.io.PrintWriter;

/**
 * Class representing a delay table for scheduled trips between hubs.
 * This class computes the time it takes to travel from one hub to
 * another.  For any two end points, the travel time may be different
 * each time {@link #getDelay(double,Hub,Hub,int)} is called with the same
 * arguments due to the use of random variables.
 * <P>
 * This class allows one to set up a table to compute delays for any
 * two pairs of hubs (table entries are directional so for each pair,
 * two entries are normally needed) and a sequence of starting times.
 */
public class SchedDelayTable extends DelayTable {

    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of this object
     * @param intern true if this object should be interned in the
     *        simulation's name table; false otherwise
     */
    public SchedDelayTable(DramaSimulation sim, String name, boolean intern) {
	super(sim, name, intern);
	this.sim  = sim;
    }

    static class Entry implements Comparable<Entry> {
	double startingTime;
	double endingTime;
	Entry() {}

	Entry(double startingTime, double endingTime) {
	    this.startingTime = startingTime;
	    this.endingTime = endingTime;
	}

	public boolean equals(Object object) {
	    if (object instanceof Entry) {
		Entry other = (Entry) object;
		return startingTime == other.startingTime
		    && endingTime == other.endingTime;
	    } else {
		return false;
	    }
	}

	public int compareTo(Entry e) {
	    if (endingTime < e.endingTime) return -1;
	    if (endingTime > e.endingTime) return 1;
	    if (startingTime < e.startingTime) return -1;
	    if (startingTime > e.startingTime) return 1;
	    return 0;
	}
    }
    Map<Hub,Map<Hub,TreeSet<Entry>>> map = new HashMap<>();

    /**
     * Add an entry describing a trip between two hubs.
     * One will provide a starting and ending time for a
     * scheduled trip (one direction only).
     * @param src the starting hub
     * @param dest the ending hub
     * @param startingTime a starting time for a trip
     * @param endingTime an ending time for a trip
     */
    public void addEntry(Hub src, Hub dest,
			 double startingTime,
			 double endingTime) {
	Map<Hub,TreeSet<Entry>> emap = map.get(src);
	if (emap == null) {
	    emap = new HashMap<Hub,TreeSet<Entry>>();
	    map.put(src, emap);
	}
	TreeSet<Entry> list = emap.get(dest);
	if (list == null) {
	    list = new TreeSet<Entry>();
	    emap.put(dest, list);
	}
	Entry entry = new Entry(startingTime, endingTime);
	if (list.contains(entry)) return;
	list.add(entry);
    }

    /**
     * Add a periodic entry describing a sequence of trips between two hubs.
     * One will provide an initial time and a final time, a period,
     * and the duration for each trip.  The initial and final times refer
     * to the starting times of a trip.
     * scheduled trip (one direction only).
     * <P>
     * The cutoff should generally be slightly larger than the desired
     * time for the last trip in the sequence so that floating point
     * accuracy will not be an issue.
     * @param src the starting hub
     * @param dest the ending hub
     * @param initialTime the time in seconds at which a sequence of trips
     *        is defined.
     * @param cutoffTime an upper bound on the starting time in units of
     *        seconds
     * @param period the period (in seconds) at which starting times repeat
     * @param duration the duration of each trip.
     */
    public void addEntry(Hub src, Hub dest,
			 double initialTime, double cutoffTime,
			 double period, double duration)
    {
	double startingTime = initialTime;
	do {
	    addEntry(src, dest, startingTime, startingTime + duration);
	    startingTime += period;

	} while (startingTime <= cutoffTime);
    }

    /**
     * Get the best entry for a source, destination, and starting time.
     * Given a source and destination hub, the method finds the a
     * description of a scheduled trip with the earliest arrival time
     * that has a departure time that is past the time specified by
     * the third argument. If multiple entries have the same arrival
     * time, the one with the latest departure time is selected.
     * <P>
     * This method is used {@link #getDelay(double,Hub,Hub,int)} and indirectly
     * by {@link #estimateDelay(double,Hub,Hub,int)} in order fetch data for
     * computing the delay.
     * @param src the starting hub
     * @param dest the ending hub
     * @param time the starting time for a trip.
     * @return an entry that will describe the trip
     */
    protected  Entry getEntry(Hub src, Hub dest, double time) {
	Map<Hub,TreeSet<Entry>> emap = map.get(src);
	if (emap == null) return null;
	TreeSet<Entry> list = emap.get(dest);
	if (list == null) return null;
	Entry test = new Entry(time, time);
	test = list.higher(test);
	while (test != null) {
	    if (test.endingTime <= time) continue;
	    if (test.startingTime >= time) {
		Entry next = list.higher(test);
		if (next == null) return test;
		if (next.endingTime == test.endingTime) {
		    test = next;
		    continue;
		} else {
		    return test;
		}
	    } else {
		test = list.higher(test);
	    }
	}
	return null;
    }

    @Override
    public double latestStartingTime(double time,
				     Hub src, Hub dest)
    {
	if (src == null || dest == null) {
	    throw new NullPointerException();
	}
	Entry entry = getEntry(src, dest, time);
	if (entry == null) return Double.NEGATIVE_INFINITY;
	return entry.startingTime;
    }

    @Override
    public double estimateDelay(double startingTime, Hub src, Hub dest, int n) {
	return getDelay(startingTime, src, dest, n);
    }

    @Override
    public double getDelay(double startingTime, Hub src, Hub dest, int n)
    {
	if (src == null || dest == null) {
	    throw new NullPointerException();
	}
	if (src == dest) return 0.0;
	Entry entry = getEntry(src, dest, startingTime);
	if (entry == null) return Double.POSITIVE_INFINITY;
	return entry.endingTime - startingTime;
    }

    @Override
    protected long localDelay(Domain domain,
			      Actor src,
			      Object msg,
			      Actor dest)
    {
	if (src instanceof Hub && dest instanceof Hub) {
	    Hub srcHub = (Hub) src;
	    Hub destHub = (Hub) dest;
	    return sim.getTicks(getDelay(sim.currentTime(), srcHub, destHub,
					 1));
	} else {
	    return 0L;
	}
    }


     /**
     * Print the configuration for an instance of StdDelayTable.
     * The documentation for method
     * {@link org.bzdev.devqsim.SimObject#printConfiguration(String,String,boolean,java.io.PrintWriter)}
     * contains a description of how this method is used and how to
     * override it. The method
     * {@link org.bzdev.drama.MsgForwardingInfo#printConfiguration(String,String,boolean,java.io.PrintWriter)}
     * describes the data that will be printed for the
     * superclass of this class. The data that will be printed
     * when this method is called are the following.
     * <P>
     * For class {@link StdDelayTable}:
     * <UL>
     *   <LI> the random variable giving the average speed (m/s).
     *   <LI> the default distance for the number of stops.
     *   <LI> the number of stops over the default distance.
     *   <LI> the default probability of stopping at a stop.
     *   <LI> the default maximum wait time at a stop.
     *   <LI> the distance fraction (0.0 means line of sight and 1.0
     *        means the value following a rectangular grid when computing
     *        distances from XY coordinates)
     *   <LI> the hub table. For each pair of hubs, this table shows the
     *        following:
     *        <UL>
     *          <LI> the distance in meters between two hubs.
     *          <LI> the number of stops.
     *          <LI> the probability of stopping.
     *          <LI> the maximum wait while stopped.
     *        </UL>
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

	if (map.size() > 0) {
	    out.println(prefix + "hub table:");
	    for (Map.Entry<Hub,Map<Hub,TreeSet<Entry>>> entry: map.entrySet()) {
		Hub key = entry.getKey();
		for (Map.Entry<Hub,TreeSet<Entry>> entry2:
			 entry.getValue().entrySet()) {
		    out.println(prefix + "  src = " + key.getName()
				+ ", dest = "
				+ entry2.getKey().getName() + ":");
		    for (Entry entry3: entry2.getValue()) {
			out.format(prefix
				   + "    startingTime %g, endingTime %g\n",
				   entry3.startingTime, entry3.endingTime);
		    }
		}
	    }
	} else {
	    out.println(prefix + "hub table: <empty>");
	}
    }
}


//  LocalWords:  getDelay sim src dest startingTime endingTime XY
//  LocalWords:  initialTime cutoffTime estimateDelay StdDelayTable
//  LocalWords:  printConfiguration boolean superclass iPrefix
//  LocalWords:  printName whitespace
