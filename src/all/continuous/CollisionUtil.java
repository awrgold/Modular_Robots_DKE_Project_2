package all.continuous;

import javafx.geometry.Point3D;

import java.util.ArrayList;
import java.util.List;

public class CollisionUtil {
	public static class Collision {
		public final CollisionType type;
		public final Point3D location;
		public final Cube collided;

		public Collision(CollisionType type, Point3D location) {
			this(type, location, null);
		}

		public Collision(CollisionType type, Point3D location, Cube collided) {
			this.type = type;
			this.location = location;
			this.collided = collided;
		}

		public Point3D getLocation(){
			return location;
		}
	}

	private static final double DEFAULT_DELTA = 0.05;
	private static final boolean DEBUG = false;
	
	public static Collision castRay(Configuration conf, Ray ray, Agent exclude) {
		return castRay(conf, ray, DEFAULT_DELTA, 1.0, 0.0, exclude);
	}

	public static Collision castRay(Configuration conf, Ray ray, double delta, double maxDist, double minDist, Agent exclude) {
		return castRay(conf, ray, delta, maxDist, minDist, exclude, false);
	}

	public static Collision castRay(Configuration conf, Ray ray, double delta, double maxDist, double minDist, Agent exclude, boolean includeObs) {
		for (double dist=minDist; dist<=maxDist; dist+=delta) {
			Point3D p = ray.getPoint(dist);
			Collision c = isColliding(conf, p, exclude, includeObs);
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

	public static Collision isColliding(Configuration conf, Point3D point, Agent exclude, boolean includeObs) {
		double vSize = World.VOXEL_SIZE;

		if (includeObs) {
			for (Obstacle agent : conf.simulation.getTerrain().obstacles) {
				Point3D min = agent.location;
				Point3D max = min.add(vSize, vSize, vSize);
				if (isColliding(min, max, point)) 
					return new Collision(CollisionType.OBSTACLE, point, agent);
			}
		}

		// Check agent collision
		for (Agent agent : conf.agents) {
			if (agent == exclude) continue;

			Point3D min = agent.location;
			Point3D max = min.add(vSize, vSize, vSize);
			if (isColliding(min, max, point))
				return new Collision(CollisionType.AGENT, point, agent);
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
	
	private static final double epsilon2 = 0.15;
	
	public static List<Collision> isCollidingCubeMult(Configuration conf, Point3D point, Agent exclude) {
		double vSize = World.VOXEL_SIZE;

		Point3D minB = point;
		Point3D maxB = minB.add(vSize, vSize, vSize);
		minB.add(epsilon2, epsilon2, epsilon2);
		maxB.subtract(epsilon2, epsilon2, epsilon2);
		
		List<Collision> result = new ArrayList<>();

		if (minB.getY() < 0) {
			result.add(new Collision(CollisionType.OBSTACLE, point));
		}

		// Check agent collision
		for (Agent agent : conf.agents) {
			if (agent.equals(exclude)) continue;

			Point3D minA = agent.location;
			Point3D maxA = minA.add(vSize, vSize, vSize);

			minA.add(epsilon, epsilon, epsilon);
			maxA.subtract(epsilon, epsilon, epsilon);

			if (isColliding(minA, maxA, minB, maxB))
				result.add(new Collision(CollisionType.AGENT, point, agent));
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

			if (isColliding(minA, maxA, minB, maxB)) result.add(new Collision(CollisionType.OBSTACLE, point, obs));
		}
		
		return result;
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
