package physics;

import org.joml.Vector3d;
import org.joml.Vector3dc;

class Manifold {
	public final Body a;
	public final Body b;
	
	public Manifold(Body a, Body b) {
		this.a = a;
		this.b = b;
	}
	
	public double penetration;
	public Vector3d normal;
}

public class PhysicsUtil {
	public static boolean AABBvsAABB(Manifold manifold) {
		Body a = manifold.a;
		Body b = manifold.b;
		if (a == b)
			throw new IllegalArgumentException("Bodies of manifold should differ");
		if (!(a.getGeometry() instanceof AABBGeometry) || !(b.getGeometry() instanceof AABBGeometry))
			throw new IllegalArgumentException("Both geometries of manifold should be AABB");
		
		AABBGeometry aGeom = (AABBGeometry) a.getGeometry();
		AABBGeometry bGeom = (AABBGeometry) b.getGeometry();
		
		Vector3d aMin = aGeom.min.add(a.getPosition(), new Vector3d());
		Vector3d aMax = aGeom.max.add(a.getPosition(), new Vector3d());;
		Vector3d bMin = bGeom.min.add(b.getPosition(), new Vector3d());;
		Vector3d bMax = bGeom.max.add(b.getPosition(), new Vector3d());;
		
		Vector3d aCenter = aMin.add(aMax, new Vector3d()).div(2.0);
		Vector3d bCenter = bMin.add(bMax, new Vector3d()).div(2.0);
		
		Vector3d n = bCenter.sub(aCenter, new Vector3d());
		
		double aXExtent = (aMax.x() - aMin.x()) / 2.0;
		double bXExtent = (bMax.x() - bMin.x()) / 2.0;
		
		double xOverlap = aXExtent + bXExtent - Math.abs(n.x);
		
		if (xOverlap > 0) { // There exists an x overlap
			double aYExtent = (aMax.y() - aMin.y()) / 2.0;
			double bYExtent = (bMax.y() - bMin.y()) / 2.0;
			
			double yOverlap = aYExtent + bYExtent - Math.abs(n.y);
			
			if (yOverlap > 0) { // There exists a y overlap
				double aZExtent = (aMax.z() - aMin.z()) / 2.0;
				double bZExtent = (bMax.z() - bMin.z()) / 2.0;
				
				double zOverlap = aZExtent + bZExtent - Math.abs(n.z);
				
				if (zOverlap > 0) { // There exists an overlap
					double minOverlap = Math.min(Math.min(xOverlap, yOverlap), zOverlap);
					if (xOverlap == minOverlap) { // x collision
						if (n.x < 0)
							manifold.normal = new Vector3d(-1, 0, 0);
						else
							manifold.normal = new Vector3d(1, 0, 0);
						
						manifold.penetration = xOverlap;
					} else if (yOverlap == minOverlap) { // y collision
						if (n.y < 0)
							manifold.normal = new Vector3d(0, -1, 0);
						else
							manifold.normal = new Vector3d(0, 1, 0);
						
						manifold.penetration = yOverlap;
					} else { // z collision
						if (n.z < 0)
							manifold.normal = new Vector3d(0, 0, -1);
						else
							manifold.normal = new Vector3d(0, 0, 1);
						
						manifold.penetration = zOverlap;
					}
					
					return true; // A collision occurred (and the manifold has been updated)
				}
			}
		}
		return false; // No collision occurred
	}
	
	public static boolean AABBvsFloor(Manifold manifold) {
		Body a = manifold.a;
		Body b = manifold.b;
		AABBGeometry aGeom;
		FloorGeometry bGeom;
		if (a == b)
			throw new IllegalArgumentException("Bodies of manifold should differ");
		if (a.getGeometry() instanceof AABBGeometry && b.getGeometry() instanceof FloorGeometry) {
			aGeom = (AABBGeometry) a.getGeometry();
			bGeom = (FloorGeometry) b.getGeometry();
		} else if (a.getGeometry() instanceof FloorGeometry && b.getGeometry() instanceof AABBGeometry) {
			Body temp = a;
			a = b;
			b = temp;
			aGeom = (AABBGeometry) a.getGeometry();
			bGeom = (FloorGeometry) b.getGeometry();
		} else
			throw new IllegalArgumentException("Geometries in manifold should be AABB and floor");
		
		Vector3d aMin = aGeom.min.add(a.getPosition(), new Vector3d());
		double by = bGeom.y;
		
		if (aMin.y < by) { // There exists y overlap
			manifold.normal = new Vector3d(0, -1, 0);
			manifold.penetration = by - aMin.y;
			return true; // A collision occurred (and the manifold has been updated)
		}
		
		return false; // No collision occurred
	}

	public static boolean bodyVsBody(Manifold manifold) {
		Body a = manifold.a;
		Body b = manifold.b;
		if (a.getGeometry() instanceof AABBGeometry) {
			if (b.getGeometry() instanceof AABBGeometry) {
				return AABBvsAABB(manifold);
			} else if (b.getGeometry() instanceof FloorGeometry) {
				return AABBvsFloor(manifold);
			}
		} else {
			if (b.getGeometry() instanceof AABBGeometry) {
				return AABBvsFloor(manifold);
			} else if (b.getGeometry() instanceof FloorGeometry) {
				throw new IllegalArgumentException("Unsupported collision type: floor vs floor");
			}
		}
		return false;
	}
}
