package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.math.stats.BasicStats;
import org.bzdev.math.stats.BasicStats.Sample;
import org.bzdev.util.StaticRandom;
import org.bzdev.util.rv.DoubleRandomVariable;
import java.util.Map;
import java.util.HashMap;
import java.io.PrintWriter;

/**
 * Factory for delay tables used for determining the time for
 * unscheduled trips between hubs.
 * This class computes the time it takes to travel from one hub to
 * another.  For any two end points, the travel time may be different
 * each time {@link #getDelay(double,Hub,Hub,int)} is called with the same
 * arguments due to the use of random variables.
 * <P>
 * This class allows one to set up a table to compute delays for any
 * two pairs of hubs (table entries are directional so for each pair,
 * two entries are normally needed).  A default computation based on
 * the hub's coordinates will be used when an entry is missing from
 * the table.
 * <P>
 * A {@link org.bzdev.util.rv.DoubleRandomVariable} determines
 * the average speed of travel, measured while in motion.
 * For each route, one can specify
 * <UL>
 *   <LI> the length of the route
 *   <LI> the number of possible stops along the route
 *   <LI> the maximum wait at a stop (this is an average
 *        over all stops)
 *   <LI> the probability that one will stop at any particular
 *        stopping point
 * </UL>
 * A similar set of values is provided for the default, but in this
 * case one provides a unit distance over which the number of stops
 * applies.  The number of stops used is then scaled by an estimated
 * distance divided by the unit distance.  For the default, the
 * distance is a weighed average of two values: the line-of-sight
 * distance between the two points and the sum of the absolute values
 * of the differences between the end points' X values and Y values.
 */
public class StdDelayTable extends DelayTable {
    DramaSimulation sim;

    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of this object
     * @param intern true if this object should be interned in the
     *        simulation's name table; false otherwise
     */
    public StdDelayTable(DramaSimulation sim, String name, boolean intern) {
	super(sim, name, intern);
	this.sim  = sim;
    }

    DoubleRandomVariable speedRV;

    static class Entry {
	Entry() {}
	Entry(Entry entry, double dist) {
	    stops = (int)(Math.round(entry.stops*(dist/entry.dist)));
	    stopProbability = entry.stopProbability;
	    maxWait = maxWait;
	    this.dist = dist;
	}
	double dist;
	int stops;
        double stopProbability;
	double maxWait;
    }

    Entry defaultEntry = new Entry();

    Map<Integer,Double> estimatedSpeedMap = new HashMap<Integer,Double>();

    private double getSpeed(int n) {
	double speed = speedRV.next();
	for (int j = 1; j < n; j++) {
	    double s = speedRV.next();
	    if (s < speed) speed = s;
	}
	return speed;
    }

    private double estimatedSpeed(int n) {
	Double speed = estimatedSpeedMap.get(n);
	if (speed == null) {
	    BasicStats stats = new BasicStats.Sample();
	    for (int i = 0; i < 10000; i++) {
		stats.add(getSpeed(n));
	    }
	    speed = stats.getMean();
	    estimatedSpeedMap.put(n, speed);
	}
	return speed;
    }

    /**
     * Initialize a local delay table.
     * One will provide a random variable giving a speed (the speed while
     * in motion, not an average speed that includes stops), a unit distance
     * the number of stops in the unit distance, the probability of stopping
     * at one of the stops, and a maximum waiting time for the time spent at
     * a stop (if stopped at that point).  When used, the number of stops
     * will be scaled based on the unit distance and the actual distance.
     * <P>
     * While the random variable speedRV will be used in general for
     * computing travel times, the other arguments are used only in the
     * case where an entry has not been added to the table by calling
     * {@link #addEntry(Hub,Hub,double,int,double,double) addEntry} to
     * provide explicit values for trips between two hubs.  In this case
     * the distance traveled is estimated based on the X and Y coordinates
     * of the two hubs by using a weighted combination of the line-of-sight
     * distance between the two points and the sum of the absolute values
     * of the differences in X and Y coordinates
     * @param speedRV A random variable providing a travel speed in units of
     *        meters per second.
     * @param dist a unit distance
     * @param stops the number of stops per unit distance
     * @param stopProbability the probability of stopping at a stop
     * @param maxWait the maximum waiting time when stopped
     */
    public void init(DoubleRandomVariable speedRV,
		     double dist,
		     int stops,
		     double stopProbability,
		     double maxWait) {
	this.speedRV = speedRV;
	speedRV.tightenMinimum(BikeShare.minStdDelayTableSpeed, true);
	defaultEntry.dist = dist;
	defaultEntry.stops = stops;
	defaultEntry.stopProbability = stopProbability;
	defaultEntry.maxWait = maxWait;
    }

