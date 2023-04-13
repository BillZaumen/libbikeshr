package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.lang.*;
import org.bzdev.lang.annotations.*;
import org.bzdev.math.rv.DoubleRandomVariable;
import org.bzdev.math.StaticRandom;
import org.bzdev.math.RealValuedFunctionTwo;
import org.bzdev.devqsim.SimFunctionTwo;

import java.util.Vector;
import java.io.PrintWriter;

/**
 * Class modeling a hub.
 * Hubs contain a bicycle-storage area, called the preferred area,
 * with a fixed capacity, and cover a geographic area. There is also
 * a second area called the overflow area. When the preferred
 * area is filled to capacity, new arrivals go to the overflow area.
 * New arrivals may, however, go the overflow area at any time: it
 * represents the immediate vicinity of the preferred area an thus
 * users may go directly to the overflow area as it may be slightly
 * closer to the actual destination.
 * <P>
 * In addition, workers (instances of {@link HubWorker}), may move bicycles
 * from one hub to another.  There are several values that control the
 * behavior of workers:
 * <UL>
 *   <LI> the capacity of the hub's preferred area.
 *   <LI> the lower trigger for the hub.
 *   <LI> the nominal value for the hub.
 *   <LI> the upper trigger for the hub.
 *   <LI> the number of bicycles stored in the preferred area.
 *   <LI> the number of bicycles stored in the overflow area.
 *   <LI> a random variable giving the time it takes for a worker to
 *        pick up one bicycle from the overflow area.
 * </UL>
 * <P>
 * Finally, each hub has an (X,Y) coordinate and is a member of a
 * user domain and a system domain.
 * Hubs are configured by the method
 * {@link #init(int,int,int,int,DoubleRandomVariable,int,int,double,double,UsrDomain,SysDomain) init}.  There are two listeners that may be added to
 * a hub:
 * <UL>
 *   <LI> HubListener. This is handled automatically for listeners
 *        used to notify instances of HubCondition of a change.
 *   <LI> HubDataListener. This is intended to be used for instrumenting
 *        a simulation.
 * </UL>
 * The methods that modify the state of a hub due to users or workers are:
 * <UL>
 *   <LI>{@link #pickupOverflow(int)}.
 *   <LI>{@link #decrBikeCount(int)}.
 *   <LI>{@link #incrBikeCount(int)}.
 *   <LI>{@link #incrOverflow(int)}.
 * </UL>
 * The method {@link #sendUsers(Hub,int,boolean,Callable)} or
 * {@link #sendUsers(Hub,int,boolean,RealValuedFunctionTwo,Callable)}, however,
 * can be used as a higher-level interface for user operations.
 */
@DMethodContext(helper="org.bzdev.drama.DoReceive",
		localHelper="HubDoReceive")
public class Hub extends Actor {

    static String errorMsg(String key, Object... args) {
	return BikeShare.errorMsg(key, args);
    }

    DramaSimulation sim;

    UsrDomain usrDomain;
    SysDomain sysDomain;

    /**
     * Get the user domain for this hub.
     * @return this hub's user domain
     */
    public UsrDomain getUsrDomain() {
	return usrDomain;
    }

    /**
     * Get the system domain for this hub.
     * @return this hub's system domain
     */
    public SysDomain getSysDomain() {
	return sysDomain;
    }

    static {
	HubDoReceive.register();
    }

    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of this object
     * @param intern true if this object should be interned in the
     *        simulation's name table; false otherwise
     */
    public Hub(DramaSimulation sim, String name, boolean intern) {
	super(sim, name, intern);
	this.sim = sim;
    }

    private int initialBikeCount;

    /**
     * The bicycle count.
     */
    protected int bikeCount;
    /**
     * The overflow count.
     */
    protected int overflow;
    private int capacity;
    private int lowerTrigger;
    private int nominal;
    private int upperTrigger;
    DoubleRandomVariable pickupTime;

    private double x;
    private double y;

    /**
     * Get this object's X coordinate.
     * @return the X coordinate in units of meters
     */
    public double getX() {return x;}

