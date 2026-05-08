package graphics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL30;

import core.Camera;
import core.Shader;
import core.Window;
import util.AssetPool;
import util.JMath;

/**
 * Immediate-mode debug renderer. Lines, boxes, and circles added each frame
 * are batched into a single VBO and drawn at the end of the frame. Each shape
 * has a lifetime in frames; shapes with lifetime 1 disappear after one frame.
 * Lines outside the camera frustum are culled before upload.
 */
public class DebugDraw {

	private static int MAX_LINES = 3000;
	private static List<Line2D> lines = new ArrayList<Line2D>();
	private static float[] vertexArray = new float[MAX_LINES * 6 * 2];
	private static Shader shader = AssetPool.getShader("res/shaders/debugLine2D.glsl");
	
	private static int vaoId;
	private static int vboId;
	
	private static boolean started = false;
	
	public static void start() {
		vaoId = GL30.glGenVertexArrays();
		GL30.glBindVertexArray(vaoId);
		
		vboId = GL30.glGenBuffers();
		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId);
		GL30.glBufferData(GL30.GL_ARRAY_BUFFER, vertexArray.length * Float.BYTES, GL30.GL_DYNAMIC_DRAW);
		
		GL30.glVertexAttribPointer(0, 3, GL30.GL_FLOAT, false, 6 * Float.BYTES, 0);
		GL30.glEnableVertexAttribArray(0);
		
		GL30.glVertexAttribPointer(1, 3, GL30.GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
		GL30.glEnableVertexAttribArray(1);
		
		GL30.glLineWidth(2.0f);
	}
	
	public static void beginFrame() {
		if(!started) {
			start();
			started = true;
		}
		
		for(int i = 0; i < lines.size(); i++) {
			if(lines.get(i).beginFrame() < 0) {
				lines.remove(i);
				i--;
			}
		}
	}
	
	public static void draw() {
		if(lines.size() <= 0) {
			return;
		}
		
		int index = 0;
		for(Line2D line : lines) {
			for(int i = 0; i < 2; i++) {
				Vector2f position = i == 0 ? line.getFrom() : line.getTo();
				Vector3f color = line.getColor();
				
				vertexArray[index] = position.x;
				vertexArray[index + 1] = position.y;
				vertexArray[index + 2] = -10.0f;
				
				vertexArray[index + 3] = color.x;
				vertexArray[index + 4] = color.y;
				vertexArray[index + 5] = color.z;
				
				index += 6;
			}
		}
		
		GL30.glBindBuffer(GL30.GL_ARRAY_BUFFER, vboId);
		GL30.glBufferSubData(GL30.GL_ARRAY_BUFFER, 0, Arrays.copyOfRange(vertexArray, 0, lines.size() * 6 * 2));
		
		shader.use();
		shader.uploadMat4("uProjection", Window.getScene().camera().getProjectionMatrix());
		shader.uploadMat4("uView", Window.getScene().camera().getViewMatrix());
		
		GL30.glBindVertexArray(vaoId);
		GL30.glEnableVertexAttribArray(0);
		GL30.glEnableVertexAttribArray(1);
		
		GL30.glDrawArrays(GL30.GL_LINES, 0, lines.size() * 6 * 2);
		
		GL30.glDisableVertexAttribArray(0);
		GL30.glDisableVertexAttribArray(1);
		GL30.glBindVertexArray(0);
		
		shader.detach();
	}
	
	// add line methods
	public static void addLine2D(Vector2f from, Vector2f to) {
		addLine2D(from, to, new Vector3f(0, 1, 0), 1);
	}
	
	public static void addLine2D(Vector2f from, Vector2f to, Vector3f color) {
		addLine2D(from, to, color, 1);
	}
	
	public static void addLine2D(Vector2f from, Vector2f to, Vector3f color, int lifeTime) {
		Camera camera = Window.getScene().camera();
		Vector2f cameraLeft = new Vector2f(camera.position).add(new Vector2f(-2.0f, -2.0f));
		Vector2f cameraRight = new Vector2f(camera.position).add(new Vector2f(camera.getProjectionSize()).mul(camera.getZoom())).add(new Vector2f(4.0f, 4.0f));
		
		boolean lineInView = ((from.x >= cameraLeft.x && from.x <= cameraRight.x) && (from.y >= cameraLeft.y && from.y <= cameraRight.y)) || ((to.x >= cameraLeft.x && to.x <= cameraRight.x) && (to.y >= cameraLeft.y && to.y <= cameraRight.y));
		if(lines.size() >= MAX_LINES || !lineInView) {
			return;
		}
		
		DebugDraw.lines.add(new Line2D(from, to, color, lifeTime));
	}
	
	// add box methods
	public static void addBox2D(Vector2f center, Vector2f dimensions, float rotation) {
		addBox2D(center, dimensions, rotation, new Vector3f(0, 1, 0), 1);
	}
	
	public static void addBox2D(Vector2f center, Vector2f dimensions, float rotation, Vector3f color) {
		addBox2D(center, dimensions, rotation, color, 1);
	}
	
	public static void addBox2D(Vector2f center, Vector2f dimensions, float rotation, Vector3f color, int lifeTime) {
		Vector2f min = new Vector2f(center).sub(new Vector2f(dimensions).mul(0.5f));
		Vector2f max = new Vector2f(center).add(new Vector2f(dimensions).mul(0.5f));
		
		Vector2f[] vertices = {
			new Vector2f(min.x, min.y),
			new Vector2f(min.x, max.y),
			new Vector2f(max.x, max.y),
			new Vector2f(max.x, min.y)
		};
		
		if(rotation != 0.0) {
			for(Vector2f vert : vertices) {
				JMath.rotate(vert, rotation, center);
			}
		}
		
		addLine2D(vertices[0], vertices[1], color, lifeTime);
		addLine2D(vertices[0], vertices[3], color, lifeTime);
		addLine2D(vertices[1], vertices[2], color, lifeTime);
		addLine2D(vertices[2], vertices[3], color, lifeTime);
	}
	
	// add circle methods
	public static void addCircle(Vector2f center, float radius) {
		addCircle(center, radius, new Vector3f(0, 1, 0), 1);
	}
	
	public static void addCircle(Vector2f center, float radius, Vector3f color) {
		addCircle(center, radius, color, 1);
	}
	
	public static void addCircle(Vector2f center, float radius, Vector3f color, int lifeTime) {
		Vector2f[] points = new Vector2f[32];
		float increment = 360 / points.length;
		float currentAngle = 0;
		
		for(int i = 0; i < points.length; i++) {
			Vector2f tmp = new Vector2f(radius, 0);
			JMath.rotate(tmp, currentAngle, new Vector2f());
			points[i] = new Vector2f(tmp).add(center);
			
			if(i > 0) {
				addLine2D(points[i - 1], points[i], color, lifeTime);
			}
			
			currentAngle += increment;
		}
		
		addLine2D(points[points.length - 1], points[0], color, lifeTime);
	}
}
