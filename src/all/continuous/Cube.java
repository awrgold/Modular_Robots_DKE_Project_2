package all.continuous;

import javafx.geometry.Point3D;

public abstract class Cube {
    protected float id;
    protected int index;
    protected Point3D location;

    public boolean isCollidingWith(Cube cube){
        Point3D center1 = new Point3D(this.getLocation().getX()+0.5, this.getLocation().getY(), this.getLocation().getZ());
        Point3D center2 = new Point3D(cube.getLocation().getX()+0.5, cube.getLocation().getY(), cube.getLocation().getZ());

        double xDist = Math.abs(center1.getX() - center2.getX());
        double yDist = Math.abs(center1.getY() - center2.getY());
        double zDist = Math.abs(center1.getZ() - center2.getZ());

        return (xDist < 1 && yDist <1 && zDist <1);
    }

    public void setIndex(int index){
        this.index = index;
    }

    public int getIndex(){
        return index;
    }

    public Point3D getLocation(){
        return location;
    }
}
