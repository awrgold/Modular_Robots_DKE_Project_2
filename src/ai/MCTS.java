package ai;

import java.util.ArrayList;
import java.util.Collections;

import all.continuous.*;

public class MCTS extends ModuleAlgorithm{

	//SETTINGS
	private final double GREEDY_SIMULATION_CHANCE = 1;
	private final int MAX_ITERATIONS = 10000;
	private final int MINIMUM_VISITS = 20;
	private final double EXPLORATION = Math.sqrt(2);
	private final int SIMULATION_DEPTH = 3;

	ArrayList<Action> path = new ArrayList<Action>();
	ArrayList<MCTSNode> nodePath = new ArrayList<MCTSNode>();

	private static int turnCounter=0;
	private static int height=0;

	private boolean continueLooping = true;
	private int iterationCounter = 0;

	private MCTSNode finalNode = null;

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
			if(iterationCounter==MAX_ITERATIONS) continueLooping = false;

			if(iterationCounter%500==0) System.out.println("MCTS iteration: "+iterationCounter);
			iterationCounter++;

			MCTSNode workingNode = root;

			while(workingNode.children.size() != 0) workingNode = select(workingNode);

			if(workingNode.getVisits() >= MINIMUM_VISITS) {
				expand(workingNode);

				int childID = (int) (workingNode.getChildren().size() * Math.random());

				workingNode = workingNode.getChildren().get(childID);
			}

			double score = simulate(workingNode);

			backPropagate(score, workingNode);
		}

		//Construct best path

		int i = 0;
		if(finalNode!=null){
			System.out.println("Reconstructing path that leads to goal config");
			MCTSNode workingNode = finalNode;

			while(workingNode.getParent() != null){
				nodePath.add(workingNode);
				workingNode = workingNode.getParent();
			}

			Collections.reverse(nodePath);

			for (MCTSNode node: nodePath) {
				path.add(node.getAction());
			}
		} else {
			System.out.println("Reconstructing best path");
			while(root.getChildren().size()>0){
				MCTSNode next = bestValueChild(root);

				System.out.println("Frame " + i + ": " + estimateScore(next.getConfiguration()));
				i++;

				nodePath.add(next);

				Action a = next.getAction();
				path.add(a);

				root = next;
			}
		}
	}

	private void backPropagate(double score, MCTSNode workingNode) {
		while(true){
			workingNode.addScore(score);
			if(workingNode.getParent() != null) workingNode = workingNode.getParent();
			else break;
		}
	}

	public MCTSNode bestValueChild(MCTSNode parent){
		ArrayList<MCTSNode> children = parent.getChildren();

		double min = Double.MAX_VALUE;
		MCTSNode best = null;

		for (MCTSNode child: children) {
			if(child.getAverageScore()<min){ // && child.getScore()!=Integer.MIN_VALUE
				min = child.getAverageScore();
				best = child;
			}

		}

		return best;
	}

	public double selectPolicy(MCTSNode node){
		double selectScore = node.getAverageScore() - Math.sqrt(EXPLORATION)*Math.sqrt(Math.log(node.getParent().getVisits())/node.getVisits());

		return selectScore;
	}

	public MCTSNode select(MCTSNode origin){
		double min = Double.MAX_VALUE;
		MCTSNode minNode = null;

		for (MCTSNode child: origin.getChildren()) {
			if(child.getVisits() < MINIMUM_VISITS) return child;

			double selectScore = selectPolicy(child);

			if(selectScore < min){
				min = selectScore;
				minNode = child;
			}
		}

		return minNode;
	}

	public void expand(MCTSNode origin){
		ArrayList<Action> validActions = origin.getConfiguration().getAllValidActions();

		if (origin.getConfiguration().equals(sim.getGoalConfiguration())) {
			System.out.println("Found goal config!");
			continueLooping = false;
			finalNode = origin;
		} else {
			for (Action action:validActions) {
				Configuration configCopy = origin.getConfiguration().copy();
				configCopy.apply(action);

				MCTSNode child = new MCTSNode(configCopy);
				child.setAction(action);

				origin.addChild(child);

				if (isSameAsAParent(origin)) origin.getParent().getChildren().remove(origin);
			}
		}
	}

	public double simulate(MCTSNode origin) {
		Configuration currentConfig = origin.getConfiguration();
		if (currentConfig.equals(sim.getGoalConfiguration())) {
			System.out.println("Found goal config!");
			continueLooping = false;
			finalNode = origin;
		} else {

			int moveCounter = 0;

			while (moveCounter < SIMULATION_DEPTH) {
				moveCounter++;

				Configuration nextConfig = currentConfig.copy();
				currentConfig = nextConfig;

				ArrayList<Action> validActions = currentConfig.getAllValidActions();

				double chance = Math.random();

				if (chance > GREEDY_SIMULATION_CHANCE) { //random
					int size = validActions.size();
					int random = (int) (Math.random() * size);

					currentConfig.apply(validActions.get(random));
				} else { //greedy
					double bestScore = Integer.MAX_VALUE;
					Action bestAction = null;
					for (Action action : validActions) {
						Configuration testConfig = currentConfig.copy();
						testConfig.apply(action);

						double currentScore = estimateScore(testConfig);

						if (currentScore < bestScore) {
							bestAction = action;
							bestScore = currentScore;
						}
					}

					currentConfig.apply(bestAction);
				}
			}
		}
		return estimateScore(currentConfig);
	}

	public double estimateScore(Configuration config){
		ArrayList<Agent> agents = config.getAgents();
		ArrayList<Agent> goals = config.getSimulation().getGoalConfiguration().getAgents();
		double totalManhattanDistance = 0;

		for(int i = 0; i < agents.size() ; i++) totalManhattanDistance += Math.pow(agents.get(i).getManhattanDistanceTo(goals.get(i).getLocation()),2);

		totalManhattanDistance = totalManhattanDistance/agents.size();

		return totalManhattanDistance;
	}

	public boolean isSameAsAParent(MCTSNode startingNode){
		if(startingNode.getParent() == null) return false;

		MCTSNode workingNode = startingNode.getParent();

		while(true){
			if(workingNode.getConfiguration().equals(startingNode.getConfiguration())) return true;

			if(workingNode.getParent() == null) return false;
			else workingNode = workingNode.getParent();
		}
	}

	@Override
	public void takeTurn() {
		if(turnCounter==0){
			mainMCTS(sim);
		}

		if(turnCounter < path.size()){
			sim.apply(path.get(turnCounter));
			turnCounter++;
		} else sim.finish();
	}
}