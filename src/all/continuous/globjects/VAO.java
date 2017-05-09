package all.continuous.globjects;

import static org.lwjgl.opengl.GL30.*;

public class VAO {
	private int vaoHandle;

	public VAO() {
		vaoHandle = glGenVertexArrays();
		bind();
	}

	public void bind() {
		glBindVertexArray(vaoHandle);
	}

	public void unbind() {
		glBindVertexArray(0);
	}

	public void destroy() {
		glDeleteVertexArrays(vaoHandle);
	}
}
