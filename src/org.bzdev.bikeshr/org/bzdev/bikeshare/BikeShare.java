package org.bzdev.bikeshare;
import java.util.ResourceBundle;
import org.bzdev.util.SafeFormatter;
import org.bzdev.util.units.MKS;

/**
 * Package configuration class.
 * This class is a global configuration class that sets
 * numerical values for trace levels (the default is for
 * no tracing), and the domain priorities for the user
 * and system hub domains (the defaults are 0 and 1 respectively).
 * The priority for a user domain must be lower than that for
 * a system domain. It also sets the minimum speed used by
 * the {@link StdDelayTable} class.
 * <P>
 * To enable tracing, set the levels of interest to non-zero
 * values (normally one should set all of them, with level1 &lt;
 * level2 &lt; level3 &lt; level4.  The default domain priorities
 * should be set to other values only if other communication
 * domains are defined (e.g., if this simulation library is used
 * in conjunction with other libraries).
 */
public class BikeShare {

    private static ResourceBundle exbundle =
	ResourceBundle.getBundle("org.bzdev.bikeshare.lpack.BikeShare");

    static String errorMsg(String key, Object... args) {
	return (new SafeFormatter()).format(exbundle.getString(key), args)
	    .toString();
    }


    static int level1 = -1; // configuration and initialization.
                            // starting & stopping trip generators.
    static int level2 = -1; // starting, stopping, and queuing workers.
    static int level3 = -1; // worker actions at hubs.
    static int level4 = -1; // Bicycle traffic between hubs.

    /**
     * Set trace levels explicitly.
     * A level of -1 indicates that nothing will be displayed. Otherwise
     * the level must be a non-negative integer.
     * For the models in the org.bzdev.bikeshare package, the levels are
     * used as follows:
     * <UL>
     *   <LI> For level1, the traced operations are the configuration of
     *        trip generators, starting and stopping of trip generators,
     *        and a hub balancer starting workers that run persistently.
     *   <LI> For level2, the traced operations are 
     *        <UL>
     *           <LI> a BasicHubBalancer starting additional workers,
     *                noting when these workers cannot be scheduled
     *                and when a worker started successfully,
     *           <LI> a StorageHub queuing a worker, unqueuing a worker,
     *                or delaying unqueuing until a worker becomes
     *                available.
     *       </UL>
     *   <LI> For level3, the operations of a HubWorker at each hub
     *        are traced.
     *   <LI> For level4, the bicycle trips are traced.
     * </UL>
     * Normally level1 &lt; level2 &lt; level3 &lt; level4. The initial
     * value for each of these levels is -1, indicating that nothing should
     * be shown.
     * @param level1 the first trace level
     * @param level2 the second trace level
     * @param level3 the third trace level
     * @param level4 the fourth trace level
     * @exception IllegalArgumentException an argument was smaller than -1
     */
    public static void setTraceLevels(int level1, int level2, int level3,
				      int level4) 
	throws IllegalArgumentException
    {
	if (level1 < -1 || level2 < -1 || level3 < -1 || level4 < -1)
	    throw new IllegalArgumentException(errorMsg("traceLevel"));
	BikeShare.level1 = level1;
	BikeShare.level2 = level2;
	BikeShare.level3 = level3;
	BikeShare.level4 = level4;
    }

