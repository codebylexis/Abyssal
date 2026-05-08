package core;
import java.nio.ByteBuffer;

import org.joml.Vector4f;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALC11;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import graphics.DebugDraw;
import graphics.Framebuffer;
import graphics.PickingTexture;
import graphics.Renderer;
import observers.EventSystem;
import observers.Observer;
import observers.events.Event;
import physics2d.Physics2D;
import scenes.LevelEditorSceneInitializer;
import scenes.LevelSceneInitializer;
import scenes.Scene;
import scenes.SceneInitializer;
import util.AssetPool;

/**
 * Application window and main game loop. Owns the GLFW window, the OpenAL
 * audio device, and the active Scene. Drives the editor and runtime update
 * cycles and responds to engine-level events (play, stop, save, load).
 */
public class Window implements Observer {

	private ImGuiLayer imGuiLayer;
	private Framebuffer frameBuffer;
	private PickingTexture pickingTexture;
	
	private int width;
	private int height;
	private String title;
	private long glfwWindow;
	
	public float r, g, b, a;
	private boolean runtimePlaying = false;
	
	private static Window window = null;
	
	private long audioContext;
	private long audioDevice;
	
	private static Scene currentScene = null;
	
	private Window() {
		this.width = 1920;
		this.height = 1080;
		this.title = "Abyssal";
		EventSystem.addObserver(this);
	}
	
	public static void changeScene(SceneInitializer sceneInitializer) {
		if(currentScene != null) {
			currentScene.destroy();
		}
		
		getImGuiLayer().getPropertiesWindow().setActiveGameObject(null);
		currentScene = new Scene(sceneInitializer);
		currentScene.load();
		currentScene.init();
		currentScene.start();
	}
	
	public static Window get() {
		if(Window.window == null) {
			Window.window = new Window();
		}
		
		return window;
	}
	
	public static Physics2D getPhysics() {
		return currentScene.getPhysics();
	}
	
	public static Scene getScene() {
		return currentScene;
	}
	
	public void run() {
		init();
		loop();
		
		ALC11.alcDestroyContext(audioContext);
		ALC11.alcCloseDevice(audioDevice);
		
		Callbacks.glfwFreeCallbacks(glfwWindow);
		GLFW.glfwDestroyWindow(glfwWindow);
		
		GLFW.glfwTerminate();
		GLFW.glfwSetErrorCallback(null).free();
	}
	
	public void init() {
		GLFWErrorCallback.createPrint(System.err).set();
		
		if(!GLFW.glfwInit()) {
			throw new IllegalStateException("Unable to initialize glfw");
		}
		
		GLFW.glfwDefaultWindowHints();
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
		GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
		GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
		GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
		GLFW.glfwWindowHint(GLFW.GLFW_MAXIMIZED, GLFW.GLFW_TRUE);
		
		glfwWindow = GLFW.glfwCreateWindow(this.width, this.height, this.title, MemoryUtil.NULL, MemoryUtil.NULL);
		if(glfwWindow == MemoryUtil.NULL) {
			throw new IllegalStateException("Failed to create GLFW window");
		}
		
		GLFW.glfwSetCursorPosCallback(glfwWindow, MouseListener::mousePosCallback);
		GLFW.glfwSetMouseButtonCallback(glfwWindow, MouseListener::mouseButtonCallback);
		GLFW.glfwSetScrollCallback(glfwWindow, MouseListener::mouseScrollCallback);
		GLFW.glfwSetKeyCallback(glfwWindow, KeyListener::keyCallback);
		GLFW.glfwSetWindowSizeCallback(glfwWindow, (w, newWidth, newHeight) -> {
			Window.setWidth(newWidth);
			Window.setHeight(newHeight);
		});
				
		GLFW.glfwMakeContextCurrent(glfwWindow);
		GLFW.glfwSwapInterval(1);
		
		GLFW.glfwShowWindow(glfwWindow);
		
		
		//init audio device
		String defaultDeviceName = ALC11.alcGetString(0, ALC11.ALC_DEFAULT_DEVICE_SPECIFIER);
		audioDevice = ALC11.alcOpenDevice(defaultDeviceName);
		
		int[] attributes = {0};
		audioContext = ALC11.alcCreateContext(audioDevice, attributes);
		ALC11.alcMakeContextCurrent(audioContext);
		
		ALCCapabilities alcCapabilities = ALC.createCapabilities(audioDevice);
		ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);
		
