package core;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Loads a combined GLSL source file containing both a vertex and a fragment
 * shader separated by "#type vertex" / "#type fragment" directives, compiles
 * and links them into an OpenGL program, and exposes upload helpers for every
 * uniform type used by the engine.
 */
public class Shader {

	private int programId;
	
	private String vertexSource;
	private String fragmentSource;
	private String filePath;
	
	private boolean beingUsed = false;
	
	public Shader(String filePath) {
		this.filePath = filePath;
		try {
			String source = new String(Files.readAllBytes(Paths.get(filePath))).replace("\r\n", "\n");
			String[] splitString = source.split("(#type)( )+([a-zA-Z]+)");

			int index = source.indexOf("#type") + 6;
			int eol = source.indexOf("\n", index);
			String firstPattern = source.substring(index, eol).trim();

			index = source.indexOf("#type", eol) + 6;
			eol = source.indexOf("\n", index);
			String secondPattern = source.substring(index, eol).trim();
			
			if(firstPattern.equals("vertex")) {
				vertexSource = splitString[1];
			}else if(firstPattern.equals("fragment")) {
				fragmentSource = splitString[1];
			}else {
				throw new IOException("Unexpected token " + firstPattern + " in " + filePath);
			}
			
			if(secondPattern.equals("vertex")) {
				vertexSource = splitString[2];
			}else if(secondPattern.equals("fragment")) {
				fragmentSource = splitString[2];
			}else {
				throw new IOException("Unexpected token " + secondPattern + " in " + filePath);
			}
		}catch(IOException e) {
			e.printStackTrace();
			assert false : "Error: could not open file for shader " + filePath;
		}
	}
	
	public void compile() {
		int vertexId, fragmentId;
		
		vertexId = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
		GL20.glShaderSource(vertexId, vertexSource);
		GL20.glCompileShader(vertexId);
		
		int success = GL20.glGetShaderi(vertexId, GL20.GL_COMPILE_STATUS);
		if(success == GL20.GL_FALSE) {
			int len = GL20.glGetShaderi(vertexId, GL20.GL_INFO_LOG_LENGTH);
			System.out.println("ERROR: default.glsl vertex shader compilation failed");
			System.out.println(GL20.glGetShaderInfoLog(vertexId, len));
			assert false : "";
		}
		
		fragmentId = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
		GL20.glShaderSource(fragmentId, fragmentSource);
		GL20.glCompileShader(fragmentId);
		
		success = GL20.glGetShaderi(fragmentId, GL20.GL_COMPILE_STATUS);
		if(success == GL20.GL_FALSE) {
			int len = GL20.glGetShaderi(fragmentId, GL20.GL_INFO_LOG_LENGTH);
			System.out.println("ERROR: " + filePath + " fragment shader compilation failed");
			System.out.println(GL20.glGetShaderInfoLog(fragmentId, len));
			assert false : "";
		}
		
		programId = GL20.glCreateProgram();
		GL20.glAttachShader(programId, vertexId);
		GL20.glAttachShader(programId, fragmentId);
		GL20.glLinkProgram(programId);
		
		success = GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS);
		if(success == GL20.GL_FALSE) {
			int len = GL20.glGetProgrami(programId, GL20.GL_INFO_LOG_LENGTH);
			System.out.println("ERROR: " + filePath + " program link failed");
			System.out.println(GL20.glGetProgramInfoLog(programId, len));
			assert false : "";
		}
	}
	
	public void use() {
		if(!beingUsed) {
			GL30.glUseProgram(programId);
			beingUsed = true;
		}
	}
	
	public void detach() {
		GL30.glUseProgram(0);
		beingUsed = false;
	}
	
	public void uploadMat4(String name, Matrix4f mat4) {
		int location = GL20.glGetUniformLocation(programId, name);
		use();
		FloatBuffer matBuffer = BufferUtils.createFloatBuffer(16);
		mat4.get(matBuffer);
		GL20.glUniformMatrix4fv(location, false, matBuffer);
	}
	
	public void uploadMat3(String name, Matrix4f mat3) {
		int location = GL20.glGetUniformLocation(programId, name);
		use();
		FloatBuffer matBuffer = BufferUtils.createFloatBuffer(9);
		mat3.get(matBuffer);
		GL20.glUniformMatrix3fv(location, false, matBuffer);
	}
	
	public void uploadVec4f(String name, Vector4f vec4f) {
		int location = GL20.glGetUniformLocation(programId, name);
		use();
		GL20.glUniform4f(location, vec4f.x, vec4f.y, vec4f.z, vec4f.w);
	}
	
	public void uploadVec3f(String name, Vector3f vec3f) {
		int location = GL20.glGetUniformLocation(programId, name);
		use();
		GL20.glUniform3f(location, vec3f.x, vec3f.y, vec3f.z);
	}
	
	public void uploadVec2f(String name, Vector2f vec2f) {
		int location = GL20.glGetUniformLocation(programId, name);
		use();
		GL20.glUniform2f(location, vec2f.x, vec2f.y);
	}
	
	public void uploadFloat(String name, float value) {
		int location = GL20.glGetUniformLocation(programId, name);
		use();
		GL20.glUniform1f(location, value);
	}
	
	public void uploadInt(String name, int value) {
		int location = GL20.glGetUniformLocation(programId, name);
		use();
		GL20.glUniform1i(location, value);
	}
	
	public void uploadTexture(String name, int slot) {
		int location = GL20.glGetUniformLocation(programId, name);
		use();
		GL20.glUniform1i(location, slot);
	}
	
	public void uploadIntArray(String name, int[] array) {
		int location = GL20.glGetUniformLocation(programId, name);
		use();
		GL20.glUniform1iv(location, array);
	}
}
