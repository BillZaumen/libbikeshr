package org.bzdev.bikeshare;
import org.bzdev.drama.*;

/**
 * Factory for creating instances of HubWorker.
 * <P>
 * The parameters this factory supports are shown in the following table:
 * <P>
 * <IFRAME SRC="../../../factories-api/org/bzdev/bikeshare/HubWorkerFactory.html" width="95%" height="600">
 * Please see
 *  <A HREF="../../../factories-api/org/bzdev/bikeshare/HubWorkerFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public class HubWorkerFactory extends AbstrHubWorkerFactory<HubWorker>
{
    /**
     * Constructor required by the service-provider interface used
     * to find factories.
     */
    public HubWorkerFactory() {
	this(null);
    }

    public HubWorkerFactory(DramaSimulation sim) {
	super(sim);
    }

    @Override
    protected HubWorker newObject(String name) {
	return new HubWorker(getSimulation(), name, willIntern());
    }
}

//  LocalWords:  HubWorker sysDomain currentHub storageHub boolean
//  LocalWords:  timeline traceSetMode traceSets TraceSet SimObject
