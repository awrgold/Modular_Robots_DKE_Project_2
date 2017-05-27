package ai;

import java.util.ArrayList;
import java.util.List;

import all.continuous.*;
import javafx.geometry.Point3D;

public class AStarNode
{
	private final static boolean DEBUG=true; 
	
	private Point3D location; 
	
	//FSCORE : this is the HScore and Gscore combined
	private double fScore; 
	
	//GSCORE : this is the distance from start to current node (integer because we just always add 1 when the robot moves)
	private int gScore; 
	
	//HSCORE : this is the Manhattan distance from node to target, best approximation
	private double hScore; 
	
	//PARENT
	private AStarNode parent; 
	
	//list of legal moves from that position
	private ArrayList<Action> legalActions;
	
	private ArrayList<Float> agentsVisited;  
	
	//A star node constructor
	public AStarNode(Point3D location, Configuration config, Terrain terrain)
	{
		this.fScore=Double.MAX_VALUE; 
		this.gScore=Integer.MAX_VALUE;

		this.location=location;
		
		this.legalActions=computeLegalAction(config, terrain); 
		
		agentsVisited = new ArrayList<Float>();
	}
	
	public void setFScore(double fScore){this.fScore=fScore;}
	public void setGScore(int gScore){this.gScore=gScore;}
	public void setHScore(double hScore){this.hScore=hScore;}
	
	public Point3D getLocation(){return location;}
	
	public double getFScore(){ return fScore;}
	public int getGScore(){return gScore;}
	public double getHScore(){return hScore;}
	
	public void setParent(AStarNode parent)
	{
		this.parent=parent;
	}
	public AStarNode getParent(){return parent; }
	
	public void addLegalAction(Action a){legalActions.add(a);}
	public ArrayList<Action> getLegalActions(){return legalActions;}
	
	public void addVisitedAgent(float ID)
	{
		agentsVisited.add(ID);
	}
	
	public ArrayList<Float> getAgentsVisited()
	{
		return agentsVisited;
	}
	/*TO BE IMPLEMENTED*/
	/*For the purpose of testing, I'm just returning the 4 nodes on the sides of this one*/
	public ArrayList<Action> computeLegalAction(Configuration config, Terrain terrain)
	{
		List<Obstacle> obstacles = terrain.getObstacles();
		
		if(DEBUG)
		{
			if(this.location==null)
				System.out.println("location is null");
		}
			
		Point3D left = new Point3D(this.location.getX()-1, 0, this.location.getZ());
		Point3D right = new Point3D(this.location.getX()+1, 0, this.location.getZ());
		Point3D front = new Point3D(this.location.getX(), 0, this.location.getZ()+1);
		Point3D behind = new Point3D(this.location.getX(), 0, this.location.getZ()-1);
		
		ArrayList<Action> pos4 = new ArrayList<Action>();
		
		if(!isObstacle(left, obstacles))
		{Action a1 = new Action(left, null, 1);
		pos4.add(a1);}
		
		if(!isObstacle(right, obstacles))
		{Action a2 = new Action(right, null, 1);
		pos4.add(a2);}
		
		if(!isObstacle(front, obstacles))
		{Action a3 = new Action(front, null, 1);
		pos4.add(a3);}
		
		if(!isObstacle(behind, obstacles))
		{Action a4 = new Action(behind, null, 1);
		pos4.add(a4);}
		
		return pos4;   
	}
	
	//Get the Manhattan distance from this node to the goal 
	public double getManhattanDistTo(AStarNode goal)
	{
		double dist=0; 
		
		double xDiff = Math.abs(goal.getLocation().getX()-this.location.getX()); 
		double yDiff = Math.abs(goal.getLocation().getY()-this.location.getY()); 
		double zDiff = Math.abs(goal.getLocation().getZ()-this.location.getZ()); 
		
		dist+=(xDiff+yDiff+zDiff);
		
		return dist; 
	}
	
	//Given a position, check if it has already been translated into an A star Node
	public boolean isTheSameAs(Point3D pos)
	{
		if(pos.getX() == this.location.getX())
		{
			if(pos.getY()==this.location.getY())
			{
				if(pos.getZ()==this.location.getZ())
					return true; 
			}
		}
		
		return false; 
	}
	
	//Compare 2 AStar nodes
	public boolean isEqualTo(AStarNode node2)
	{
		if(node2.getLocation().getX() == this.location.getX())
		{
			if(node2.getLocation().getY()==this.location.getY())
			{
				if(node2.getLocation().getZ()==this.location.getZ())
					return true; 
			}
		}
		
		return false; 
	}
	
	//IMPORTANT : HAS NOTHING TO DO HERE, SHOULD BE PUT IN ANOTHER CLASS, only thre for the purpose of testing
	//Check if a certain location is an obstacle
	public boolean isObstacle(Point3D location, List<Obstacle> obstacles)
	{
		for(int i=0; i<obstacles.size(); i++)
		{
			if(obstacles.get(i).getLocation().getX() == location.getX())
			{
				if(obstacles.get(i).getLocation().getY() == location.getY())
				{
					if(obstacles.get(i).getLocation().getZ() == location.getZ())
					{
						return true; 
					}
				}
			}
				
		}
		return false; 
	}
	
	
	
}