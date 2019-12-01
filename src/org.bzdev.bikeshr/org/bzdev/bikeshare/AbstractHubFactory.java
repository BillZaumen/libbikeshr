package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.obnaming.*;
import org.bzdev.obnaming.annotations.*;
import org.bzdev.util.rv.*;
import org.bzdev.util.units.MKS;


/**
 * Abstract factory for creating hubs.
 * <P>
 * The factory parameters this factory provides are the same as the parameters
 * provided by its subclass {@link HubFactory}:
 * <P>
 * <IFRAME SRC="{@docRoot}/factories-api/org/bzdev/bikeshare/HubFactory.html" style= "width:95%;height:500px;border:3px solid steelblue">
 * Please see
 *  <A HREF="{@docRoot}/factories-api/org/bzdev/bikeshare/HubFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 * <P>
 * This factory's superclass has a parameter named "domainMember" and
 * that parameter was removed.
 */
@FactoryParmManager(value = "AbstrHubFactoryPM",
		    labelResourceBundle = "*.lpack.HubLabels",
		    tipResourceBundle = "*.lpack.HubTips",
		    stdFactory = "HubFactory",
		    namerVariable = "sim",
		    namerDocumentation = "the simulation")
public abstract class AbstractHubFactory<Obj extends Hub>
    extends AbstractActorFactory<Obj>
{

    DramaSimulation sim;

    @PrimitiveParm("usrDomain")
    UsrDomain usrDomain = null;

    protected UsrDomain getUsrDomain() {return usrDomain;}

    @PrimitiveParm("sysDomain")
    SysDomain sysDomain = null;

    protected SysDomain getSysDomain() {return sysDomain;}

    private static final int DEFAULT_CAPACITY = 20;
    private static final int DEFAULT_LOWER_TRIGGER = 5;
    private static final int DEFAULT_NOMINAL = 10;
    private static final int DEFAULT_UPPER_TRIGGER = 15;

    @PrimitiveParm(value="capacity", lowerBound="0", lowerBoundClosed=true)
    int capacity = -1;

    /**
     * Get the value of the "capacity" parameter.
     * @return the value of the "capacity" parameter; -1 if a default
     *         value is to be used instead
     */
    protected int getCapacity() {return capacity;}

    @PrimitiveParm(value="nominal", lowerBound="0", lowerBoundClosed=true)
    int nominal = -1;
    
    /**
     * Get the value of the "nominal" parameter.
     * @return the value of the "nominal" parameter; -1 if a default
     *         value is to be used instead
     */
    protected int getNominal() {return nominal;}

    @PrimitiveParm(value="lowerTrigger", lowerBound="0", lowerBoundClosed=true)
    int lowerTrigger = -1;

    /**
     * Get the value of the "lowerTrigger" parameter.
     * @return the value of the "lowerTrigger" parameter; -1 if a default
     *         value is to be used instead
     */
    protected int getLowerTrigger() {return lowerTrigger;}

    @PrimitiveParm(value="upperTrigger", lowerBound="0", lowerBoundClosed=true)
    int upperTrigger = -1;

    /**
     * Get the value of the "upperTrigger" parameter.
     * @return the value of the "upperTrigger" parameter; -1 if a default
     *         value is to be used instead
     */
    protected int getUpperTrigger() {return upperTrigger;}

    @PrimitiveParm(value="count", lowerBound="0", lowerBoundClosed=true)
    int count = 10;

    /**
     * Get the count parameter.
     * This is used by AbstrStorageHubFactory.
     * @return the value of the "count" parameter;  -1 if a default
     *         value is to be used instead
     */
    protected int getCount() {return count;}

    @PrimitiveParm(value="overCount", lowerBound="0", lowerBoundClosed=true)
    int overCount = 0;

    /**
     * Get the overCount parameter.
     * @return the value of the "overCount" parameter.
     */
    protected int getOverCount() {return overCount;}

    @PrimitiveParm(value="pickupTime",
		   lowerBound="0.0")
    DoubleRandomVariable pickupTime =
	new GaussianRV(MKS.minutes(5.0), 30.0);

    /**
     * Get the pickupTime parameter.
     * @return the value of the "pickupTime" parameter
     */
    protected DoubleRandomVariable getPickupTime() {return pickupTime;}


    @PrimitiveParm("x")
    double x = 0.0;

    /**
     * Get the X value.
     * @return the value of the "x" parameter
     */
    protected double getX() {return x;}

    @PrimitiveParm("y")
    double y = 0.0;

    /**
     * Get the Y value.
     * @return the value of the "y" parameter
     */
    protected double getY() {return y;}


    AbstrHubFactoryPM<Obj> pm;

    protected AbstractHubFactory(DramaSimulation sim) {
	super(sim);
	this.sim = sim;
	removeParm("domainMember");
	pm = new AbstrHubFactoryPM<Obj>(this);
	initParms(pm, AbstractHubFactory.class);
    }

    @Override
    public void clear() {
	super.clear();
	pm.setDefaults(this);
    }

    @Override
    protected void initObject(Obj hub) {
	super.initObject(hub);
	int cap = (capacity == -1)? DEFAULT_CAPACITY: capacity;
	int lt = (lowerTrigger == -1)? DEFAULT_LOWER_TRIGGER: lowerTrigger;
	int nom = (nominal == -1)? DEFAULT_NOMINAL: nominal;
	int ut = (upperTrigger == -1)? DEFAULT_UPPER_TRIGGER: upperTrigger;
	int cnt = (count == -1)? nom: count;
	hub.init(cap, lt, nom, ut, pickupTime, cnt, overCount,
		 x, y, usrDomain, sysDomain);
    }
}

//  LocalWords:  usrDomain sysDomain upperTrigger lowerTrigger
//  LocalWords:  overCount pickupTime superclasses boolean timeline
//  LocalWords:  traceSetMode traceSets TraceSet SimObject superclass
//  LocalWords:  domainMember AbstrHubFactoryPM
//  LocalWords:  AbstrStorageHubFactory
