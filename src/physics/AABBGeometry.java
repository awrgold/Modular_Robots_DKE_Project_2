package physics;

import org.joml.Vector3d;
import org.joml.Vector3dc;

public class AABBGeometry extends Geometry {
	public final Vector3dc min, max;
	
	public AABBGeometry(Vector3d min, Vector3d max) {
		this.min = min.toImmutable();
		this.max = max.toImmutable();
	}
}
