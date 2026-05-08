package observers;

import core.GameObject;
import observers.events.Event;

/** Receiver interface for the EventSystem publish/subscribe bus. */
public interface Observer {

	void onNotify(GameObject object, Event event);
}
