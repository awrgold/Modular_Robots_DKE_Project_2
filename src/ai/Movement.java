package ai;

import all.continuous.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import javafx.geometry.Point3D;


/**
 * Created by God on the 8th day, and it was good...
 */
public class Movement {

    private Simulation simulation;

    public void moveAlongPath(ArrayList<Point3D> path, ArrayList<Agent> agents) throws InvalidMoveException, InvalidStateException{
        Comparator<Agent> compare = new PQComparator();
        PriorityQueue<Agent> PQ = new PriorityQueue<>(agents.size(), compare);


        for(int i = 0; i < agents.size(); i++){
            agents.get(i).setIntermediateGoal(path.get(0));
        }

        fillPQ(agents, PQ);

        Point3D currentGoal = agents.get(0).getIntermediateGoal();
        boolean goalReached = false;
        int pathCounter = 0;
        while(!goalReached){

            // moving agent if possible and distance to goal is reduced
            for(int i = 0; i < agents.size(); i++){
                Agent first = PQ.poll();
                if(isCloser(first) != null){
                    Action action = isCloser(first);
                    first.move(action.getDestination());
                }
            }

            // check if the intermediate goal is reached, if so update intermediate goal for all agents
            if(isGoalReached(agents, currentGoal)){
               if(currentGoal == path.get(path.size()-1)) {
                   goalReached = true;
               }
               else {
                   pathCounter++;

                   for(int i = 0; i < agents.size(); i++){
                       agents.get(i).setIntermediateGoal(path.get(pathCounter));
                   }
               }

            }

            // place agents back into the priority queue
            for(int i = 0; i < agents.size(); i++){
                PQ.add(agents.get(i));
            }

            simulation.endTurn();
        }
    }

    // method to check whether the valid move chosen reduces distance to the intermediate goal
    public Action isCloser(Agent agent){
        ArrayList<Action> moves = simulation.getCurrentConfiguration().getAllValidActions(agent);
        double minDistance = agent.getManhattanDistanceTo(agent.getIntermediateGoal());
        Action best = null;

        for(int i = 0; i < moves.size(); i++){
           double newDistance = moves.get(i).getDestination().distance(agent.getIntermediateGoal());

           if(minDistance > newDistance) {
               minDistance = newDistance;
               best = moves.get(i);
           }
        }
        return best;

    }

    // priority queue filler
    public void fillPQ(ArrayList<Agent> agents, PriorityQueue<Agent> PQ){
        for(int i = 0; i < agents.size(); i++){
            PQ.add(agents.get(i));
        }
    }

    // method to check whether goal has been reached
    public boolean isGoalReached(ArrayList<Agent> agents, Point3D intermediateGoal){
         for(int i = 0; i < agents.size(); i++){
             if(agents.get(i).getLocation() == intermediateGoal){
                 return true;
             }
        }
        return false;
    }

}
