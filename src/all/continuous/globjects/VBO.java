package all.continuous.globjects;

import static org.lwjgl.opengl.GL15.*;

public class VBO {
	private final int vboHandle;

	public VBO(float[] data) {
		vboHandle = glGenBuffers();

		bind();
		glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW);
	}

	public void bind() {
		glBindBuffer(GL_ARRAY_BUFFER, vboHandle);
	}

	public void unbind() {
		glBindBuffer(GL_ARRAY_BUFFER, 0);
	}

	public void destroy() {
		glDeleteBuffers(vboHandle);
	}
}
