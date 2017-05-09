package all.continuous.gfx;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import java.util.Stack;

public class MVP {

	private static Matrix4f model, view, projection;
	private static Stack<Matrix4f> modelStack;

	static {
		model = new Matrix4f();
		view = new Matrix4f();
		projection = new Matrix4f();

		modelStack = new Stack<>();
	}

	public static void ortho(Display disp) {
		Vector2f wSize = disp.getSize();
		ortho(0, wSize.x, 0, wSize.y);
	}

	public static void ortho(float left, float right, float top, float bottom) {
		projection.setOrtho(left, right, bottom, top, 0, 1);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
	}

	public static void perspective(float fovy, float aspect) {
		perspective(fovy, aspect, 0.001f, 100);
	}

	public static void perspective(float fovy, float aspect, float zNear, float zFar) {
		projection.setPerspective((float) Math.toRadians(fovy), aspect, zNear, zFar);

		GL11.glEnable(GL11.GL_DEPTH_TEST);
	}

	public static void translate(float x, float y) {
		translate(x, y, 0);
	}

	public static void translate(float x, float y, float z) {
		model.translate(x, y, z);
	}

	public static void translateView(float x, float y) {
		translateView(x, y, 0);
	}

	public static void translateView(float x, float y, float z) {
		view.translate(-x, -y, -z);
	}

	public static void updateShader(ShaderProgram program) {
		program.setMatrix4("model", model);
		program.setMatrix4("view", view);
		program.setMatrix4("projection", projection);
	}

	public static void resetView() {
		view.identity();
	}

	public static void resetModel() {
		model.identity();
	}

	public static void pushTransform() {
		modelStack.push(new Matrix4f(model));
	}
	
	public static void pushTransform(Matrix4f transform) {
		pushTransform();
		model = transform;
	}

	public static void popTransform() {
		model = modelStack.pop();
	}

	public static void rotateX(float f) {
		model.rotateX((float) Math.toRadians(f));
	}
	public static void rotateY(float f) {
		model.rotateY((float) Math.toRadians(f));
	}
	public static void rotateZ(float f) {
		model.rotateZ((float) Math.toRadians(f));
	}

	public static void rotateXView(float f) {
		view.rotateX((float) Math.toRadians(f));
	}
	public static void rotateYView(float f) {
		view.rotateY((float) Math.toRadians(f));
	}
	public static void rotateZView(float f) {
		view.rotateZ((float) Math.toRadians(f));
	}

	public static void scale(float f) {
		model.scale(f);
	}

	/* FIXME: Rewrite for new engine
	public static Vector3f[] castRay(Vector2f pos) {
		pos.y = Display.getSize().y - pos.y();
		Vector3f dest = new Vector3f();
		Vector3f origin = new Vector3f();
		projection.unprojectRay(pos, new int[] { 0, 0, (int) Display.getSize().x, (int) Display.getSize().y }, origin,
				dest);

		Vector4f dest4 = new Vector4f(dest, 0);
		Vector4f origin4 = new Vector4f(origin, 1);

		dest4.mul(view.invert(new Matrix4f()));
		dest.x = dest4.x;
		dest.y = dest4.y;
		dest.z = dest4.z;

		origin4.mul(view.invert(new Matrix4f()));
		origin.x = origin4.x;
		origin.y = origin4.y;
		origin.z = origin4.z;

		return new Vector3f[] { dest, origin };
	}
	
	*/

	public static void setViewTranslation(Vector3f trans) {
		view.translation(trans);
	}

	public static void perspective(float fov) {
		perspective(fov, Display.getInstance().getAspectRatio());
	}

	private static final Vector3f UP = new Vector3f(0, 1, 0);
	public static void lookAt(Vector3f eye, Vector3f at) {
		view.lookAt(eye, at, UP);
	}

	public static Vector3f getRayDir() {
		Vector2f mousePos = Display.getInstance().getMouseCoords(new Vector2f());
		Vector2f size = Display.getInstance().getSize();
		Vector3f ray = new Vector3f();
		projection.unproject(mousePos.x, size.y-mousePos.y, 1, new int[] {0, 0, (int) size.x, (int) size.y}, ray);
		ray.normalize();
		return view.invert(new Matrix4f()).transformDirection(ray);
	}

}
