package all.continuous;

import javafx.geometry.Point3D;

import java.util.ArrayList;

/**
 * Created by God on the 8th day, and it was good...
 */
public class SuperAgent {

    /*
    This SuperAgent class acts as an observer of all CubeCouples used in the Pheromone experiment.
    It contains:
    - A list of all agent couples
    - Methods to find all pheromones, active or not
    - CubeCouple contains a pheromone switch that turns on pheromone search within SuperAgent class
     */

    private ArrayList<AgentCouple> allAgents;
    private ArrayList<Point3D> activePheromoneTrail;

    public boolean isGoalReached(ArrayList<AgentCouple> agents, Point3D intermediateGoal){
        for(int i = 0; i < agents.size(); i++){
            if(agents.get(i).getAgent1().getPath().equals(intermediateGoal) || agents.get(i).getAgent2().getPath().equals(intermediateGoal)){
                agents.get(i).setPheromoneSwitch();
                return true;
            }
        }
        return false;
    }

    public ArrayList<Point3D> getActivePheromoneTrail(){
        for (AgentCouple ac : allAgents){
            if (ac.isPheromoneActive()){
                return ac.getPheromones();
            }
        }
        System.out.println("**No active pheromone trail found**");
        return null;
    }

    public void addPheromoneTrail(){
        for(int i = 0; i < allAgents.size(); i++){
            for(int j = 0; j < getActivePheromoneTrail().size(); i++){
                if(allAgents.get(i).getAgent1().getLocation().equals(getActivePheromoneTrail().get(j)) || allAgents.get(i).getAgent2().getLocation().equals(getActivePheromoneTrail().get(j))){
                        activePheromoneTrail.addAll(allAgents.get(i).getPheromones());
                }
            }
        }
    }



}
