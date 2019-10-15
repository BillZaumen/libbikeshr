package org.bzdev.bikeshare;
import org.bzdev.drama.*;

/**
 * Factory to create instances of {@link UsrDomain}.
 * <P>
 * The parameters this factory supports are shown in the following table:
 * <P>
 * <IFRAME SRC="../../../factories-api/org/bzdev/bikeshare/UsrDomainFactory.html" width="95%" height="600">
 * Please see
 *  <A HREF="../../../factories-api/org/bzdev/bikeshare/UsrDomainFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public class UsrDomainFactory extends AbstractUsrDomainFactory<UsrDomain>
{
    /**
     * Constructor required by the service-provider interface used
     * to find factories.
     */
    public UsrDomainFactory() {
	this(null);
    }

    /**
     * Constructor.
     * @param sim the simulation
     */
    public UsrDomainFactory(DramaSimulation sim) {
	super(sim);
    }

    @Override
    protected UsrDomain newObject(String name) {
	return new UsrDomain(getSimulation(), name, willIntern(),
			     getParent());
    }

}

//  LocalWords:  UsrDomain timeline traceSetMode traceSets TraceSet
//  LocalWords:  SimObject sim
