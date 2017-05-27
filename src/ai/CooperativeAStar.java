package ai;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import all.continuous.*;
import javafx.geometry.Point3D;
import physics.Physics;

public class CooperativeAStar extends ModuleAlgorithm
{
	public static ArrayList<AStarNode> aStarNodes = new ArrayList<AStarNode>(); 
	private static final boolean DEBUG=true; 
	private static ArrayList<Point3D> path;
	//private static ArrayList<Agent> agents;
	private int pathCounter=1; 
	private static Point3D[][] visited;
	private static int iterations = 0;
	
	
	public static void main(String[] args)throws InvalidMoveException, InvalidStateException
	{TestCases.AStar();}
	public CooperativeAStar(Simulation sim)
	{
		super(sim);
		path = AStarComplete(super.sim);
	}
	public static ArrayList<Point3D> AStarComplete(Simulation sim)
	{
		/*1*/
		ObstacleAnalysis.analyseObstacles(sim.getCurrentConfiguration().getAgents().size(), sim.getCurrentConfiguration(), sim.getTerrain());
		
		/*2*/
		//choose which agent to compute the path on 
		Agent aStarAgent = sim.getCurrentConfiguration().getAgent(0);
		aStarAgent.setIntermediateGoal(sim.getGoalConfiguration().getAgent(0).getLocation());
		Point3D goal = aStarAgent.getIntermediateGoal();
		
		System.out.println("agent start pos : "+AStarSearch.printPoint(aStarAgent.getLocation()));
		//System.out.println("agent start pos : "+AStarSearch.printPoint(aStarAgent.getLocation()));
		
		path = AStarSearch.aStarSearch(aStarAgent, goal, sim.getCurrentConfiguration(), sim.getTerrain());
		
		
		//Set the intermediate goal of all agents to the first position in the path
		//to prepare for the modules movement
		//ArrayList<Agent> agents = sim.getCurrentConfiguration().getAgents();
		 for(int i = 0; i < sim.getCurrentConfiguration().getAgents().size(); i++){
	            sim.getCurrentConfiguration().getAgents().get(i).setIntermediateGoal(path.get(1));
	            if(DEBUG)
	            {
	            	System.out.println("agent number : "+sim.getCurrentConfiguration().getAgents().get(i).getId()+" "
	            			+ " has for intermediate goal "+sim.getCurrentConfiguration().getAgents().get(i).getIntermediateGoal());
	            }
	        }
		
		 //this matrix will store for each agent the nodes it has already visited
		 //i give each agent 100 nodes, 
		 //ATTENTION, can cause future problems if the world gets too big
		visited = new Point3D[100][100]; 
		 
		return path;
		
		
		/*3*/
		
		//CooperativeModulesMovement.moveAlongPath(path, sim.getCurrentConfiguration().getAgents());
	
		
	}
	
