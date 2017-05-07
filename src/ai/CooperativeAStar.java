package ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import all.continuous.*;
import javafx.geometry.Point3D;
import physics.Physics;

public class CooperativeAStar
{
	private ArrayList<AStarNode> aStarNodes; 
	
	public static void main(String[] args)throws InvalidMoveException, InvalidStateException
	{
		
			TestCases.basicTestAgentMovement();
	}
	/*Analyse all the obstacles, trying to find out if our group of modules 
	 * would be able to climb over it. 
	 * If so, add the "trampoline" position to a list of positions, 
	 * specifying a certain weight to that movements, weight based on the number of lost
	 * modules, but also number of steps required.*/
	public void analyseObstacles(int numberAgents, Configuration config, Terrain terrain)
	{
		ArrayList<Obstacle> obstacles = terrain.getObstacles();
		
		//Create priority queue that sorts the obstacles based on their height
		Comparator<Obstacle> comp = new ObstacleHeightComparator();
		PriorityQueue<Obstacle> Q = new PriorityQueue<>(obstacles.size(), comp); 
		for(int i =0; i<Q.size(); i++)
		{
			Q.add(obstacles.get(i));
		}
		
		int size = Q.size();
		//Go through all the obstacles
		for(int i=0; i<size;i++)
		{
			Obstacle ob = Q.poll();
			//only consider the obstacles that are "on top" of the rest, ie there is no cube on top of them
			if(!ob.isSupportedFromTop(config, terrain))
			{
				double x = ob.getLocation().getX();
				double y = ob.getLocation().getY(); 
				double z = ob.getLocation().getZ();
				
				//List of the 4 falling positions (left, right, front, behind), or null if one is not possible
				ArrayList<Point3D> finalPos = Physics.returnFinal4FallPos(ob.getLocation(), config, terrain);
			
				for(int j=0; j<finalPos.size(); j++)
				{
					AStarNode node = new AStarNode(finalPos.get(j), config, terrain);
					aStarNodes.add(node);
					double deltaZ = z - finalPos.get(j).getZ();
					//if the number of agents we have is enough to climb the obs, mark it so 
					if(possibleClimb(deltaZ, numberAgents))
					{
						//create an "jump" action, with the right weight
						Action a = new Action(ob.getLocation(), null, (int)deltaZ);
						//Add this action to the list of legal actions of that node
						node.addLegalAction(a);
					}
				}
			}
			
		}
	}
	
	public boolean possibleClimb(double height, int numberAgents)
	{
		int requiredN = getRequiredNumbAgents(height, 0);
		
		if(numberAgents>=requiredN)
			return true; 
		else
			return false; 
	}
	
	public int getRequiredNumbAgents(double height, int numbAgents)
	{
		if(height==0)
			return numbAgents+2; 
		
		else
		{
			numbAgents +=height;
			return getRequiredNumbAgents(height--, numbAgents);
		}
	}
}