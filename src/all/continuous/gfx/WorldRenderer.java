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
import all.continuous.gfx.User.Ray;
import javafx.geometry.Point3D;
import jdk.nashorn.internal.runtime.regexp.joni.Config;

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
	private User camera;
	
	private DisplayWindow window;
	
	private long lastTime;
	
	public WorldRenderer() {
		this.camera = new User(this);
		this.floorMesh = ShapeFactory.genBox(-50, -0.5f, -50, 100, 0.5f, 100);
		lastTime = System.nanoTime();
		
		window = new DisplayWindow();
		Display.cont.addWindow(window);
	}
	
	private List<WorldObject> adding = new ArrayList<>();
	
	public WorldObject addObject(WorldObject obj) {
		this.adding.add(obj);
		return obj;
	}
	
	private List<WorldObject> removing = new ArrayList<>();
	
	public void removeObject(WorldObject obj) {
		this.removing.add(obj);
	}
	
	public void update() {
		float delta = (float) (System.nanoTime() - lastTime)/1000000000.0f;
		lastTime = System.nanoTime();
		
		for (WorldObject obj : this.adding) {
			obj.addMesh();
			this.objects.add(obj);
		}
		for (WorldObject obj : this.removing) {
			this.objects.remove(obj);
		}
		this.adding.clear();
		
		for (WorldObject obj : this.objects) {
			obj.update(delta);
		}
	}
	
	public void input(boolean guiHovered) {
		this.camera.update(guiHovered);
	}
	
	public Vector3f intersectRay(User.Ray ray, float maxDist) {
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
		this.objects.sort((a, b) -> 
			Float.compare(camera.getPos().distance(a.getTransform().position), camera.getPos().distance(b.getTransform().position)));
		if (window.renderModules) renderType(ObjectType.MODULE);
		if (window.renderGoals) renderType(ObjectType.GOAL);
		if (window.renderInits) renderType(ObjectType.INIT);
		if (window.renderObstacles) renderType(ObjectType.OBSTACLE);
		camera.draw();
	}
	
	private void renderType(ObjectType type) {
		this.objects.stream().filter((e) -> e.getType() == type).forEach((obj) -> obj.render());
	}

	public boolean deathRay(User.Ray ray, int maxDist) {
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

	public Vector3f intersectRayFree(User.Ray ray, int maxDist) {
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
		return createSimulation(true);
	}

	public Simulation createSimulation(boolean addInitial) {
		ArrayList<Agent> agents = new ArrayList<>();
		ArrayList<Agent> goal = new ArrayList<>();
		List<Obstacle> obstacles = new ArrayList<>();
		List<WorldObject> modules = getModules();
		int i = 0;
		for (WorldObject obj : objects) {
			switch (obj.getType()) {
			case MODULE:
				agents.add(new Agent(obj.id, obj.getTransform().toPoint3D()));
				if (addInitial) {
				// Add init
					Vector3f trans = obj.getTransform().position;
					addObject(new WorldObject(ObjectType.INIT)).setPosition(trans.x, trans.y, trans.z);
				}
				break;
			case OBSTACLE:
				obstacles.add(new Obstacle((float) Math.random(), obj.getTransform().toPoint3D()));
				break;
			case GOAL:
				//goal.add(new Agent(modules.get(Math.min(i, modules.size()-1)).id, obj.getTransform().toPoint3D()));
				goal.add(new Agent(-1234, obj.getTransform().toPoint3D()));
				i++;
				break;
			case INIT:
				removeObject(obj);
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
			obj.changed = false;
			for (Agent a : conf.getAgents()) {
				if (a.getId() == obj.id) {
					Point3D pos = a.getLocation();
					Vector3f vecPos =new Vector3f((float)pos.getX(), (float)pos.getY(), (float)pos.getZ());
					if (vecPos.equals(obj.getTransform().position)) break;
					obj.changed = true;
					obj.moveTo(vecPos);
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

	public WorldObject pick(Ray ray, int maxDist) {
		for (float i=0.2f; i<=maxDist; i+=0.2f) {
			Vector3f point = ray.getPoint(i);
			for (WorldObject obj : this.objects) {
				int val = obj.containsPoint(point);
				if (val > -1) {
					return obj;
				}
			}
		}
		return null;
	}

	public void loadFromSimulation(Simulation sim) {
		this.objects.clear();
		for (Agent a : sim.getCurrentConfiguration().getAgents()) {
			Point3D pos = a.getLocation();
			addObject(new WorldObject()).setPosition((float)pos.getX(), (float)pos.getY(), (float)pos.getZ());
		}
		
		for (Obstacle a : sim.getTerrain().getObstacles()) {
			Point3D pos = a.getLocation();
			addObject(new WorldObject(ObjectType.OBSTACLE)).setPosition((float)pos.getX(), (float)pos.getY(), (float)pos.getZ());
		}
		
		for (Agent a : sim.getGoalConfiguration().getAgents()) {
			Point3D pos = a.getLocation();
			addObject(new WorldObject(ObjectType.GOAL)).setPosition((float)pos.getX(), (float)pos.getY(), (float)pos.getZ());
		}
	}
}
