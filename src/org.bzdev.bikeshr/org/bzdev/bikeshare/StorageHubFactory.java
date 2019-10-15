package org.bzdev.bikeshare;
import org.bzdev.drama.*;

/**
 * Factory for creating instances of StorageHub.
 * <P>
 * The parameters this factory supports are shown in the following table:
 * <P>
 * <IFRAME SRC="../../../factories-api/org/bzdev/bikeshare/StorageHubFactory.html" width="95%" height="600">
 * Please see
 *  <A HREF="../../../factories-api/org/bzdev/bikeshare/StorageHubFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public class StorageHubFactory extends AbstrStorageHubFactory<StorageHub>
{
    /**
     * Constructor required by the service-provider interface used
     * to find factories.
     */
    public StorageHubFactory() {
	this(null);
    }

    public StorageHubFactory(DramaSimulation sim) {
	super(sim);
    }

    @Override
    protected StorageHub newObject(String name) {
	return new StorageHub(getSimulation(), name, willIntern());
    }

}

//  LocalWords:  StorageHub upperTrigger lowerTrigger sysDomain
//  LocalWords:  hubTable HubWorker boolean timeline traceSetMode
//  LocalWords:  traceSets TraceSet SimObject
