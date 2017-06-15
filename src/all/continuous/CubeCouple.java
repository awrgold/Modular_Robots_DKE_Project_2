package all.continuous;

import javafx.geometry.Point3D;

import java.util.ArrayList;

/**
 * Created by bvsla on 4/22/2017.
 */


public class CubeCouple {
    private Cube cube1;
    private Cube cube2;
    private boolean isPheromoneActive = false;

    public CubeCouple(Cube cube1, Cube cube2){
        this.cube1 = cube1;
        this.cube2 = cube2;
    }

    public Cube getCube1(){
        return cube1;
    }

    public Cube getCube2(){
        return cube2;
    }

    public String toString(){
        if(cube2 instanceof Obstacle) return ("Agent " + cube1.index + "(ID:" + cube1.id + ") and obstacle " + cube2.index + "(ID:" + cube2.id + ")");
        else return ("Agent " + cube1.index + "(ID:" + cube1.id + ") and agent " + cube2.index + "(ID:" + cube2.id + ")");
    }

    public Point3D getCube1Loc(){
        return cube1.getLocation();
    }

    public Point3D getCube2Loc(){
        return cube2.getLocation();
    }

    public void switchPheromoneOn(){
        if(isPheromoneActive == false) isPheromoneActive = true;
    }

}
