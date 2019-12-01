package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.util.rv.*;
import org.bzdev.util.units.MKS;


/**
 * Abstract factory for instances of BasicHubBalander and any subclasses.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link BasicHubBalancerFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/bikeshare/BasicHubBalancerFactory.html" style= "width:95%;height:500px;border:3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/bikeshare/BasicHubBalancerFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */

@FactoryParmManager(value = "BasicHubBalancerFactoryPM",
		    labelResourceBundle = "*.lpack.BasicHubBalancerLabels",
		    tipResourceBundle = "*.lpack.BasicHubBalancerTips",
		    stdFactory = "BasicHubBalancerFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbstrBasicHubBalancerFactory<Obj extends BasicHubBalancer>
    extends HubBalancerFactory<Obj>
{

    @PrimitiveParm(value = "quietPeriod",
		   lowerBound = "0.0",
		   lowerBoundClosed = true)
    double quietPeriod = 0.0;

    @PrimitiveParm(value = "threshold",
		   lowerBound = "0.0",
		   lowerBoundClosed = true,
		   upperBound = "1.0",
		   upperBoundClosed = true)
    double threshold = 0.5;

    BasicHubBalancerFactoryPM<Obj> pm;

    protected AbstrBasicHubBalancerFactory(DramaSimulation sim) {
	super(sim);
	removeParm("domainMember");
	pm = new BasicHubBalancerFactoryPM<Obj>(this);
	initParms(pm, AbstrBasicHubBalancerFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	pm.setDefaults(this);
    }

    @Override
    protected void initObject(Obj hubBalancer) {
	super.initObject(hubBalancer);
	hubBalancer.setQuietPeriod(quietPeriod);
	hubBalancer.setThreshold(threshold);
    }
}
//  LocalWords:  BasicHubBalander quietPeriod BasicHubBalancer
//  LocalWords:  startAdditionalWorkers attemps balancer's sysDomain
//  LocalWords:  superclasses balancer boolean timeline traceSetMode
//  LocalWords:  traceSets TraceSet SimObject domainMember
//  LocalWords:  BasicHubBalancerFactoryPM
