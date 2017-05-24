package ai;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

import all.continuous.Action;
import all.continuous.Configuration;
import all.continuous.Obstacle;
import all.continuous.Terrain;
import javafx.geometry.Point3D;
import physics.Physics;

/**
 * 
 * @author marion
 * 
 * 
 * This class is the first part of our general AI called CooperativeAStar
 * It solves the problem of having to create an a star that works for multiple agents at the same time
 * Its goal is to find those positions, those movements, that are possible (legal) for multiple agents but not one by itself. 
 * example : climbing a wall is not possible for a single agent but is for multiple that work together
 * Once those possible movements are found and stored, we can run our AStarSearch on one single module, that is now consider
 * those moves as wel when choosing a path
 *
 */
public class ObstacleAnalysis
{
	private final static boolean DEBUG = true; 
	/*Analyse all the obstacles, trying to find out if our group of modules 
	 * would be able to climb over it. 
	 * If so, add the "trampoline" position to a list of positions, 
	 * specifying a certain weight to that movements, weight based on the number of lost
	 * modules, but also number of steps required.*/
	public static void analyseObstacles(int numberAgents, Configuration config, Terrain terrain)
	{
		List<Obstacle> obstacles = terrain.getObstacles();
	
		if(DEBUG)
		{
			System.out.println("obstacles : ");
			for(int i=0; i<obstacles.size(); i++)
			{
				System.out.println(obstacles.get(i).getLocation());
			}
		}
		
		//Create priority queue that sorts the obstacles based on their height
		Comparator<Obstacle> comp = new ObstacleHeightComparator();
		PriorityQueue<Obstacle> Q = new PriorityQueue<>(obstacles.size(), comp); 
		for(int i =0; i<obstacles.size(); i++)
		{
			Q.add(obstacles.get(i));
		}
		
		if(DEBUG)
		{System.out.println("sorted obstacles : "+Q.size()+" "+Q.toString());}
		
		int size = Q.size();
		//Go through all the obstacles
		for(int i=0; i<size;i++)
		{
			Obstacle ob = Q.poll();
			
			/*Check is it's possible to go over the obstacle*/
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
					AStarNode node; 
					if(!CooperativeAStar.isAlreadyAStar(finalPos.get(j)))
					{
						node = new AStarNode(finalPos.get(j), config, terrain);
						CooperativeAStar.aStarNodes.add(node);
					}
					else
					{
						node = CooperativeAStar.aStarNodes.get(CooperativeAStar.indexAStar(finalPos.get(j), CooperativeAStar.aStarNodes));
					}
					
					double deltaZ = z - finalPos.get(j).getZ();
					if(DEBUG)
						System.out.println("delta z : "+deltaZ);
					//if the number of agents we have is enough to climb the obs, mark it so 
					if(possibleClimb(deltaZ, numberAgents))
					{
						if(DEBUG)
							System.out.println("climb possible");
						//create an "jump" action, with the right weight
						Point3D topObstacle = new Point3D(ob.getLocation().getX(), ob.getLocation().getY(), ob.getLocation().getZ()+1);
						Action a = new Action(topObstacle, null, getRequiredNumbAgents(deltaZ, numberAgents));
						//Add this action to the list of legal actions of that node
						node.addLegalAction(a);
						if(DEBUG)
							System.out.println("legal actions size : "+node.getLegalActions().size());
					}
				}
			}
			
			/*Check if it's possible to go under it (ie. space should be 2 or more)
			 * If height under obstacle less than 2, adjust the weight of that move
			 * TO BE IMPLEMENTED : finction that decides the weight of the move, for the moment = 20*/
			if(!ob.isSupportedFromBottom(config, terrain))
			{
				if(ob.getLocation().getZ() <2)
				{
					//create new node if not already in a star
					//get node from a star list if already translated
					
					AStarNode node; 
					Point3D position = new Point3D(ob.getLocation().getX(), ob.getLocation().getY(),0);
					if(!CooperativeAStar.isAlreadyAStar(position))
					{
						node = new AStarNode(position, config, terrain);
						CooperativeAStar.aStarNodes.add(node);
					}
					else
					{
						node = CooperativeAStar.aStarNodes.get(CooperativeAStar.indexAStar(position, CooperativeAStar.aStarNodes));
					}
					
					ArrayList<Action> actions = node.getLegalActions();
					//TO BE IMPLEMENTED, for the moment, front movement is the third one in the list, to be 
					//changed when legal moves method will be changed
					Action front = actions.get(2);
					front.setWeight(20);
				}
			}
			
		}
	}
	
	//Returns boolean value if it possible to climb a certain obstacle or not!u                                                                                       	k
	public static boolean possibleClimb(double height, int numberAgents)
	{
		int requiredN = getRequiredNumbAgents(height, 0);
		if(DEBUG)
			System.out.println("required number of agents : "+requiredN);
		if(numberAgents>=requiredN)
			return true; 
		else
			return false; 
	}
	
	//Given a certain obstacle height, returns the number of modules required in order to climb it
	public static int getRequiredNumbAgents(double height, int numbAgents)
	{
		if(height==0)
		{
			//System.out.println("height is 0 ");
			return numbAgents+2; 
		}
			
		
		else
		{
			numbAgents +=height;
			//System.out.println("numb agents : "+numbAgents);
			height-=1;
			return getRequiredNumbAgents(height, numbAgents);
		}
	}
}