package ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import org.joml.Vector3d;

import all.continuous.Action;
import all.continuous.Agent;
import all.continuous.AgentAction;
import all.continuous.Configuration;
import all.continuous.ModuleAlgorithm;
import all.continuous.Simulation;
import javafx.geometry.Point3D;

public class SimpleAI extends ModuleAlgorithm
{
	private static final boolean DEBUG = true;

	private static ArrayList<Point3D> reachedGoals = new ArrayList<>();
	private static ArrayList<Point3D> visited = new ArrayList<Point3D>();
	private static float currentAgentID=0;
	
	private static PriorityQueue<Agent> PQ; 
	
	private int iterations=0;
	
	ArrayList<Agent> agents;

	private Point3D origin;
	
	private int unsuccessfulTurns;
	
	private static ArrayList<Float> notToBeAdded;

	public SimpleAI(Simulation sim)
	{
		super(sim);
		
	}
	@Override
	public void takeTurn() {
		// TODO Auto-generated method stub
		
		
		//fill the agents array with the sim agents
		agents=sim.getCurrentConfiguration().getAgents();
		
		//set their respective intermediate goals
		/*for(int i=0; i<agents.size(); i++)
		{agents.get(i).setIntermediateGoal(sim.getGoalConfiguration().getAgent(i).getLocation());}*/
		

		if(iterations==0)
		{
			origin = agents.get(0).getLocation();
			for(int i=0; i<agents.size(); i++)
			{
				agents.get(i).setIntermediateGoal(getFurthestGoal(sim));
			}

			unsuccessfulTurns=0;
			notToBeAdded = new ArrayList<Float>();

		}


		//FILL THE PQ WITH AGENTS, BASED ON THEIR DISTANCE TO RESPECTIVE GOAL
		Comparator<Agent> compare = new PQComparator();
        PQ = new PriorityQueue<>(agents.size(), compare);

        /*if(DEBUG)
        {if(PQ == null)
        		System.out.println("pq is null");}*/
        
        
        
        //This keeps track of the ID of the agent we're currently moving
       
        //if it's the first iteration, fill the PQ entirely and store the id of the furthest agent
		//set their respective intermediate goals
		/*
		Once the furthest goal is occupied by an agent (call it agent0), that agent's movements become EXPENSIVE.
		- From this point, the next furthest available goal becomes the new target for agent1
		- Once agent1 finds its goal, agent2 searches for the NEXT furthest goal, and so on until all goals are occupied.
		 */
        if(iterations==0)
        {
        	fillPQ(agents, PQ, sim);
        	currentAgentID = PQ.poll().getId();
        	if(DEBUG)
        		System.out.println("furthest agent : "+currentAgentID);
		}
        //if it's not the first, add all the new agents to the PQ, except the one we're working with
        else
        {
        	for(int i=0; i<agents.size(); i++)
        	{
        		if(agents.get(i).getId()!=currentAgentID && !isIn(agents.get(i).getId()))
        			PQ.add(agents.get(i));
        	}
        }
		
        if(DEBUG)
			System.out.println("current agent : "+currentAgentID);
        
        //print agents location
        /*for(int i=0; i<agents.size();i++)
        {System.out.println("agent at "+agents.get(i).getLocation());}*/
        
        //Get the agent we were moving at the previous turn
        Agent currentAgent = findAgentWithID(currentAgentID, agents);
        
        //if none of the agents can move, force one to move
        //IDEA : to be om
        if(unsuccessfulTurns>=agents.size()+1)
        {
        	if(DEBUG)
        		System.out.println("AGENTS STUCK");
        	AgentAction action = getSmallerActionWhenStuck(currentAgent, sim);
        	if(!isAGoal(currentAgent.getPointPosition(), sim) && !currentAgent.getPointPosition().equals(currentAgent.getIntermediateGoal()) && !currentAgent.hasMoved() && action !=null)
        	{
        		sim.applyPhysical(action);
        		currentAgent.addPath(getDestination(action, sim.getCurrentConfiguration()));
            	unsuccessfulTurns=0;
            	if(PQ.size()==0)
            		sim.finish();
            	currentAgent = PQ.poll();
	    		currentAgentID = currentAgent.getId();
	    		//unsuccessfulTurns++;
	    		if(DEBUG)
	    			System.out.println("next agent : "+currentAgentID);
        	}
        	
        	else
        	{
        		notToBeAdded.add(currentAgentID);
        		
        		if(DEBUG)
        			System.out.println("PQ size : "+PQ.size());
        		
        		if(PQ.size()==0)
        			sim.finish();
        		else
        		{
	    		currentAgent = PQ.poll();
	    		currentAgentID = currentAgent.getId();
	    		unsuccessfulTurns++;
	    		if(DEBUG)
	    			System.out.println("next agent : "+currentAgentID);
        		}
	    		
        	}
        	
        }
        else
        {
    	
        	notToBeAdded.clear();
	        
	    	//Check if there is an action that would bring it closer to its goal
	    
	        AgentAction best = isCloser(currentAgent, sim);
	    	//if there is an action that brings the agent closer to the goal
	    	if(!isAGoal(currentAgent.getLocation(), sim) &&!currentAgent.getLocation().equals(currentAgent.getIntermediateGoal()) && !currentAgent.hasMoved() && best != null)
	    	{
	    		if(DEBUG)
	    			System.out.println("agent moves to "+getDestination(best, sim.getCurrentConfiguration()));
	    		sim.applyPhysical(best);
	    		currentAgent.addPath(getDestination(best, sim.getCurrentConfiguration()));
	    		//we set the number of unsuccessful turns back to 0 
	    		unsuccessfulTurns=0;
	    		//add the position to the list of visited positions, so we can later update the weight of that move
	    		//visited.add(best.getDestination());
	    	}
	        
	    	//if there is NO action that brings the agent closer to the goal --> change agent
	    	else
	    	{
	    		
	    		//Agent previous = currentAgent;
	    		//new furthest agent
	    		if(PQ.size()==0)
	    			sim.finish();
	    		else
	    		{
	    		currentAgent = PQ.poll();
	    		currentAgentID = currentAgent.getId();
	    		unsuccessfulTurns++;
	    		if(DEBUG)
	    			System.out.println("next agent : "+currentAgentID);
	    		}
	    		//put the agent we just moved back in the PQ
	    		//PQ.add(previous);
	    		
	    		
	    	}
        }
    	
    	
    	iterations++;
    	if(DEBUG)
    		System.out.println("iterations : "+iterations);
    	
    	//this is just for stopping my infinite for loop
    	if(iterations>500)
    		sim.finish();
    	
    	//if the agent we're working with finds its goal, we stop the simulation
    	if(currentAgent.getPointPosition().equals(currentAgent.getIntermediateGoal()) || isAGoal(currentAgent.getPointPosition(), sim)){
    		reachedGoals.add(currentAgent.getPointPosition());
    		for (int i = 0; i < agents.size(); i++)
    		{
    			if(!reachedGoals.contains(agents.get(i).getPointPosition()))
    			agents.get(i).setIntermediateGoal(getFurthestGoal(sim));
			}
		}
    	
    	
        
		
	}


