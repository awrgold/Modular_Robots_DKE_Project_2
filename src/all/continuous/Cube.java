package all.continuous;

import javafx.geometry.Point3D;

import static all.continuous.Direction.*;

public abstract class Cube {
    protected float id;
    protected int index;
    protected Point3D location;

    public boolean isCollidingWith(Cube cube){
        return (Math.abs(getXDist(cube))<1 && Math.abs(getYDist(cube))<1 && Math.abs(getZDist(cube))<1);
    }

    public Direction isAttachedTo(Cube cube){
        if(getXDist(cube)==1 && Math.abs(getYDist(cube))<1 && Math.abs(getZDist(cube))<1) return LEFT;
        if(getXDist(cube)==-1 && Math.abs(getYDist(cube))<1 && Math.abs(getZDist(cube))<1) return RIGHT;

        if(Math.abs(getXDist(cube))<1 && getYDist(cube)==1 && Math.abs(getZDist(cube))<1) return BACKWARD;
        if(Math.abs(getXDist(cube))<1 && getYDist(cube)==-1 && Math.abs(getZDist(cube))<1) return FORWARD;

        if(Math.abs(getXDist(cube))<1 && Math.abs(getYDist(cube))<1 && getZDist(cube)==1) return DOWNWARD;
        if(Math.abs(getXDist(cube))<1 && Math.abs(getYDist(cube))<1 && getZDist(cube)==-1) return UPWARD;

        return null;
    }

    public double getXDist(Cube cube){
        return (this.location.getX()+0.5 - cube.location.getX()+0.5);
    }

    public double getYDist(Cube cube){
        return (this.location.getY()+0.5 - cube.location.getY()+0.5);
    }

    public double getZDist(Cube cube){
        return (this.location.getZ()+0.5 - cube.location.getZ()+0.5);
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
