package all.continuous.globjects;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL13;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Texture {
	private int textureHandle;
	private int w, h;

	public Texture() {
		textureHandle = glGenTextures();
	}

	public Texture(ByteBuffer data, int w, int h) {
		this();
		setData(data, w, h);
	}

	private void setData(ByteBuffer data, int w, int h) {
		this.w = w;
		this.h = h;
		bind();
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, data);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		unbind();
	}

	public Texture(BufferedImage img) {
		this();
		int[] pixels = new int[img.getWidth() * img.getHeight()];
		img.getRGB(0, 0, img.getWidth(), img.getHeight(), pixels, 0, img.getWidth());

		ByteBuffer buffer = BufferUtils.createByteBuffer(img.getWidth() * img.getHeight() * 4);

		for (int y = 0; y < img.getHeight(); y++) {
			for (int x = 0; x < img.getWidth(); x++) {
				int pixel = pixels[y * img.getWidth() + x];
				buffer.put((byte) ((pixel >> 16) & 0xFF));
				buffer.put((byte) ((pixel >> 8) & 0xFF));
				buffer.put((byte) (pixel & 0xFF));
				buffer.put((byte) ((pixel >> 24) & 0xFF));
			}
		}

		buffer.flip();

		setData(buffer, img.getWidth(), img.getHeight());
	}

	public Texture(BufferedImage img, boolean interpolation) {
		this(img);
		if (!interpolation) {
			bind();
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			unbind();
		}
	}
	
	public Texture(File f) throws IOException {
		this(ImageIO.read(f));
	}

	public Texture(File f, boolean interpolation) throws IOException {
		this(ImageIO.read(f), interpolation);
	}

	public void bind() {
		bind(0);
	}
	
	public void bind(int texUnit) {
		GL13.glActiveTexture(GL13.GL_TEXTURE0 + texUnit);
		glBindTexture(GL_TEXTURE_2D, textureHandle);
	}

	public void unbind() {
		glBindTexture(GL_TEXTURE_2D, 0);
	}

	// TODO: More reliable texture size getters
	public int getWidth() {
		return this.w;
	}
	
	public int getHeight() {
		return this.h;
	}
}