    /**
     * Get this object's Y coordinate.
     * @return the Y coordinate in units of meters
     */
    public double getY() {return y;}
    
    Vector<HubListener> hubListenerList = new Vector<>();

    /**
     * Add a hub listener to the current object.
     * @param listener the listener to add
     */
    public void addHubListener(HubListener listener) {
	hubListenerList.add(listener);
	// Tell the listener the initial value.
	listener.hubChanged(this, needBikes(), excessBikes(), getOverflow());
    }

    /**
     * Remove a hub listener from the current object.
     * @param listener the listener to remove
     */
    public void removeHubListener(HubListener listener) {
	hubListenerList.remove(listener);
    }

    /**
     * Notify each hub listener of a change.
     * @param need the number of stored bicycles needed to reach
     *        this hub's lower trigger
     * @param excess the number of stored bicycles that have to be removed
     *        to reach this hub's upper trigger
     * @param overflow the number of bicycles stored elsewhere in the
     *        this hub
     */
    protected void fireHubListeners(int need, int excess, int overflow) {
	for (HubListener listener: hubListenerList) {
	    listener.hubChanged(this, need, excess, overflow);
	}
    }

    Vector<HubDataListener> hubDataListenerList = new Vector<>();

    /**
     * Add a hub data listener to the current object.
     * @param listener the listener to add
     */
    public void addHubDataListener(HubDataListener listener) {
	hubDataListenerList.add(listener);
	// Tell the listener the initial value.
	listener.hubChanged(this, bikeCount, true, overflow, true,
			    sim.currentTime(), sim.currentTicks());
    }

    /**
     * Remove a hub data listener from the current object.
     * @param listener the listener to remove
     */
    public void removeHubDataListener(HubDataListener listener) {
	hubDataListenerList.remove(listener);
    }

    /**
     * Notify each hub listener of a change.
     * @param bikeCount the new bike count
     * @param newBikeCount true if the bicycle count has changed
     *        during the event that invoked this listener; false otherwise
     * @param overflowCount the new overflow count
     * @param newOverflowCount true if the overflow count has
     *        changed during the event that invoked this listener; false
     *        otherwise
     */
    protected void fireHubDataListeners(int bikeCount,
					boolean newBikeCount,
					int overflowCount,
					boolean newOverflowCount)
    {
	double time = sim.currentTime();
	long ticks = sim.currentTicks();
	for (HubDataListener listener: hubDataListenerList) {
	    listener.hubChanged(this, bikeCount, newBikeCount,
				overflowCount, newOverflowCount,
				time, ticks);
	}
    }

