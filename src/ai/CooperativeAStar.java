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
	
	public static void main(String[] args)throws InvalidMoveException, InvalidStateException
	{TestCases.AStar();}
	public CooperativeAStar(Simulation sim)
	{
		super(sim);
	}
	public static ArrayList<Point3D> AStarComplete(Simulation sim)
	{
		/*1*/
		ObstacleAnalysis.analyseObstacles(sim.getCurrentConfiguration().getAgents().size(), sim.getCurrentConfiguration(), sim.getTerrain());
		
		/*2*/
		//choose which agent to compute the path on 
		Agent aStarAgent = sim.getCurrentConfiguration().getAgent(0);
		Point3D goal = aStarAgent.getIntermediateGoal();
		return AStarSearch.aStarSearch(aStarAgent, goal, sim.getCurrentConfiguration(), sim.getTerrain());
		
		/*3*/
		
		//CooperativeModulesMovement.moveAlongPath(path, sim.getCurrentConfiguration().getAgents());
	
		
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

    @Override
    public void takeTurn() {
    	// TODO Auto-generated method stub
    	CooperativeModulesMovement.moveAlongPath(AStarComplete(sim), sim.getCurrentConfiguration().getAgents());
    	sim.finish();
    			
}
	
	
	
}