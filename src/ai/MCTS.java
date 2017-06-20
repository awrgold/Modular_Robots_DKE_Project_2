package ai;

import java.util.ArrayList;

import all.continuous.*;

public class MCTS extends ModuleAlgorithm
{
	ArrayList<Action> path = new ArrayList<Action>();
	ArrayList<MCTSNode> nodePath = new ArrayList<MCTSNode>();

	private static int counter=0;
	private static int turnCounter=0;
	private static int height=0;

	private boolean continueLooping = true;
	private int iterationCounter = 0;

	public MCTS(Simulation sim) {
		super(sim);
		System.out.println("Initializing MCTS");
	}

	public void mainMCTS(Simulation sim){
		MCTSNode root = new MCTSNode(sim.getCurrentConfiguration());
		root.addVisit();
		expand(root);
		for(int i = 0; i < root.getChildren().size(); i++) simulate(root.getChildren().get(i));

		//Build tree
		while(continueLooping){
			if(iterationCounter==100000) continueLooping = false; //TODO: change this to checking whether the goal has been reached

			if(iterationCounter%500==0) System.out.println("MCTS iteration: "+iterationCounter);
			iterationCounter++;

			root.addVisit();

			MCTSNode next = select(root);

			if(next != null){
				while(next.getChildren().size()!=0){
					MCTSNode next2 = select(next);
					next = next2;
				}

				expand(next);

				for(int i = 0; i < next.getChildren().size(); i++) simulate(next.getChildren().get(i));

				backUp(next, next.getChildren());
			}
		}

		//Construct best path
		while(root.getChildren().size()>0){
			MCTSNode next = bestValueChild(root);

			nodePath.add(next);

			Action a = next.getAction();
			path.add(a);

			root = next;
		}
	}

	public MCTSNode bestValueChild(MCTSNode parent){
		ArrayList<MCTSNode> children = parent.getChildren();

		int min = Integer.MAX_VALUE;
		MCTSNode best = null;

		for (MCTSNode child: children) {
			if(child.getScore()<min && child.getScore()!=Integer.MIN_VALUE){
				min = child.getScore();
				best = child;
			}

		}

		return best;
	}

	public MCTSNode bestVisitsChild(MCTSNode parent){
		ArrayList<MCTSNode> children = parent.getChildren();

		int max = Integer.MIN_VALUE;
		MCTSNode best = null;

		for (MCTSNode child: children) {
			if(child.getVisits() > max){
				max = child.getScore();
				best = child;
			}
		}

		return best;
	}

	public double selectPolicy(MCTSNode node){
		double selectScore=0;

		if(node.getVisits()==0) selectScore = node.getScore()  - 100;
		else selectScore = node.getScore() - Math.sqrt(1/5)*Math.sqrt(Math.log(node.getParent().getVisits())/node.getVisits());

		return selectScore;
	}

	public MCTSNode select(MCTSNode origin){
		ArrayList<MCTSNode> children = origin.getChildren();

		double min = 1000000; //CHANGED BY BOBBY
		MCTSNode minNode = null;

		for (MCTSNode child: children) {
			double selectScore = selectPolicy(child);

			if(selectScore < min){
				min = selectScore;
				minNode = child;
			}
		}
		minNode.addVisit();

		return minNode;
	}

	public void expand(MCTSNode origin){
		ArrayList<Action> validActions = origin.getConfiguration().getAllValidActions();

		for (Action action:validActions) {
			Configuration configCopy = origin.getConfiguration().copy();
			configCopy.apply(action);

			MCTSNode child = new MCTSNode(configCopy);
			child.setAction(action);

			origin.addChild(child);
		}
	}

	public void simulate(MCTSNode origin){
		Configuration currentConfig = origin.getConfiguration();

		long t = System.nanoTime();
		long end = t + 100000;

		while(System.nanoTime() < end){
			Configuration nextConfig = currentConfig.copy();
			currentConfig = nextConfig;

			ArrayList<Action> validActions = currentConfig.getAllValidActions();
			int size = validActions.size();
			int random = (int) Math.random()*size;

			currentConfig.apply(validActions.get(random));
		}

		int score = estimateScore(currentConfig);
		if(isSameAsAParent(origin)) score = score*10;

		origin.setScore(score);
	}

	public int estimateScore(Configuration config){
		ArrayList<Agent> agents = config.getAgents();
		ArrayList<Agent> goals = config.getSimulation().getGoalConfiguration().getAgents();
		int totalManhattanDistance = 0;

		for(int i = 0; i < agents.size() ; i++) totalManhattanDistance += agents.get(i).getManhattanDistanceTo(goals.get(i).getLocation());

		totalManhattanDistance = (int) (totalManhattanDistance/agents.size());

		return totalManhattanDistance;
	}

	public void backUp(MCTSNode start, ArrayList<MCTSNode> ends){
		int newHeight=0;

		int totalScore=0;
		for (MCTSNode end: ends) totalScore+=end.getScore();
		totalScore = totalScore/ends.size();

		start.setScore(totalScore);

		while(start.getParent() != null){
			totalScore=0;

			for (MCTSNode child: start.getParent().getChildren()) totalScore+=child.getScore();
			totalScore = totalScore/start.getParent().getChildren().size();

			start.getParent().setScore(totalScore);

			start = start.getParent();
			newHeight++;
		}

		if(newHeight>height) height=newHeight;
	}

	public boolean isSameAsAParent(MCTSNode node){ //TODO: remove literally all of this
		ArrayList<Agent> nodeAgents = node.getConfiguration().getAgents();

		while(node.getParent() != null)
		{
			ArrayList<Agent> pathAgents = node.getParent().getConfiguration().getAgents();

			int agentCounter=0;
			for(int j=0; j<nodeAgents.size(); j++)
			{
				int count2=0;
				for(int k=0; k<pathAgents.size(); k++)
				{
					if(DEBUG5)
						System.out.println("analyse : "+nodeAgents.get(j).getLocation()+ "and "+ pathAgents.get(k).getLocation());
					if(nodeAgents.get(j).getLocation().equals(pathAgents.get(k).getLocation()))
					{

						count2++;
						if(DEBUG5)
							System.out.println("count2 : "+count2);
					}


				}
				if(count2>=1)
				{
					agentCounter++;
					if(DEBUG5)
						System.out.println("agentCounter : "+agentCounter);
				}

			}
			if(agentCounter==pathAgents.size())
			{
				if(DEBUG5)
					System.out.println("RETURN TRUE");
				return true;
			}

			node = node.getParent();
		}

		return false;
	}
	
	@Override
	public void takeTurn() {
		if(turnCounter==0){
			mainMCTS(sim);
			turnCounter++;
		}

		if(counter<path.size()){
			sim.apply(path.get(counter));
			counter++;
			turnCounter++;
		} else {
			sim.finish();
		}
	}
}