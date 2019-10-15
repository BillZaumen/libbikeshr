package org.bzdev.bikeshare;
import org.bzdev.drama.*;


/**
 * Factory for creating instances of BurstTripGenerator.
 * <P>
 * The parameters this factory supports are shown in the following table:
 * <P>
 * <IFRAME SRC="../../../factories-api/org/bzdev/bikeshare/BurstTripGenFactory.html" width="95%" height="600">
 * Please see
 *  <A HREF="../../../factories-api/org/bzdev/bikeshare/BurstTripGenFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public class BurstTripGenFactory
    extends AbstrBurstTripGenFactory<BurstTripGenerator>
{
    /**
     * Constructor required by the service-provider interface used
     * to find factories.
     */
    public BurstTripGenFactory() {
	this(null);
    }


    public BurstTripGenFactory(DramaSimulation sim) {
	super(sim);
    }

    @Override
    protected BurstTripGenerator newObject(String name) {
	return new BurstTripGenerator(getSimulation(), name, willIntern());
    }
}