    /**
     * Set trace levels using enumerations.
     * A level set to null indicates that nothing will be displayed. Otherwise
     * the level must be an enumeration constant, with the constant's ordinal
     * value providing the level.
     * For the models in the org.bzdev.bikeshare package, the levels are
     * used as follows:
     * <UL>
     *   <LI> For level1, the traced operations are the configuration of
     *        trip generators, starting and stopping of trip generators,
     *        and a hub balancer starting workers that run persistently.
     *   <LI> For level2, the traced operations are 
     *        <UL>
     *           <LI> a BasicHubBalancer starting additional workers,
     *                noting when these workers cannot be scheduled
     *                and when a worker started successfully,
     *           <LI> a StorageHub queuing a worker, unqueuing a worker,
     *                or delaying unqueuing until a worker becomes
     *                available.
     *       </UL>
     *   <LI> For level3, the operations of a HubWorker at each hub
     *        are traced.
     *   <LI> For level4, the bicycle trips are traced.
     * </UL>
     * Normally level1 &lt; level2 &lt; level3 &lt; level4 where the
     * comparison uses the enumerations' ordinal values.
     * @param <T> the enumeration type used to name trace levels
     * @param level1 the first trace level
     * @param level2 the second trace level
     * @param level3 the third trace level
     * @param level4 the fourth trace level
     */
    public static
	<T extends Enum<T>> void setTraceLevels(T level1, T level2,
						T level3, T level4)
    {
	BikeShare.level1 = (level1 == null)? -1: level1.ordinal();
	BikeShare.level2 = (level2 == null)? -1: level2.ordinal();
	BikeShare.level3 = (level3 == null)? -1: level3.ordinal();
	BikeShare.level4 = (level4 == null)? -1: level4.ordinal();
    }

    private static final int DEFAULT_USR_DOMAIN_PRIORITY = 0;
    private static final int DEFAULT_EXT_DOMAIN_PRIORITY = 1;
    private static final int DEFAULT_SYS_DOMAIN_PRIORITY = 2;

    static int usrDomainPriority = DEFAULT_USR_DOMAIN_PRIORITY;
    static int extDomainPriority = DEFAULT_EXT_DOMAIN_PRIORITY;
    static int sysDomainPriority = DEFAULT_SYS_DOMAIN_PRIORITY;

    /**
     * Set domain priorities for hub domains.
     * The values set will be used by constructors and the priorities
     * of existing hub domains will not be changed when this method is
     * used.  It is intended for cases where multiple models are integrated
     * to interoperate and 
     * @param usrPriority the priority for  UsrDomain
     * @param extPriority the priority for ExtDomain
     * @param sysPriority the priority for SysDomain
     * @exception IllegalArgumentException the usrPriority argument was
     * not lower than the schedPriority argument, which should in turn
     * be lower than the sysPriority argument.
     */
    public static void setPriorities(Integer usrPriority,
				     Integer extPriority,
				     Integer sysPriority)
	throws IllegalArgumentException
    {
	if (usrPriority >= extPriority || extPriority >= sysPriority ) {
	    String msg =
	      errorMsg("domainPriority", usrPriority, extPriority, sysPriority);
	    throw new IllegalArgumentException(msg);
	}
	usrDomainPriority = (usrPriority == null)?
	    DEFAULT_USR_DOMAIN_PRIORITY: usrPriority;
	extDomainPriority = (extPriority == null)?
	    DEFAULT_EXT_DOMAIN_PRIORITY: extPriority;
	sysDomainPriority = (sysPriority == null)?
	    DEFAULT_SYS_DOMAIN_PRIORITY: sysPriority;
    }

    static final double DEFAULT_MINSTDDELAYTABLESPEED = MKS.mph(3.0);

    static double minStdDelayTableSpeed = DEFAULT_MINSTDDELAYTABLESPEED;

    /**
     * Set the minimum speed for the StdDelayTable class.
     * @param value the speed in units of meters/second; 0.0 or
     *        a negative value to restore the default
     */
    public static void setMinStdDelayTableSpeed(double value) {
	if (value <= 0.0) {
	    minStdDelayTableSpeed = DEFAULT_MINSTDDELAYTABLESPEED;
	} else {
	    minStdDelayTableSpeed = value;
	}
    }

}

//  LocalWords:  lt balancer BasicHubBalancer StorageHub unqueuing
//  LocalWords:  HubWorker IllegalArgumentException traceLevel
//  LocalWords:  constant's interoperate usrPriority UsrDomain
//  LocalWords:  extPriority ExtDomain sysPriority SysDomain
//  LocalWords:  schedPriority domainPriority StdDelayTable
