package org.bzdev.bikeshare;
import java.util.EventListener;

/**
 * Hub listener interface.
 * This listener responds to changes in the state or status of a hub.
 * An instance of it is created by each system domain and used to
 * notify the system domain's condition of a change in status.
 * <P>
 * Users of this library should rarely have to create an instance of
 * this class directly except possibly for instrumentation reasons.
 */
public interface HubListener extends EventListener {
    /**
     * Notify this listener that a hub changed.
     * The values provided by the <code>need</code> and
     * <code>excess</code> arguments are the numbers needed to reach the
     * trigger values, not the nominal value.
     * @param hub the hub that changed
     * @param need the number of bicycles needed for the
     *        preferred location of the hub to have enough
     *        bicycles to reach its lower trigger value
     * @param excess the number of bicycles stored at the hub's
     *        preferred location beyond the number provided by
     *        the hub's upper trigger value
     * @param overflow the number of bicycles in the overflow area
     */
    void hubChanged(Hub hub, int need, int excess, int overflow);
}
