package scenes;

/**
 * Abstract strategy for setting up a Scene. loadResources registers assets
 * into AssetPool; init populates the scene with GameObjects; imgui renders
 * any scene-specific editor panels.
 */
public abstract class SceneInitializer {

	public abstract void init(Scene scene);
	public abstract void loadResources(Scene scene);
	public abstract void imgui();
}
