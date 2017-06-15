package ai;

import java.util.ArrayList;

import all.continuous.*;

public class MCTS extends ModuleAlgorithm
{

	private boolean DEBUG = true;
	ArrayList<Action> path = new ArrayList<Action>();
	private static int counter=0;
	
	public MCTS(Simulation sim) {
		super(sim);
		// TODO Auto-generated constructor stub
		if(DEBUG)
			System.out.println("CONSTRUCTION OF MCTS");
		
		mainMCTS(sim);
	}

	public void mainMCTS(Simulation sim){
		
		MCTSNode root = new MCTSNode(sim.getCurrentConfiguration());
		root.addVisit();
		expand(root);
		
		int counter1 = 0 ; 
		//TO BE LOOPED UNTIL GOAL IS FOUND
		while(counter1<3)
		{
			System.out.println("counter : "+counter1);
			
			MCTSNode next = select(root);
			while(next.getChildren().size()!=0)
			{
				MCTSNode next2 = select(next);
				next = next2;
			}
			if(next == null)
				System.out.println("next is nul!!");
			//if it has already been visited
			if(next.getVisits() > 0)
			{
				expand(next);
				
				for(int i = 0; i < next.getChildren().size(); i++)
				{
					if(DEBUG)
						System.out.println("simulate child numb "+i);
					simulate(next.getChildren().get(i));
				}
			}
			else
			{
				
				simulate(next);
			}
			
			backUp(next, next.getChildren());
			
			counter1++;
		}
		
		while(root.getChildren().size()!=0)
		{
			System.out.println("Start computing the path");
			MCTSNode next = select(root);
			if(DEBUG)
			{
				if(next == null)
					System.out.println("next is null");
				if(next.getAction() == null)
					System.out.println("action is null");
			}
			
			Action a = next.getAction();
			path.add(a);
			root = next;
		}
		
		
	}
	//SELECT
	public double selectPolicy(MCTSNode node){
		
		/*if(DEBUG)
			System.out.println("node score : "+node.getScore() );
		
		if(DEBUG)
			System.out.println("parent visits : "+node.getParent().getVisits() );
		
		if(DEBUG)
			System.out.println("node visits : "+node.getVisits() );*/
		
		if(node.getVisits()==0)
			return Double.MAX_VALUE;
		
		double selectScore = node.getScore() + Math.sqrt(2)*Math.sqrt(Math.log(node.getParent().getVisits())/node.getVisits());
		
		
		return selectScore;
	}
	
	public MCTSNode select(MCTSNode origin){
		
		if(DEBUG)
			System.out.println("SELECT");
		
		ArrayList<MCTSNode> children = origin.getChildren();
		/*if(DEBUG)
			System.out.println("children size "+children.size());*/
		double max = - (Double.MAX_VALUE);
		MCTSNode maxNode = null;
		
		for(int i = 0; i < children.size(); i++){
			double selectScore = selectPolicy(children.get(i));
			if(DEBUG)
				System.out.println("select policy : "+selectScore);
			
			if(DEBUG)
				System.out.println("current max value"+ max);
			if(selectScore > max){
				if(DEBUG)
					System.out.println("update the max");
				max = selectScore;
				maxNode = children.get(i);
			}
		}
		return maxNode;	
	}
	
	//EXPAND
	public void expand(MCTSNode origin){
		if(DEBUG)
			System.out.println("EXPAND");
		
		ArrayList<Action> validActions = origin.getConfiguration().getAllValidActions();
		
		for(int i = 0; i < validActions.size(); i++){
			
			Configuration configCopy = origin.getConfiguration().copy();
			configCopy.apply(validActions.get(i));
		
			MCTSNode child = new MCTSNode(configCopy);
			child.setAction(validActions.get(i));
			
			origin.addChild(child);
			
		}	
		
		System.out.println("origin now has "+origin.getChildren().size()+ " children");
	}
	
	//SIMULATE
	public void simulate(MCTSNode origin){
		if(DEBUG)
			System.out.println("SIMULATE");
		
		origin.addVisit();
		Configuration currentConfig = origin.getConfiguration();
		
		
		long t = System.nanoTime();
		long end = t + 1000000000;
		
		while(System.nanoTime() < end){
			
			Configuration nextConfig = currentConfig.copy();
			currentConfig = nextConfig;
			
			/*if(DEBUG)
			{
				if(origin == null)
					System.out.println("node is null");
				if(origin.getConfiguration() == null)
					System.out.println("config is null");
				System.out.println("number of agents : "+origin.getConfiguration().getAgents().size());
				System.out.println(origin.getConfiguration().getAgent(0).getLocation() + " "+ origin.getConfiguration().getAgent(1).getLocation());
			}*/
			ArrayList<Action> validActions = currentConfig.getAllValidActions();
			int size = validActions.size();
			int random = (int) Math.random()*size;
			
			currentConfig.apply(validActions.get(random));
			
		}
		
		int score = estimateScore(currentConfig);
		origin.setScore(score);
	}
	
	public int estimateScore(Configuration config){
		
		ArrayList<Agent> agents = config.getAgents();
		ArrayList<Agent> goals = config.getSimulation().getGoalConfiguration().getAgents();
		int totalManhattanDistance = 0;
		
		for(int i = 0; i < agents.size() ; i++){
			
			totalManhattanDistance += agents.get(i).getManhattanDistanceTo(goals.get(i).getLocation());
		}
		
		totalManhattanDistance = (int) -(totalManhattanDistance/agents.size());
		
		return totalManhattanDistance;
	}
	//BACK UP 
	public void backUp(MCTSNode start, ArrayList<MCTSNode> ends){
		if(DEBUG)
			System.out.println("BACKUP");
		for(int i = 0; i < ends.size(); i++){
			start.addScore(ends.get(i).getScore());
		}
		
		if(DEBUG)
			System.out.println("start score : "+start.getScore());
		
		while(start.getParent() != null){
			start.getParent().addScore(start.getScore());
			start = start.getParent();
			if(DEBUG)
				System.out.println("backed up score parent "+start.getScore());
		}
	}
	
	

	@Override
	public void takeTurn() {
		// TODO Auto-generated method stub
		if(counter<path.size())
		{
			sim.apply(path.get(counter));
			counter++;
		}
		else
		{
			sim.finish();
		}
		
		
	}
}