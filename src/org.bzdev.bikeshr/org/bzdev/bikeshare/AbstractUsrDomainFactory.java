package org.bzdev.bikeshare;
import org.bzdev.drama.*;

/**
 * Abstract Factory for creating user domains.
 * This class is a "place holder" and actually does nothing
 * except restrict a type parameter so that subclasses produce
 * instances of UsrDomain.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link UsrDomainFactory}:
 * <P>
 * <IFRAME SRC="../../../factories-api/org/bzdev/bikeshare/UsrDomainFactory.html" width="95%" height="600">
 * Please see
 *  <A HREF="../../../factories-api/org/bzdev/bikeshare/UsrDomainFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public abstract class AbstractUsrDomainFactory<Obj extends UsrDomain>
    extends HubDomainFactory<Obj>
{
    protected AbstractUsrDomainFactory(DramaSimulation sim) {
	super(sim);
    }
}

//  LocalWords:  UsrDomain superclasses timeline traceSetMode
//  LocalWords:  traceSets TraceSet SimObject
