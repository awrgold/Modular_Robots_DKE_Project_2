package all.continuous;

import all.continuous.CollisionUtil.Collision;
import javafx.geometry.Point3D;

enum CollisionType {
	NONE, 
	OBSTACLE, 
	AGENT
}

public class CollisionUtil {
	static class Collision {
		public final CollisionType type;
		public final Point3D location;
		
		public Collision(CollisionType type, Point3D location) {
			this.type = type;
			this.location = location;
		}
	}

	private static final double DEFAULT_DELTA = 0.05;
	
	public static Collision castRay(Simulation sim, Ray ray, Agent exclude) {
		return castRay(sim, ray, DEFAULT_DELTA, 1.0, 0.0, exclude);
	}
	
	public static Collision castRay(Simulation sim, Ray ray, double delta, double maxDist, double minDist, Agent exclude) {
		for (double dist=minDist; dist<=maxDist; dist+=delta) {
			Point3D p = ray.getPoint(dist);
			Collision c = isColliding(sim, p, exclude);
			if (c.type != CollisionType.NONE) return c;
		}
		return new Collision(CollisionType.NONE, ray.getPoint(maxDist));
	}
	
	public static Collision castRayCube(Simulation sim, Ray ray, Agent exclude) {
		return castRayCube(sim, ray, DEFAULT_DELTA*0.1, 1.0, 0.0, exclude);
	}
	
	public static Collision castRayCube(Simulation sim, Ray ray, double delta, double maxDist, double minDist, Agent exclude) {
		for (double dist=minDist; dist<=maxDist; dist+=delta) {
			Point3D p = ray.getPoint(dist);
			Collision c = isCollidingCube(sim, p, exclude);
			if (c.type != CollisionType.NONE) return c;
		}
		return new Collision(CollisionType.NONE, ray.getPoint(maxDist));
	}
	
	private static final double epsilon = 0.000;

	public static Collision isColliding(Simulation sim, Point3D point, Agent exclude) {
		double vSize = World.VOXEL_SIZE;
		
		// Check obstacle collision
//		for (Obstacle obs : sim.getTerrain().obstacles) {
//			Point3D min = obs.location;
//			Point3D max = min.add(vSize, vSize, vSize);
//			if (isColliding(min, max, point)) return new Collision(CollisionType.OBSTACLE, point);
//		}
		
		// Check agent collision
		for (Agent agent : sim.getCurrentConfiguration().agents) {
			if (agent == exclude) continue;
			
			Point3D min = agent.location;
			Point3D max = min.add(vSize, vSize, vSize);
			if (isColliding(min, max, point)) return new Collision(CollisionType.AGENT, point);
		}
		
		return new Collision(CollisionType.NONE, null);
	}
	
	private static Collision isCollidingCube(Simulation sim, Point3D point, Agent exclude) {
		double vSize = World.VOXEL_SIZE;
		
		// Check obstacle collision
//		for (Obstacle obs : sim.getTerrain().obstacles) {
//			Point3D min = obs.location;
//			Point3D max = min.add(vSize, vSize, vSize);
//			if (isColliding(min, max, point)) return new Collision(CollisionType.OBSTACLE, point);
//		}
		
		Point3D minB = point;
		Point3D maxB = minB.add(vSize, vSize, vSize);
		minB.add(epsilon, epsilon, epsilon);
		maxB.subtract(epsilon, epsilon, epsilon);
		
		if (minB.getY() < 0) return new Collision(CollisionType.OBSTACLE, point);
		
		// Check agent collision
		for (Agent agent : sim.getCurrentConfiguration().agents) {
			if (agent == exclude) continue;
			
			Point3D minA = agent.location;
			Point3D maxA = minA.add(vSize, vSize, vSize);
			
			minA.add(epsilon, epsilon, epsilon);
			maxA.subtract(epsilon, epsilon, epsilon);
			
			if (isColliding(minA, maxA, minB, maxB)) return new Collision(CollisionType.AGENT, point);
		}
		
		for (Obstacle obs : sim.getTerrain().obstacles) {
			Point3D minA = obs.location;
			Point3D maxA = minA.add(vSize, vSize, vSize);
			
			minA.add(epsilon, epsilon, epsilon);
			maxA.subtract(epsilon, epsilon, epsilon);
			
			if (isColliding(minA, maxA, minB, maxB)) return new Collision(CollisionType.OBSTACLE, point);
		}
		
		return new Collision(CollisionType.NONE, null);
	}

	private static boolean isColliding(Point3D min, Point3D max, Point3D point) {
		return point.getX() >= min.getX() && point.getY() >= min.getY() && point.getZ() >= min.getZ() &&
					point.getX() <= max.getX() && point.getY() <= max.getY() && point.getZ() <= max.getZ();
	}
	
	private static boolean isColliding(Point3D minA, Point3D maxA, Point3D minB, Point3D maxB) {
		return !(maxA.getX() <= minB.getX() || maxB.getX() <= minA.getX() || 
				maxA.getY() <= minB.getY() || maxB.getY() <= minA.getY() || 
				maxA.getZ() <= minB.getZ() || maxB.getZ() <= minA.getZ());
	}
}