	public Point3D getFurthestGoal(Simulation sim){
		double maxDist = -1;
		Point3D pos = null;

		for(int i = 0; i < sim.getGoalConfiguration().getAgents().size(); i++){
			Agent currentGoal = sim.getGoalConfiguration().getAgent(i);
			if (!reachedGoals.contains(currentGoal) && (currentGoal.getManhattanDistanceTo(origin) > maxDist)){
				maxDist = currentGoal.getManhattanDistanceTo(origin);
				pos = currentGoal.getLocation();
			}
		}
		return pos;
	}


	// priority queue filler
    public static void fillPQ(ArrayList<Agent> agents, PriorityQueue<Agent> PQ, Simulation sim){
        for(int i = 0; i < agents.size(); i++){
        	if(DEBUG) {
        		System.out.println("PQ intermediate goal : "+agents.get(i).getIntermediateGoal());
        		System.out.println("dist to goql : "+agents.get(i).getManhattanDistanceTo(agents.get(i).getIntermediateGoal()));
			}
        	if(!isAGoal(agents.get(i).getLocation(), sim))
            PQ.add(agents.get(i));
        }
    }
    
    public static boolean isIn(float id)
    {
    	for(int i=0; i<notToBeAdded.size(); i++)
    	{
    		if(notToBeAdded.get(i).equals(id))
    			return true; 
    	}
    	
    	return false; 
    }
    // method that returns the valid move that reduces the distance to the intermediate goal the most, null if no such move
    public static AgentAction isCloser(Agent agent, Simulation sim){
    	
    	/*if(DEBUG)
    	{
    		System.out.println("config agents size "+simulation.getCurrentConfiguration().getAgents().size());
    		System.out.println("current agent at pos : "+agent.getLocation());
    	}*/
    	if(DEBUG)
    	System.out.println("current agent at pos : "+agent.getLocation());
    	
    	//get the valid moves
        ArrayList<AgentAction> moves = sim.getCurrentConfiguration().getPhysicalActions(agent);
        for(int  i=0; i<moves.size();i++)
        {System.out.println("possible move : "+getDestination(moves.get(i), sim.getCurrentConfiguration()));}
        /*if(DEBUG)
        	System.out.println("valid moves size "+moves.size());*/
        
        //get the current distance to the goal
        double minDistance = agent.getManhattanDistanceTo(agent.getIntermediateGoal());
        if(DEBUG)
        	System.out.println("current distance : "+minDistance);
        AgentAction best = null;

        //check if one of the moves reduces that current minimal distance to goal
        for(int i = 0; i < moves.size(); i++){
           double newDistance = getManhattanDistPoint3D(getDestination(moves.get(i), sim.getCurrentConfiguration()) , agent.getIntermediateGoal());
           
           //increase weight of distance if the agent has already visited that position
           if(agentHasVisited(agent,getDestination(moves.get(i), sim.getCurrentConfiguration())))
        	   newDistance = newDistance*20;
           
           /*if(agent.getLocation().equals(agent.getIntermediateGoal()))
        		   newDistance = newDistance*50;*/

           if(DEBUG)
        	   System.out.println("new move dist : "+newDistance);
           
           if(minDistance >= newDistance) {
               minDistance = newDistance;
               best = moves.get(i);
           }
        }
        return best;

    }
    
