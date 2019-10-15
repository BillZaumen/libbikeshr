package org.bzdev.bikeshare;
import java.util.EventListener;

/**
 * Hub data listener interface.
 * This listener responds to changes in the bike count and overflow area
 * for a hub.
 */

public interface HubDataListener extends EventListener {
    /**
     * Notify this listener that a hub's counts changed.
     * This listener is intended for instrumentation purposes.
     * @param hub the hub that changed
     * @param bikeCount the new bike count
     * @param newBikeCount true if the bicycle count has changed
     *        during the event that invoked this listener; false otherwise
     * @param overflowCount the new overflow count
     * @param newOverflowCount true if the overflow count has
     *        changed during the event that invoked this listener; false
     *        otherwise
     * @param time the simulation time at which this method was called
     * @param ticks the simulation time in units of ticks at which this
     *        method was called
     */
    void hubChanged(Hub hub, int bikeCount, boolean newBikeCount,
		    int overflowCount, boolean newOverflowCount,
		    double time, long ticks);
}

//  LocalWords:  bikeCount newBikeCount overflowCount
//  LocalWords:  newOverflowCount
