package org.bzdev.bikeshare;
import org.bzdev.drama.*;
import org.bzdev.drama.common.*;

/**
 * Class providing a user domain.
 * All hubs (except for storage hubs) are configured to be members of
 * a user domain. A user domain's delay table gives the travel time by
 * bicycle between pairs of hubs.
 */
public class UsrDomain extends HubDomain {
    static String COMM_DOMAIN_NAME = "usrDomain";
    private HubCondition condition;

    /**
     * The communication domain type for all user domains.
     */
    public static final CommDomainType commDomainType
	= CommDomainType.findType(COMM_DOMAIN_NAME);

    @Override
    protected CommDomainType fetchCommDomainType() {
	return commDomainType;
    }

    /**
     * Constructor.
     * @param sim the simulation
     * @param name the name of this object
     * @param intern true if this object should be interned in the
     *        simulation's name table; false otherwise
     */
    public UsrDomain(DramaSimulation sim, String name, boolean intern)
    {
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
    public UsrDomain(DramaSimulation sim, String name,
		     boolean intern, Domain parent)
    {
	super(sim, name, intern, parent, BikeShare.usrDomainPriority);
    }
}

//  LocalWords:  usrDomain sim
