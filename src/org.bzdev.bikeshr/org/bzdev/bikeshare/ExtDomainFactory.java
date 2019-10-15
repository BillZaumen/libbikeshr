package org.bzdev.bikeshare;
import org.bzdev.drama.*;

/**
 * Factory to create instances of {@link ExtDomain}.
 * <P>
 * The parameters this factory supports are shown in the following table:
 * <P>
 * <IFRAME SRC="../../../factories-api/org/bzdev/bikeshare/ExtDomainFactory.html" width="95%" height="600">
 * Please see
 *  <A HREF="../../../factories-api/org/bzdev/bikeshare/ExtDomainFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public class ExtDomainFactory extends AbstractExtDomainFactory<ExtDomain>
{
    /**
     * Constructor required by the service-provider interface used
     * to find factories.
     */
    public ExtDomainFactory() {
	this(null);
    }

    /**
     * Constructor.
     * @param sim the simulation
     */
    public ExtDomainFactory(DramaSimulation sim) {
	super(sim);
    }

    @Override
    protected ExtDomain newObject(String name) {
	return new ExtDomain(getSimulation(), name, willIntern());
    }
}

//  LocalWords:  timeline traceSetMode traceSets TraceSet SimObject
//  LocalWords:  sim ExtDomain