    Map<Hub,Map<Hub,Entry>> map = new HashMap<>();

    /*
     * Add an entry describing a trip between two hubs.
     * One will provide a distance between the hubs the number of
     * stops, the probability of stopping at one of the stops, and a
     * maximum waiting time for the time spent at a stop (if stopped at
     * that point).  The entry applies to trips from src to dest, but
     * not the reverse trip.
     * @param src the starting hub
     * @param dest the ending hub
     * @param distance the distance between the two hubs (src and dest)
     * @param stops the number of stops
     * @param stopProbability the probability of stopping at a stop
     * @param maxWait the maximum waiting time when stopped
     */
    public void addEntry(Hub src, Hub dest,
			 double distance,
			 int stops,
			 double stopProbability,
			 double maxWait)
    {
	Map<Hub,Entry> emap = map.get(src);
	if (emap == null) {
	    emap = new HashMap<Hub,Entry>();
	    map.put(src, emap);
	}
	Entry entry = emap.get(dest);
	if (entry == null) {
	    entry = new Entry();
	    emap.put(dest, entry);
	}
	entry.dist = distance;
	entry.stops = stops;
	entry.stopProbability = stopProbability;
	entry.maxWait = maxWait;
    }
    
    private double distFraction = 0.5;
    private double oneMinusDistFraction = 0.5;

    /**
     * Set the distance fraction.
     * The distance fraction is used to compute distances between hubs
     * when no explicit distance is provided. One computes two quantities:
     * <UL>
     *   <LI> d<sub>1</sub> - the line of sight distance (the square root of
     *           &delta;x<sup>2</sup> + &delta;y<sup>2</sup>).
     *   <LI> d<sub>2</sub> - the sum of the distances in the X and Y directions
     *           (i.e., |&delta;x| + |&delta;y|).
     * </UL>
     * These quantities are combined to give a distance of
     * (1-f)d<sub>1</sub> + fd<sub>2</sub>, where f is the distance
     * fraction.
     * <P>
     * Setting f to 0.0 will result in the line of sight distance being
     * used, whereas setting the value to 1.0 will set the distance
     * to one appropriate for traveling parallel to the X axis and then
     * parallel to the Y access.  Other values will provide estimates
     * between these two cases.
     * The default value is 0.5.
     * @param value the new value for the distance fraction (a value
     *        in the range [0.0, 1.0]).
     */
    public void setDistFraction(double value) {
	distFraction = value;
	oneMinusDistFraction = 1.0 - value;
    }

    /**
     * Get the distance fraction.
     * The distance fraction is used to compute distances between hubs
     * when no explicit distance is provided. One computes two quantities:
     * <UL>
     *   <LI> d<sub>1</sub> - the line of sight distance (the square root of
     *           &delta;x<sup>2</sup> + &delta;y<sup>2</sup>).
     *   <LI> d<sub>2</sub> - the sum of the distances in the X and Y directions
     *           (i.e., |&delta;x| + |&delta;y|).
     * </UL>
     * These quantities are combined to give a distance of
     * (1-f)d<sub>1</sub> + fd<sub>2</sub>, where f is the distance
     * fraction.
     * <P>
     * A value of 0.0 will result in the line of sight distance being
     * used, whereas a value to 1.0 will set the distance
     * to one appropriate for traveling parallel to the X axis and then
     * parallel to the Y access.  Other values will provide estimates
     * between these two cases.
     * The default value is 0.5.
     * @return the distance fraction
     */
    public double getDistFraction() {return distFraction;}

