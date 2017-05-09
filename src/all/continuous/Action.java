package all.continuous;

import javafx.geometry.Point3D;

public class Action {
    public final int agentIndex;
    public final Point3D movement; // Relative?
    
    public Action(int agentIndex, Point3D movement) {
    	this.agentIndex = agentIndex;
    	this.movement = movement;
    }
    
    @Override
    public String toString() {
    	return String.format("Agent %s: moving %s", agentIndex, movement);
    }
}
