package org.bzdev.bikeshare;
import org.bzdev.drama.*;

/**
 * Factory for creating instances of DelayTable.
 * <P>
 * The parameters this factory supports are shown in the following table:
 * <P>
 * <IFRAME SRC="../../../factories-api/org/bzdev/bikeshare/StdDelayTableFactory.html" width="95%" height="600">
 * Please see
 *  <A HREF="../../../factories-api/org/bzdev/bikeshare/StdDelayTableFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public class StdDelayTableFactory extends AbstrStdDelayTblFactory<StdDelayTable>
{
    /**
     * Constructor required by the service-provider interface used
     * to find factories.
     */
    public StdDelayTableFactory() {
	this(null);
    }


    public StdDelayTableFactory(DramaSimulation sim) {
	super(sim);
    }

    @Override
    protected StdDelayTable newObject(String name) {
	return new StdDelayTable(getSimulation(), name, willIntern());
    }

}

//  LocalWords:  DelayTable speedRV DoubleRandomVariable nStops fd
//  LocalWords:  stopProbablity maxWait distFraction dest timeline
//  LocalWords:  stopProbability traceSetMode traceSets TraceSet
//  LocalWords:  SimObject
