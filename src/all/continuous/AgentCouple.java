package all.continuous;

import javafx.geometry.Point3D;

import java.awt.*;
import java.util.ArrayList;

/**
 * Created by God on the 8th day, and it was good...
 */
public class AgentCouple {

    private Agent agent1;
    private Agent agent2;
    private boolean pheromoneSwitch = false;
    private ArrayList<Agent> AgentPair;
    private ArrayList<Point3D> pheromones;
    private int pathNumber;
    private int positionInPath;


    public AgentCouple(Agent agent1, Agent agent2) {
        this.agent1 = agent1;
        this.agent2 = agent2;
        pathNumber = -1;
        positionInPath=-1;
    }

    public Agent getAgent1() {
        return agent1;
    }

    public Agent getAgent2() {
        return agent2;
    }

    public Point3D getAgentLoc(Agent agent){
        return agent.getPath().get(agent.getPath().size()-1);
    }

    public void setPheromones(){
        int j = 0;
        for (int i = 0; i < agent1.getPath().size() + agent2.getPath().size(); i++){
            pheromones.add(agent1.getPath().get(i));
            j++;
            pheromones.add(agent2.getPath().get(i));
            j++;
        }
    }

    public ArrayList<Point3D> getPheromones(){
        return pheromones;
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


}