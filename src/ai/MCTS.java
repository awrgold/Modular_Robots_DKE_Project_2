package ai;

import java.util.ArrayList;
import java.util.Collections;

import all.continuous.*;

public class MCTS extends ModuleAlgorithm
{
	ArrayList<Action> path = new ArrayList<Action>();
	ArrayList<MCTSNode> nodePath = new ArrayList<MCTSNode>();

	private static int turnCounter=0;
	private static int height=0;

	private final double GREEDY_CHANCE = 0.6;

	boolean printed = false;

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
			if(iterationCounter==10000) continueLooping = false; //TODO: change this to checking whether the goal has been reached

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
		if(finalNode!=null){
			nodePath.add(finalNode);
			MCTSNode workingNode = finalNode;

			while(workingNode.getParent() != null){
				workingNode = workingNode.getParent();
				nodePath.add(workingNode);
			}

			Collections.reverse(nodePath);

			for (MCTSNode node: nodePath) {
				path.add(node.getAction());
			}
		} else {
			while(root.getChildren().size()>0){
				MCTSNode next = bestValueChild(root);

				nodePath.add(next);

				Action a = next.getAction();
				path.add(a);

				root = next;
			}
		}
	}

	public MCTSNode bestValueChild(MCTSNode parent){
		ArrayList<MCTSNode> children = parent.getChildren();

		double min = Double.MAX_VALUE;
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

		double max = Double.MAX_VALUE;
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

		if (origin.getConfiguration().equals(sim.getGoalConfiguration())) {
			if(!printed) System.out.println("Found goal config!");
			printed = true;
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

	public void simulate(MCTSNode origin) {
		Configuration currentConfig = origin.getConfiguration();
		if (currentConfig.equals(sim.getGoalConfiguration())) {
			if(!printed) System.out.println("Found goal config!");
			printed = true;
			continueLooping = false;
			finalNode = origin;
		} else {
			long t = System.nanoTime();
			long end = t + 1000;

			while (System.nanoTime() < end) {
				Configuration nextConfig = currentConfig.copy();
				currentConfig = nextConfig;

				ArrayList<Action> validActions = currentConfig.getAllValidActions();

				double chance = Math.random();

				if (chance > GREEDY_CHANCE) { //random
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

			double score = estimateScore(currentConfig);

			origin.setScore(score);
		}
	}

	public double estimateScore(Configuration config){
		ArrayList<Agent> agents = config.getAgents();
		ArrayList<Agent> goals = config.getSimulation().getGoalConfiguration().getAgents();
		double totalManhattanDistance = 0;

		for(int i = 0; i < agents.size() ; i++) totalManhattanDistance += Math.pow(agents.get(i).getManhattanDistanceTo(goals.get(i).getLocation()),2);

		totalManhattanDistance = totalManhattanDistance/agents.size();

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
			if(start.getParent().getChildren().size() != 0) totalScore = totalScore/start.getParent().getChildren().size();

			start.getParent().setScore(totalScore);

			start = start.getParent();
			newHeight++;
		}

		if(newHeight>height) height=newHeight;
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
	public void takeTurn() { //TODO: there could be something wrong with the counters here
		if(turnCounter==0){
			mainMCTS(sim);
		}

		if(turnCounter < path.size()){
			sim.apply(path.get(turnCounter));
			turnCounter++;
		} else sim.finish();
	}
}