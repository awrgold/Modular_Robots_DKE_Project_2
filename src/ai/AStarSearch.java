package ai;

import java.util.ArrayList;

import all.continuous.Action;
import all.continuous.Agent;
import all.continuous.Configuration;
import all.continuous.Terrain;
import javafx.geometry.Point3D;

/**
 * 
 * @author marion
 * 
 * This class is the second part of our general AI called CooperativeAStar
 * This class is the a star algorithm by itself
 * It can compute the best path for one single module to the goal 
 * it's most important method (aStarSearch) will return this path, in the form of a list of positions from start to goal
 *
 */
public class AStarSearch
{
	private static final boolean DEBUG = true;
	
	//MAIN A* SEARCH, return the path from start to goal (works for one agent only)
		public static ArrayList<Point3D> aStarSearch(Agent agent,Point3D goal, Configuration config, Terrain terrain)
	    {
			
			//GOAL 
			AStarNode AStarGoal = new AStarNode(goal, config, terrain); 
			
			//OPENSET : 
	        ArrayList<AStarNode> openSet = new ArrayList<AStarNode>();
	        //CLOSED SET
	        ArrayList<AStarNode> closedSet = new ArrayList<AStarNode>();
	        
	        //FUTURE A STAR PATH
	        ArrayList<Point3D> path = new ArrayList<Point3D>();

	        //make the agent location the first Astar node
	        AStarNode node1 = new AStarNode(agent.getLocation(), config, terrain);
	        node1.setGScore(0);
	        node1.setHScore(node1.getManhattanDistTo(AStarGoal));
	        node1.setFScore(node1.getGScore()+node1.getHScore());
	        openSet.add(node1);
	        CooperativeAStar.aStarNodes.add(node1);
	        
	       
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
	                   	System.out.println(path.get(j));
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
	   
	    
	    //Once the parents of all nodes have been found by a star, return the list of Point3D from start to end
	    public static ArrayList<Point3D> getAStarPath(AStarNode start, AStarNode goal)
	    {
	    	ArrayList<Point3D> path = new ArrayList<Point3D>();
	    	
	    	//System.out.println("current : "+printPoint(current));
	    	//System.out.println("start : "+printPoint(start));
	    	
	    	while(!goal.isEqualTo(start))
	    	{
	    		path.add(goal.getLocation());
	    		
	    		System.out.println("current : "+printPoint(goal.getLocation()));
	    		//System.out.println("parent of current "+printPoint(getParent(parents, current)));
	    		
	    		goal = goal.getParent();
	    		
	    		//System.out.println("new current "+printPoint(current));
	    	}
	    	path.add(start.getLocation());
	    	
	    	return path;
	    }
	    
	    
	   //From a list of actions (each action has a dstination), return the corresponding list of a star nodes destinations
	    public static ArrayList<AStarNode> translateFromActionsToNodes(ArrayList<Action> actions, Configuration config, Terrain terrain)
	    {
	    	ArrayList<AStarNode> possibleMoves = new ArrayList<AStarNode>();
	    	for(int i=0; i<actions.size(); i++)
	    	{
	    		int index = CooperativeAStar.indexAStar(actions.get(i).getDestination(),  CooperativeAStar.aStarNodes);
	    		if( index != -1)
	    		{
	    			possibleMoves.add( CooperativeAStar.aStarNodes.get(index));
	    		}
	    		
	    		else
	    		{
	    			AStarNode newNode = new AStarNode(actions.get(i).getDestination(), config, terrain);
	    			CooperativeAStar.aStarNodes.add(newNode);
	    			possibleMoves.add(newNode);
	    		}
	    	}
	    	
	    	return possibleMoves; 
	    }
	    
	    //Given an action (with a destination), return the corresponding destination a star node
	    public static AStarNode translateFromActionToNode(Action action, Configuration config, Terrain terrain)
	    {
	    	
			int index = CooperativeAStar.indexAStar(action.getDestination(),  CooperativeAStar.aStarNodes);
			
			if(index!=-1)
			{
				if(DEBUG)
					System.out.println("neighbour already translated");
				
				return  CooperativeAStar.aStarNodes.get(index);
			}
			else
			{
				AStarNode newNode = new AStarNode(action.getDestination(), config, terrain);
				CooperativeAStar.aStarNodes.add(newNode);
				return newNode;
			}
			
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
	    
	  
	    
	    
	    
}