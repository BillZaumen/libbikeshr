package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.util.rv.*;
import org.bzdev.util.units.MKS;


/**
 * Abstract factory for instances of HubWorker.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link HubWorkerFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/bikeshare/HubWorkerFactory.html" style= "width:95%;height:500px;border:3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/bikeshare/HubWorkerFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * <P>
 * This factory's superclass has a parameter named "domainMember" and
 * that parameter was removed.
 */

@FactoryParmManager(value = "AbstrHubWorkerFactoryPM",
		    labelResourceBundle = "*.lpack.HubWorkerLabels",
		    tipResourceBundle = "*.lpack.HubWorkerTips",
		    stdFactory = "HubWorkerFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbstrHubWorkerFactory<Obj extends HubWorker>
    extends AbstractActorFactory<Obj>
{

    @PrimitiveParm("sysDomain")
    SysDomain sysDomain = null;

    @PrimitiveParm("capacity")
    int capacity = 10;

    @PrimitiveParm("currentHub")
    Hub currentHub = null;
    
    @PrimitiveParm("storageHub")
    StorageHub shub = null;

    AbstrHubWorkerFactoryPM<Obj> pm;

    protected AbstrHubWorkerFactory(DramaSimulation sim) {
	super(sim);
	pm = new AbstrHubWorkerFactoryPM<Obj>(this);
	removeParm("domainMember");
	initParms(pm, AbstrHubWorkerFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	pm.setDefaults(this);
    }

    @Override
    protected void initObject(Obj worker) {
	super.initObject(worker);
	worker.init(capacity, shub, sysDomain,
		    ((currentHub == null)? shub: currentHub));
    }
}

//  LocalWords:  HubWorker sysDomain currentHub storageHub boolean
//  LocalWords:  superclasses timeline traceSetMode traceSets
//  LocalWords:  TraceSet SimObject superclass domainMember
//  LocalWords:  AbstrHubWorkerFactoryPM
