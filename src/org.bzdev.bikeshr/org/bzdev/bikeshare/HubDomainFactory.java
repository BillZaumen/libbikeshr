package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.math.rv.*;
import org.bzdev.util.units.MKS;

/**
 * Base factory for hub domains.
 * This factory inherits the following parameters from its
 * superclasses:
 * <UL>
 *   <LI> "parent" - the parent domain.
 *   <LI> "timeline" - an integer-keyed set of values that define
 *         changes in the object's configuration. Subclasses may provide
 *         additional parameters.  The default parameters are:
 *        <UL>
 *           <LI> "timeline.time" - the time at which timeline parameters
 *                are to change. This parameter must be provided if a
 *                timeline entry exists.  The units are those used by the
 *                double-precession time unit for the simulation (for
 *                animations, this is generally seconds).
 *           <LI> "timeline.traceSetMode" - indicates how the parameter
 *                "timeline.traceSets" is interpreted. the values are
 *                enumeration constants of type
 *                {@link org.bzdev.devqsim.TraceSetMode} and are used as
 *                follows:
 *                <UL>
 *                  <LI> "KEEP" - keep the existing trace sets,
 *                       adding additional ones specified by the
 *                       parameter "timeline.traceSets".
 *                  <LI> "REMOVE" - remove the trace sets specified
 *                       by the parameter "timeline.traceSets".
 *                  <LI> "REPLACE" - remove all existing trace sets
 *                       and replace those with the ones specified by
 *                       the timeline.traceSets parameter.
 *                </UL>
 *           <LI> "timeline.traceSets" - a parameter representing a set
 *                of TraceSet objects (the three-argument 'add' method
 *                is used to add entries).
 *        </UL>
 *   <LI> "traceSets" - a set of TraceSets a SimObject will use
 *        for tracing.  One should use the add and remove factory
 *        methods as this parameter refers to a set of values.
 * </UL>
 * <P>
 * Note: this factory disallows the following parameters from its
 * superclass:
 * <UL>
 *   <LI> "priority"
 *   <LI> "communicationDomain"
 *   <LI> "additionalCommDomainType"
 * </UL>
 * Use of these disallowed parameters could lead to a configuration
 * that would not work as expected.
 */
public abstract class HubDomainFactory<Obj extends HubDomain>
    extends AbstractDomainFactory<Obj>
{

    DramaSimulation sim;
    
    String removedParms[] = {
	"priority",
	"communicationDomain",
	"additionalCommDomainType",
    };

    /**
     * Constructor required by the service-provider interface used
     * to find factories.
     */
    public HubDomainFactory() {
	this(null);
    }
    protected HubDomainFactory(DramaSimulation sim) {
	super(sim);
	this.sim = sim;
	removeParms(removedParms);
    }


    @Override
    public void clear() {
	super.clear();
    }
}

//  LocalWords:  superclasses timeline traceSetMode traceSets
//  LocalWords:  TraceSet SimObject superclass communicationDomain
//  LocalWords:  additionalCommDomainType
