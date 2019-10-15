package org.bzdev.bikeshare;
import org.bzdev.drama.*;

/**
 * Factory for creating instances of BasicHubBalancer.
 * <P>
 * The parameters this factory supports are shown in the following table:
 * <P>
 * <IFRAME SRC="../../../factories-api/org/bzdev/bikeshare/BasicHubBalancerFactory.html" width="95%" height="600">
 * Please see
 *  <A HREF="../../../factories-api/org/bzdev/bikeshare/BasicHubBalancerFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public class BasicHubBalancerFactory 
    extends AbstrBasicHubBalancerFactory<BasicHubBalancer>
{
    /**
     * Constructor required by the service-provider interface used
     * to find factories.
     */
    public BasicHubBalancerFactory() {
	this(null);
    }

    public BasicHubBalancerFactory(DramaSimulation sim) {
	super(sim);
    }

    @Override
    protected BasicHubBalancer newObject(String name) {
	return new BasicHubBalancer(getSimulation(), name, willIntern());
    }
}

//  LocalWords:  BasicHubBalancer quietPeriod startAdditionalWorkers
//  LocalWords:  balancer's sysDomain balancer boolean timeline
//  LocalWords:  traceSetMode traceSets TraceSet SimObject
