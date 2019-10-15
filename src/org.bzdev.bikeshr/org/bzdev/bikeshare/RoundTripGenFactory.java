package org.bzdev.bikeshare;
import org.bzdev.drama.*;

/**
 * Factory for creating instances of RoundTripGenerator.
 * <P>
 * The parameters this factory supports are shown in the following table:
 * <P>
 * <IFRAME SRC="../../../factories-api/org/bzdev/bikeshare/RoundTripGenFactory.html" width="95%" height="600">
 * Please see
 *  <A HREF="../../../factories-api/org/bzdev/bikeshare/RoundTripGenFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public class RoundTripGenFactory
    extends AbstrRoundTripGenFactory<RoundTripGenerator>
{
    /**
     * Constructor required by the service-provider interface used
     * to find factories.
     */
    public RoundTripGenFactory() {
	this(null);
    }


    public RoundTripGenFactory(DramaSimulation sim) {
	super(sim);
    }

    @Override
    protected RoundTripGenerator newObject(String name) {
	return new RoundTripGenerator(getSimulation(), name, willIntern());
    }
}

//  LocalWords:  RoundTripGenerator startingHub meanIATime nBicycles
//  LocalWords:  interarrival dest overflowProb probabilityFunction
//  LocalWords:  SimFunctionTwo domainMember boolean timeline
//  LocalWords:  traceSetMode traceSets TraceSet SimObject
