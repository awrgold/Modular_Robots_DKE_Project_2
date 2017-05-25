package ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import all.continuous.Action;
import all.continuous.Agent;
import all.continuous.ModuleAlgorithm;
import all.continuous.Simulation;
import javafx.geometry.Point3D;

public class SimpleAI extends ModuleAlgorithm
{
	private static final boolean DEBUG = true;
	
	private static ArrayList<Point3D> visited = new ArrayList<Point3D>();
	private static float currentAgentID;
	
	private static PriorityQueue<Agent> PQ; 
	
	private int iterations=0;
	
	ArrayList<Agent> agents; 

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
		for(int i=0; i<agents.size(); i++)
		{agents.get(i).setIntermediateGoal(sim.getGoalConfiguration().getAgent(i).getLocation());}
		
		//FILL THE PQ WITH AGENTS, BASED ON THEIR DISTANCE TO RESPECTIVE GOAL
		Comparator<Agent> compare = new PQComparator();
        PQ = new PriorityQueue<>(agents.size(), compare);

        if(DEBUG)
        {if(PQ == null)
        		System.out.println("pq is null");}
        
        
        
        //This keeps track of the ID of the agent we're currently moving
       
        //if it's the first iteration, fill the PQ entirely and store the id of the furthest agent
        if(iterations==0)
        {
        	fillPQ(agents, PQ);
        	currentAgentID = PQ.poll().getId();
        }
        //if it's not the first, add all the new agents to the PQ, except the one we're working with
        else
        {
        	for(int i=0; i<agents.size(); i++)
        	{
        		if(agents.get(i).getId()!=currentAgentID)
        			PQ.add(agents.get(i));
        	}
        }
		
        if(DEBUG)
			System.out.println("current agent : "+currentAgentID);
        
        //print agents location
        for(int i=0; i<agents.size();i++)
        {System.out.println("agent at "+agents.get(i).getLocation());}
        
        //Get the agent we were moving at the previous turn
        Agent currentAgent = findAgentWithID(currentAgentID, agents);
    	
        //Check if there is an action that would bring it closer to its goal
        Action best = isCloser(currentAgent, sim);
    	//if there is an action that brings the agent closer to the goal
    	if(best != null)
    	{
    		if(DEBUG)
    			System.out.println("agent moves to "+best.getDestination());
    		sim.apply(best);
    		currentAgent.addPath(best.getDestination());
    		//add the position to the list of visited positions, so we can later update the weight of that move
    		//visited.add(best.getDestination());
    	}
    	//if there is NO action that brings the agent closer to the goal --> change agent
    	else
    	{
    		
    		//Agent previous = currentAgent;
    		//new furthest agent
    		currentAgent = PQ.poll();
    		currentAgentID = currentAgent.getId();
    		if(DEBUG)
    			System.out.println("next agent : "+currentAgentID);
    		//put the agent we just moved back in the PQ
    		//PQ.add(previous);
    		
    		
    	}
    	
    	
    	iterations++;
    	if(DEBUG)
    		System.out.println("iterations : "+iterations);
    	
    	//this is just for stopping my infinite for loop
    	if(iterations>40)
    		sim.finish();
    	
    	//if the agent we're working with finds its goal, we stop the simulation
    	if(currentAgent.getLocation().equals(currentAgent.getIntermediateGoal()))
    			sim.finish();
        
        
		
	}
	
	// priority queue filler
    public static void fillPQ(ArrayList<Agent> agents, PriorityQueue<Agent> PQ){
        for(int i = 0; i < agents.size(); i++){
        	if(DEBUG)
        		System.out.println("intermediate goal : "+agents.get(i).getIntermediateGoal());
            PQ.add(agents.get(i));
        }
    }
    
    // method that returns the valid move that reduces the distance to the intermediate goal the most, null if no such move
    public static Action isCloser(Agent agent, Simulation simulation){
    	
    	if(DEBUG)
    	{
    		System.out.println("config agents size "+simulation.getCurrentConfiguration().getAgents().size());
    		System.out.println("current agent at pos : "+agent.getLocation());
    	}
    	
    	//get the valid moves
        ArrayList<Action> moves = simulation.getCurrentConfiguration().getAllValidActions(agent);
        for(int  i=0; i<moves.size();i++)
        {System.out.println("possible move : "+moves.get(i).getDestination());}
        if(DEBUG)
        	System.out.println("valid moves size "+moves.size());
        
        //get the current distance to the goal
        double minDistance = agent.getManhattanDistanceTo(agent.getIntermediateGoal());
        if(DEBUG)
        	System.out.println("current distance : "+minDistance);
        Action best = null;

        //check if one of the moves reduces that current minimal distance to goal
        for(int i = 0; i < moves.size(); i++){
           double newDistance = getManhattanDistPoint3D(moves.get(i).getDestination() , agent.getIntermediateGoal());
           
           //increase weight of distance if the agent has already visited that position
           if(agentHasVisited(agent, moves.get(i).getDestination()))
        	   newDistance = newDistance*20;

           if(DEBUG)
        	   System.out.println("new move dist : "+newDistance);
           
           if(minDistance >= newDistance) {
               minDistance = newDistance;
               best = moves.get(i);
           }
        }
        return best;

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
    
    
	
}