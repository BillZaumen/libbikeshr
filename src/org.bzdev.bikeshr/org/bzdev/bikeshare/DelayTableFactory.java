package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.drama.common.*;
import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;

import java.util.Set;
import java.util.LinkedHashSet;

/**
 * Abstract factory for delay tables.
 * This factory provides the following parameters:
 * <UL>
 *   <LI> "domains" - a set of hub domains to which this delay
 *        table applies.
 * </UL>
 * The parameters inherited from superclasses are the following:
 * <UL>
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

@FactoryParmManager(value = "DelayTblFactoryPM",
		    labelResourceBundle = "*.lpack.DelayTableLabels",
		    tipResourceBundle = "*.lpack.DelayTableTips")
public abstract class DelayTableFactory<Obj extends DelayTable>
    extends AbstrMsgFrwdngInfoFactory<Obj>
{

    @PrimitiveParm("domains")
    Set<HubDomain> domains = new LinkedHashSet<HubDomain>();

    DelayTblFactoryPM<Obj> pm;

    /**
     * Constructor required by the service-provider interface used
     * to find factories.
     */
    public DelayTableFactory() {
	this(null);
    }

    /**
     * Constructor.
     * @param sim the simulation
     */
    protected DelayTableFactory(DramaSimulation sim) {
	super(sim);
	pm = new DelayTblFactoryPM<Obj>(this);
	initParms(pm, DelayTableFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	pm.setDefaults(this);
    }

    @Override
    protected void initObject(Obj dt) {
	super.initObject(dt);
	for (HubDomain domain: domains) {
	    dt.addToDomain(domain);
	}
    }
}

//  LocalWords:  superclasses timeline traceSetMode traceSets
//  LocalWords:  TraceSet SimObject DelayTblFactoryPM
