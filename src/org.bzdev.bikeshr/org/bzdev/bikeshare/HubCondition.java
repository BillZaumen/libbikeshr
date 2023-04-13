package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.io.PrintWriter;

/**
 * Condition class for tracking the status of a set of hubs
 * A hub condition keeps track of sets of hubs that
 * <UL>
 *   <LI> have more bicycles at a hub's preferred location
 *        than the value of the hub's upper trigger.
 *   <LI> have fewer bicycles at a hub's preferred location
 *        than the value of the hub's lower trigger.
 *   <LI> have bicycles in a hub's overflow area.
 *   <LI> have no more and no fewer bicycles at a hub's
 *        preferred location than the values of the hub's
 *        upper and lower triggers respectively.
 * </UL>
 * A hub condition also keeps track of the last hub that
 * changed its status (its membership in the sets listed
 * above).
 * <P>
 * Normally a bub condition does not have to be created explicitly as
 * a system domain will automatically create a condition and arrange
 * for it to be configured.
 */
public class HubCondition extends Condition {

    private Set<Hub> overSet = new HashSet<>();
    private Set<Hub> overflowSet = new HashSet<>();
    private Set<Hub> underSet = new HashSet<>();
    private Set<Hub> inRangeSet = new HashSet<>();
    private Hub changedHub = null;
    private boolean inOverSet = false;
    private boolean inOverflowSet = false;
    private boolean inUnderSet = false;
    
    /**
     * Get the last hub that was changed.
     * @return the last hub that was changed
     */
    public Hub getChangedHub() {return changedHub;}

    /**
     * Determine if the last hub that was changed has more bicycles
     * stored at the hub's preferred location than the upper trigger value
     * @return true if the last hub that was changed has more bicycles
     *         stored at this hub's preferred location than the upper
     *         trigger value; false otherwise
     */
    public boolean getInOverSet() {return inOverSet;}

    /**
     * Determine if the last hub that was changed has bicycles in its
     * overflow area.
     * @return true if the last hub that was changed has bicycles in its
     *         overflow area; false otherwise
     */
    public boolean getInOverflowSet() {return inOverflowSet;}

    /**
     * Determine if the last hub that was changed has fewer bicycles at
     * the hub's preferred location than the lower trigger value.
     * @return true if the last hub that was changed has fewer bicycles at
     *         the hub's preferred location than the lower trigger value;
     *         false otherwise
     */
    public boolean getInUnderSet() {return inUnderSet;}

    /**
     * Get the set containing the hubs whose bicycle count at the
     * preferred location exceeds the value of the upper trigger.
     * @return the set containing the hubs whose bicycle count at the
     *         preferred location exceeds the value of the upper
     *         trigger
     */
    public Set<Hub> getOverSet() {
	return Collections.unmodifiableSet(overSet);
    }

    /**
     * Get the set containing the hubs whose bicycle count in the
     * overflow area is nonzero.
     * @return the set containing the hubs whose bicycle count in the
     *         overflow area is nonzero
     */
    public Set<Hub> getOverflowSet() {
	return Collections.unmodifiableSet(overflowSet);
    }

    /**
     * Get the set containing the hubs whose bicycle count at the
     * preferred location is below the value of the lower trigger.
     * @return the set containing the hubs whose bicycle count at the
     *         preferred location is below the value of the lower
     *         trigger
     */
    public Set<Hub> getUnderSet() {
	return Collections.unmodifiableSet(underSet);
    }

    /**
     * Get the set of hubs whose bicycle count at the preferred location
     * is between or at the lower and upper triggers.
     * @return the set of hubs whose bicycle count at the preferred
     *         location is between or at the lower and upper triggers
     */
    public Set<Hub> getInRangeSet() {
	return Collections.unmodifiableSet(inRangeSet);
    }

    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of this object
     * @param intern true if this object should be interned in the
     *        simulation's name table; false otherwise
     */
    HubCondition(DramaSimulation sim, String name, boolean intern) {
	super(sim, name, intern);
    }

