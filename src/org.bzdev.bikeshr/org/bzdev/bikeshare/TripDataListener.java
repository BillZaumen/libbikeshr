package org.bzdev.bikeshare;

/**
 * Data listener for trip generators.
 * This class is used for instrumenting a simulation.
 */
public interface TripDataListener {

    /**
     * Indicate that a trip has started.
     * @param tripID the ID for a trip;
     * @param time the simulation time in double-precision units
     * @param ticks the simulation time in ticks
     * @param hub the hub at which this event occurred
     * @param d the HubDomain that determined the travel time for the
     *        trip
     */
    void tripStarted(long tripID, double time, long ticks, Hub hub,
		     HubDomain d);

    /**
     * Indicates that the start of an interval during which
     * a trip paused at an intermediate hub.
     * @param tripID the ID for a trip;
     * @param time the simulation time in double-precision units
     * @param ticks the simulation time in ticks
     * @param hub the hub at which this event occurred
     */
    void tripPauseStart(long tripID, double time, long ticks, Hub hub);

    /**
     * Indicates that the end of an interval during which
     * a trip paused at an intermediate hub.
     * @param tripID the ID for a trip;
     * @param time the simulation time in double-precision units
     * @param ticks the simulation time in ticks
     * @param hub the hub at which this event occurred
     * @param d the HubDomain that determined the travel time for the
     *        trip
     */
    void tripPauseEnd(long tripID, double time, long ticks, Hub hub,
		      HubDomain d);

    /**
     * Indicate that a trip has ended.
     * @param tripID the ID for a trip;
     * @param time the simulation time in double-precision units
     * @param ticks the simulation time in ticks
     * @param hub the hub at which this event occurred
     */
    void tripEnded(long tripID, double time, long ticks, Hub hub);
    
    /**
     * Indicate that a trip has failed to start.
     * This can occur if a hub does not have a sufficient number
     * of bicycles available.
     * @param tripID the ID for a trip;
     * @param time the simulation time in double-precision units
     * @param ticks the simulation time in ticks
     * @param hub the hub at which this event occurred
     */
    void tripFailedAtStart(long tripID, double time, long ticks, Hub hub);

    /**
     * Indicate that a trip has failed to complete.
     * This can occur if a hub does not have a sufficient number
     * of bicycles available at an intermediate hop (e.g., for
     * intermodal trips).
     * <P>
     * Note: the class {@link BasicTripGenerator} does
     * not call this method.
     * @param tripID the ID for a trip;
     * @param time the simulation time in double-precision units
     * @param ticks the simulation time in ticks
     * @param hub the hub at which this event occurred
     */
    void tripFailedMidstream(long tripID, double time, long ticks, Hub hub);
}

//  LocalWords:  tripID HubDomain intermodal BasicTripGenerator
