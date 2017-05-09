package all.continuous.gfx;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;

import all.continuous.Agent;
import all.continuous.Configuration;
import all.continuous.Obstacle;
import all.continuous.Simulation;
import all.continuous.Terrain;
import javafx.geometry.Point3D;

public class WorldRenderer {
	private static final int[][] dirs = {
			{1, 0, 0}, 
			{-1, 0, 0}, 
			{0, 1, 0}, 
			{0, -1, 0}, 
			{0, 0, 1}, 
			{0, 0, -1}
	};
	
	private List<WorldObject> objects = new ArrayList<>();
	private Mesh floorMesh;
	private Camera camera;
	
	private long lastTime;
	
	public WorldRenderer() {
		this.camera = new Camera(this);
		this.floorMesh = ShapeFactory.genBox(-50, -0.5f, -50, 100, 0.5f, 100);
		lastTime = System.nanoTime();
	}
	
	public WorldObject addObject(WorldObject obj) {
		this.objects.add(obj);
		return obj;
	}
	
	public void removeObject(WorldObject obj) {
		this.objects.remove(obj);
	}
	
	public void update() {
		float delta = (float) (System.nanoTime() - lastTime)/1000000000.0f;
		lastTime = System.nanoTime();
		
		for (WorldObject obj : this.objects) {
			obj.update(delta);
		}
	}
	
	public void input() {
		this.camera.update();
	}
	
	public Vector3f intersectRay(Camera.Ray ray, float maxDist) {
		for (float i=0.2f; i<=maxDist; i+=0.05f) {
			Vector3f point = ray.getPoint(i);
			for (WorldObject obj : this.objects) {
				int val = obj.containsPoint(point);
				if (val > -1) {
					int[] dir = dirs[val];
					Vector3f pos = new Vector3f(
							Math.round(obj.getX()), 
							Math.round(obj.getY()),
							Math.round(obj.getZ()));
					pos.x += dir[0];
					pos.y += dir[1];
					pos.z += dir[2];
					return pos;
				}
			}
		}
		Vector3f pos =  ray.getFloorIntersect();
		pos.x = (float) Math.floor(pos.x);
		pos.z = (float) Math.floor(pos.z);
		return pos;
	}
	
	public void render() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		ShaderManager.getInstance().getShader().setVector4("colour", new Vector4f(0.2f, 0.7f, 0.4f, 1.0f));
		this.floorMesh.draw();
		for (WorldObject obj : this.objects.stream().filter((e) -> e.getType() != ObjectType.GOAL).toArray(WorldObject[]::new)) {
			obj.render();
		}
		for (WorldObject obj : this.objects.stream().filter((e) -> e.getType() == ObjectType.GOAL).toArray(WorldObject[]::new)) {
			obj.render();
		}
		camera.draw();
	}

	public boolean deathRay(Camera.Ray ray, int maxDist) {
		for (float i=0.2f; i<=maxDist; i+=0.2f) {
			Vector3f point = ray.getPoint(i);
			WorldObject shouldRemove = null;
			for (WorldObject obj : this.objects) {
				int val = obj.containsPoint(point);
				if (val > -1) {
					shouldRemove = obj;
					break;
				}
			}
			
			if (shouldRemove != null) {
				removeObject(shouldRemove);
				return true;
			}
		}
		return false;
	}

	public Vector3f intersectRayFree(Camera.Ray ray, int maxDist) {
		for (float i=0.2f; i<=maxDist; i+=0.2f) {
			Vector3f point = ray.getPoint(i);
			for (WorldObject obj : this.objects) {
				int val = obj.containsPoint(point);
				if (val > -1) {
					return point;
				}
			}
		}
		Vector3f pos =  ray.getFloorIntersect();
		pos.y = Math.min(pos.y, 0);
		return pos;
	}

	public List<WorldObject> getObjects() {
		return this.objects;
	}
	
	public List<WorldObject> getModules() {
		return objects.stream().filter((o) -> o.getType() == ObjectType.MODULE).collect(Collectors.toList());
	}

	public Simulation createSimulation() {
		ArrayList<Agent> agents = new ArrayList<>();
		ArrayList<Agent> goal = new ArrayList<>();
		List<Obstacle> obstacles = new ArrayList<>();
		List<WorldObject> modules = getModules();
		int i = 0;
		for (WorldObject obj : objects) {
			switch (obj.getType()) {
			case MODULE:
				agents.add(new Agent(obj.id, obj.getTransform().toPoint3D()));
				break;
			case OBSTACLE:
				obstacles.add(new Obstacle((float) Math.random(), obj.getTransform().toPoint3D()));
				break;
			case GOAL:
				goal.add(new Agent(modules.get(i).id, obj.getTransform().toPoint3D()));
				i++;
				break;
			}
		}
		Configuration conf = new Configuration(agents);
		Configuration goalConf = new Configuration(goal);
		Terrain terrain = new Terrain(obstacles);
		Simulation sim = new Simulation(terrain, conf, goalConf);
		return sim;
	}

	public void animateTo(Configuration conf) {
		for (WorldObject obj : objects) {
			for (Agent a : conf.getAgents()) {
				if (a.getId() == obj.id) {
					Point3D pos = a.getLocation();
					obj.moveTo(new Vector3f((float)pos.getX(), (float)pos.getY(), (float)pos.getZ()));
					break;
				}
			}
		}
	}

	public boolean pointColliding(Vector3f point) {
		if (point.y <= 0) return true;
		for (WorldObject obj : this.objects) {
			int val = obj.containsPoint(point);
			if (val > -1) {
				return true;
			}
		}
		return false;
	}
}
