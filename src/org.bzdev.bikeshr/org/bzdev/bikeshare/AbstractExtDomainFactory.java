package org.bzdev.bikeshare;
import org.bzdev.drama.*;

/**
 * Abstract Factory for creating external domains.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link ExtDomainFactory}:
 * <P>
 * <IFRAME SRC="../../../factories-api/org/bzdev/bikeshare/ExtDomainFactory.html" width="95%" height="600">
 * Please see
 *  <A HREF="../../../factories-api/org/bzdev/bikeshare/ExtDomainFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * <P>
 * The parameter "parent" (provided by a superclass) was removed.
 */
public abstract class AbstractExtDomainFactory<Obj extends ExtDomain>
    extends HubDomainFactory<Obj>
{
    protected AbstractExtDomainFactory(DramaSimulation sim) {
	super(sim);
	removeParm("parent");
    }
}

//  LocalWords:  superclasses timeline traceSetMode traceSets
//  LocalWords:  TraceSet SimObject
