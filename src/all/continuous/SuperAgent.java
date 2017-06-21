package all.continuous;

import javafx.geometry.Point3D;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by God on the 8th day, and it was good...
 */
public class SuperAgent {

	private static boolean DEBUG = true; 
	
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
    	if(DEBUG)
    		System.out.println("check if goal reached");
    	for(int i = 0; i < agents.size(); i++){
              for(int j = 0; j < goals.size(); j++){
                  if(agents.get(i).getAgent1().getPath().equals(goals.get(j)) || agents.get(i).getAgent2().getPath().equals(goals.get(j))){
                      agents.get(i).setPheromoneSwitch();
                      if(DEBUG)
                    	  System.out.println("goal reached!!!");
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
    	if(DEBUG)
    		System.out.println("check if any agent has reached an active trail");
    	
        for(int i = 0; i < allAgents.size(); i++){
            for(int j = 0; j < activeTrails.length; j++){
                if(activeTrails[j] != null){
                    for(int k = 0; k < activeTrails[j].size(); k++){
                        if(allAgents.get(i).getAgent1().getLocation().equals(activeTrails[j].get(k)) || allAgents.get(i).getAgent2().getLocation().equals(activeTrails[j].get(k))){
                            if(!allAgents.get(i).isPheromoneActive() && allAgents.get(i).getPathNumber() == -1){
                                allAgents.get(i).setPheromoneSwitch();
                                addActiveTrail(allAgents.get(i).getPheromones());
                                allAgents.get(i).setPathNumber(j);
                                allAgents.get(i).setPositionInPath(k);
                                if(DEBUG)
                                	System.out.println("agent has stepped on an active trail");
                            }
                        }
                    }
                }
            }
        }
    }

    public void mergeTrails(){
    	if(DEBUG)
    		System.out.println("check if an agent needs to pass onto another trail");
        for (int i = 0; i < allAgents.size(); i++){
            if(allAgents.get(i).getPathNumber() > -1){
                for (int j = 0; j < activeTrails[allAgents.get(i).getPathNumber()].size(); j++){
                    if (allAgents.get(i).getPositionInPath() == activeTrails[allAgents.get(i).getPathNumber()].size()-1){
                        if(DEBUG)
                        	System.out.println("old path number : "+allAgents.get(i).getPathNumber());
                    	allAgents.get(i).setPathNumber(getPathJunction(allAgents.get(i))[0]);
                        allAgents.get(i).setPositionInPath(getPathJunction(allAgents.get(i))[1]);
                        if(DEBUG)
                        	System.out.println("new path number : "+allAgents.get(i).getPathNumber());
                    }
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
