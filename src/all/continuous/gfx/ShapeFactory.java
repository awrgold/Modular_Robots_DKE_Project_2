package all.continuous.gfx;

public class ShapeFactory {
	public static Mesh genRect(int x, int y, int w, int h) {
		return genRect(x, y, w, h, 0, 0, 1, 1);
	}

	public static Mesh genRect(int x, int y, int w, int h, float u1, float v1, float u2, float v2) {
		return new Mesh(new float[] {
				x  , y  , 0, 
				x+w, y  , 0, 
				x+w, y+h, 0, 
				x+w, y+h, 0, 
				x  , y+h, 0, 
				x  , y  , 0
		}, new float[] {
				u1, v1, 0, 0, 
				u2, v1, 0, 0, 
				u2, v2, 0, 0, 
				u2, v2, 0, 0, 
				u1, v2, 0, 0, 
				u1, v1, 0, 0
		});
	}

	// TODO: Make this an indexed mesh
	public static Mesh genBox(float x, float y, float z, float w, float h, float l) {
		float[] texCoords = new float[144];
		for (int i = 0; i < texCoords.length; i++)
			texCoords[i] = 0;

		float xOff = x, yOff = y, zOff = z;

		Mesh m = new Mesh(
				new float[] {
						// back
						xOff, yOff, zOff,
                        xOff + w, yOff, zOff,
                        xOff + w, yOff + h, zOff,
                        xOff + w, yOff + h, zOff,
						xOff, yOff + h, zOff,
                        xOff, yOff, zOff,

						// front
						xOff, yOff + h, zOff + l,
                        xOff + w, yOff + h, zOff + l,
                        xOff + w, yOff, zOff + l,
                        xOff + w, yOff, zOff + l,
                        xOff, yOff, zOff + l,
                        xOff, yOff + h, zOff + l,

						// left
						xOff, yOff, zOff + l,
                        xOff, yOff, zOff,
                        xOff, yOff + h, zOff,
                        xOff, yOff + h, zOff ,
						xOff, yOff + h, zOff + l,
                        xOff, yOff, zOff + l,

						// right
						xOff + w, yOff, zOff,
                        xOff + w, yOff, zOff + l,
                        xOff + w, yOff + h, zOff + l,
                        xOff + w, yOff + h, zOff + l,
                        xOff + w, yOff + h, zOff,
                        xOff + w, yOff, zOff,

						// bottom
						xOff, yOff, zOff + l,
                        xOff + w, yOff, zOff + l,
                        xOff + w, yOff, zOff,
                        xOff + w, yOff, zOff,
						xOff, yOff, zOff,
                        xOff, yOff, zOff + l,

						// top
						xOff, yOff + h, zOff,
                        xOff + w, yOff + h, zOff,
                        xOff + w, yOff + h, zOff + l,
                        xOff + w, yOff + h, zOff + l,
                        xOff, yOff + h, zOff + l,
                        xOff, yOff + h, zOff },
				texCoords,
				new float[] {
						// back
						0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1,

						// front
						0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1,

						// left
						-1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0,

						// right
						1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0,

						// bottom
						0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0, 0, -1, 0,

						// top
						0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, });

		return m;
	}
}
