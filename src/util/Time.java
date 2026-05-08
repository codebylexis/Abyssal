package util;

/** Engine clock. getTime() returns seconds elapsed since the class was first loaded. */
public class Time {

	public static float timeStarted = System.nanoTime();
	
	public static float getTime() {
		return (float)((System.nanoTime() - timeStarted) * 1E-9);
	}
}
