package all.continuous;

import all.continuous.CollisionUtil.Collision;
import javafx.geometry.Point3D;

import java.util.ArrayList;

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
	private static final boolean DEBUG = false;
	
	public static Collision castRay(Configuration conf, Ray ray, Agent exclude) {
		return castRay(conf, ray, DEFAULT_DELTA, 1.0, 0.0, exclude);
	}
	
	public static Collision castRay(Configuration conf, Ray ray, double delta, double maxDist, double minDist, Agent exclude) {
		for (double dist=minDist; dist<=maxDist; dist+=delta) {
			Point3D p = ray.getPoint(dist);
			Collision c = isColliding(conf, p, exclude);
			if (c.type != CollisionType.NONE) return c;
		}
		return new Collision(CollisionType.NONE, ray.getPoint(maxDist));
	}
	
	public static Collision castRayCube(Configuration sim, Ray ray, Agent exclude) {
		return castRayCube(sim, ray, DEFAULT_DELTA*0.1, 1.0, 0.0, exclude);
	}
	
	public static Collision castRayCube(Configuration sim, Ray ray, double delta, double maxDist, double minDist, Agent exclude) {
		for (double dist=minDist; dist<=maxDist; dist+=delta) {
			Point3D p = ray.getPoint(dist);
			
			if(DEBUG)
			{
				if(p == null)
				System.out.println("castRayCune point is null");
			}
			Collision c = isCollidingCube(sim, p, exclude);
			if (c.type != CollisionType.NONE) return c;
		}
		return new Collision(CollisionType.NONE, ray.getPoint(maxDist));
	}

	public static Collision castRayCubeFalling(Configuration sim, Ray ray, double delta, double maxDist, double minDist, Agent exclude) {
		ArrayList<Collision> list = new ArrayList<>();
		for (double dist=minDist; dist<=maxDist; dist+=delta) {
			Point3D p = ray.getPoint(dist);
			Collision c = isCollidingCube(sim, p, exclude);
			list.add(c);
			if (c.type != CollisionType.NONE){
				if(list.size() > 2) return list.get(list.size() - 2);
				else return list.get(0);
			}
		}
		return new Collision(CollisionType.NONE, ray.getPoint(maxDist));
	}
	
	private static final double epsilon = 0.000;

	public static Collision isColliding(Configuration conf, Point3D point, Agent exclude) {
		double vSize = World.VOXEL_SIZE;
		
		// Check agent collision
		for (Agent agent : conf.agents) {
			if (agent == exclude) continue;
			
			Point3D min = agent.location;
			Point3D max = min.add(vSize, vSize, vSize);
			if (isColliding(min, max, point)) return new Collision(CollisionType.AGENT, point);
		}
		
		return new Collision(CollisionType.NONE, null);
	}
	
	private static Collision isCollidingCube(Configuration conf, Point3D point, Agent exclude) {
		double vSize = World.VOXEL_SIZE;
		
		Point3D minB = point;
		Point3D maxB = minB.add(vSize, vSize, vSize);
		minB.add(epsilon, epsilon, epsilon);
		maxB.subtract(epsilon, epsilon, epsilon);
		
		if (minB.getY() < 0) return new Collision(CollisionType.OBSTACLE, point);
		
		// Check agent collision
		for (Agent agent : conf.agents) {
			if (agent.equals(exclude)) continue;
			
			Point3D minA = agent.location;
			Point3D maxA = minA.add(vSize, vSize, vSize);
			
			minA.add(epsilon, epsilon, epsilon);
			maxA.subtract(epsilon, epsilon, epsilon);
			
			if (isColliding(minA, maxA, minB, maxB))
				return new Collision(CollisionType.AGENT, point);
		}
		
		if(DEBUG)
		{
			if(conf == null)
				System.out.println("conf is null");
			if(conf.getSimulation() == null)
				System.out.println("conf sim is null");
			if(conf.getSimulation().getTerrain() == null)
				System.out.println("terrain is ull");
			if(conf.getSimulation().getTerrain().getObstacles() == null)
				System.out.println("obstacles is null");
		}
		
		for (Obstacle obs : conf.getSimulation().getTerrain().obstacles) {
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
