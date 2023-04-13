package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.lang.Callable;
import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.math.rv.*;
import org.bzdev.util.units.MKS;

import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.LinkedHashMap;


/**
 * Abstract factory for BasicTripGenerator.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link BasicTripGenFactory}:
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/bikeshare/BasicTripGenFactory.html" style= "width:95%;height:500px;border:3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/bikeshare/BasicTripGenFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */

@FactoryParmManager(value = "BasicTripGenFactoryPM",
		    labelResourceBundle = "*.lpack.BasicTripGenLabels",
		    tipResourceBundle = "*.lpack.BasicTripGenTips",
		    stdFactory = "BasicTripGenFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbstrBasicTripGenFactory<Obj extends BasicTripGenerator>
    extends TripGeneratorFactory<Obj>
{

    @PrimitiveParm("startingHub")
    Hub startingHub = null;
    
    @PrimitiveParm(value="meanIATime",
		   lowerBound = "0.0",
		   lowerBoundClosed = false)
    double meanIATime = 0.0;

    @PrimitiveParm(value = "nBicycles",
		   lowerBound = "1",
		   lowerBoundClosed = true)
    int nbikes = 1;


    @CompoundParmType(labelResourceBundle = "*.lpack.DestInfoLabels",
		      tipResourceBundle = "*.lpack.DestInfoTips")
    static class DestInfo {
	@PrimitiveParm(value = "prob",
		       lowerBound = "0.0",
		       lowerBoundClosed = false)
	double prob = 0.0;

	@PrimitiveParm(value = "overflowProb",
		       lowerBound = "0.0",
		       lowerBoundClosed = true)
	double overflowProb = 0.0;
    }

    @KeyedCompoundParm("dest")
    Map<Hub,DestInfo> map = new LinkedHashMap<>();

    @CompoundParmType(tipResourceBundle = "*.lpack.TripGenTimelineTips",
		      labelResourceBundle = "*.lpack.TripGenTimelineLabels")
    static class TimelineEntry {
	@PrimitiveParm("meanIATime")
	Double meanIATime = null;
    };
    @KeyedCompoundParm("timeline")
    Map<Integer,TimelineEntry> timelineMap =
	new HashMap<Integer,TimelineEntry>();


    BasicTripGenFactoryPM<Obj> pm;

    /**
     * Constructor.
     * @param sim the simulation
     */
    protected AbstrBasicTripGenFactory(DramaSimulation sim) {
	super(sim);
	pm = new BasicTripGenFactoryPM<Obj>(this);
	initParms(pm, AbstrBasicTripGenFactory.class);
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
	if (entry.meanIATime != null) {
	    final Obj obj = object;
	    final double mean = entry.meanIATime;
	    addToTimelineResponse(new Callable() {
		    public void call() {
			obj.setMean(mean);
		    }
		});
	}
    }

    @Override
    protected void initObject(Obj tripGen) {
	super.initObject(tripGen);
	int len = map.size();
	Hub[] destHubs = new Hub[len];
	double[] weights = new double[len];
	double[] overflowProb = new double[len];
	int i = 0;
	for (Map.Entry<Hub,DestInfo> entry: map.entrySet()) {
	    destHubs[i] = entry.getKey();
	    DestInfo info = entry.getValue();
	    weights[i] = info.prob;
	    overflowProb[i] = info.overflowProb;
	    i++;
	}
	tripGen.init(startingHub, meanIATime, nbikes,
		     destHubs, weights, overflowProb);
    }
}
//  LocalWords:  BasicTripGenerator startingHub meanIATime nBicycles
//  LocalWords:  interarrival dest overflowProb probabilityFunction
//  LocalWords:  SimFunctionTwo superclasses domainMember boolean
//  LocalWords:  timeline traceSetMode traceSets TraceSet SimObject
//  LocalWords:  BasicTripGenFactoryPM
