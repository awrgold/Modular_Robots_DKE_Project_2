package all.continuous.gfx;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.*;

import all.continuous.globjects.VAO;
import all.continuous.globjects.VBO;

public class Mesh {

	private VAO vao;

	private VBO positionVBO;
	private VBO texCoordsVBO;
	private VBO normalsVBO;
	private VBO coloursVBO;
	private int vertexCount;

	public Mesh(float[] positions) {
		vao = new VAO();

		positionVBO = new VBO(positions);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		
		vertexCount = positions.length / 3;
	}

	public Mesh(float[] positions, float[] texCoords) {
		this(positions);
		texCoordsVBO = new VBO(texCoords);
		glVertexAttribPointer(1, 4, GL_FLOAT, false, 0, 0);
	}

	public Mesh(float[] positions, float[] texCoords, float[] normals) {
		this(positions, texCoords);
		normalsVBO = new VBO(normals);
		glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);
	}
	
	public Mesh(float[] positions, float[] texCoords, float[] normals, float[] colours) {
		this(positions, texCoords, normals);
		coloursVBO = new VBO(colours);
		glVertexAttribPointer(3, 4, GL_FLOAT, false, 0, 0);
	}

	public void draw() {
		MVP.updateShader(ShaderManager.getInstance().getShader());

		vao.bind();
		glEnableVertexAttribArray(0);
		if (texCoordsVBO != null)
			glEnableVertexAttribArray(1);
		else
			glDisableVertexAttribArray(1);
		if (normalsVBO != null)
			glEnableVertexAttribArray(2);
		else
			glDisableVertexAttribArray(2);
		if (coloursVBO != null)
			glEnableVertexAttribArray(3);
		else
			glDisableVertexAttribArray(3);

		glDrawArrays(GL_TRIANGLES, 0, vertexCount);
	}

	public void destroy() {
		positionVBO.destroy();
		texCoordsVBO.destroy();
		normalsVBO.destroy();

		vao.destroy();
	}
}
