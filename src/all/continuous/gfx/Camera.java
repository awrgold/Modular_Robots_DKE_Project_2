package all.continuous.gfx;

import java.util.Collections;

import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

/**
 * Created by Roel on 19-03-17.
 */
public class Camera {
	private static final float SPEED = 0.15F;

	private Vector3f pos;
	private Vector3f front;
	private Vector3f worldUp = new Vector3f(0, 1, 0);
	private Vector3f up;
	private Vector3f right;
	private float yaw, pitch;

	private Vector2f lastMouseCoords;
	private Vector2f newMouseCoords = new Vector2f();
	private Vector2f deltaMouse = new Vector2f();

	private boolean mouseGrabbed = false;

	private double scrollOffset;
	private float fov = 45;

	private boolean editing = false;

	private WorldRenderer world;
	
	private float blockPlaceCooldown;
	
	private Mesh previewMesh;
	
	private boolean panning = false;

	public Camera(WorldRenderer world) {
		this.pos = new Vector3f();
		this.world = world;
		lastMouseCoords = Display.getInstance().getMouseCoords(new Vector2f());
		this.pitch = 20;

		this.updateCameraVectors();

		Display.getInstance().setScrollListener(this::scrollCallback);
		Display.getInstance().setKeyListener(this::keyCallback);
		Display.getInstance().setMouseButtonListener(this::mouseCallback);
		
		this.previewMesh = ShapeFactory.genBox(0, 0, 0, WorldObject.OBJECT_SIZE, WorldObject.OBJECT_SIZE, WorldObject.OBJECT_SIZE);
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
 		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			panning = action == GLFW.GLFW_PRESS;
		}
	}

	public void scrollCallback(long window, double xOff, double yOff) {
		scrollOffset = yOff;
	}

	public void keyCallback(long window, int key, int scancode, int action, int mods) {
		if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_E) this.editing = !this.editing;
		if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_R) {
			float i = 0;
			
			Collections.shuffle(world.getObjects());
			for (WorldObject obj : world.getObjects()) {
				float h = i;
				float x = (float) Math.cos(h*2.5) * (5.5f-h*0.5F);
				float y = (float) Math.sin(h*2.5) * (5.5f-h*0.5F);
				//obj.moveTo(new Vector3f((float)Math.random()*20-10, (float)Math.random()*10, (float)Math.random()*20-10));
				obj.moveTo(new Vector3f(
							x, 
							h*1.3F, 
							y
						));
				i += 0.14;
			}
		}
		
		if (action == GLFW.GLFW_PRESS) {
			if (key == GLFW.GLFW_KEY_1) {
				Display.window.type = ObjectType.MODULE;
			} else if (key == GLFW.GLFW_KEY_2) {
				Display.window.type = ObjectType.GOAL;
			} else if (key == GLFW.GLFW_KEY_3) {
				Display.window.type = ObjectType.OBSTACLE;
			}
		}
	}

	public void update() {
		// TODO: Prevent unnecessary instantiations
		Display.getInstance().getMouseCoords(newMouseCoords);
		newMouseCoords.sub(lastMouseCoords, deltaMouse);
		lastMouseCoords.set(newMouseCoords);

		if (!editing) {
			if (Display.getInstance().isButtonDown(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
				if (!mouseGrabbed) {
					Display.getInstance().setMouseLocked(true);
					mouseGrabbed = true;
				}
				this.yaw += deltaMouse.x * 0.6f;
				this.pitch += deltaMouse.y * 0.6f;

				this.pitch = Math.min(Math.max(this.pitch, -89.9f), 89.9f);

				if (Display.getInstance().isKeyDown(GLFW.GLFW_KEY_W)) {
					pos.z += Math.sin(Math.toRadians(yaw)) * SPEED;
					pos.x += Math.cos(Math.toRadians(yaw)) * SPEED;
				}
				if (Display.getInstance().isKeyDown(GLFW.GLFW_KEY_S)) {
					pos.z -= Math.sin(Math.toRadians(yaw)) * SPEED;
					pos.x -= Math.cos(Math.toRadians(yaw)) * SPEED;
				}

				if (Display.getInstance().isKeyDown(GLFW.GLFW_KEY_D)) {
					pos.z += Math.sin(Math.toRadians(yaw + 90)) * SPEED;
					pos.x += Math.cos(Math.toRadians(yaw + 90)) * SPEED;
				}

				if (Display.getInstance().isKeyDown(GLFW.GLFW_KEY_A)) {
					pos.z += Math.sin(Math.toRadians(yaw - 90)) * SPEED;
					pos.x += Math.cos(Math.toRadians(yaw - 90)) * SPEED;
				}

				if (Display.getInstance().isKeyDown(GLFW.GLFW_KEY_SPACE)) {
					pos.y += SPEED * 0.7f;
				}
				if (Display.getInstance().isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
					pos.y -= SPEED * 0.7f;
				}

				this.updateCameraVectors();

			} else if (panning) {
				if (!mouseGrabbed) {
					Display.getInstance().setMouseLocked(true);
					mouseGrabbed = true;
				}

				pos.add(this.right.mul(deltaMouse.x*SPEED, new Vector3f()));
				pos.add(this.up.mul(-deltaMouse.y*SPEED, new Vector3f()));
			} else {
				if (mouseGrabbed) {
					Display.getInstance().setMouseLocked(false);
					mouseGrabbed = false;
				}
			}
		} else {
			if (blockPlaceCooldown <= 0.01) {
				if (Display.getInstance().isButtonDown(GLFW.GLFW_MOUSE_BUTTON_LEFT)) {
					// TODO: Implement method to detect whether side is already occupied
					Ray ray = new Ray(this.pos, MVP.getRayDir());
					if (Display.getInstance().isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
						Vector3f intersect = world.intersectRayFree(ray, 30);
						Vector3f addPos = new Vector3f((float)(intersect.x-0.5), (float)(intersect.y),(float)(intersect.z-0.5));
						if (!world.pointColliding(new Vector3f(0.5f, 0.5f, 0.5f).add(addPos)))
							world.addObject(new WorldObject(Display.window.getType())).setPosition((float)(intersect.x-0.5), (float)(intersect.y),(float)(intersect.z-0.5));
					} else {
						Vector3f intersect = world.intersectRay(ray, 30);
						Vector3f addPos = new Vector3f((float)Math.floor(intersect.x), (float)Math.floor(intersect.y),(float)Math.floor(intersect.z));
						if (!world.pointColliding(new Vector3f(0.5f, 0.5f, 0.5f).add(addPos)))
							world.addObject(new WorldObject(Display.window.getType())).setPosition((float)Math.floor(intersect.x), (float)Math.floor(intersect.y),(float)Math.floor(intersect.z));
					}
					
					blockPlaceCooldown = 1F;
				} else if (Display.getInstance().isButtonDown(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
					Ray ray = new Ray(this.pos, MVP.getRayDir());
					if (world.deathRay(ray, 30)) blockPlaceCooldown = 1;
				}
			}
		}
		
		if (blockPlaceCooldown > 0) {
			blockPlaceCooldown -= 0.1f;
		}

		this.fov += scrollOffset;

		if (this.fov < 15) this.fov = 15;
		if (this.fov > 50) this.fov = 50;

		MVP.resetView();
		MVP.lookAt(this.pos, this.pos.add(this.front, new Vector3f()));

		MVP.perspective(this.fov);

	}
	
	public void draw() {
		if (editing) {
			Ray ray = new Ray(this.pos, MVP.getRayDir());
			Vector3f intersect = world.intersectRay(ray, 30);
			
			if (Display.getInstance().isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
				intersect = world.intersectRayFree(ray, 30);
				intersect.x -= 0.5;
				intersect.z -= 0.5;
			}
			MVP.pushTransform();
				Vector4f color = WorldObject.getColorForType(Display.window.type);
				color.w /= 2;
				ShaderManager.getInstance().getShader().setVector4("colour", color);
				MVP.translate(intersect.x, intersect.y, intersect.z);
				previewMesh.draw();
			MVP.popTransform();
		}
	}
	
	class Ray {
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
}