	 public  void takeTurn(){
	    
		 ArrayList<Agent> agents=super.sim.getCurrentConfiguration().getAgents();
		 
		 if(DEBUG)
			 System.out.println("path size : "+path.size());

		 		
	    	if(DEBUG)
	    		System.out.println("cooperative movement has started");
	    	
	    	if(DEBUG)
	    	{
	    			System.out.println("agents size "+agents.size());
	    	}
	    	
	    	Comparator<Agent> compare = new PQComparator();
	        PriorityQueue<Agent> PQ = new PriorityQueue<>(agents.size(), compare);

	        if(DEBUG)
	        {if(PQ == null)
	        		System.out.println("pq is null");}
	        
	        fillPQ(agents, PQ);
	        
	        System.out.println("heyyyy");
	        
	        if(DEBUG)
	        	System.out.println("PQ size : "+PQ.size());

	        Point3D currentGoal = agents.get(0).getIntermediateGoal();
	        boolean goalReached = false;
	       // int pathCounter = 0;
	        int counter=0;
	        
	        //ajctions that will be done this time step
	        ArrayList<Action> actions = new ArrayList<Action>();
	       // while(!goalReached && counter<15){
	        	
	            // moving agent if possible and distance to goal is reduced
	            for(int i = 0; i < agents.size(); i++){
	                Agent first = PQ.poll();
	                if(first.hasMoved()) continue;
	                if(DEBUG)
	                {
	                	//System.out.println("iteration  : "+i);
	                	System.out.println("agent number  : "+first.getId());
	                	System.out.println("PQ size  : "+PQ.size());
	                }
	                
	                Action toBeEvaluated = isCloser(first, sim);
	                if(toBeEvaluated != null){
	                    Action action = toBeEvaluated;
	                    //actions.add(action);
	                    //first.move(action.getDestination());
	                    if(isAlreadyAStar(action.getDestination()))
	                    {
	                    	AStarNode node = aStarNodes.get(indexAStar(action.getDestination(), aStarNodes));
	                    	node.addVisitedAgent(first.getId());
	                    }
	                    else
	                    {
	                    	AStarNode node = new AStarNode(action.getDestination(), sim.getCurrentConfiguration(), sim.getTerrain());
	                    	node.addVisitedAgent(first.getId());
	                    }
	                    
	                    
	                    sim.apply(action);
	                    
	                    if(DEBUG)
	                    	System.out.println("agent should have moved to  : "+AStarSearch.printPoint(action.getDestination()));
	                    if(DEBUG)
	                    	System.out.println("agent is at  : "+AStarSearch.printPoint(first.getLocation()));
	                    //break;
	                }
	           }

	            iterations++;
	            // check if the intermediate goal is reached, if so update intermediate goal for all agents
	            if(isGoalReached(agents, currentGoal)){
	               if(currentGoal == path.get(path.size()-1)) {
	                   sim.finish();
	               }
	               else {
	                   pathCounter++;

	                   for(int i = 0; i < agents.size(); i++){
	                       agents.get(i).setIntermediateGoal(path.get(pathCounter));
	                   }
	               }

	            }
	            
	            if(iterations>45)
	            	sim.finish();

	            // place agents back into the priority queue
	            /*for(int i = 0; i < agents.size(); i++){
	                PQ.add(agents.get(i));
	                if(DEBUG)
	                	System.out.println("refill PQ");
	            }*/

	            //sim.endTurn();
	            counter++;
	        
	           /* for(int i=0; i<actions.size(); i++)
	            {
	            	sim.apply(actions.get(i));
	            }*/
	        //sim.finish();
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
	        if(DEBUG)
	        	System.out.println("current distance : "+minDistance);
	        Action best = null;

	        for(int i = 0; i < moves.size(); i++){
	           double newDistance = moves.get(i).getDestination().distance(agent.getIntermediateGoal());
	           
	           if(agentHasVisited(agent, moves.get(i).getDestination()))
	        	   newDistance = newDistance*20;

	           if(DEBUG)
	        	   System.out.println("new move dist : "+newDistance);
	           
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
	        	if(DEBUG)
	        		System.out.println("intermediate goal : "+agents.get(i).getIntermediateGoal());
	            PQ.add(agents.get(i));
	        }
	    }

	    // method to check whether goal has been reached
	    public static boolean isGoalReached(ArrayList<Agent> agents, Point3D intermediateGoal){
	         for(int i = 0; i < agents.size(); i++){
	             if(agents.get(i).getLocation().equals(intermediateGoal)){
	                 return true;
	             }
	        }
	        return false;
	    }
	
	//Given a Point3d, return if it has already been translated to an A* node
    public static boolean isAlreadyAStar(Point3D pos)
    {
    	for(int i=0; i<aStarNodes.size(); i++)
    	{
    		if(aStarNodes.get(i).isTheSameAs(pos))
    			return true; 
    	}
    	
    	return false; 
    }
    
  //From the list of a* nodes, returns the index of the node that corresponds to the given Point3D
    public static int indexAStar(Point3D pos, ArrayList<AStarNode> aStarNodes)
    {
    	for(int i=0; i<aStarNodes.size(); i++)
    	{
    		if(aStarNodes.get(i).isTheSameAs(pos))
    			return i; 
    	}
    	
    	return -1; 
    }
    
    public static boolean agentHasVisited(Agent agent, Point3D location)
    {
    	if(DEBUG)
    		System.out.println("a star nodes size : "+aStarNodes.size() );
    	
    	if(indexAStar(location, aStarNodes) != -1)
    	{
	    	AStarNode node = aStarNodes.get(indexAStar(location, aStarNodes));
	    	
	    	
	    	for(int i=0; i<node.getAgentsVisited().size();i++)
	    	{
	    		if(agent.getId() == node.getAgentsVisited().get(i))
	    			return true;
	    	}
    	}
    	
    	return false; 
    }

   // @Override
  /*  public void takeTurn() {
    	// TODO Auto-generated method stub
    	CooperativeModulesMovement.moveAlongPath(AStarComplete(sim), sim.getCurrentConfiguration().getAgents(), sim);
    	sim.finish();*/
    			

	
	
	
}