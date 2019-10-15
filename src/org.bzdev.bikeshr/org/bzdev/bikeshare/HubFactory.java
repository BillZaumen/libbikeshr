package org.bzdev.bikeshare;
import org.bzdev.drama.*;

/**
 * Factory for creating instances of Hub.
 * <P>
 * The parameters this factory supports are shown in the following table:
 * <P>
 * <IFRAME SRC="../../../factories-api/org/bzdev/bikeshare/HubFactory.html" width="95%" height="600">
 * Please see
 *  <A HREF="../../../factories-api/org/bzdev/bikeshare/HubFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */

public class HubFactory extends AbstractHubFactory<Hub>
{
    /**
     * Constructor required by the service-provider interface used
     * to find factories.
     */
    public HubFactory() {
	this(null);
    }

    public HubFactory(DramaSimulation sim) {
	super(sim);
    }

    @Override
    protected Hub newObject(String name) {
	return new Hub(getSimulation(), name, willIntern());
    }
}

//  LocalWords:  usrDomain sysDomain upperTrigger lowerTrigger
//  LocalWords:  overCount pickupTime boolean timeline traceSetMode
//  LocalWords:  traceSets TraceSet SimObject
