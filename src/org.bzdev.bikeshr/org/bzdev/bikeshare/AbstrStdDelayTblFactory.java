package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.util.rv.*;
import org.bzdev.util.units.MKS;

import java.util.TreeMap;
import java.util.Map;
import java.util.Set;
import java.util.LinkedHashSet;

/**
 * Abstract factory for delay tables used for determining the time for
 * unscheduled trips between hubs.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link StdDelayTableFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/bikeshare/StdDelayTableFactory.html" style= "width:95%;height:500px;border:3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/bikeshare/StdDelayTableFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
@FactoryParmManager(value = "AbstrStdDelayTblFactoryPM",
		    labelResourceBundle = "*.lpack.StdDelayTableLabels",
		    tipResourceBundle = "*.lpack.StdDelayTableTips",
		    stdFactory = "StdDelayTableFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbstrStdDelayTblFactory<Obj extends StdDelayTable>
    extends DelayTableFactory<Obj>
{

    static String errorMsg(String key, Object... args) {
	return BikeShare.errorMsg(key, args);
    }

    DramaSimulation sim;

    @PrimitiveParm(value = "speedRV",
		   lowerBound = "0.0",
		   lowerBoundClosed = true)
    DoubleRandomVariable speedRV = null;

    @PrimitiveParm(value = "dist",
		   lowerBound = "0.0",
		   lowerBoundClosed = true)
    double dist = 0.0;

    @PrimitiveParm(value = "nStops",
		   lowerBound = "0",
		   lowerBoundClosed = true)
    int stops = 0;

    @PrimitiveParm(value = "stopProbability",
		   lowerBound = "0.0",
		   lowerBoundClosed = true,
		   upperBound = "1.0",
		   upperBoundClosed = true)
    double stopProbability = 0.0;

    @PrimitiveParm(value = "maxWait",
		   lowerBound = "0.0",
		   lowerBoundClosed = true)
    double maxWait = 0.0;

    @PrimitiveParm(value = "distFraction",
		   lowerBound = "0.0",
		   lowerBoundClosed = true,
		   upperBound = "1.0",
		   upperBoundClosed = true)
    double distFraction = 0.5;

    @CompoundParmType(labelResourceBundle = "*.lpack.DTEntryLabels",
		      tipResourceBundle = "*.lpack.DTEntryTips")
    static class Entry {
	@PrimitiveParm("origin")
	Hub src = null;

	@PrimitiveParm("dest")
	Hub dest = null;

	@PrimitiveParm(value="distance",
		       lowerBound = "0.0",
		       lowerBoundClosed = true)
	double dist = 0.0;

	@PrimitiveParm(value="stops",
		       lowerBound = "0",
		       lowerBoundClosed = true)
	int stops = 0;

	@PrimitiveParm(value="stopProbability",
		       lowerBound = "0.0",
		       lowerBoundClosed = true,
		       upperBound = "1.0",
		       upperBoundClosed = true)
	double stopProbability = 0.0;

	@PrimitiveParm(value="maxWait",
		       lowerBound = "0",
		       lowerBoundClosed = true)
	double maxWait = 0.0;
    }

    @KeyedCompoundParm("entry")
    TreeMap<Integer,Entry> entries = new TreeMap<Integer,Entry>();


    AbstrStdDelayTblFactoryPM<Obj> pm;

    protected AbstrStdDelayTblFactory(DramaSimulation sim) {
	super(sim);
	this.sim = sim;
	pm = new AbstrStdDelayTblFactoryPM<Obj>(this);
	initParms(pm, AbstrStdDelayTblFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	pm.setDefaults(this);
    }

    @Override
    protected void initObject(Obj dt) {
	super.initObject(dt);
	if (speedRV == null) {
	    throw new IllegalStateException(errorMsg("speedRV"));
	}
	dt.init(speedRV, dist, stops, stopProbability, maxWait);
	dt.setDistFraction(distFraction);
	for (Entry entry: entries.values()) {
	    dt.addEntry(entry.src, entry.dest,
			entry.dist, entry.stops,
			entry.stopProbability,
			entry.maxWait);
	}
    }
}

//  LocalWords:  speedRV DoubleRandomVariable nStops stopProbablity
//  LocalWords:  maxWait distFraction fd dest stopProbability
//  LocalWords:  superclasses timeline traceSetMode traceSets
//  LocalWords:  TraceSet SimObject AbstrStdDelayTblFactoryPM
