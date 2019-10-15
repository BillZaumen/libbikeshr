package org.bzdev.bikeshare;
import org.bzdev.scripting.ScriptListenerAdapter;
import org.bzdev.scripting.ScriptingContext;

/**
 * Adapter for hub-worker listeners.
 * This class provides default implementations for the
 * methods specified by HubWorkerListener and allows the
 * listener to be implemented in a scripting language.
 * For use in Java code, one should override the methods of
 * this class.  For use in a scripting environment, the
 * implementation of the methods is provided by a scripting-language
 * object whose methods include ones that the same names as the methods of
 * this class.  If a method is missing, the call will succeed but no
 * action will be performed.
 */
public class HubWorkerAdapter extends ScriptListenerAdapter
    implements HubWorkerListener
{
    /**
     * Constructor.
     */
    public HubWorkerAdapter() {
	super(null,null);
    }

    /**
     * Constructor given a scripting context and script object.
     * @param sc the scripting context for this adapter
     * @param scriptObject the scripting-language object implementing
     *        the listener interface for this adapter.
     */
    public HubWorkerAdapter(ScriptingContext sc, Object scriptObject) {
	super(sc, scriptObject);
    }

    @Override
    public void dequeued(HubWorker worker, double time, long ticks, Hub hub) {
	callScriptMethod("dequeued", worker, time, ticks, hub);
    }

    @Override
    public void enteredHub(HubWorker worker, double time, long ticks, Hub hub) {
	callScriptMethod("enteredHub", worker, time, ticks, hub);
    }

    @Override
    public void fixingOverflows(HubWorker worker, double time, long ticks,
				Hub hub)
    {
	callScriptMethod("fixingOverflows", worker, time, ticks, hub);
    }
    
    @Override
    public void fixingPreferred(HubWorker worker, double time, long ticks,
				Hub hub)
    {
	callScriptMethod("fixingPreferred", worker, time, ticks, hub);
    }
    
    @Override
    public void leftHub(HubWorker worker, double time, long ticks, Hub hub) {
	callScriptMethod("leftHub", worker, time, ticks, hub);
    }

    @Override
    public void queued(HubWorker worker, double time, long ticks, Hub hub) {
	callScriptMethod("queued", worker, time, ticks, hub);
    }
    
    @Override
    public void changedCount(HubWorker worker, double time, long ticks, Hub hub,
		      int oldcount, int newcount)
    {
	callScriptMethod("changedCount", worker, time, ticks, hub,
			 oldcount, newcount);
    }
}
//  LocalWords:  HubWorkerListener sc scriptObject dequeued leftHub
//  LocalWords:  enteredHub fixingOverflows fixingPreferred
//  LocalWords:  changedCount
