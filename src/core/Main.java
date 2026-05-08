package core;

/** Application entry point. Creates the Window singleton and starts the engine loop. */
public class Main {

	public static void main(String[] args) {
		Window window = Window.get();
		window.run();
	}
}
