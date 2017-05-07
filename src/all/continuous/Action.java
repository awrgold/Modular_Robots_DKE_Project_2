package all.continuous;

import javafx.geometry.Point3D;

public class Action {
    private int agent;
    private Point3D destination;
    private Direction secondaryDirection;
    //in an upward climb, the direction in which the robot moves after the climb needs to be specified
    
    //This is the cost of an action. By defalut = 1.
    //Will be updates during A Star for the obstacle climbing. 
    //Will be used in A* as GScore
    private int weight; 

    public Action(int agent, Point3D destination, Direction secondaryDirection){
        this.agent = agent;
        this.destination = destination;
        this.secondaryDirection = secondaryDirection;
        //by default, we assume the action has a weight of 1 
        weight=1;
    }
    
    public Action(Point3D destination, Direction secondaryDirection, int weight)
    {
    	// this.agent = agent;
         this.destination = destination;
         this.secondaryDirection = secondaryDirection;
         this.weight=weight;
    }
    
    public int getWeight(){return weight;}
    public void setWeight(int weight){
    	this.weight=weight; 
    }
    
    public Point3D getDestination(){return destination;}
}
