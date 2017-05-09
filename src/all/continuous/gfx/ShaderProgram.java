package all.continuous.gfx;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import all.continuous.exceptions.ShaderException;

import java.nio.FloatBuffer;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {
	private int programHandle;

	public ShaderProgram(String vertCode, String fragCode) throws ShaderException {
		programHandle = glCreateProgram();
		if (programHandle == 0)
			throw new ShaderException("Could not create shader program");

		Shader vertexShader = new Shader(vertCode, GL_VERTEX_SHADER);
		glAttachShader(programHandle, vertexShader.shaderHandle);

		Shader fragmentShader = new Shader(fragCode, GL_FRAGMENT_SHADER);
		glAttachShader(programHandle, fragmentShader.shaderHandle);

		link();
	}

	private void link() throws ShaderException {
		glLinkProgram(programHandle);
		if (glGetProgrami(programHandle, GL_LINK_STATUS) == 0)
			throw new ShaderException("Error linking Shader code: " + glGetProgramInfoLog(programHandle, 1024));

		glValidateProgram(programHandle);
		if (glGetProgrami(programHandle, GL_VALIDATE_STATUS) == 0)
			System.out.println("Warning validating Shader code: " + glGetProgramInfoLog(programHandle, 1024));
	}

	public void bind() {
		glUseProgram(programHandle);
	}

	public void unbind() {
		glUseProgram(0);
	}

	class Shader {
		private int shaderHandle;

		public Shader(String code, int type) throws ShaderException {
			shaderHandle = glCreateShader(type);
			if (shaderHandle == 0)
				throw new ShaderException("Error creating shader. Code: " + shaderHandle);

			glShaderSource(shaderHandle, code);
			glCompileShader(shaderHandle);
			if (glGetShaderi(shaderHandle, GL_COMPILE_STATUS) == 0)
				throw new ShaderException("Error compiling " + (type == GL_VERTEX_SHADER ? "vertex" : "fragment")
						+ " Shader code: \n" + glGetShaderInfoLog(shaderHandle, 1024));
		}
	}

	public void setMatrix4(String name, Matrix4f mat) {
		int loc = glGetUniformLocation(programHandle, name);
		FloatBuffer fbMat = BufferUtils.createFloatBuffer(16);
		mat.get(fbMat);
		glUniformMatrix4fv(loc, false, fbMat);
	}

	public void setVector3(String name, Vector3f vec) {
		int loc = glGetUniformLocation(programHandle, name);
		FloatBuffer fbVec = BufferUtils.createFloatBuffer(3);
		vec.get(fbVec);
		glUniform3fv(loc, fbVec);
	}

	public void setVector4(String name, Vector4f vec) {
		int loc = glGetUniformLocation(programHandle, name);
		FloatBuffer fbVec = BufferUtils.createFloatBuffer(4);
		vec.get(fbVec);
		glUniform4fv(loc, fbVec);
	}

	public void setSampler2d(String name, int value) {
		int loc = glGetUniformLocation(programHandle, name);
		glUniform1i(loc, value);
	}

	public void setFloat(String name, float value) {
		int loc = glGetUniformLocation(programHandle, name);
		glUniform1f(loc, value);
	}
}
