package org.bzdev.bikeshare;
import org.bzdev.scripting.ScriptListenerAdapter;
import org.bzdev.scripting.ScriptingContext;

/**
 * Adapter for hub-data listeners.
 * This class provides a default implementation for the method
 * specified by HubDataListener and allows the listener to be
 * implemented in a scripting language.  For use in Java code, one
 * should override the methods of this class.  For use in a scripting
 * environment, the implementation of the method is provided by a
 * scripting-language object with a method that has the same names as
 * the method defined by this class. If a method is missing, the call
 * will succeed but no action will be performed.
 */
public class HubDataAdapter extends ScriptListenerAdapter
    implements HubDataListener
{
    /**
     * Constructor.
     */
    public HubDataAdapter() {
	super(null,null);
    }

    /**
     * Constructor given a scripting context and script object.
     * @param sc the scripting context for this adapter
     * @param scriptObject the scripting-language object implementing
     *        the listener interface for this adapter.
     */
    public HubDataAdapter(ScriptingContext sc, Object scriptObject) {
	super(sc, scriptObject);
    }

    @Override
    public void hubChanged(Hub hub, int bikeCount, boolean newBikeCount,
			   int overflowCount, boolean newOverflowCount,
			   double time, long ticks)
    {
	callScriptMethod("hubChanged", hub, bikeCount, newBikeCount,
			 overflowCount, newOverflowCount,
			 time, ticks);
    }
}

//  LocalWords:  HubDataListener sc scriptObject hubChanged