    @Override
    public double latestStartingTime(double time,
				     Hub src, Hub dest)
    {
	return time;
    }

    /**
     * {@inheritDoc}
     * <P>
     * Note: the startingTime argument (the first argument) is ignored
     * by StdDelayTable instances.
     * @param startingTime {@inheritDoc}
     * @param src {@inheritDoc}
     * @param dest {@inheritDoc}
     * @param n {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public double estimateDelay(double startingTime, Hub src, Hub dest,
				int n)
    {
	if (src == null || dest == null) {
	    throw new NullPointerException();
	}
	Map<Hub,Entry> emap = map.get(src);
	Entry entry = null;
	if (emap != null) {
	    entry = emap.get(dest);
	}
	if (entry == null) {
	    double deltax = src.getX() - dest.getX();
	    double deltay = src.getY() - dest.getY();
	    double tmp1 = Math.sqrt(deltax*deltax + deltay*deltay);
	    double tmp2 = Math.abs(deltax) + Math.abs(deltay);
	    double dist = tmp1 * oneMinusDistFraction + tmp2 * distFraction;
	    entry = new Entry(defaultEntry, dist);
	}
	double time = entry.dist/estimatedSpeed(n);
	time += entry.stops * (entry.maxWait/2.0) * entry.stopProbability;
	return time;
    }

    /**
     * {@inheritDoc}
     * <P>
     * Note: the startingTime argument (the first argument) is ignored
     * by StdDelayTable instances.
     * @param startingTime {@inheritDoc}
     * @param src {@inheritDoc}
     * @param dest {@inheritDoc}
     * @param n {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
	public double getDelay(double startingTime, Hub src, Hub dest, int n) {
	if (src == null || dest == null) {
	    throw new NullPointerException();
	}
	Map<Hub,Entry> emap = map.get(src);
	Entry entry = null;
	if (emap != null) {
	    entry = emap.get(dest);
	}
	if (entry == null) {
	    double deltax = src.getX() - dest.getX();
	    double deltay = src.getY() - dest.getY();
	    double tmp1 = Math.sqrt(deltax*deltax + deltay*deltay);
	    double tmp2 = Math.abs(deltax) + Math.abs(deltay);
	    double dist = tmp1 * oneMinusDistFraction + tmp2 * distFraction;
	    entry = new Entry(defaultEntry, dist);
	}
	double speed = getSpeed(n);
	double time = entry.dist/speed;
	for (int i = 0; i < entry.stops; i++) {
	    if (StaticRandom.nextDouble() < entry.stopProbability) {
		time += StaticRandom.nextDouble() * entry.maxWait;
	    }
	}
	return time;
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
	out.println(prefix + "speedRV: " + speedRV.toString());
	out.println(prefix + "default dist: " + defaultEntry.dist);
	out.println(prefix + "default stops: " + defaultEntry.stops);
	out.println(prefix + "default stopProbability: "
		    + defaultEntry.stopProbability);
	out.println(prefix + "default maxWait: "
		    + defaultEntry.maxWait);
	out.println(prefix + "distFraction: " + distFraction);
	if (map.size() > 0) {
	    out.println(prefix + "hub table:");
	    for (Map.Entry<Hub,Map<Hub,Entry>> entry: map.entrySet()) {
		Hub key = entry.getKey();
		for (Map.Entry<Hub,Entry> entry2: entry.getValue().entrySet()) {
		    out.println(prefix + "  src = " + key.getName()
				+ ", dest = "
				+ entry2.getKey().getName() + ":");
		    Entry value = entry2.getValue();
		    out.println(prefix + "    dist: " + value.dist);
		    out.println(prefix + "    stops: " + value.stops);
		    out.println(prefix + "    stopProbability: "
				+ value.stopProbability);
		    out.println(prefix + "    maxWait: " + value.maxWait);
		}
	    }
	}
    }
}

//  LocalWords:  getDelay sim speedRV addEntry stopProbability src fd
//  LocalWords:  maxWait dest startingTime StdDelayTable boolean XY
//  LocalWords:  printConfiguration superclass iPrefix printName
//  LocalWords:  whitespace distFraction
