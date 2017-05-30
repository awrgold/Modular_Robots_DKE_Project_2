package all.continuous.gfx;

import java.awt.Graphics;
import java.util.Arrays;
import java.util.stream.IntStream;

import org.joml.RayAabIntersection;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;

import static java.util.Comparator.comparingInt;

enum ObjectType {
	OBSTACLE, 
	MODULE, 
	GOAL, 
	INIT
}

public class WorldObject {
	public static final float OBJECT_SIZE = 1.0f;
	private static final float MOVEMENT_SPEED = 10.4f;
	
	public boolean changed;
	
	private Transform transform = new Transform();
	public Transform getTransform() { return transform; }

	private Transform target;
	
	private ObjectType type;
	public ObjectType getType() { return this.type; }
	
	private Mesh mesh;
	
	private static float currentId = 0;
	
	public float id;
	
	public WorldObject() {
		this(ObjectType.MODULE);
	}
	
	public WorldObject(ObjectType type) {
		this.type = type;
		
		this.id = currentId;
		currentId++;
	}
	
	public void addMesh() {
		if (this.mesh != null) return;
		this.mesh = ShapeFactory.genBox(0, 0, 0, 
				OBJECT_SIZE, OBJECT_SIZE, OBJECT_SIZE);
	}
	
	public WorldObject(Mesh mesh) {
		this.mesh = mesh;
	}
	
	public void setPosition(float x, float y, float z) {
		this.transform.setPosition(x, y, z);
	}
	
	public void moveTo(Vector3f newPos) {
		target = new Transform(this.transform);
		target.position = newPos;
	}
	
	public void update(float delta) {
		if (this.target != null) {
			float lerp = 1f*delta*MOVEMENT_SPEED;
			Vector3f dif = this.target.position.sub(this.transform.position, new Vector3f()).mul(lerp);
			this.transform.position.add(dif);
			
			if (dif.length()/lerp < 0.04) {
				this.transform = this.target;
				this.target = null;
			}
		}
	}
	
	public static Vector4f getColorForType(ObjectType type) {
		Vector4f colour = null;
		switch (type) {
		case MODULE:
			colour = new Vector4f(1, 1, 1, 1);
			break;
		case OBSTACLE:
			colour = new Vector4f(0.1f, 0.7f, 0.5f, 0.5f);
			break;
		case GOAL:
			colour = new Vector4f(1.0f, 0.0f, 0.0f, 0.4f);
			break;
		case INIT:
			colour = new Vector4f(0.0f, 0.0f, 0.8f, 0.4f);
			break;
		}
		return colour;
	}
	
	public static final Vector4f RED = new Vector4f(1, 0, 0, 1);
	
	public void render() {
		Vector4f colour = changed ? RED : getColorForType(this.type);
		ShaderManager.getInstance().getShader().setVector4("colour", colour);
		MVP.pushTransform(transform.getMatrix());
			this.mesh.draw();
		MVP.popTransform();
	}

	public int containsPoint(Vector3f point) {
		float xMin = this.transform.position.x;
		float yMin = this.transform.position.y;
		float zMin = this.transform.position.z;
		float xMax = this.transform.position.x+OBJECT_SIZE; 
		float yMax = this.transform.position.y+OBJECT_SIZE; 
		float zMax = this.transform.position.z+OBJECT_SIZE;
		
		if (!(point.x >= xMin && point.x <= xMax &&
				point.y >= yMin && point.y <= yMax &&
				point.z >= zMin && point.z <= zMax)) {
			return -1;
		}
		
		float[] depths = new float[6];
		depths[0] = xMax - point.x; // Right
		depths[1] = OBJECT_SIZE-depths[0]; // Left
		depths[2] = yMax - point.y; // Up
		depths[3] = OBJECT_SIZE-depths[2]; // Down
		depths[4] = zMax - point.z; // Front
		depths[5] = OBJECT_SIZE-depths[4]; // back
		
		return IntStream.range(0, 6)
				.reduce((i,j) -> depths[i] > depths[j] ? j : i)
	            .getAsInt();
	}

	public float getX() {
		return this.transform.position.x;
	}
	
	public float getY() {
		return this.transform.position.y;
	}
	
	public float getZ() {
		return this.transform.position.z;
	}
}