    public static Point3D getDestination(AgentAction action, Configuration config ){
    	
    	Configuration configCopy = config.copy();
    	configCopy.applyPhysical(action);
    	Agent agent = config.getAgent(action.index);
    	Vector3d pos = agent.getPosition();
    	return new Point3D(pos.x, pos.y, pos.z);
    }
    
    public static boolean agentHasVisited(Agent agent, Point3D location)
    {
    	
    	for(int i=0; i<agent.getPath().size(); i++)
    	{
    		if(agent.getPath().get(i).equals(location))
    		{
    			if(DEBUG)
    				System.out.println("location has already been visited");
    			
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    public static double getManhattanDistPoint3D(Point3D one, Point3D two)
    {
    	double distance = 0;

  		double xDiff = Math.abs(one.getX()-two.getX());
  		double yDiff = Math.abs(one.getY()-two.getY());
  		double zDiff = Math.abs(one.getZ()-two.getZ());

  		distance += (xDiff + yDiff + zDiff);

  		return distance;
    	
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
    
    public static Agent findAgentWithID(float id, ArrayList<Agent> agents)
    {
    	for(int i=0; i<agents.size(); i++)
    	{
    		if(agents.get(i).getId() == id)
    			return agents.get(i);
    	}
    	
    	return null;
    }
    
    public static AgentAction getSmallerActionWhenStuck(Agent agent, Simulation sim)
    {
    	if(DEBUG)
    	System.out.println("current agent at pos : "+agent.getLocation());
    	
    	//get the valid moves
        ArrayList<AgentAction> moves = sim.getCurrentConfiguration().getPhysicalActions(agent);
        for(int  i=0; i<moves.size();i++)
        {System.out.println("possible move : "+getDestination(moves.get(i), sim.getCurrentConfiguration()));}
        /*if(DEBUG)
        	System.out.println("valid moves size "+moves.size());*/
        
        //get the current distance to the goal
        double minDistance = Double.MAX_VALUE;
        if(DEBUG)
        	System.out.println("current distance : "+minDistance);
        AgentAction best = null;

        //check if one of the moves reduces that current minimal distance to goal
        for(int i = 0; i < moves.size(); i++){
           double newDistance = getManhattanDistPoint3D(getDestination(moves.get(i), sim.getCurrentConfiguration()) , agent.getIntermediateGoal());
           
           //increase weight of distance if the agent has already visited that position
           if(agentHasVisited(agent,getDestination(moves.get(i), sim.getCurrentConfiguration())))
        	   newDistance = newDistance*20;
           
           /*if(agent.getLocation().equals(agent.getIntermediateGoal()))
        		   newDistance = newDistance*50;*/

           if(DEBUG)
        	   System.out.println("new move dist : "+newDistance);
           
           if(minDistance >= newDistance) {
               minDistance = newDistance;
               best = moves.get(i);
           }
        }
        return best;
    }
    
    public static boolean isAGoal(Point3D location, Simulation sim)
    {
    	ArrayList<Agent> goalConfig = sim.getGoalConfiguration().getAgents();
    	
    	for(int i=0; i<goalConfig.size(); i++)
    	{
    		if(goalConfig.get(i).getLocation().equals(location))
    		{
    			return true; 
    		}
    	}
    	
    	return false; 
    }
    
    
	
}