package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.obnaming.annotations.*;

import java.util.Set;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.LinkedHashMap;

/**
 * Abstract factory class for a storage hub.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link StorageHubFactory}:
 * <P>
 * <IFRAME SRC="../../../factories-api/org/bzdev/bikeshare/StorageHubFactory.html" width="95%" height="600">
 * Please see
 *  <A HREF="../../../factories-api/org/bzdev/bikeshare/StorageHubFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */

@FactoryParmManager(value = "StorageHubFactoryPM",
		    labelResourceBundle = "*.lpack.StorageHubLabels",
		    tipResourceBundle = "*.lpack.StorageHubTips",
		    stdFactory = "StorageHubFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbstrStorageHubFactory<Obj extends StorageHub>
    extends AbstractHubFactory<Obj>
{

    @PrimitiveParm(value="nworkersNoPickup",
		   lowerBound = "0",
		   lowerBoundClosed=true)
    int nworkersNP = 0;

    @PrimitiveParm(value="nworkersWithPickup",
		   lowerBound = "0",
		   lowerBoundClosed=true)
    int nworkersWP = 0;

    @PrimitiveParm(value="nworkersFixOnly",
		   lowerBound = "0",
		   lowerBoundClosed=true)
    int nworkersFO = 0;

    @PrimitiveParm(value="intervalNoPickup",
		   lowerBound = "0.0",
		   lowerBoundClosed=true)
    double intervalNP = 0.0;

    @PrimitiveParm(value="intervalWithPickup",
		   lowerBound = "0.0",
		   lowerBoundClosed=true)
    double intervalWP = 0.0;

    @PrimitiveParm(value="intervalFixOnly",
		   lowerBound = "0.0",
		   lowerBoundClosed=true)
    double intervalFO = 0.0;

    @CompoundParmType(labelResourceBundle = "*.lpack.StorageHubTableLabels",
		      tipResourceBundle = "*.lpack.StorageHubTableTips")
    static class HubTableEntry {
	@PrimitiveParm("mode")
	HubWorker.Mode mode = null;

	@PrimitiveParm ("hub")
	Hub hub;
    }

    @KeyedCompoundParm("hubTable")
    LinkedHashMap<Integer,HubTableEntry> hubTable = new LinkedHashMap<>();

    // These parameters for a hub are not used when the hub is a
    // storage hub.
    static String removedParms[] = {
	"capacity",
	"pickupTime",
	"overCount",
	"usrDomain"
    };

    StorageHubFactoryPM<Obj> pm;    

    /**
     * Constructor.
     * @param sim the simulation
     */
    public AbstrStorageHubFactory(DramaSimulation sim) {
	super(sim);
	removeParms(removedParms);
	pm = new StorageHubFactoryPM<Obj>(this);
	initParms(pm, AbstrStorageHubFactory.class);
    }

    @Override
    protected void initObject(Obj hub) {
	super.initObject(hub);

	hub.init(getLowerTrigger(), getNominal(), getUpperTrigger(),
		 getCount(), getX(), getY(), getSysDomain());
	hub.setInitialNumberOfWorkers(nworkersNP, nworkersWP, nworkersFO,
				      intervalNP,
				      intervalWP,
				      intervalFO);
	for (Map.Entry<Integer,HubTableEntry> entry: hubTable.entrySet()) {
	    HubTableEntry row = entry.getValue();
	    hub.addHub(row.mode, row.hub);
	}
    }
}

//  LocalWords:  hubTable HubWorker superclasses upperTrigger boolean
//  LocalWords:  lowerTrigger sysDomain timeline traceSetMode sim
//  LocalWords:  traceSets TraceSet SimObject superclass domainMember
//  LocalWords:  pickupTime overCount usrDomain StorageHubFactoryPM
//  LocalWords:  nworkersNoPickup nworkersWithPickup nworkersFixOnly
//  LocalWords:  intervalNoPickup intervalWithPickup intervalFixOnly
