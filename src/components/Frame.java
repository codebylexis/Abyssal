package components;

/** One frame in an AnimationState: a sprite and how long (in seconds) to display it. */
public class Frame {

	public Sprite sprite;
	public float frameTime;
	
	public Frame() {
		
	}
	
	public Frame(Sprite sprite, float time) {
		this.sprite = sprite;
		this.frameTime = time;
	}
}
