package ai;

import java.util.ArrayList;
import java.util.List;

import all.continuous.*;

public class MCTSNode{
	
	Configuration config;
	MCTSNode parent;
	ArrayList<MCTSNode> children;
	int visits;
	int score;
	Action fromParent;
	
	public MCTSNode(Configuration config){
		this.config = config;
		children = new ArrayList<MCTSNode>();
		visits = 0;
		score=20;
	}
	
	public MCTSNode getParent(){
		return parent;
	}
	
	public int getVisits(){
		return visits;
	}
	
	public int getScore(){
		return score;
	}
	
	public ArrayList<MCTSNode> getChildren(){
		return children;
	}
	
	public Configuration getConfiguration(){
		return config;
	}
	
	public void setChildren(ArrayList<MCTSNode> children){
		this.children = children;	
		for(int i=0; i<children.size(); i++)
		{
			children.get(i).setParent(this);
		}
	}
	
	public void setScore(int score){
		this.score = score;
	}
	
	public void addScore(int score){
		this.score += score;
	}
	
	public void addChild(MCTSNode child){
		child.setParent(this);
		children.add(child);
		
	}
	
	public void setAction(Action action)
	{
		fromParent = action;
	}
	
	public Action getAction()
	{
		return fromParent;
	}
	
	public void setParent(MCTSNode parent)
	{
		this.parent = parent;
	}
	
	public void addVisit()
	{
		this.visits++;
	}
	
}