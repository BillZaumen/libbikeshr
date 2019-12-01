package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.obnaming.annotations.FactoryParmManager;

/**
 * Abstract Factory for creating external domains.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link ExtDomainFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/bikeshare/ExtDomainFactory.html" style= "width:95%;height:500px;border:3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/bikeshare/ExtDomainFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * <P>
 * The parameter "parent" (provided by a superclass) was removed.
 */
@FactoryParmManager(value="ExtDomainFactoryPM",
		    stdFactory = "ExtDomainFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation"
		    )
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