    /**
     * Notify a HubCondition of a change.
     * @param hub the latest hub that changed its counts
     * @param nb the number of bicycles needed to get the bicycle
     *        count at the preferred location in range
     * @param eb the number of bicycles that should be removed from
     *        the preferred location to get the bicycle count at the
     *        preferred location in range
     * @param ofb the number of bicycles in the overflow region
     */
    protected void hubChanged(Hub hub, int nb, int eb, int ofb) {
	if (hub instanceof StorageHub) {
	    // should not happen - added just in case.
	    return;
	}
	boolean changed;
	if (nb > 0) {
	    changed = !underSet.contains(hub);
	    if (changed) {
		underSet.add(hub);
	    }
	    inUnderSet = true;
	} else {
	    changed = underSet.contains(hub);
	    underSet.remove(hub);
	    inUnderSet = false;
	}
	
	if (eb > 0) {
	    boolean xchanged = !overSet.contains(hub);
	    changed = changed || xchanged;
	    if (xchanged) {
		overSet.add(hub);
	    }
	    inOverSet = true;
	} else {
	    changed = changed || overSet.contains(hub);
	    overSet.remove(hub);
	    inOverSet = false;
	}

	if (ofb > 0) {
	    boolean xchanged = !overflowSet.contains(hub);
	    changed |= changed || xchanged;
	    if (xchanged) {
		overflowSet.add(hub);
	    }
	    inOverflowSet = true;
	} else {
	    changed = changed || overflowSet.contains(hub);
	    overflowSet.remove(hub);
	    inOverflowSet = false;
	}

	if (eb == 0 && nb == 0) {
	    changed = changed || !inRangeSet.contains(hub);
	}
	if (changed) {
	    if (!inOverSet && !inUnderSet) {
		inRangeSet.add(hub);
	    }
	    changedHub = hub;
	    notifyObservers();
	    completeNotification();
	    changedHub = null;
	}
    }

     /**
     * Print the state for an instance of HubCondition.
     * The documentation for method
     * {@link org.bzdev.devqsim.SimObject#printState(String,String,boolean,java.io.PrintWriter)}
     * contains a description of how this method is used and how to
     * override it. The method
     * {@link org.bzdev.drama.Actor#printState(String,String,boolean,java.io.PrintWriter)}
     * describes the data that will be printed for the
     * superclass of this class. The data that will be printed
     * when this method is called are the following.
     * <P>
     * For class {@link HubCondition}:
     * <UL>
     *   <LI><B>last changed hub</B>. The last hub that was changed.
     *   <LI><B>inOverSet</B>. True if the last hub that was changed
     *       has more bicycles stored at the hub's preferred location than
     *       the upper-trigger value.
     *   <LI><B>inOverflowSet</B>. True if the last hub that was changed
     *       has bicycles in its overflow area.
     *   <LI><B>inUnderSet</B>. True if the last hub that was changed
     *       has fewer bicycles at the hub's preferred location than the
     *       lower trigger value.
     *   <LI><B>overSet</B>. Get the set of hubs for which the number of
     *       bicycles at a hub's preferred location  exceed the hub's
     *       upper trigger value.
     *   <LI><B>overflowSet</B>. Get the set of hubs for which the number
     *       of bicycles in the overflow area is nonzero.
     *   <LI><B>underSet</B>. Get the set of hubs for which the number
     *       of bicycles at a hub's preferred location is less than the
     *       lower trigger value.
     *   <LI><B>inRangeSet</B>. Get the set of hubs for which the number
     *       of bicycles at a hub's preferred location is between or at
     *       the lower and upper triggers.
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
    public void printState(String iPrefix, String prefix,
			   boolean printName, PrintWriter out)
    {
	super.printState(iPrefix, prefix, printName, out);
	if (changedHub != null) {
	    out.println(prefix + "last changed hub (" + changedHub.getName()
			+ "):");
	    out.println(prefix + "    inOverSet: " + inOverSet);
	    out.println(prefix + "    inOverflowSet: " + inOverflowSet);
	    out.println(prefix + "    inUnderSet: " + inUnderSet);
	}
	if (overSet.isEmpty()) {
	    out.println(prefix + "overSet: <empty>");
	} else {
	    out.println(prefix + "overSet:");
	    for (Hub hub: overSet) {
		out.println(prefix + "    " + hub.getName());
	    }
	}
	if (overflowSet.isEmpty()) {
	    out.println(prefix + "overflowSet: <empty>");
	} else {
	    out.println(prefix + "overflowSet:");
	    for (Hub hub: overflowSet) {
		out.println(prefix + "    " + hub.getName());
	    }
	}
	if (underSet.isEmpty()) {
	    out.println(prefix + "underSet: <empty>");
	} else {
	    out.println(prefix + "underSet:");
	    for (Hub hub: underSet) {
		out.println(prefix + "    " + hub.getName());
	    }
	}
	if (inRangeSet.isEmpty()) {
	    out.println(prefix + "inRangeSet: <empty>");
	} else {
	    out.println(prefix + "inRangeSet:");
	    for (Hub hub: inRangeSet) {
		out.println(prefix + "    " + hub.getName());
	    }
	}
    }
}

//  LocalWords:  sim HubCondition nb eb ofb printState boolean
//  LocalWords:  superclass iPrefix printName whitespace inOverSet
//  LocalWords:  inOverflowSet inUnderSet overSet overflowSet
//  LocalWords:  underSet inRangeSet
