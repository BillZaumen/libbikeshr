package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.lang.Callable;
import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.util.rv.*;
import org.bzdev.util.units.MKS;

import java.util.Map;
import java.util.Map.Entry;
import java.util.HashMap;
import java.util.LinkedHashMap;


/**
 * Abstract factory for BurstTripGenerator.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link BurstTripGenFactory}:
 * <P>
 * <IFRAME SRC="../../../factories-api/org/bzdev/bikeshare/BurstTripGenFactory.html" width="95%" height="600">
 * Please see
 *  <A HREF="../../../factories-api/org/bzdev/bikeshare/BurstTripGenFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */

@FactoryParmManager(value = "BurstTripGenFactoryPM",
		    labelResourceBundle = "*.lpack.BurstTripGenLabels",
		    tipResourceBundle = "*.lpack.BurstTripGenTips")
public abstract class AbstrBurstTripGenFactory<Obj extends BurstTripGenerator>
    extends TripGeneratorFactory<Obj>
{
    @PrimitiveParm("centralHub")
    Hub centralHub = null;
    
    @PrimitiveParm(value="burstTime",
		   lowerBound = "0.0",
		   lowerBoundClosed = false)
    double burstTime = 0.0;

    @PrimitiveParm(value = "burstSize",
		   rvmode = true,
		   lowerBound = "0",
		   lowerBoundClosed = true)
    IntegerRandomVariable burstSize = null;

    @PrimitiveParm(value="estimationCount",
		   lowerBound="0",
		   lowerBoundClosed=true)
    int estimationCount = 25;

    @PrimitiveParm(value="estimationFactor",
		   lowerBound="0.0",
		   lowerBoundClosed=false)
    double estimationFactor = 1.0;
    
    @PrimitiveParm(value="estimationOffset",
		   lowerBound="0.0",
		   lowerBoundClosed=true)
    double estimationOffset = 0.0;


    @PrimitiveParm("fanIn")
    boolean fanIn = false;

    @CompoundParmType(labelResourceBundle = "*.lpack.OtherInfoLabels",
		      tipResourceBundle = "*.lpack.OtherInfoTips")
    static class OtherInfo {
	@PrimitiveParm(value = "prob",
		       lowerBound = "0.0",
		       lowerBoundClosed = false)
	double prob = 0.0;

	@PrimitiveParm(value = "overflowProb",
		       lowerBound = "0.0",
		       lowerBoundClosed = true)
	double overflowProb = 0.0;
    }

    @KeyedCompoundParm("other")
    Map<Hub,OtherInfo> map = new LinkedHashMap<>();

    @CompoundParmType(tipResourceBundle = "*.lpack.TripGenTimelineTips",
		      labelResourceBundle = "*.lpack.TripGenTimelineLabels")
    static class TimelineEntry {
	@PrimitiveParm("meanIATime")
	Double meanIATime = null;
    };
    @KeyedCompoundParm("timeline")
    Map<Integer,TimelineEntry> timelineMap =
	new HashMap<Integer,TimelineEntry>();


    BurstTripGenFactoryPM<Obj> pm;

    protected AbstrBurstTripGenFactory(DramaSimulation sim) {
	super(sim);
	pm = new BurstTripGenFactoryPM<Obj>(this);
	initParms(pm, AbstrBurstTripGenFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	pm.setDefaults(this);
    }

    @Override
    protected void initObject(Obj object) {
	super.initObject(object);
	int len = map.size();
	Hub[] otherHubs = new Hub[len];
	double[] weights = new double[len];
	double[] overflowProb = new double[len];
	int i = 0;
	for (Map.Entry<Hub,OtherInfo> entry: map.entrySet()) {
	    otherHubs[i] = entry.getKey();
	    OtherInfo info = entry.getValue();
	    weights[i] = info.prob;
	    overflowProb[i] = info.overflowProb;
	    i++;
	}
	int nbikes = burstSize.next();
	object.init(centralHub, burstTime, nbikes,
		    otherHubs, weights, overflowProb,
		    fanIn);
	if (fanIn) {
	    object.setFanInParameters(estimationCount,
				      estimationFactor,
				      estimationOffset);
	}
    }
}