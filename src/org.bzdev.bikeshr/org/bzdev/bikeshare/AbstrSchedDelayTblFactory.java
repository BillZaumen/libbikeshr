package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.math.rv.*;
import org.bzdev.util.units.MKS;

import java.util.TreeMap;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 * Abstract factory for delay tables used for determining the time for
 * scheduled trips between hubs.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link SchedDelayTableFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/bikeshare/SchedDelayTableFactory.html" style= "width:95%;height:500px;border:3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/bikeshare/SchedDelayTableFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
@FactoryParmManager(value = "AbstrSchedDelayTblFactoryPM",
		    labelResourceBundle = "*.lpack.SchedDelayTableLabels",
		    tipResourceBundle = "*.lpack.SchedDelayTableTips",
		    stdFactory = "SchedDelayTableFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbstrSchedDelayTblFactory<Obj extends SchedDelayTable>
    extends DelayTableFactory<Obj>
{

    static String errorMsg(String key, Object... args) {
	return BikeShare.errorMsg(key, args);
    }

    DramaSimulation sim;

    @CompoundParmType(labelResourceBundle = "*.lpack.SchedEntryLabels",
		      tipResourceBundle = "*.lpack.SchedEntryTips")
    static class Entry {
	@PrimitiveParm("origin")
	Hub src = null;

	@PrimitiveParm("dest")
	Hub dest = null;
	
	@PrimitiveParm(value = "initialTime",
		       lowerBound = "0.0", lowerBoundClosed = true)
	double startingTime = 0.0;

	@PrimitiveParm(value = "duration",
		       lowerBound = "0.0", lowerBoundClosed = false)
	double duration = 0.0;

	@PrimitiveParm(value = "cutoffTime",
		       lowerBound = "0.0", lowerBoundClosed = false)
	double cutoffTime = 0.0;

	@PrimitiveParm(value = "period",
		       lowerBound = "0.0", lowerBoundClosed = false)
	double period = 0.0;
    }

    @KeyedCompoundParm("entry")
    TreeMap<Integer,Entry> entries = new TreeMap<Integer,Entry>();


    AbstrSchedDelayTblFactoryPM<Obj> pm;

    protected AbstrSchedDelayTblFactory(DramaSimulation sim) {
	super(sim);
	this.sim = sim;
	pm = new AbstrSchedDelayTblFactoryPM<Obj>(this);
	initParms(pm, AbstrSchedDelayTblFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	pm.setDefaults(this);
    }

    @Override
    protected void initObject(Obj dt) {
	super.initObject(dt);
	for (Entry entry: entries.values()) {
	    if (entry.period == 0.0) {
		dt.addEntry(entry.src, entry.dest,
			    entry.startingTime,
			    entry.startingTime + entry.duration);
	    } else {
		dt.addEntry(entry.src, entry.dest,
			    entry.startingTime, entry.cutoffTime,
			    entry.period, entry.duration);
	    }
	}
    }
}

//  LocalWords:  dest initialTime cutoffTime ge le superclasses
//  LocalWords:  timeline traceSetMode traceSets
//  LocalWords:  TraceSet SimObject AbstrSchedDelayTblFactoryPM
