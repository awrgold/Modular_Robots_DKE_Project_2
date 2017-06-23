package all.continuous;

import javafx.geometry.Point3D;

public class Ray {
	private Point3D origin;
	private Point3D direction;
	
	public Ray(Point3D origin, Point3D direction) {
		if (direction.magnitude() == 0)
			throw new IllegalArgumentException("Direction may not be the zero vector");
		this.origin = origin;
		this.direction = direction.normalize();
	}
	
	public Point3D getPoint(double dist) {
		return this.origin.add(this.direction.multiply(dist));
	}
}
