package org.bzdev.bikeshare;
import org.bzdev.drama.*;

/**
 * Abstract Factory for creating system domains.
 * This class is a "place holder" and actually does nothing
 * except restrict a type parameter so that subclasses produce
 * instances of SysDomain.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SysDomainFactory}:
 * <P>
 * <IFRAME SRC="../../../factories-api/org/bzdev/bikeshare/SysDomainFactory.html" width="95%" height="600">
 * Please see
 *  <A HREF="../../../factories-api/org/bzdev/bikeshare/SysDomainFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public abstract class AbstractSysDomainFactory<Obj extends SysDomain>
    extends HubDomainFactory<Obj>
{
    protected AbstractSysDomainFactory(DramaSimulation sim) {
	super(sim);
    }
}

//  LocalWords:  SysDomain superclasses timeline traceSetMode
//  LocalWords:  traceSets TraceSet SimObject
