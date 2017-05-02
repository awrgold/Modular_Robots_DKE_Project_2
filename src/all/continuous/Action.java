package all.continuous;

import javafx.geometry.Point3D;

public class Action {
    private int agent;
    private Point3D movement;
    private Direction secondaryDirection; //in an upward climb, the direction in which the robot moves after the climb needs to be specified

    public Action(int agent, Point3D movement, Direction secondaryDirection){
        this.agent = agent;
        this.movement = movement;
        this.secondaryDirection = secondaryDirection;
    }
}
