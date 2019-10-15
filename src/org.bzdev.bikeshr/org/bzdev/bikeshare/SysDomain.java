package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.drama.common.*;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.io.PrintWriter;

/**
 * Class providing a system domain.
 * All hubs (including storage hubs) are configured to be members of
 * a system domain. A system domain's delay table gives the travel time 
 * for workers between pairs of hubs.  The workers will typically use
 * a vehicle of some sort due to the need to move a number of bicycles.
 */
public class SysDomain extends HubDomain {
    static String COMM_DOMAIN_NAME = "sysDomain";
    private HubCondition condition;

    public static final CommDomainType commDomainType
	= CommDomainType.findType(COMM_DOMAIN_NAME);

    @Override
    protected CommDomainType fetchCommDomainType() {
	return commDomainType;
    }

    HubListener hubListener;

    /*
     * Constructor.
     * @param sim the simulation
     * @param name the name of this object
     * @param intern true if this object should be interned in the
     *        simulation's name table; false otherwise
     */
    public SysDomain(DramaSimulation sim, String name, boolean intern) {
	this(sim, name, intern, null);
    }
    /**
     * Constructor with parent domain.
     * @param sim the simulation
     * @param name the name of this object
     * @param intern true if this object should be interned in the
     *        simulation's name table; false otherwise
     * @param parent the parent domain
     */
    public SysDomain(DramaSimulation sim, String name, boolean intern,
		     Domain parent) {
	super(sim, name, intern, parent, BikeShare.sysDomainPriority);
	condition = new HubCondition(sim, name+"_condition", false);
	addCondition(condition);
	hubListener = new HubListener() {
		public void hubChanged(Hub hub,
				       int need,
				       int excess,
				       int over)
		{
		    condition.hubChanged(hub, need, excess, over);
		}
	    };
    }

    Set<StorageHub> storageHubs = new HashSet<>();
    Set<Hub> userHubs = new HashSet<>();

    Set<StorageHub> storageHubsView = Collections.unmodifiableSet(storageHubs);
    Set<Hub> userHubsView = Collections.unmodifiableSet(userHubs);

    /**
     * Get the storage hubs that are members of a system domain.
     * @return a set of storage hubs associated with this domain
     */
    public Set<StorageHub> getStorageHubs() {
	// return Collections.unmodifiableSet(storageHubs);
	return storageHubsView;
    }

    /**
     * Get the hubs that are members of a system domain but that
     * are not storage hubs.
     * @return a set of hubs that are associated with this domain but that
     *         are not storage hubs
     */
    public Set<Hub> getUserHubs() {
	// return Collections.unmodifiableSet(userHubs);
	return userHubsView;
    }

    @Override
    protected void onJoinedDomain(Actor actor, boolean trackCondition) {
	if (actor instanceof StorageHub) {
	    StorageHub hub = (StorageHub) actor;
	    storageHubs.add(hub);
	} else if (actor instanceof Hub) {
	    Hub hub = (Hub) actor;
	    userHubs.add(hub);
	    hub.addHubListener(hubListener);
	}
    }

    @Override
    protected void onLeftDomain(Actor actor) {
	if (actor instanceof StorageHub) {
	    StorageHub hub = (StorageHub)actor;
	    storageHubs.remove(hub);
	} else if (actor instanceof Hub) {
	    Hub hub = (Hub) actor;
	    userHubs.remove(hub);
	    hub.removeHubListener(hubListener);
	}
    }

     /**
     * {@inheritDoc}
     * Defined for class SysDomain:
     * <UL>
     *   <LI> a list of the storage hubs for this domain.
     * </UL>
     * @param iPrefix {@inheritDoc}
     * @param prefix {@inheritDoc}
     * @param printName {@inheritDoc}
     * @param out {@inheritDoc}
     */
    @Override
    public void printConfiguration(String iPrefix, String prefix,
				   boolean printName, PrintWriter out)
    {
	super.printConfiguration(iPrefix, prefix, printName, out);
	out.println(prefix + "storage hubs:");
	for (StorageHub hub: storageHubs) {
	    out.println(prefix + "    " + hub.getName());
	}
    }
}
//  LocalWords:  sysDomain sim unmodifiableSet storageHubs userHubs
//  LocalWords:  iPrefix printName
