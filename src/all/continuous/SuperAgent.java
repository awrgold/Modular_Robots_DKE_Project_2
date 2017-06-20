package all.continuous;

import javafx.geometry.Point3D;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by God on the 8th day, and it was good...
 */
public class SuperAgent {

    private int agentAmount;
    private int counter;
    private ArrayList<AgentCouple> allAgents;
    private ArrayList<Point3D> activePheromoneTrail;
    private ArrayList[] activeTrails;

    public SuperAgent(int agentAmount, ArrayList<AgentCouple> allAgents){
        this.allAgents = allAgents;
        this.agentAmount = agentAmount;
        activeTrails = new ArrayList[agentAmount];
    }

    public void addActiveTrail(ArrayList<Point3D> trail){
        if(counter <= agentAmount){
            activeTrails[counter] = trail;
            counter++;
        }
        else System.out.println("Too many trails and not enough agents");
    }

    public boolean isGoalReached(ArrayList<AgentCouple> agents, ArrayList<Point3D> goals){
        for(int i = 0; i < agents.size(); i++){
            for(int j = 0; j < goals.size(); j++){
                if(agents.get(i).getAgent1().getPath().equals(goals.get(j)) || agents.get(i).getAgent2().getPath().equals(goals.get(j))){
                    agents.get(i).setPheromoneSwitch();
                    return true;
                }
            }
        }
        return false;
    }

    public ArrayList<Point3D> getActiveTrail(int index){
         return activeTrails[index];

    }


    // Must be modified for "smell" as it only checks if.equals(location)
    public void checkPheromoneTrail(){
        for(int i = 0; i < allAgents.size(); i++){
            for(int j = 0; j < activeTrails.length; j++){
                for(int k = 0; k < activeTrails[j].size(); k++){
                    if(allAgents.get(i).getAgent1().getLocation().equals(activeTrails[j].get(k)) || allAgents.get(i).getAgent2().getLocation().equals(activeTrails[j].get(k))){
                        if(!allAgents.get(i).isPheromoneActive() && allAgents.get(i).getPathNumber() == -1){
                            allAgents.get(i).setPheromoneSwitch();
                            addActiveTrail(allAgents.get(i).getPheromones());
                            allAgents.get(i).setPathNumber(j);
                            allAgents.get(i).setPositionInPath(k);
                        }
                    }
                }
            }
        }
    }

    public void mergeTrails(){
        for (int i = 0; i < allAgents.size(); i++){
            for (int j = 0; j < activeTrails[allAgents.get(i).getPathNumber()].size(); j++){
                if (allAgents.get(i).getPositionInPath() == activeTrails[allAgents.get(i).getPathNumber()].size()-1){
                    allAgents.get(i).setPathNumber(getPathJunction(allAgents.get(i))[0]);
                    allAgents.get(i).setPositionInPath(getPathJunction(allAgents.get(i))[1]);
                }
            }
        }
    }

    public int[] getPathJunction(AgentCouple ac){
        int[] pathJunction = new int[2];
        for (int i = 0; i < activeTrails.length; i++){
            for (int j = 0; j < activeTrails[i].size(); j++){
                if (ac.getAgent1().getLocation().equals(activeTrails[i].get(j)) || ac.getAgent2().getLocation().equals(activeTrails[i].get(j)) && i != ac.getPathNumber()){
                    pathJunction[0] = i;
                    pathJunction[1] = j;
                    return pathJunction;
                }
            }
        }
        return null;
    }








}
