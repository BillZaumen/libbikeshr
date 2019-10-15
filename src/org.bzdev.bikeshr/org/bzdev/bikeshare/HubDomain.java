package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.drama.common.*;
import java.io.PrintWriter;

/**
 * Base class for hub domains.
 * Hub domains are communication domains, and have an
 * associated instance of the class {@link DelayTable}
 * to determine travel times between hubs.
 */
public abstract class HubDomain extends Domain {

    DramaSimulation sim;
    DelayTable delayTable;
    DelayTable originalTable;

    /**
     * Get this object's communication domain type.
     * Concrete subclasses must override this method and ensure
     * that the method is idempotent. The value returned must not
     * be null.
     * <P>
     * This method is provided because the subclasses
     * {@link UsrDomain} and {@link SysDomain} use different
     * communication-domain types, while this class is expected
     * to be a communication domain.
     * @return this domain's communication-domain type
     */
    protected abstract CommDomainType fetchCommDomainType();

    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of this object
     * @param intern true if this object should be interned in the
     *        simulation's name table; false otherwise
     * @param priority the priority for this domain
     */
    protected HubDomain(DramaSimulation sim, String name, boolean intern,
			int priority) {
	this(sim, name, intern, null, priority);
    }
    /**
     * Constructor with a parent domain.
     * @param sim the simulation
     * @param name the name of this object
     * @param intern true if this object should be interned in the
     *        simulation's name table; false otherwise
     * @param parent the parent domain
     * @param priority the priority for this domain
     */
    protected HubDomain(DramaSimulation sim, String name, boolean intern,
			Domain parent, int priority) {
	super(sim, name, intern, parent, priority);
	this.sim = sim;
	originalTable = new StdDelayTable(sim, name + "_delayTable", false);
	delayTable = originalTable;
	configureAsCommunicationDomain(fetchCommDomainType());
    }

    /**
     * Set the delay table.
     * @param table the delay table; null to use a default table
     */
    void setDelayTable(DelayTable table) {
	delayTable = (table == null)? originalTable: table;
    }

    /**
     * Get the delay table.
     * Each hub domain contains a table providing delay information
     * for each hub. A factory for a hub domain will typically call
     * this method to configure delays.  Users not using factories
     * will have to call this method directly to configure delays
     * @return the delay table for this hub
     */
    public DelayTable getDelayTable() {
	return delayTable;
    }

    /**
     * Estimate the delay for travel between two hubs given a starting time.
     * @param time the simulation time at which to get the travel delay
     *        between any two hubs.
     * @param hub1 the starting hub
     * @param hub2 the ending hub
     * @param n the number of individuals traveling together
     * @return the delay in units of seconds
     */
    public double estimateDelay(double time, Hub hub1, Hub hub2, int n) {
	if (hub1 == hub2) return 0.0;
	return delayTable.estimateDelay(time, hub1, hub2, n);
    }

    /**
     * Estimate the delay for travel between two hubs.
     * The starting time is the current simulation time.
     * @param hub1 the starting hub
     * @param hub2 the ending hub
     * @param n the number of individuals traveling together
     * @return the delay in units of seconds
     */
    public double estimateDelay(Hub hub1, Hub hub2, int n) {
	if (hub1 == hub2) return 0.0;
	return delayTable.estimateDelay(sim.currentTime(), hub1, hub2, n);
    }


    /**
     * Get the delay for travel between two hubs given a starting time.
     * @param time the simulation time at which to get the travel delay
     *        between any two hubs.
     * @param hub1 the starting hub
     * @param hub2 the ending hub
     * @param n the number of individuals traveling together
     * @return the delay in units of seconds
     */
    public double getDelay(double time, Hub hub1, Hub hub2, int n) {
	if (hub1 == hub2) return 0.0;
	return delayTable.getDelay(time, hub1, hub2, n);
    }

    /**
     * Get the delay for travel between two hubs.
     * The starting time is the current simulation time.
     * @param hub1 the starting hub
     * @param hub2 the ending hub
     * @param n the number of individuals traveling together
     * @return the delay in units of seconds
     */
    public double getDelay(Hub hub1, Hub hub2, int n) {
	if (hub1 == hub2) return 0.0;
	return delayTable.getDelay(sim.currentTime(), hub1, hub2, n);
    }

    /**
     * Get a user domain's parent, provided it is an instance of
     * HubDomain.
     * @return the parent if it is an instance of ExtDomain; null
     *         otherwise
     */
    public HubDomain getParentHubDomain() {
	Domain parent = getParent();
	if (parent == null) {
	    return null;
	} else if (parent instanceof HubDomain) {
	    return (HubDomain) parent;
	} else {
	    return null;
	}
    }

     /**
     * Print the configuration for an instance of HubDomain.
     * The documentation for method
     * {@link org.bzdev.devqsim.SimObject#printConfiguration(String,String,boolean,java.io.PrintWriter)}
     * contains a description of how this method is used and how to
     * override it. The method
     * {@link org.bzdev.drama.Actor#printConfiguration(String,String,boolean,java.io.PrintWriter)}
     * describes the data that will be printed for the
     * superclass of this class. The data that will be printed
     * when this method is called are the following.
     * <P>
     * For class {@link HubDomain}:
     * <UL>
     *   <LI> the delay table for this domain.
     * </UL>
     * @param iPrefix the prefix to use for an initial line when printName is
     *        true with null treated as an empty string
     * @param prefix a prefix string (typically whitespace) to put at
     *        the start of each line other than the initial line that is
     *        printed when printName is true
     * @param printName requests printing the name of an object
     * @param out the output print writer
     */
    @Override
    public void printConfiguration(String iPrefix, String prefix,
				   boolean printName,
				   PrintWriter out)
    {
	super.printConfiguration(iPrefix, prefix, printName, out);
	out.println(prefix + "DelayTable: " + delayTable.getName());
    }
}

//  LocalWords:  DelayTable UsrDomain SysDomain sim delayTable
//  LocalWords:  HubDomain ExtDomain printConfiguration boolean
//  LocalWords:  superclass iPrefix printName whitespace
