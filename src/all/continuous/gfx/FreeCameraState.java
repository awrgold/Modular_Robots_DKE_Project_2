package all.continuous.gfx;

import java.util.Collections;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.glfw.GLFW;

import all.continuous.gfx.User.Ray;

public class FreeCameraState implements CameraControlState {
	private static final float SPEED = 0.15F;

	private boolean editing = false;
	private WorldRenderer world;
	private float blockPlaceCooldown;
	private boolean panning = false;
	private User camera;

	private Vector2f lastMouseCoords;
	private Vector2f newMouseCoords = new Vector2f();
	private Vector2f deltaMouse = new Vector2f();

	private boolean mouseGrabbed = false;

	private double scrollOffset;
	
	private Mesh previewMesh;

	public FreeCameraState(WorldRenderer world, User camera) {
		this.world = world;
		this.camera = camera;
		
		this.lastMouseCoords = Display.getInstance().getMouseCoords(new Vector2f());
		this.previewMesh = ShapeFactory.genBox(0, 0, 0, WorldObject.OBJECT_SIZE, WorldObject.OBJECT_SIZE, WorldObject.OBJECT_SIZE);
	}

	@Override
	public void keyCallback(long window, int key, int scancode, int action, int mods) {
		if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_T) { // Tracking
			WorldObject obj = world.pick( new Ray(camera.getPos(), MVP.getRayDir()), 30);
			if (obj == null || obj.getType() != ObjectType.MODULE) return;
			camera.setState(new TrackingCameraState(obj, world, camera));
		}
		if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_E) this.editing = !this.editing;
//		if (action == GLFW.GLFW_PRESS && key == GLFW.GLFW_KEY_R) {
//			float i = 0;
//
//			Collections.shuffle(world.getObjects());
//			for (WorldObject obj : world.getObjects()) {
//				float h = i;
//				float x = (float) Math.cos(h*2.5) * (5.5f-h*0.5F);
//				float y = (float) Math.sin(h*2.5) * (5.5f-h*0.5F);
//				//obj.moveTo(new Vector3f((float)Math.random()*20-10, (float)Math.random()*10, (float)Math.random()*20-10));
//				obj.moveTo(new Vector3f(
//						x, 
//						h*1.3F, 
//						y
//						));
//				i += 0.14;
//			}
//		}

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

	@Override
	public void update(boolean guiHovered) {
		// TODO: Prevent unnecessary instantiations
		Display.getInstance().getMouseCoords(newMouseCoords);
		newMouseCoords.sub(lastMouseCoords, deltaMouse);
		lastMouseCoords.set(newMouseCoords);
		
		if (guiHovered) return;

		if (!editing) {
			if (Display.getInstance().isButtonDown(GLFW.GLFW_MOUSE_BUTTON_RIGHT)) {
				if (!mouseGrabbed) {
					Display.getInstance().setMouseLocked(true);
					mouseGrabbed = true;
				}
				camera.rotate(deltaMouse.x * 0.6f, deltaMouse.y * 0.6f);


				if (Display.getInstance().isKeyDown(GLFW.GLFW_KEY_W)) {
					camera.forward(SPEED);
				}
				if (Display.getInstance().isKeyDown(GLFW.GLFW_KEY_S)) {
					camera.forward(-SPEED);
				}

				if (Display.getInstance().isKeyDown(GLFW.GLFW_KEY_D)) {
					camera.right(SPEED);
				}

				if (Display.getInstance().isKeyDown(GLFW.GLFW_KEY_A)) {
					camera.right(-SPEED);
				}

				if (Display.getInstance().isKeyDown(GLFW.GLFW_KEY_SPACE)) {
					camera.upAbs(SPEED * 0.7f);
				}
				if (Display.getInstance().isKeyDown(GLFW.GLFW_KEY_LEFT_SHIFT)) {
					camera.upAbs(-SPEED * 0.7f);
				}

			} else if (panning) {
				if (!mouseGrabbed) {
					Display.getInstance().setMouseLocked(true);
					mouseGrabbed = true;
				}

				camera.pan(deltaMouse.x*SPEED, -deltaMouse.y*SPEED);
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
					Ray ray = new Ray(camera.getPos(), MVP.getRayDir());
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
					Ray ray = new Ray(camera.getPos(), MVP.getRayDir());
					if (world.deathRay(ray, 30)) blockPlaceCooldown = 1;
				}
			}
		}

		if (blockPlaceCooldown > 0) {
			blockPlaceCooldown -= 0.1f;
		}

		camera.addFov((float) scrollOffset);
	}

	@Override
	public void draw() {
		if (editing) {
			Ray ray = new Ray(camera.getPos(), MVP.getRayDir());
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

	@Override
	public void mouseCallback(long window, int button, int action, int mods) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
			panning = action == GLFW.GLFW_PRESS;
		}
	}

	@Override
	public void scrollCallback(long window, double xOff, double yOff) {
		scrollOffset = yOff;
	}

}
