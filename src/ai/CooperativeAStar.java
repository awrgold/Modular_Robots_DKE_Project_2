package ai;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.PriorityQueue;

import all.continuous.*;
import javafx.geometry.Point3D;
import physics.Physics;

public class CooperativeAStar
{
	private static ArrayList<AStarNode> aStarNodes = new ArrayList<AStarNode>(); 
	private static final boolean DEBUG=true; 
	
	public static void main(String[] args)throws InvalidMoveException, InvalidStateException
	{
		
			TestCases.AStar();
	}
	/*Analyse all the obstacles, trying to find out if our group of modules 
	 * would be able to climb over it. 
	 * If so, add the "trampoline" position to a list of positions, 
	 * specifying a certain weight to that movements, weight based on the number of lost
	 * modules, but also number of steps required.*/
	public static void analyseObstacles(int numberAgents, Configuration config, Terrain terrain)
	{
		ArrayList<Obstacle> obstacles = terrain.getObstacles();
	
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
					if(!isAlreadyAStar(finalPos.get(j), aStarNodes))
					{
						node = new AStarNode(finalPos.get(j), config, terrain);
						aStarNodes.add(node);
					}
					else
					{
						node = aStarNodes.get(indexAStar(finalPos.get(j), aStarNodes));
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
					if(!isAlreadyAStar(position, aStarNodes))
					{
						node = new AStarNode(position, config, terrain);
						aStarNodes.add(node);
					}
					else
					{
						node = aStarNodes.get(indexAStar(position, aStarNodes));
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
	
	//MAIN A* SEARCH, return the path from start to goal (works for one agent only)
	public static ArrayList<AStarNode> aStarSearch(Agent agent,Point3D goal, Configuration config, Terrain terrain)
    {
		
		//GOAL 
		AStarNode AStarGoal = new AStarNode(goal, config, terrain); 
		
		//OPENSET : 
        ArrayList<AStarNode> openSet = new ArrayList<AStarNode>();
        //CLOSED SET
        ArrayList<AStarNode> closedSet = new ArrayList<AStarNode>();
        
        //FUTURE A STAR PATH
        ArrayList<AStarNode> path = new ArrayList<AStarNode>();

        //make the agent location the first Astar node
        AStarNode node1 = new AStarNode(agent.getLocation(), config, terrain);
        node1.setGScore(0);
        node1.setHScore(node1.getManhattanDistTo(AStarGoal));
        node1.setFScore(node1.getGScore()+node1.getHScore());
        openSet.add(node1);
        aStarNodes.add(node1);
        
       
        //While all nodes haven't been analysed yet
        while(!openSet.isEmpty())       
        {
        	if(DEBUG)
        	{
        		System.out.println("open set values : ");
        		for(int i=0; i<openSet.size(); i++)
        		{
        			System.out.println(printPoint(openSet.get(i).getLocation())+" f score : "+openSet.get(i).getFScore());
        		}
        	}
        	
        	
        	
        	/*I am now unsure we need this here. Indeed, the point of A* is that it can take the top 
        	 * of the list, eventhough it is not possible to reach it from the path we were on before. 
        	 * */
        	//From the positions in the open set, get the ones that are possible for the current node
        	//ArrayList<AStarNode> legalActions = getLegalActionsFromSet(openSet, node1.computeLegalAction(config, terrain));
        	
        	
        	//Out of the valid moves, find the point that has the smallest FSCORE (ie. the best distance approximation to target)
            int min = findMinFScore(openSet);
            AStarNode minPos = openSet.get(min);
            if(DEBUG)
            	System.out.println("min F Score :"+printPoint(minPos.getLocation()));
       
            
            //Remove the "new agent position" from the open set, because it "has been used". Add it to the closed set.
            //IMPORTANT : durng the first iteration, minPos is not yet in the open set but the start position is, so we just remove the start position
            openSet.remove(min);
            closedSet.add(minPos);
            
            
            if(DEBUG)
            	System.out.println("open set size "+openSet.size());

            
            //Time to update the scores of the neighbours of the new agent position
            ArrayList<Action> possibleActions = minPos.getLegalActions();
            
            //Go through possible moves
            for(int i=0; i<possibleActions.size(); i++)
            {
            	Action currentAction = possibleActions.get(i);
            	AStarNode currentPosition = translateFromActionToNode(currentAction, config, terrain);
            	
				if(DEBUG)
            		System.out.println("possible move : "+printPoint(currentPosition.getLocation())+" and number of neighbours possible : "+possibleActions.size());
				
				//If we've reached the goal, we found the best path and we stop. There is no way this path is not the best
				//because we chose the best approximation at every step. So if it reaches the goal, it is the best path. 
                if(currentPosition.isEqualTo(AStarGoal))
                {
                	if(DEBUG)
                    System.out.println("found target : "+printPoint(currentPosition.getLocation()));
                	
                	//set the parent of the goal as the current agent position (minPos)
                	currentPosition.setParent(minPos);
                	minPos = currentPosition; 
                	
            	   System.out.println("final path from start to goal");
                   path = getAStarPath(node1, currentPosition);
                  
                   for(int j=path.size()-1; j>=0; j--)
                   {
                   	System.out.println(path.get(j).getLocation());
                   }
                    return path;
                }
                
                //If the current neighbour is acutally the node itself. (This can happen because in "getlegalActions" method, 
                //staying at its own position is possible. 
                if(currentPosition.isEqualTo(minPos))
            	{
                	if(DEBUG)
                		System.out.println("current neighbour is the same as the robot location");
                	continue;
            	}
            	
                //NOT TO DELETE§§ I think, but I'm not sure, that it doesn't have to be here. 
                //Indeed, if we don't even consider the neighbours that have already been analysed, when are we supposed
                //to update the best path???
            	//If the current neighbour is already in the "to be analysed set", it doesn't make sense to add it a second time.
                if(isInSet(openSet,currentPosition))
            	{
                	if(DEBUG)
                		System.out.println("current neighbour is already in the openSet");
                	continue;
            	
            	}
                
                //If neighbour has already been analysed, ie. the robot has already moved there 
                if(isInSet(closedSet, currentPosition))
                {
                	System.out.println("neighbour already analysed");
                	
                	//We set its score very high so that it's very unlikely that the robot goes back to the same position twice. 
                	//We allow the possibility but we set is as "last option" move
            		//set(fScore, currentPosition, (currentPosition.getManhattanDistanceTo(goal)+get(fScore,currentPosition))*10);
            		currentPosition.setFScore(currentPosition.getManhattanDistTo(AStarGoal)+currentPosition.getFScore()*10);
                   // System.out.println("current pos : "+printPoint(currentPosition)+" should have big f score : "+get(fScore, currentPosition));
                    
                    //We add it to the open set if it is not there yet 
                    if(!isInSet(openSet, currentPosition))
                		openSet.add(currentPosition);
                	
                    continue;
                }
                
                
                
                int newGScore = minPos.getGScore() + currentAction.getWeight();
                if(DEBUG)
                	System.out.println("newGScore "+newGScore);
                
                //set(hScore, currentPosition, currentPosition.getManhattanDistTo(goal));
                currentPosition.setHScore( currentPosition.getManhattanDistTo(AStarGoal));

                if(DEBUG)
                	System.out.println("newHScore "+currentPosition.getHScore());
                
                //previous path was better, we don't update the scores, neither the parents
                if(newGScore>=currentPosition.getGScore())
                {
                	System.out.println("new G score worse than before");
                	continue;
                }	
                //Otherwise, update the scores
                //set(gScore, currentPosition, newGScore);
                currentPosition.setGScore(newGScore);
                currentPosition.setFScore((newGScore) + currentPosition.getHScore());
	            currentPosition.setParent(minPos);
                
                openSet.add(currentPosition);
            }
            
            
        }
        return path;
      

    }

	
/*	public static void moveAgentsAlongPath(ArrayList<Point3D> path, ArrayList<Agent> )
	{
		//Sort agents based on dist to goal
		//while loop, one loop = simulation time step
			//if(goal is reached), update goal to next pos in path
		//until final position in path is reached
		
		
	}*/
	
	
	//Find the minimum fScore in a list of point3D
    public static int findMinFScore(ArrayList<AStarNode> list)
    {
        double minVal=Double.MAX_VALUE;
        int minIndex=0;
        AStarNode minPoint=null;

        for(int i=0; i<list.size();i++)
        {
            AStarNode current = list.get(i);
           /* if(DEBUG)
            	System.out.println("fScore of "+printPoint(current)+" is "+get(fScore, current));*/
            
            if(current.getFScore()<minVal)
            {
                minVal=current.getFScore();
                minIndex=i;
                minPoint=current;
            }
        }

        return minIndex;
    }
   
    
    //Once the parents of all nodes have been found by a star, return the list of nodes from start to end
    public static ArrayList<AStarNode> getAStarPath(AStarNode start, AStarNode goal)
    {
    	ArrayList<AStarNode> path = new ArrayList<AStarNode>();
    	
    	//System.out.println("current : "+printPoint(current));
    	//System.out.println("start : "+printPoint(start));
    	
    	while(!goal.isEqualTo(start))
    	{
    		path.add(goal);
    		
    		System.out.println("current : "+printPoint(goal.getLocation()));
    		//System.out.println("parent of current "+printPoint(getParent(parents, current)));
    		
    		goal = goal.getParent();
    		
    		//System.out.println("new current "+printPoint(current));
    	}
    	path.add(start);
    	
    	return path;
    }
    
    
   //From a list of actions (each action has a dstination), return the corresponding list of a star nodes destinations
    public static ArrayList<AStarNode> translateFromActionsToNodes(ArrayList<Action> actions, Configuration config, Terrain terrain)
    {
    	ArrayList<AStarNode> possibleMoves = new ArrayList<AStarNode>();
    	for(int i=0; i<actions.size(); i++)
    	{
    		int index = indexAStar(actions.get(i).getDestination(), aStarNodes);
    		if( index != -1)
    		{
    			possibleMoves.add(aStarNodes.get(index));
    		}
    		
    		else
    		{
    			AStarNode newNode = new AStarNode(actions.get(i).getDestination(), config, terrain);
    			aStarNodes.add(newNode);
    			possibleMoves.add(newNode);
    		}
    	}
    	
    	return possibleMoves; 
    }
    
    //Given an action (with a destination), return the corresponding destination a star node
    public static AStarNode translateFromActionToNode(Action action, Configuration config, Terrain terrain)
    {
    	
		int index = indexAStar(action.getDestination(), aStarNodes);
		
		if(index!=-1)
		{
			if(DEBUG)
				System.out.println("neighbour already translated");
			
			return aStarNodes.get(index);
		}
		else
		{
			AStarNode newNode = new AStarNode(action.getDestination(), config, terrain);
			aStarNodes.add(newNode);
			return newNode;
		}
		
    }
   
    //Find the index of an a* in a list of such nodes
    public static int findIndexOfPos(AStarNode pos, ArrayList<AStarNode> list)
    {
    	for(int i=0; i<list.size(); i++)
    	{
    		if(list.get(i).getLocation().equals(pos.getLocation()) )
    		{
    			return i;
    		}
    	}
    	
    	return -1;
    }
    
    //Check if a certain a* node is in a list of a* nodes
    public static boolean isInSet(ArrayList<AStarNode> list, AStarNode point)
    {
    	for(int i=0; i<list.size(); i++)
    	{
    		if(list.get(i).isEqualTo(point))
    			return true;
    	}
    	
    	return false; 
    }
    
    //print a Point3D
    public static String printPoint(Point3D point)
	{
		String toBePrinted = point.getX() + " "+ point.getY()+ " "+point.getZ();
		return toBePrinted;
	}
    
    //Given a Point3d, return if it has already been translated to an A* node
    public static boolean isAlreadyAStar(Point3D pos, ArrayList<AStarNode> aStarNodes)
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
    
}