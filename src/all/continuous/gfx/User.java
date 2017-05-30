package all.continuous.gfx;

import java.io.File;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import com.bobjob.engine.util.swing.SwingHandle;

import all.continuous.IOUtil;

/**
 * Created by Roel on 19-03-17.
 */
public class User {
	private Vector3f pos;
	public Vector3f getPos() { return this.pos; }
	private Vector3f front;
	private Vector3f worldUp = new Vector3f(0, 1, 0);
	private Vector3f up;
	private Vector3f right;
	private float yaw, pitch;

	private float fov = 60;
	public void addFov(float delta) {
		this.fov += delta;

		if (this.fov < 15) this.fov = 15;
		if (this.fov > 70) this.fov = 70;
	}
	private WorldRenderer world;

	private CameraControlState state;

	public User(WorldRenderer world) {
		this.pos = new Vector3f();
		this.world = world;

		this.updateCameraVectors();

		state = new FreeCameraState(world, this);

		Display.getInstance().setScrollListener(this::scrollCallback);
		Display.getInstance().setKeyListener(this::keyCallback);
		Display.getInstance().setMouseButtonListener(this::mouseCallback);
	}

	private void updateCameraVectors() {
		front = new Vector3f();
		front.x = (float) (Math.cos(Math.toRadians(this.yaw)) * Math.cos(Math.toRadians(this.pitch)));
		front.y = (float) (Math.sin(Math.toRadians(-this.pitch)));
		front.z = (float) (Math.sin(Math.toRadians(this.yaw)) * Math.cos(Math.toRadians(this.pitch)));
		front.normalize();

		this.right = front.cross(worldUp, new Vector3f()).normalize();
		this.up = right.cross(this.front, new Vector3f()).normalize();
	}

	public void mouseCallback(long window, int button, int action, int mods) {
		state.mouseCallback(window, button, action, mods);
	}

	public void scrollCallback(long window, double xOff, double yOff) {
		state.scrollCallback(window, xOff, yOff);
	}

	public void keyCallback(long window, int key, int scancode, int action, int mods) {
		if (action == GLFW.GLFW_RELEASE) {
			if ((mods & (GLFW.GLFW_MOD_SUPER | GLFW.GLFW_MOD_CONTROL)) != 0 && key == GLFW.GLFW_KEY_S) {
				// Save
				SwingHandle swing;
				try {
					swing = new SwingHandle(true); // Java: Write once? RUN FOR YOUR LIFE!
				} catch (Exception e1) {
					e1.printStackTrace();
					return;
				} 

				if (swing.showSaveDialog() == JFileChooser.APPROVE_OPTION) {
					File file = swing.getSelectedFile();
					try {
						IOUtil.saveSimulation(file.getAbsolutePath(), world.createSimulation(false));
					} catch (IOException e) {
						swing.showMessageDialog("Failed to save file "+file.getAbsolutePath());
					}
				}
			} else if ((mods & (GLFW.GLFW_MOD_SUPER | GLFW.GLFW_MOD_CONTROL)) != 0 && key == GLFW.GLFW_KEY_O) {
				// Load
				SwingHandle swing;
				try {
					swing = new SwingHandle(true); // Java: Write once? RUN FOR YOUR LIFE!
				} catch (Exception e1) {
					e1.printStackTrace();
					return;
				} 

				try {
					if (swing.showOpenDialog() == JFileChooser.APPROVE_OPTION) {
						File file = swing.getSelectedFile();
						try {
							world.loadFromSimulation(IOUtil.loadSimulation(file.getAbsolutePath()));
						} catch (IOException e) {
							swing.showMessageDialog("Failed to save file "+file.getAbsolutePath());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		}
		state.keyCallback(window, key, scancode, action, mods);
	}

	public void update(boolean guiHovered) {
		state.update(guiHovered);
		this.updateCameraVectors();

		MVP.resetView();
		MVP.lookAt(this.pos, this.pos.add(this.front, new Vector3f()));

		MVP.perspective(this.fov);
	}

	public void draw() {
		state.draw();
	}

	protected void forward(float dist) {
		pos.z += Math.sin(Math.toRadians(yaw)) * dist;
		pos.x += Math.cos(Math.toRadians(yaw)) * dist;
	}

	protected void right(float dist) {
		pos.z += Math.sin(Math.toRadians(yaw + 90)) * dist;
		pos.x += Math.cos(Math.toRadians(yaw + 90)) * dist;
	}

	protected void pan(float x, float y) {
		pos.add(this.right.mul(x, new Vector3f()));
		pos.add(this.up.mul(y, new Vector3f()));
	}

	protected void upAbs(float dist) {
		pos.y += dist;
	}

	protected void rotate(float yaw, float pitch) {
		this.yaw += yaw;
		this.pitch += pitch;

		this.pitch = Math.min(Math.max(this.pitch, -89.9f), 89.9f);
	}

	public static final Vector3f UP = new Vector3f(0, 1, 0);

	public void lookAt(Vector3f at) {
		Vector3f direction = at.sub(this.pos, new Vector3f()).normalize();
		this.pitch = (float) -Math.toDegrees(Math.asin(direction.y));
		this.yaw = ((float) -Math.toDegrees(Math.atan2(direction.x, direction.z))) + 90;
	}	

	public static class Ray {
		private Vector3f origin;
		private Vector3f dir;

		public Ray(Vector3f origin, Vector3f dir) {
			this.origin = new Vector3f(origin);
			this.dir = dir.normalize(new Vector3f());
		}

		public Vector3f getPoint(float i) {
			return dir.mul(i, new Vector3f()).add(origin);
		}

		public Vector3f getFloorIntersect() {
			float i = -origin.y/dir.y;
			return getPoint(i);
		}
	}

	public void setState(CameraControlState state) {
		this.state = state;
	}


}
