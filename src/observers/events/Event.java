package observers.events;

/** Payload for the EventSystem, carrying an EventType that describes what happened. */
public class Event {

	public EventType type;
	
	public Event(EventType type) {
		this.type = type;
	}
	
	public Event() {
		this.type = EventType.UserEvent;
	}
}