    /**
     * Initialize this object.  Each instance of this object has a
     * preferred location at a specified coordinate with a fixed
     * capacity.  The hub allows bicycles to be stored at the
     * preferred location and at other locations in the hub, called
     * the "overflow" area.  Users may choose to use the overflow area
     * , but will be assigned to the overflow area when the preferred
     * location is at capacity.  
     * <P> 
     * At the preferred location, there is a nominal value for the
     * number of bicycles and an upper and lower trigger.  The nominal
     * value represents the ideal number of bicycles at this hub's
     * preferred location.  For values for the number of bicycles at
     * the preferred location that are less extreme than the upper and
     * lower triggers or equal to those values, nothing special is
     * done. Otherwise additional resources may be used to help
     * restore the number of bicycles to a value between the two
     * triggers, and as close to the nominal value as possible.
     * <P>
     * Each hub has an X and Y coordinate for its preferred location.
     * These coordinates are used to estimate travel time between
     * two locations when an explicit value is not available, but are
     * available for other purposes (e.g., graphics). Each hub also
     * will be a member of two domains: a user domain and a system
     * domain.  The user domain contains only hubs provided for users,
     * and can calculate the time needed to move from one hub to
     * another when traveling by bicycle. By contrast, the system
     * domain contains both hubs provided for users and hubs used to
     * store vehicles used by workers to balance the distribution of
     * bicycles between hubs. The system domain provides the time
     * needed for workers to move between hubs using these vehicles.
     * @param capacity a non-negative integer giving the capacity
     *        for this hub (the number of bicycles that can be stored
     *        at the hub's preferred location)
     * @param lowerTrigger the lower trigger for the number of bicycles
     *        at the preferred location
     * @param nominal the preferred number of bicycles at this
     *        hub's preferred location
     * @param upperTrigger the upper trigger for the number of bicycles
     *        at the preferred location
     * @param pickupTime a random variable providing the time it takes
     *        to pick up a bicycle that is at this hub but not at the
     *        hub's preferred location
     * @param count the initial number of bicycles stored at the
     *        preferred location of this hub
     * @param overCount the initial number of bicycles that are stored at
     *        this hub but not at this hub's preferred location
     * @param x the X value in meters of the preferred location for
     *        this hub
     * @param y the Y value in meters of the preferred location for
     *        this hub
     * @param usrDomain the user domain for this hub
     * @param sysDomain the system domain for his hub
     */
    public void init(int capacity,
		     int lowerTrigger, int nominal, int upperTrigger,
		     DoubleRandomVariable pickupTime,
		     int count, int overCount,
		     double x, double y,
		     UsrDomain usrDomain, SysDomain sysDomain)
    {
	this.bikeCount = count;
	this.capacity = capacity;
	this.lowerTrigger = lowerTrigger;
	this.nominal = nominal;
	this.upperTrigger = upperTrigger;
	this.pickupTime = pickupTime;
	overflow = overCount;
	initialBikeCount = count;
	this.x = x;
	this.y = y;
	if (usrDomain != null) {
	    joinDomain(usrDomain, false);
	}
	if (sysDomain != null) {
	    joinDomain(sysDomain, false);
	}
	this.usrDomain = usrDomain;
	this.sysDomain = sysDomain;
    }

    /**
     * Get the number of bicycles needed to raise the
     * number of bicycles stored at a hub's preferred location
     * to the lower-trigger value.
     * @return the number of bicycles needed to raise the
     * number of bicycles stored at a hub's preferred location
     * to the lower-trigger value; zero if no rebalancing is
     * necessary
     */
    public int needBikes() {
	if (bikeCount < lowerTrigger) {
	    return (lowerTrigger - bikeCount);
	} else {
	    return 0;
	}
    }

    /**
     * Get the number of bicycles that must be removed from the
     * hub's preferred location to reduce that number to the
     * upper-trigger value.
     * @return the number of bicycles that must be removed from the
     * hub's preferred location to reduce that number to the
     * upper-trigger value; zero if no rebalancing is necessary
     */
    public int excessBikes() {
	if (bikeCount > upperTrigger) {
	    return bikeCount - upperTrigger;
	} else {
	    return 0;
	}
    }

    /**
     * Get the initial bicycle count at the preferred location.
     * @return the initial bicycle count at the preferred location
     */
    public int getInitialBikeCount() {
	return initialBikeCount;
    }

