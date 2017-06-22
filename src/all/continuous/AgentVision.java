package all.continuous;

import javafx.geometry.Point3D;
import org.joml.Vector3d;

/**
 * Created by God on the 8th day, and it was good...
 */
public class AgentVision {

    private Simulation sim;
    private AgentCouple agents;
    private Point3D agent1Loc;
    private Point3D agent2Loc;


    public void updateAgentsLoc(){
        Vector3d vector1 = agents.getAgent1(sim).getPosition();
        agent1Loc = new Point3D(vector1.x, vector1.y, vector1.z);
        Vector3d vector2 = agents.getAgent2(sim).getPosition();
        agent2Loc = new Point3D(vector2.x, vector2.y, vector2.z);
    }

    public Point3D comparePos(){
        if (agent1Loc.getX() > agent2Loc.getX()){
            return agent1Loc;
        }
        if (agent2Loc.getX() > agent1Loc.getX()){
            return agent2Loc;
        }
        if (agent1Loc.getZ() > agent2Loc.getZ()){
            return agent1Loc;
        }
        if (agent2Loc.getZ() > agent1Loc.getZ()){
            return agent2Loc;
        }

    }

    public void visualSearch(AgentCouple agents, Configuration conf, Simulation sim){
        // Cast Ray in 180 degree search field from front agent in "agents"
        Ray ray = new Ray;
        if (Ray )

        // if object (obstacle, agent, goal) found, get all valid actions and choose move based on manhattan distance to that object

        // else apply another valid action (forwards? What movement heuristic?)


    }


}
