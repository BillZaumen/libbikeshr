package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.util.rv.*;
import org.bzdev.util.units.MKS;


/**
 * Base factory for hub balancers.
 * This factory defines the common parameter for all
 * hub balancers:
 * <UL>
 *   <LI> "sysDomain" - the system domain for a hub balancer.
 * </UL>
 * It also inherits the following properties from its superclasses:
 * <UL>
 *  <LI> "domains".  Used to clear a set of domains explicitly
 *       provided, but cannot be used to set or add domains.
 *  <LI> "domain".  Used to specify a domain that can added
 *       or removed from the actor's domain set. For this parameter,
 *       the key is the domain and the value is a boolean
 *       that is true if conditions for that domain should be tracked;
 *       false otherwise. If a specified domain was already joined by
 *       a shared domain member, an explicit request to join that
 *       domain will be ignored when an actor is created.
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
 */
@FactoryParmManager(value = "HubBalancerFactoryPM",
		    labelResourceBundle = "*.lpack.HubBalancerLabels",
		    tipResourceBundle = "*.lpack.HubBalancerTips")
public abstract class HubBalancerFactory<Obj extends HubBalancer>
    extends AbstractActorFactory<Obj>
{
    @PrimitiveParm("sysDomain")
    SysDomain sysDomain = null;

    HubBalancerFactoryPM<Obj> pm;

    /**
     * Constructor.
     * @param sim the simulation for which this factory can create objects
     */
    protected HubBalancerFactory(DramaSimulation sim) {
	super(sim);
	pm = new HubBalancerFactoryPM<Obj>(this);
	removeParm("domainMember");
	initParms(pm, HubBalancerFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	pm.setDefaults(this);
    }

    @Override
    protected void initObject(Obj hub) {
	super.initObject(hub);
	hub.initDomain(sysDomain);
    }
}
//  LocalWords:  balancers sysDomain balancer superclasses boolean
//  LocalWords:  timeline traceSetMode traceSets TraceSet SimObject
//  LocalWords:  HubBalancerFactoryPM sim domainMember