		if(!alCapabilities.OpenAL10) {
			assert false : "Audio library not supported.";
		}
		
		GL.createCapabilities();
		
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
		
		
		frameBuffer = new Framebuffer(1920, 1080);
		pickingTexture = new PickingTexture(1920, 1080);
		GL30.glViewport(0, 0, 1920, 1080);
		
		this.imGuiLayer = new ImGuiLayer(glfwWindow, pickingTexture);
		this.imGuiLayer.initImGui();
		
		Window.changeScene(new LevelEditorSceneInitializer());
	}
	
	public void loop() {
		float beginTime = (float)GLFW.glfwGetTime();
		float endTime;
		float delta = -1.0f;
		
		Shader defaultShader = AssetPool.getShader("res/shaders/default.glsl");
		Shader pickingShader = AssetPool.getShader("res/shaders/pickingShader.glsl");
		
		while(!GLFW.glfwWindowShouldClose(glfwWindow)) {
			GLFW.glfwPollEvents();
			
			GL11.glDisable(GL11.GL_BLEND);
			pickingTexture.enableWriting();
			
			GL11.glViewport(0, 0, 1920, 1080);
			GL11.glClearColor(0, 0, 0, 0);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			
			Renderer.bindShader(pickingShader);
			currentScene.render();
			
			pickingTexture.disableWriting();
			GL11.glEnable(GL11.GL_BLEND);
			
			DebugDraw.beginFrame();
			
			frameBuffer.bind();
			Vector4f clearColor = currentScene.camera().clearColor;
			GL11.glClearColor(clearColor.x, clearColor.y, clearColor.z, clearColor.w);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
			
			if(delta >= 0) {
				Renderer.bindShader(defaultShader);
				
				if(runtimePlaying) {
					currentScene.update(delta);
				}else {
					currentScene.editorUpdate(delta);
				}
				
				currentScene.render();
				DebugDraw.draw();
			}
			
			frameBuffer.unbind();
			
			imGuiLayer.update(delta, currentScene);
			
			KeyListener.endFrame();
			MouseListener.endFrame();
			GLFW.glfwSwapBuffers(glfwWindow);
			
			endTime = (float)GLFW.glfwGetTime();
			delta = endTime - beginTime;
			beginTime = endTime;
		}
	}
	
	public static int getWidth() {
		return 1920;
	}
	
	public static int getHeight() {
		return 1080;
	}
	
	public static void setWidth(int newWidth) {
		get().width = newWidth;
	}
	
	public static void setHeight(int newHeight) {
		get().height = newHeight;
	}
	
	public static Framebuffer getFramebuffer() {
		return get().frameBuffer;
	}
	
	public static Float getTargetAspectRatio() {
		return 16.0f / 9.0f;
	}
	
	public static ImGuiLayer getImGuiLayer() {
		return get().imGuiLayer;
	}

	@Override
	public void onNotify(GameObject object, Event event) {
		switch(event.type) {
		case GameEngineStartPlay: 
			runtimePlaying = true;
			currentScene.save();
			Window.changeScene(new LevelSceneInitializer());
			break;
		case GameEngineStopPlay: 
			runtimePlaying = false;
			Window.changeScene(new LevelEditorSceneInitializer());
			break;
		case LoadLevel: 
			Window.changeScene(new LevelEditorSceneInitializer());
			break;
		case SaveLevel: 
			currentScene.save();
			break;
		}
	}
}
