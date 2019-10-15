package org.bzdev.bikeshare;
import org.bzdev.drama.*;

/**
 * Factory for creating instances of BasicTripGenerator.
 * <P>
 * The parameters this factory supports are shown in the following table:
 * <P>
 * <IFRAME SRC="../../../factories-api/org/bzdev/bikeshare/BasicTripGenFactory.html" width="95%" height="600">
 * Please see
 *  <A HREF="../../../factories-api/org/bzdev/bikeshare/BasicTripGenFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public class BasicTripGenFactory
    extends AbstrBasicTripGenFactory<BasicTripGenerator>
{
    /**
     * Constructor required by the service-provider interface used
     * to find factories.
     */
    public BasicTripGenFactory() {
	this(null);
    }

    public BasicTripGenFactory(DramaSimulation sim) {
	super(sim);
    }

    @Override
    protected BasicTripGenerator newObject(String name) {
	return new BasicTripGenerator(getSimulation(), name, willIntern());
    }
}

//  LocalWords:  BasicTripGenerator startingHub meanIATime nBicycles
//  LocalWords:  interarrival dest overflowProb probabilityFunction
//  LocalWords:  SimFunctionTwo domainMember boolean timeline
//  LocalWords:  traceSetMode traceSets TraceSet SimObject