    /**
     * Get the current bicycle count at the preferred location.
     * @return the current bicycle count at the preferred location
     */
    public int getBikeCount() {
	return bikeCount;
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

    /**
     * Get the number of bicycles in a hub's overflow area.
     * @return the number of bicycles in this hub's overflow area
     */
    public int getOverflow() {
	return overflow;
    }

    /**
     * Get the nominal value for the number of bicycles that are
     * stored at a hub's preferred location.
     * Load-balancing operations will try to adjust the number of
     * bicycles at each hub to the nominal value for that hub.
     * @return the nominal value for the number of bicycles that are
     * stored at this hub's preferred location
     */
    public int getNominal() {
	return nominal;
    }

    /**
     * Get the lower trigger for the number of bicycles
     * stored at a hub's preferred location.
     * When the number of bicycles at the preferred location is
     * smaller than this value, hub listeners will be notified.
     * @return the lower trigger
     */
    public int getLowerTrigger() {
	return lowerTrigger;
    }

    /**
     * Get the upper trigger for the number of bicycles
     * stored at a hub's preferred location.
     * When the number of bicycles at the preferred location is
     * larger than this value, hub listeners will be notified.
     * @return the upper trigger
     */
    public int getUpperTrigger() {
	return upperTrigger;
    }

    /**
     * Pickup bicycles from the overflow area.
     * This method will reduce the count of bicycles in the overflow
     * area for this hub.
     * @param n the number of bicycles to remove from the overflow area
     * @return the time it will take to pick up the specified number
     *         of bicycles
     * @exception IllegalArgumentException the argument was larger than
     *            the number of bicycles in the overflow area or the

     *            argument was negative
     */
    public long pickupOverflow(int n) throws IllegalArgumentException {
	if (n < 0 || overflow < n) {
	    throw new IllegalArgumentException
		(errorMsg("pickupRange", overflow, n));
	}
	double interval = 0;

	for (int i = 0; i < n; i++) {
	    interval += pickupTime.next();
	    overflow--;
	}
	if (n != 0) {
	    fireHubDataListeners(bikeCount, false, overflow, true);
	}
	return sim.getTicks(interval);
    }

    /**
     * Decrement the number of bicycles at a hub's preferred location.
     * The value returned may differ from the argument as the number
     * of bicycles at the current location cannot be negative or larger
     * then the hub's capacity.
     * @param decr the decrement in the number of bicycles
     * @return the number of bicycles actually removed from the
     *         preferred location
     */
    public int decrBikeCount(int decr) {
	int oldBikeCount = bikeCount;
	int oldOverflow = overflow;
	int result;
	int need0 = needBikes();
	int excess0 = excessBikes();
	if (bikeCount - decr < 0) {
	    result = bikeCount;
	    bikeCount = 0;
	} else {
	    int oldCount = bikeCount;
	    bikeCount -= decr;
	    if (bikeCount > capacity) {
		overflow += bikeCount - capacity;
		bikeCount = capacity;
		result = oldCount - capacity;
	    } else {
		result = decr;
	    }
	}
	int need = needBikes();
	int excess = excessBikes();
	if (need != need0 || excess != excess0) {
	    fireHubListeners(need, excess, overflow);
	}
	if (decr != 0) {
	    fireHubDataListeners(bikeCount, (oldBikeCount != bikeCount),
				 overflow, (oldOverflow != overflow));
	}
	return result;
    }

    /**
     * Increment the number of bicycles in the overflow area of a hub.
     * @param incr the increment
     * @exception IllegalArgumentException if the argument is negative
     */
    public void incrOverflow(int incr) throws IllegalArgumentException {
	if (incr < 0) throw new IllegalArgumentException
			  (errorMsg("argNegative", incr));
	overflow += incr;
	if (incr != 0) {
	    int need = needBikes();
	    int excess = excessBikes();
	    fireHubListeners(need, excess, overflow);
	}
    }

    /**
     * Increment the number of bicycles at a hub's preferred location.
     * The value returned may differ from the argument as the number
     * of bicycles at the current location cannot be negative or larger
     * then the hub's capacity.
     * @param incr the increment in the number of bicycles
     * @return the number of bicycles actually added to the
     *         preferred location
     */
    public int incrBikeCount(int incr) {
	return -decrBikeCount(-incr);
    }


    /**
     * Message representing a trip.
     */
    public static class TripMessage {
	int n;
	boolean bikeMode;
	boolean  willOverflow;
	Callable continuation;

	/**
	 * Get the number of bicycles or persons traveling together
	 * @return the number traveling together
	 */
	public int getN() {return n;}

	TripMessage(int n, boolean bikeMode, boolean willOverflow,
		    Callable continuation) {
	    this.n = n;
	    this.bikeMode = bikeMode;
	    this.willOverflow = willOverflow;
	    this.continuation = continuation;
	}
    }

    /**
     * Send users to another hub.
     * If the requested or required number of bicycles is not available, no
     * bicycles will be send.
     * @param dest the hub to which bicycles should be sent
     * @param m the number of bicycles or users to send
     * @param willOverflow true if the destination hub must put the
     *        bicycles in the overflow area; false if the destination
     *        hub will put bicycles in the preferred area and use the
     *        overflow area only if the capacity would be exceeded
     * @param continuation code to call when the destination is reached
     * @return the domain used to compute delays on success; null otherwise
     * @exception IllegalArgumentException the second argument was negative.
     */
    public HubDomain sendUsers(Hub dest, int m, boolean willOverflow,
			     Callable continuation)
	throws IllegalArgumentException
    {
	return sendUsers(dest, m, willOverflow, (RealValuedFunctionTwo) null,
			 continuation);
    }

    /**
     * Send users to another hub using a probability function.
     * If the requested or required number of bicycles is not available, no
     * bicycles will be send. The probability function is a real-valued
     * function of two arguments and is used when the distance hub is
     * a member of this hub's user domain and that domain's parent domain
     * (provided it is also a hub domain). The first argument is the
     * delay one obtains from the user domain and the second argument is
     * the delay one obtains from the parent domain.  The value that is
     * returned is the probability of using the hub's  user domain to
     * determine delays.  When the probability function is null, it
     * is equivalent to a function whose value is 1.0 when the user domain's
     * delay table estimated travel time is shorter and 0.0 otherwise.

     * @param dest the hub to which bicycles should be sent
     * @param m the number of bicycles or users to send
     * @param willOverflow true if the destination hub must put the
     *        bicycles in the overflow area; false if the destination
     *        hub will put bicycles in the preferred area and use the
     *        overflow area only if the capacity would be exceeded
     * @param probFunction the probability function; null for the default
     *        behavior
     * @param continuation code to call when the destination is reached
     * @return the domain used to compute delays on success; null otherwise
     * @exception IllegalArgumentException the second argument was negative.
     */
    public HubDomain sendUsers(Hub dest, int m, boolean willOverflow,
			       RealValuedFunctionTwo probFunction,
			       Callable continuation)
	throws IllegalArgumentException
    {
	if (m < 0) throw new IllegalArgumentException
			    (errorMsg("argNegative", m));
	HubDomain d = usrDomain.getParentHubDomain();
	boolean bikeMode = true;
	if (d != null && inDomain(d) && dest.inDomain(d)) {
	    double delay1 = usrDomain.estimateDelay(this, dest, m);
	    double delay2 = d.estimateDelay(this, dest, m);
	    boolean test;
	    if (probFunction == null || delay2 == Double.POSITIVE_INFINITY) {
		test = (delay1 < delay2);
	    } else {
		double p = probFunction.valueAt(delay1, delay2);
		if (p == 1.0) {
		    test = true;
		} else if (p == 0.0) {
		    test = false;
		} else {
		    test = p > StaticRandom.nextDouble();
		}
	    }
	    if (test) {
		d = usrDomain;
		if (!dest.inDomain(d))  return null;
	    } else {
		bikeMode = false;
		willOverflow = false;
	    }
	} else {
	    d = usrDomain;
	    if (!dest.inDomain(d))  return null;
	}
	int n = bikeMode? decrBikeCount(m): m;
	if (n != m) {
	    incrBikeCount(n);
	    n = -1;
	}
	if (n != -1) {
	    trace(BikeShare.level4,
		  "sending %d %s to %s",
		  m, (bikeMode? "bicycles": "users"), dest.getName());
	    TripMessage tmsg =
		new TripMessage(m, bikeMode, willOverflow, continuation);
	    send(tmsg, dest, d);
	    return d;
	} else {
	    return null;
	}
    }

    @DMethodImpl("org.bzdev.drama.DoReceive")
    void doReceiveImpl(TripMessage msg, Actor src, boolean wereQueued) {
	
	int n = msg.n;
	boolean bikeMode = msg.bikeMode;
	if (bikeMode) {
	    if (msg.willOverflow) {
		trace(BikeShare.level4,
		      "accepting %d bicycles, "
		      +"intended and added to the overflow area", n);
		incrOverflow(n);
	    } else {
		int nn = incrBikeCount(n);
		trace(BikeShare.level4,
		      "accepting %d bicycles, %d added to the preferred area",
		      n, nn);
	    }
	} else {
	    trace(BikeShare.level4,
		  "accepting %d users traveling without shared bicycles", n);
	}
	if (msg.continuation != null) {
	    msg.continuation.call();
	}
    }

     /**
     * Print the configuration for an instance of Hub.
     * The documentation for method
     * {@link org.bzdev.devqsim.SimObject#printConfiguration(String,String,boolean,java.io.PrintWriter)}
     * contains a description of how this method is used and how to
     * override it. The method
     * {@link org.bzdev.drama.Actor#printConfiguration(String,String,boolean,java.io.PrintWriter)}
     * describes the data that will be printed for the
     * superclass of this class. The data that will be printed
     * when this method is called are the following.
     * <P>
     * For class {@link Hub}:
     * <UL>
     *  <LI> the X coordinate of this hub in meters.
     *  <LI> the Y coordinate of this hub in meters.
     *  <LI> the initial bicycle count.
     *  <LI> the capacity of this hub.
     *  <LI> the lower trigger for this hub.
     *  <LI> the upper trigger for this hub.
     *  <LI> the user domain for this hub.
     *  <LI> the system domain for this hub.
     *  <LI> the pickup time for this hub (the random variable that
     *       determines the time in seconds it takes to pickup a bicycle
     *       from the overflow area).
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
	out.println(prefix + "x:" + x);
	out.println(prefix + "y:" + y);
	out.println(prefix + "initialBikeCount: " + initialBikeCount);
	out.println(prefix + "capacity: " + capacity);
	out.println(prefix + "nominal: " + nominal);
	out.println(prefix + "lowerTrigger: " + lowerTrigger);
	out.println(prefix + "upperTrigger " + upperTrigger);
	// out.println(prefix + "user domain: " + usrDomain.getName());
	// out.println(prefix + "system domain: " + sysDomain.getName());
	out.println(prefix + "pickup time: " + 
		    ((pickupTime == null)? "null": pickupTime.toString()));
    }

     /**
     * Print the state for an instance of Hub.
     * The documentation for method
     * {@link org.bzdev.devqsim.SimObject#printState(String,String,boolean,java.io.PrintWriter)}
     * contains a description of how this method is used and how to
     * override it. The method
     * {@link org.bzdev.drama.Actor#printState(String,String,boolean,java.io.PrintWriter)}
     * describes the data that will be printed for the
     * superclass of this class. The data that will be printed
     * when this method is called are the following.
     * <P>
     * For class {@link Hub}:
     * <UL>
     *   <LI> the bicycle count for this hub.
     *   <LI> the count of the number of bicycles in the overflow area
     *        for this hub.
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
			   boolean printName, PrintWriter out) {
	super.printState(iPrefix, prefix, printName, out);
	out.println(prefix + "bike count: " + bikeCount);
	out.println(prefix + "overflow count: " + overflow);
    }
}

//  LocalWords:  HubWorker init DoubleRandomVariable UsrDomain sim
//  LocalWords:  SysDomain HubListener HubCondition HubDataListener
//  LocalWords:  pickupOverflow decrBikeCount incrBikeCount sendUsers
//  LocalWords:  incrOverflow boolean RealValuedFunctionTwo bikeCount
//  LocalWords:  SimFunctionTwo HubDoReceive newBikeCount pickupTime
//  LocalWords:  overflowCount newOverflowCount lowerTrigger decr
//  LocalWords:  upperTrigger overCount usrDomain sysDomain incr dest
//  LocalWords:  rebalancing IllegalArgumentException pickupRange
//  LocalWords:  argNegative willOverflow probFunction superclass
//  LocalWords:  printConfiguration iPrefix printName whitespace
//  LocalWords:  initialBikeCount println getName printState
