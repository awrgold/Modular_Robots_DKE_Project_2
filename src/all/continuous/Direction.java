package all.continuous;

import javafx.geometry.Point3D;

public class Direction {
	public static final Point3D[] DIRECTIONS = {
			new Point3D(-1, 0, 0), 
			new Point3D(1, 0, 0), 
			
			new Point3D(0, -1, 0), 
			new Point3D(0, 1, 0), 
			
			new Point3D(0, 0, -1), 
			new Point3D(0, 0, 1)
	};
	
	public static final Point3D LEFT = DIRECTIONS[0];
	public static final Point3D RIGHT = DIRECTIONS[1];

	public static final Point3D DOWN = DIRECTIONS[2];
	public static final Point3D UP = DIRECTIONS[3];

	public static final Point3D BACK = DIRECTIONS[4];
	public static final Point3D FRONT = DIRECTIONS[5];
	
	public static final Point3D ZERO = new Point3D(0, 0, 0);
	
	public static Point3D[] getPerpDirs(Point3D dir) {
		Point3D[] perpDirs = new Point3D[4];
		int i = 0;
		for (Point3D dir2 : DIRECTIONS) {
			if (dir.equals(dir2) || ZERO.subtract(dir).equals(dir2)) continue;
			perpDirs[i] = dir2;
			i++;
		}
		return perpDirs;
	}
}
