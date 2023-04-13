package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.obnaming.annotations.FactoryParmManager;

/**
 * Abstract Factory for creating system domains.
 * This class is a "place holder" and actually does nothing
 * except restrict a type parameter so that subclasses produce
 * instances of SysDomain.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SysDomainFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/bikeshare/SysDomainFactory.html" style= "width:95%;height:500px;border:3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/bikeshare/SysDomainFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
@FactoryParmManager(value = "", // no parameters
		    stdFactory = "SysDomainFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbstractSysDomainFactory<Obj extends SysDomain>
    extends HubDomainFactory<Obj>
{
    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbstractSysDomainFactory(DramaSimulation sim) {
	super(sim);
    }
}

//  LocalWords:  SysDomain superclasses timeline traceSetMode
//  LocalWords:  traceSets TraceSet SimObject
