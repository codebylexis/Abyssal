package physics2d.enums;

/** Maps to JBox2D body types. Static bodies never move; Dynamic bodies are fully simulated; Kinematic bodies are moved by code only. */
public enum BodyType {
	Static,
	Dynamic,
	Kinematic
}
