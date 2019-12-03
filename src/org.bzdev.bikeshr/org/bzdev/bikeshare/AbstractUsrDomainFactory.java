package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.obnaming.annotations.FactoryParmManager;

/**
 * Abstract Factory for creating user domains.
 * This class is a "place holder" and actually does nothing
 * except restrict a type parameter so that subclasses produce
 * instances of UsrDomain.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link UsrDomainFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/bikeshare/UsrDomainFactory.html" style= "width:95%;height:500px;border:3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/bikeshare/UsrDomainFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
@FactoryParmManager(value = "", // no parameters
		    stdFactory = "UsrDomainFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbstractUsrDomainFactory<Obj extends UsrDomain>
    extends HubDomainFactory<Obj>
{
    protected AbstractUsrDomainFactory(DramaSimulation sim) {
	super(sim);
    }
}

//  LocalWords:  UsrDomain superclasses timeline traceSetMode
//  LocalWords:  traceSets TraceSet SimObject
