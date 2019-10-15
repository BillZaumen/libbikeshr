package org.bzdev.bikeshare;
import org.bzdev.devqsim.SimFunctionTwo;
import org.bzdev.drama.*;
import org.bzdev.lang.Callable;
import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;

import java.util.Map;
import java.util.HashMap;

/**
 * Base class for trip generator factories.
 * The parameters this factory defines are:
 * <UL>
 *  <LI> "initialDelay" - the time in seconds at which a traffic
 *       generator starts running. A value of 0.0 (the default)
 *       is the simulation's current time when the traffic generator
 *       is created (typically, the simulation's starting time).
 *  <LI> "probabilityFunction" - a SimFunctionTwo that provides a
 *       probability function. The probability function is defined
 *       as a real-valued function f(d<sub>1</sub>,d<sub>2</sub>) whose range
 *       is [0.0, 1.0] and whose domain contains non-negative
 *       values of its arguments. When called, d<sub>1</sub> will
 *       be the estimated delay computed using the delay table for
 *       the source hub's user domain and d<sub>2</sub> will be the
 *       delay computed using the delay table for the parent domain
 *       of the source hub's user domain.  The probability this
 *       function computes is the probability that the delay used
 *       is d<sub>1</sub>. If the function is null, the d<sub>1</sub>
 *       will be used if it is less than d<sub>2</sub>.  If d<sub>2</sub>
 *       is used, bicycles will not be removed or added to a hub as
 *       the trip is assumed to use some other service.
 *  <LI> "timeline.running" - an integer-keyed timeline parameter
 *       (see below) whose value may be "true" or "false". If not
 *       provided, this parameter is ignored.  It is used to turn
 *       the traffic generator on or off at specific times.
 * </UL>
 * The parameters this factory inherits from its superclasses are:
 * <UL>
 *  <LI> "domainMember".  Used to configure an instance of
 *       DomainMember for handling domain membership.
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
 *
 */
@FactoryParmManager(value = "TripGeneratorFactoryPM",
		    labelResourceBundle = "*.lpack.TripGeneratorLabels",
		    tipResourceBundle = "*.lpack.TripGeneratorTips")
public abstract class TripGeneratorFactory<Obj extends TripGenerator>
    extends AbstractActorFactory<Obj>
{
    @PrimitiveParm(value = "initialDelay",
		   lowerBound = "0.0",
		   lowerBoundClosed = true)
    double initialDelay = 0.0;

    @PrimitiveParm("probabilityFunction")
    SimFunctionTwo probabilityFunction = null;


    @CompoundParmType(tipResourceBundle = "*.lpack.TripGenTimelineTips",
		      labelResourceBundle = "*.lpack.TripGenTimelineLabels")
    static class TimelineEntry {
	@PrimitiveParm("running")
	Boolean running = null;
    };

    @KeyedCompoundParm("timeline")
    Map<Integer,TimelineEntry> timelineMap =
	new HashMap<Integer,TimelineEntry>();

    TripGeneratorFactoryPM<Obj> pm;

    /**
     * Constructor.
     * @param sim the simulation
     */
    protected TripGeneratorFactory(DramaSimulation sim) {
	super(sim);
	pm = new TripGeneratorFactoryPM<Obj>(this);
	initParms(pm, TripGeneratorFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	pm.setDefaults(this);
    }

    @Override
    protected void addToTimelineRequest(Obj object, int key, double time) {
	super.addToTimelineRequest(object, key, time);
	TimelineEntry entry = timelineMap.get(key);
	if (entry.running != null) {
	    final Obj obj = object;
	    final boolean running = entry.running;
	    addToTimelineResponse(new Callable() {
		    public void call() {
			if (running) {
			    obj.restart();
			} else {
			    obj.stop();
			}
		    }
		});
	}
    }

    @Override
    protected void initObject(Obj tripGen) {
	super.initObject(tripGen);
	tripGen.setInitialDelay(initialDelay);
	if (probabilityFunction != null) {
	    tripGen.setProbabilitySimFunction(probabilityFunction);
	}
    }
}

//  LocalWords:  superclasses domainMember boolean timeline traceSets
//  LocalWords:  traceSetMode TraceSet SimObject sim
