package org.bzdev.bikeshare;
import org.bzdev.drama.*;

/**
 * Factory for creating instances of DelayTable.
 * <P>
 * The parameters this factory supports are shown in the following table:
 * <P>
 * <IFRAME SRC="../../../factories-api/org/bzdev/bikeshare/SchedDelayTableFactory.html" width="95%" height="600">
 * Please see
 *  <A HREF="../../../factories-api/org/bzdev/bikeshare/SchedDelayTableFactory.html">
 *    the parameter documentation</A> for a table of the parameters supported
 * by this factory.
 * </IFRAME>
 */
public class SchedDelayTableFactory
    extends AbstrSchedDelayTblFactory<SchedDelayTable>
{

    public SchedDelayTableFactory() {
	this(null);
    }

    public SchedDelayTableFactory(DramaSimulation sim) {
	super(sim);
    }

    @Override
    protected SchedDelayTable newObject(String name) {
	return new SchedDelayTable(getSimulation(), name, willIntern());
    }

}

//  LocalWords:  DelayTable dest initialTime cutoffTime ge le maxWait
//  LocalWords:  stopProbability timeline traceSetMode traceSets
//  LocalWords:  TraceSet SimObject
