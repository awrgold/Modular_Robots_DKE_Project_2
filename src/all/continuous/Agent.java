package all.continuous;

import javafx.geometry.Point3D;

import java.util.ArrayList;

public class Agent extends Cube {
    private boolean moved = false;

    public Agent(float id, Point3D location){
        this.id = id;
        this.location = location;
    }

    public Agent copy(){
        Agent newAgent = new Agent(this.id, this.location);
        newAgent.index = this.index;
        return newAgent;
    }

    public void move(Point3D location) throws InvalidMoveException{
        if(!moved){
            this.location = location;
            this.moved = true;
        } else throw new InvalidMoveException("Tried to move an agent that has already moved this turn!");

    }
}
