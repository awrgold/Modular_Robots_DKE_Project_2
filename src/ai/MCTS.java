package ai;

import java.util.ArrayList;

import all.continuous.*;

public class MCTS extends ModuleAlgorithm
{

	private boolean DEBUG = true;
	ArrayList<Action> path = new ArrayList<Action>();
	int counter=0;
	
	public MCTS(Simulation sim) {
		super(sim);
		// TODO Auto-generated constructor stub
		mainMCTS(sim);
	}

	public void mainMCTS(Simulation sim){
		
		MCTSNode root = new MCTSNode(sim.getCurrentConfiguration());
		root.addVisit();
		expand(root);
		
		int counter = 0 ; 
		//TO BE LOOPED UNTIL GOAL IS FOUND
		while(counter<400)
		{
			MCTSNode next = select(root);
			if(next == null)
				System.out.println("next is nul!!");
			//if it has already been visited
			if(next.getVisits() > 0)
			{
				expand(next);
				
				for(int i = 0; i < next.getChildren().size(); i++)
				{
					simulate(next.getChildren().get(i));
				}
			}
			else
			{
				simulate(next);
			}
			
			backUp(next, next.getChildren());
			
			counter++;
		}
		
		while(!sim.hasGoalBeenReached())
		{
			MCTSNode next = select(root);
			Action a = next.getAction();
			path.add(a);
			root = next;
		}
		
		
	}
	//SELECT
	public double selectPolicy(MCTSNode node){
		
		if(DEBUG)
			System.out.println("node score : "+node.getScore() );
		
		if(DEBUG)
			System.out.println("parent visits : "+node.getParent().getVisits() );
		
		if(DEBUG)
			System.out.println("node visits : "+node.getVisits() );
		
		if(node.getVisits()==0)
			return Double.MAX_VALUE;
		
		double selectScore = node.getScore() + Math.sqrt(2)*Math.sqrt(Math.log(node.getParent().getVisits())/node.getVisits());
		
		
		return selectScore;
	}
	
	public MCTSNode select(MCTSNode origin){
		
		ArrayList<MCTSNode> children = origin.getChildren();
		if(DEBUG)
			System.out.println("children size "+children.size());
		double max = Double.MIN_VALUE;
		MCTSNode maxNode = null;
		
		for(int i = 0; i < children.size(); i++){
			double selectScore = selectPolicy(children.get(i));
			if(DEBUG)
				System.out.println("select policy : "+selectScore);
			if(selectScore > max){
				max = selectScore;
				maxNode = children.get(i);
			}
		}
		return maxNode;	
	}
	
	//EXPAND
	public void expand(MCTSNode origin){
		
		ArrayList<Action> validActions = origin.getConfiguration().getAllValidActions();
		
		for(int i = 0; i < validActions.size(); i++){
			
			Configuration configCopy = origin.getConfiguration().copy();
			configCopy.apply(validActions.get(i));
			MCTSNode child = new MCTSNode(configCopy);
			child.setAction(validActions.get(i));
			
			origin.addChild(child);
			
		}	
	}
	
	//SIMULATE
	public void simulate(MCTSNode origin){
		
		origin.addVisit();
		
		long t = System.nanoTime();
		long end = t + 10000000;
		
		while(System.nanoTime() < end){
			
			if(DEBUG)
			{
				if(origin == null)
					System.out.println("node is null");
				if(origin.getConfiguration() == null)
					System.out.println("config is null");
				System.out.println("number of agents : "+origin.getConfiguration().getAgents().size());
				System.out.println(origin.getConfiguration().getAgent(0).getLocation() + " "+ origin.getConfiguration().getAgent(1).getLocation());
			}
			ArrayList<Action> validActions = origin.getConfiguration().getAllValidActions();
			int size = validActions.size();
			int random = (int) Math.random()*size;
			
			origin.getConfiguration().apply(validActions.get(random));
			
		}
		
		int score = estimateScore(origin);
		origin.setScore(score);
	}
	
	public int estimateScore(MCTSNode node){
		
		ArrayList<Agent> agents = node.getConfiguration().getAgents();
		int totalManhattanDistance = 0;
		
		for(int i = 0; i < agents.size() ; i++){
			
			totalManhattanDistance += agents.get(i).getManhattanDistanceTo(agents.get(i).getIntermediateGoal());
		}
		
		totalManhattanDistance = (int) -(totalManhattanDistance/agents.size());
		
		return totalManhattanDistance;
	}
	//BACK UP 
	public void backUp(MCTSNode start, ArrayList<MCTSNode> ends){
		
		for(int i = 0; i < ends.size(); i++){
			start.addScore(ends.get(i).getScore());
		}
		
		while(start.getParent() != null){
			start.getParent().addScore(start.getScore());
			start = start.getParent();
		}
	}
	
	

	@Override
	public void takeTurn() {
		// TODO Auto-generated method stub
		sim.apply(path.get(counter));
		counter++;
		
	}
}