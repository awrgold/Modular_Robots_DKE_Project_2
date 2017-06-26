package ai;

import java.util.ArrayList;
import java.util.Collections;

import all.continuous.*;

public class MCTS extends ModuleAlgorithm{

	//SETTINGS
	private final double GREEDY_SIMULATION_CHANCE = 0;
	private final int MAX_ITERATIONS = 50000;
	private final int MINIMUM_VISITS = 20;
	private final double EXPLORATION = Math.sqrt(2);
	private final int SIMULATION_DEPTH = 20;

	private final boolean VERBOSE_DEBUG = false;

	ArrayList<ArrayList<Action>> path = new ArrayList<>();
	ArrayList<MCTSNode> nodePath = new ArrayList<MCTSNode>();

	private int turnCounter = 0;
	private int simCounter = 0;
	private int height = 0;
	private int nodeCount = 0;
	private long startTime;
	private int moveCounter = 0;

	private boolean continueLooping = true;
	private int iterationCounter = 0;

	private MCTSNode finalNode = null;

	public MCTS(Simulation sim) {
		super(sim);
	}

	public void mainMCTS(Simulation sim){
		System.out.println("Initializing MCTS with settings: max iterations - " + MAX_ITERATIONS +
				"; greedy simulation chance - " + GREEDY_SIMULATION_CHANCE + "; minimum visits - " + MINIMUM_VISITS +
				"; exploration - " + EXPLORATION + "; simulation depth: " + SIMULATION_DEPTH + ";");

		MCTSNode root = new MCTSNode(sim.getCurrentConfiguration());
		root.addVisit();
		expand(root);

		startTime = System.nanoTime();

		//Build tree
		while(continueLooping){
			if(iterationCounter==MAX_ITERATIONS) continueLooping = false;

			if(iterationCounter%(Math.max((MAX_ITERATIONS/10),1000))==0) System.out.println(" MCTS iteration: "+iterationCounter);
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

		int i = 1;
		if(finalNode!=null){
			System.out.println(" Reconstructing path that leads to goal config");

			MCTSNode workingNode = finalNode;

			while(workingNode.getParent() != null){
				nodePath.add(workingNode);
				workingNode = workingNode.getParent();
			}

			Collections.reverse(nodePath);
		} else {
			System.out.println(" Reconstructing best path");
			while(root.getChildren().size()>0){
				MCTSNode next = bestValueChild(root);

				if(VERBOSE_DEBUG) System.out.println("  Frame " + i + ": " + estimateScore(next.getConfiguration()));
				i++;

				nodePath.add(next);

				root = next;
			}
		}

		ArrayList<Action> actions = new ArrayList<>();

		for (MCTSNode node: nodePath) {
			actions.add(node.getAction());
			if(node.getAction().getAgent() == -1 && node.getAction().getDestination()==null) {
				path.add(actions);
				actions = new ArrayList<>();
			} else moveCounter++;

			if(VERBOSE_DEBUG) System.out.println("  Frame " + i + ": " + estimateScore(node.getConfiguration()));
			i++;
		}
		path.add(actions);

		long time = System.nanoTime() - startTime;
		System.out.println("Time: " + (int) (time/(Math.pow(10,9))) + " seconds; node count: " + nodeCount + "; timestep count: " + path.size() + "; move count: " + moveCounter + "; simulation count: " + simCounter + "; iterations: " + iterationCounter);

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
		double selectScore = node.getAverageScore() - EXPLORATION*Math.sqrt(Math.log(node.getParent().getVisits())/node.getVisits());

		return selectScore;
	}

	public MCTSNode select(MCTSNode origin){
		int minVisits = MINIMUM_VISITS;
		MCTSNode minVisitsNode = null;

		for(MCTSNode child: origin.getChildren()){
			if(child.getVisits() < minVisits) {
				minVisits = child.getVisits();
				minVisitsNode = child;
			}
		}

		if(minVisitsNode != null) return minVisitsNode;

		double min = Double.MAX_VALUE;
		MCTSNode minNode = null;

		for (MCTSNode child: origin.getChildren()) {
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

				if(!(action.getAgent()==-1 && action.getDestination()==null) && isSameAsAParent(child)){ //config already exists, and action is NOT end-turn
					origin.getChildren().remove(child);
				} else nodeCount++;

				if(configCopy.equals(sim.getGoalConfiguration())){
					System.out.println("Found goal config!");
					continueLooping = false;
					finalNode = child;
				}
			}
			if(origin.getChildren().size()==0) {
				Action endTurn = new Action(-1,null);
				Configuration configCopy = origin.getConfiguration().copy();
				configCopy.apply(endTurn);

				MCTSNode child = new MCTSNode(configCopy);
				child.setAction(endTurn);

				origin.addChild(child);
			}
		}
	}

	public double simulate(MCTSNode origin) {
		Configuration currentConfig = origin.getConfiguration().copy();
		if (currentConfig.equals(sim.getGoalConfiguration())) {
			System.out.println("Found goal config!");
			continueLooping = false;
			finalNode = origin;
		} else {

			int moveCounter = 0;

			while (moveCounter < SIMULATION_DEPTH) {
				moveCounter++;
				simCounter++;

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

		totalManhattanDistance = 1 + totalManhattanDistance/agents.size();

		totalManhattanDistance = totalManhattanDistance * config.getTurns();

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
			for(Action action: path.get(turnCounter)) sim.apply(action);

			turnCounter++;
		} else sim.finish();
	}
}