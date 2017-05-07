package ai;

import java.util.ArrayList;

import all.continuous.*;
import javafx.geometry.Point3D;

public class AStarNode
{
	/*private double x; 
	private double y; 
	private double z;*/
	
	private Point3D location; 
	
	//FSCORE : this is the HScore and Gscore combined
	private double fScore; 
	
	//GSCORE : this is the distance from start to current node (integer because we just always add 1 when the robot moves)
	private int gScore; 
	
	//HSCORE : this is the Manhattan distance from node to target, best approximation
	private double hScore; 
	
	//PARENT
	private AStarNode parent; 
	
	//agent for which the scores apply
	//private Agent agent; 
	
	//list of possible "jumps" over an obstacle from that node
	//private ArrayList<Integer> trampolines; 
	
	//list of legal moves from that position
	private ArrayList<Action> legalActions;
	
	public AStarNode(Point3D location, Configuration config, Terrain terrain)
	{
		this.fScore=Double.MAX_VALUE; 
		this.gScore=Integer.MAX_VALUE;

		this.location=location;
		
		this.legalActions=computeLegalAction(config, terrain); 
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
	
	//public Agent getAgent(){return agent;}
	
	/*public ArrayList<Integer> getTrampoline(){return trampolines; }
	public void addTrampoline(int trampoline)
	{
		trampolines.add(trampoline);
	}*/
	
	public void addLegalAction(Action a){legalActions.add(a);}
	public ArrayList<Action> getLegalActions(){return legalActions;}
	
	/*TO BE IMPLEMENTED*/
	public ArrayList<Action> computeLegalAction(Configuration config, Terrain terrain)
	{
		ArrayList<Action> a = new ArrayList<Action>(); 
		return a; 
	}
	
	public double getManhattanDistTo(AStarNode goal)
	{
		double dist=0; 
		
		double xDiff = Math.abs(goal.location.getX()-this.location.getX()); 
		double yDiff = Math.abs(goal.location.getY()-this.location.getY()); 
		double zDiff = Math.abs(goal.location.getZ()-this.location.getZ()); 
		
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
	
	
	
}