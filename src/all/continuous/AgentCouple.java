package all.continuous;

import javafx.geometry.Point3D;

import java.awt.*;
import java.util.ArrayList;

import static all.continuous.gfx.Display.sim;

/**
 * Created by God on the 8th day, and it was good...
 */
public class AgentCouple {

    private int agent1Index;
    private int agent2Index;
    private boolean pheromoneSwitch = false;
    private ArrayList<Agent> AgentPair;
    //private ArrayList<Point3D> pheromones;
    private int pathNumber;
    private int positionInPath;
    private boolean reachedGoal;
    //private AgentSenses sense;
    private ArrayList<Point3D> couplePath;


    public AgentCouple(Simulation sim, int agent1Index, int agent2Index) {
        this.agent1Index = agent1Index;
        this.agent2Index = agent2Index;
        reachedGoal=false;
        pathNumber = -1;
        positionInPath=-1;
        couplePath=new ArrayList<Point3D>();
        //sense=new AgentSenses(this);
    }

    public Agent getAgent1(Simulation sim) {
        return sim.getCurrentConfiguration().getAgent(agent1Index);
    }

    public Agent getAgent2(Simulation sim) {
        return sim.getCurrentConfiguration().getAgent(agent2Index);
    }

    public Point3D getAgentLoc(Agent agent){
        return agent.getPath().get(agent.getPath().size()-1);
    }

   /* public void setPheromones(Simulation sim){
    	pheromones = new ArrayList<Point3D>();
        int j = 0;
        for (int i = 0; i < this.getAgent1(sim).getPath().size() + this.getAgent2(sim).getPath().size(); i++){
            pheromones.add(this.getAgent1(sim).getPath().get(i));
            j++;
            pheromones.add(this.getAgent2(sim).getPath().get(i));
            j++;
        }
    }*/

    public ArrayList<Point3D> getPheromones(){
        return couplePath;
    }

    public void setPheromoneSwitch(){
        pheromoneSwitch = true;
    }

    public boolean isPheromoneActive(){
        return pheromoneSwitch;
    }

    public void setPathNumber(int index){
        pathNumber=index;
    }

    public int getPathNumber(){
        return pathNumber;
    }

    public int getPositionInPath(){
        return positionInPath;
    }

    public void setPositionInPath(int pos){
        positionInPath = pos;
    }
    
    public boolean hasReachedGoal(){
    	return reachedGoal;
    }
    
    public void setReachGoal(){
    	reachedGoal = true;
    }
    
    public int getIndex1(){
    	return agent1Index;
    }
    
    public int getIndex2(){
    	return agent2Index; 
    }
    
    public AgentSenses getSense(){
    	return new AgentSenses(this);
    }
    
    public void addCouplePath(Point3D loc){
    	couplePath.add(loc);
    }
    
    

}