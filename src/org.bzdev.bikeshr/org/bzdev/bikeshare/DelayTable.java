package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.drama.common.*;

/**
 * Base class for delay tables.
 *  
 */
public abstract class DelayTable extends MsgForwardingInfo {
    DramaSimulation sim;
    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of this object
     * @param intern true if this object should be interned in the
     *        simulation's name table; false otherwise
     */
    protected DelayTable(DramaSimulation sim, String name, boolean intern) {
	super(sim, name, intern);
	this.sim = sim;
    }

    // private HubDomain domain = null;

    /**
     * Add this table to a hub domain.
     * @param domain the hub domain
     */
    public void addToDomain(HubDomain domain) {
	domain.setMessageForwardingInfo(this);
	domain.setDelayTable(this);
    }

    /**
     * Get the latest starting time, given a minimum starting time,
     * for a trip between two hubs with the same arrival time.
     * @param time the time in seconds, measured from the start of
     *        the simulation, at which a trip from src to dest could start
     * @param src the starting hub
     * @param dest the ending hub
     * @return the latest simulation time in seconds at which the trip
     *         could start without changing the arrival time;
     *         Double.NEGATIVE_INFINITY if a trip is not possible
     *         
     * @exception IllegalArgumentException there was no entry in the delay
     *         table for src and dest for the specified time
     */
    public abstract double latestStartingTime(double time, Hub src, Hub dest);

    /**
     * Get the latest starting time at or after the current simulation
     * time for a trip between two hubs assuming the same arrival time.
     * @param src the starting hub
     * @param dest the ending hub
     * @return the latest simulation time in seconds at which the trip
     *         could start without changing the arrival time;
     *         Double.NEGATIVE_INFINITY if a trip is not possible
     */
    public double latestStartingTime(Hub src, Hub dest) {
	return latestStartingTime(sim.currentTime(), src, dest);
    }


    /**
     * Estimate the time it takes to travel between two hubs.
     * @param startingTime the time in seconds, measured from the start of
     *        the simulation, at which a trip from src to dest starts
     * @param src the starting hub
     * @param dest the ending hub
     * @param n the number of individuals traveling together
     * @return the time in seconds to travel from src to dest;
     *         Double.POSITIVE_INFINITY if the trip is not possible
     * @exception NullPointerException if the second argument is null or
     *            the third argument is null;
     */
    public abstract double estimateDelay(double startingTime,
					 Hub src, Hub dest, int n);
    /**
     * Estimate the time it takes to travel between two hubs, starting
     * at the current simulation time.
     * @param src the starting hub
     * @param dest the ending hub
     * @param n the number of individuals traveling together
     * @return the time in seconds to travel from src to dest;
     *         Double.POSITIVE_INFINITY if the trip is not possible
     * @exception NullPointerException if the first argument is null or
     *            the second argument is null;
     */
    public double estimateDelay(Hub src, Hub dest, int n) {
	return estimateDelay(sim.currentTime(), src, dest, n);
    }

    /**
     * Get the time it takes to travel between two hubs.
     * test if a trip is possible.
     * @param startingTime the time in seconds, measured from the start of
     *        the simulation, at which a trip from src to dest starts
     * @param src the starting hub
     * @param dest the ending hub
     * @param n the number of individuals traveling together
     * @return the time in seconds to travel from src to dest;
     *         Double.POSITIVE_INFINITY if the trip is not possible
     * @exception NullPointerException if the second argument is null or
     *            the third argument is null;
     */
    public abstract double getDelay(double startingTime, Hub src, Hub dest,
				    int n);

    /**
     * Get the time it takes to travel between two hubs, starting at the
     * current simulation time.
     * <P>
     * @param src the starting hub
     * @param dest the ending hub
     * @param n the number of individuals traveling together
     * @return the time in seconds to travel from src to dest;
     *         Double.POSITIVE_INFINITY if the trip is not possible
     * @exception NullPointerException if the first argument is null or
     *            the second argument is null;
     */
    public double getDelay(Hub src, Hub dest, int n) {
	return getDelay(sim.currentTime(), src, dest, n);
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
	    int n = (msg instanceof Hub.TripMessage)?
		((Hub.TripMessage)msg).getN(): 0;
	    return sim.getTicks(getDelay(sim.currentTime(),srcHub, destHub, n));
	} else {
	    return 0L;
	}
    }

}

//  LocalWords:  sim HubDomain src dest IllegalArgumentException
//  LocalWords:  startingTime NullPointerException
