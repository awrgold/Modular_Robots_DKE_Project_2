package all.continuous.gfx;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

import org.joml.Matrix3dc;
import org.joml.Matrix3fc;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4fc;
import org.joml.Matrix4x3fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.glfw.GLFW;

public class TrackingCameraState implements CameraControlState {

	private WorldObject obj;
	private WorldRenderer world;
	private User cam;

	public TrackingCameraState(WorldObject obj, WorldRenderer world, User cam) {
		this.obj = obj;
		this.world = world;
		this.cam = cam;
	}

	@Override
	public void keyCallback(long window, int key, int scancode, int action, int mods) {
		if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_T) {
			cam.setState(new FreeCameraState(world, cam));
		}
	}

	@Override
	public void mouseCallback(long window, int button, int action, int mods) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scrollCallback(long window, double xOff, double yOff) {
		// TODO Auto-generated method stub

	}

	@Override
	public void update(boolean guiHovered) {
		this.cam.lookAt(new Vector3f(0.5f, 0.5f, 0.5f).add(obj.getTransform().position));
	}

	@Override
	public void draw() {
		// TODO Auto-generated method stub

	}

}
