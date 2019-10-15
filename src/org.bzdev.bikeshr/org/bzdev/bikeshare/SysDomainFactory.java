package org.bzdev.bikeshare;
import org.bzdev.drama.*;

/**
 * Factory to create an instance of SysDomain.
 * <P>
 * The parameters this factory supports are shown in the following table:
 * <P>
 * <IFRAME SRC="../../../factories-api/org/bzdev/bikeshare/SysDomainFactory.html" width="95%" height="600">
 * Please see
 *  <A HREF="../../../factories-api/org/bzdev/bikeshare/SysDomainFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public class SysDomainFactory extends AbstractSysDomainFactory<SysDomain>
{
    /**
     * Constructor required by the service-provider interface used
     * to find factories.
     */
    public SysDomainFactory() {
	this(null);
    }

    /**
     * Constructor.
     * @param sim the simulation
     */
    public SysDomainFactory(DramaSimulation sim) {
	super(sim);
    }

    @Override
    protected SysDomain newObject(String name) {
	return new SysDomain(getSimulation(), name, willIntern(),
			     getParent());
    }
}

//  LocalWords:  SysDomain timeline traceSetMode traceSets TraceSet
//  LocalWords:  SimObject sim
