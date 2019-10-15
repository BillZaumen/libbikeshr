package org.bzdev.bikeshare;
import java.util.EventListener;
import java.util.EventListener;

/**
 * Data listener class for hub workers.
 * This class is used for instrumenting a simulation.
 * It tracks a hub worker's location (which hub it is at),
 * the hub worker's bicycle count, and the hub worker's
 * activities.
 */
public interface HubWorkerListener extends EventListener {
    
    /**
     * Indicate that a worker left a storage-hub queue.
     * @param worker the hub worker being tracked
     * @param time the simulation time in double-precision units
     * @param ticks the simulation time in ticks
     * @param hub the hub at which this event occurred
     */
    void dequeued(HubWorker worker, double time, long ticks, Hub hub);

    /**
     * Indicate that a worker entered a hub.
     * @param worker the hub worker being tracked
     * @param time the simulation time in double-precision units
     * @param ticks the simulation time in ticks
     * @param hub the hub at which this event occurred
     */
    void enteredHub(HubWorker worker, double time, long ticks, Hub hub);

    /**
     * Indicated that a worker started to remove bicycles from the
     * overflow area.
     * @param worker the hub worker being tracked
     * @param time the simulation time in double-precision units
     * @param ticks the simulation time in ticks
     * @param hub the hub at which this event occurred
     */
    void fixingOverflows(HubWorker worker, double time, long ticks, Hub hub);
    
    /**
     * Indicate that a worker started to handle the preferred area.
     * @param worker the hub worker being tracked
     * @param time the simulation time in double-precision units
     * @param ticks the simulation time in ticks
     * @param hub the hub at which this event occurred
     */
    void fixingPreferred(HubWorker worker, double time, long ticks, Hub hub);
    
    /**
     * Indicate that a worker left a hub.
     * @param worker the hub worker being tracked
     * @param time the simulation time in double-precision units
     * @param ticks the simulation time in ticks
     * @param hub the hub at which this event occurred
     */
    void leftHub(HubWorker worker, double time, long ticks, Hub hub);

    /**
     * Indicate that a worker joined a queue.
     * @param worker the hub worker being tracked
     * @param time the simulation time in double-precision units
     * @param ticks the simulation time in ticks
     * @param hub the hub at which this event occurred
     */
    void queued(HubWorker worker, double time, long ticks, Hub hub);
    
    /**
     * Indicate that a worker's bicycle count changed.
     * @param worker the hub worker being tracked
     * @param time the simulation time in double-precision units
     * @param ticks the simulation time in ticks
     * @param hub the hub at which this event occurred
     * @param oldcount the bicycle count before the change
     * @param newcount the bicycle count after the change
     */
    void changedCount(HubWorker worker, double time, long ticks, Hub hub,
		      int oldcount, int newcount);
}

//  LocalWords:  oldcount newcount
