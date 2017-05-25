package ai;

import all.continuous.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import javafx.geometry.Point3D;


/**
 * Created by God on the 8th day, and it was good...
 * 
 * authors : sander, andrew and marion
 * 
 * This class is the first part of our general AI called CooperativeAStar
 * Once the best path has been computed, this class is called. 
 * Indeed, it will move all the agents together towards that path by setting as goal the next position in the path
 * and using a greedy approximation to move each of the agents closer to that goal, until the goal is met by
 * on of them, and has to be updated to the next position.
 */
public class CooperativeModulesMovement {

	private final static boolean DEBUG = true;
 

    public static void moveAlongPath(ArrayList<Point3D> path, ArrayList<Agent> agents, Simulation simulation) throws InvalidMoveException, InvalidStateException{
       
    	if(DEBUG)
    		System.out.println("cooperative movement has started");
    	
    	Comparator<Agent> compare = new PQComparator();
        PriorityQueue<Agent> PQ = new PriorityQueue<>(agents.size(), compare);


        for(int i = 0; i < agents.size(); i++){
            agents.get(i).setIntermediateGoal(path.get(0));
        }

        fillPQ(agents, PQ);
        
        if(DEBUG)
        	System.out.println("PQ size : "+PQ.size());

        Point3D currentGoal = agents.get(0).getIntermediateGoal();
        boolean goalReached = false;
        int pathCounter = 0;
        int counter=0;
        while(!goalReached && counter<15){
        	
            // moving agent if possible and distance to goal is reduced
            for(int i = 0; i < agents.size(); i++){
                Agent first = PQ.poll();
                if(DEBUG)
                {
                	System.out.println("iteration  : "+i);
                	System.out.println("agent number  : "+first.getId());
                	System.out.println("PQ size  : "+PQ.size());
                }
                
                if(isCloser(first, simulation) != null){
                    Action action = isCloser(first, simulation);
                    //first.move(action.getDestination());
                    simulation.apply(action);
                    if(DEBUG)
                    	System.out.println("agent should have moved to  : "+AStarSearch.printPoint(action.getDestination()));
                    if(DEBUG)
                    	System.out.println("agent is at  : "+AStarSearch.printPoint(first.getLocation()));
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
                if(DEBUG)
                	System.out.println("refill PQ");
            }

            simulation.endTurn();
            counter++;
        }
    }

    // method that reduces the valid move that reduces the distance to the intermediate goal the most, null if no such move
    public static Action isCloser(Agent agent, Simulation simulation){
    	
    	if(simulation==null)
    		System.out.println("null sim");
    	
    	if(DEBUG)
    	{
    		System.out.println("config agents size "+simulation.getCurrentConfiguration().getAgents().size());
    		System.out.println("curent agent at pos : "+AStarSearch.printPoint(agent.getLocation()));
    	}
    	
        ArrayList<Action> moves = simulation.getCurrentConfiguration().getAllValidActions(agent);
        for(int  i=0; i<moves.size();i++)
        {
        	System.out.println("possible move : "+AStarSearch.printPoint(moves.get(i).getDestination()));
        }
        if(DEBUG)
        	System.out.println("valid moves size "+moves.size());
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
    public static void fillPQ(ArrayList<Agent> agents, PriorityQueue<Agent> PQ){
        for(int i = 0; i < agents.size(); i++){
            PQ.add(agents.get(i));
        }
    }

    // method to check whether goal has been reached
    public static boolean isGoalReached(ArrayList<Agent> agents, Point3D intermediateGoal){
         for(int i = 0; i < agents.size(); i++){
             if(agents.get(i).getLocation() == intermediateGoal){
                 return true;
             }
        }
        return false;
    }

}
