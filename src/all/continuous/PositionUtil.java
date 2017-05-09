package all.continuous;

import javafx.geometry.Point3D;

public class PositionUtil {
	public static Point3D centerBottom(Point3D cornerPoint) {
		return cornerPoint.add(World.VOXEL_SIZE/2.0, 0, World.VOXEL_SIZE/2.0);
	}
	
	public static Point3D center(Point3D cornerPoint) {
		return cornerPoint.add(World.VOXEL_SIZE/2.0, World.VOXEL_SIZE/2.0, World.VOXEL_SIZE/2.0);
	}

	public static Point3D corner(Point3D centerPoint) {
		return centerPoint.subtract(World.VOXEL_SIZE/2.0, World.VOXEL_SIZE/2.0, World.VOXEL_SIZE/2.0);
	}
}
