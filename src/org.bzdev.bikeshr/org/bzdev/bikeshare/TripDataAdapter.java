package org.bzdev.bikeshare;
import org.bzdev.scripting.ScriptListenerAdapter;
import org.bzdev.scripting.ScriptingContext;
import org.bzdev.util.ExpressionParser;
import org.bzdev.util.ExpressionParser.ESPObject;

/**
 * Adapter for trip-data listeners.
 * This class provides default implementations for the
 * methods specified by TripDataListener and allows the
 * listener to be implemented in a scripting language.
 * For use in Java code, one should override the methods of
 * this class.  For use in a scripting environment, the
 * implementation of the methods is provided by a scripting-language
 * object whose methods include ones that the same names as the methods of
 * this class.  If a method is missing, the call will succeed but no
 * action will be performed.
 */
public class TripDataAdapter extends ScriptListenerAdapter
    implements TripDataListener
{
    /**
     * Constructor.
     */
    public TripDataAdapter() {
	super(null,null);
    }

    /**
     * Constructor with only a script object.
     * This creates an adapter for use with the ESP scripting language.
     * <P>
     * Note: This is equivalent to using the constructor
     * {@link #TripDataAdapter(ScriptingContext,Object)} with
     * a null first argument.
     * @param scriptObject the scripting-language object implementing
     *        the listener interface for this adapter.
     */
    public TripDataAdapter(ESPObject scriptObject) {
	this(null, scriptObject);
    }


    /**
     * Constructor given a scripting context and script object.
     * @param sc the scripting context for this adapter
     * @param scriptObject the scripting-language object implementing
     *        the listener interface for this adapter.
     */
    public TripDataAdapter(ScriptingContext sc, Object scriptObject) {
	super(sc, scriptObject);
    }

    @Override
    public void tripStarted(long tripID, double time, long ticks, Hub hub,
			    HubDomain d)
    {
	callScriptMethod("tripStarted", tripID, time, ticks, hub, d);
    }

    @Override
    public void tripPauseStart(long tripID, double time, long ticks, Hub hub)
    {
	callScriptMethod("tripPauseStart", tripID, time, ticks, hub);
    }

    @Override
    public void tripPauseEnd(long tripID, double time, long ticks, Hub hub,
			     HubDomain d)
    {
	callScriptMethod("tripPauseEnd", tripID, time, ticks, hub, d);
    }

    @Override
    public void tripEnded(long tripID, double time, long ticks, Hub hub)
    {
	callScriptMethod("tripEnded", tripID, time, ticks, hub);
    }

    @Override
    public void tripFailedAtStart(long tripID, double time, long ticks, Hub hub)
    {
	callScriptMethod("tripFailedAtStart", tripID, time, ticks, hub);
    }

    @Override
    public void tripFailedMidstream(long tripID,
				    double time, long ticks,
				    Hub hub)
    {
	callScriptMethod("tripFailedMidstream", tripID, time, ticks, hub);
    }
}

//  LocalWords:  TripDataListener sc scriptObject tripStarted
//  LocalWords:  tripPauseStart tripPauseEnd tripEnded
//  LocalWords:  tripFailedAtStart tripFailedMidstream
