package ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import all.continuous.Action;
import all.continuous.Agent;
import all.continuous.Configuration;
import all.continuous.ModuleAlgorithm;
import all.continuous.Simulation;
import javafx.geometry.Point3D;

public class AStarAlgorithm extends ModuleAlgorithm {

	private List<Configuration> currentPath;

	private Random rand = new Random();

	private int iter = 0;

	public AStarAlgorithm(Simulation sim) {
		super(sim);
	}

	@Override
	public void takeTurn() {
		if (currentPath == null) {
			Configuration goal = sim.getGoalConfiguration();
			currentPath = AStar.aStar(this::calculateScore, this::getNeighbours, sim.getCurrentConfiguration(), sim.getGoalConfiguration()); // TODO: Use queue instead of list
			if (currentPath.isEmpty()) {
				sim.finish();
				return;
			}
			currentPath.remove(0);
		}

		for (Agent agent : sim.getCurrentConfiguration().getAgents()) {
			Point3D nextLocation = currentPath.get(0).getAgent(agent.getIndex()).getLocation();
			Action a = new Action(agent.getIndex(), nextLocation);

			sim.apply(a);
		}
		
		currentPath.remove(0);
		
		if (currentPath.isEmpty()) sim.finish();

		iter++;
	}

	private float calculateAgentScore(int agentIndex, Configuration current, Configuration goal) {
//		double dist = Double.MAX_VALUE;
//		boolean lonelyAgent = false;
//		for (Agent a : goal.getAgents()) {
//			double agentDist = a.getManhattanDistanceTo(current.getAgent(agentIndex).getLocation());
//			dist = Math.min(agentDist, dist);
//			//if (current.getAllValidActions(a).size() == 0) lonelyAgent = true;
//		}
//		return (float) (dist + (lonelyAgent ? 20 : 0));
		float score = (float) current.getAgent(agentIndex).getLocation().distance(goal.getAgent(agentIndex).getLocation()) * 1.8f;
		score *= current.getAgent(agentIndex).isConnected(current) ? 1 : 2.5f;
		return score;
	}

	private float calculateScore(Configuration node, Configuration goal) {
		float score = 0;
		for (Agent agent : node.getAgents()) {
			score += calculateAgentScore(agent.getIndex(), node, goal);
		}
		return score;
	}

	private List<AStar.Neighbour<Configuration>> getNeighbours(Configuration conf) {
		List<Action> actions = conf.getAllValidActions();
		List<AStar.Neighbour<Configuration>> neighbours = new ArrayList<>();
		for (Action a : actions) {
			// FIXME: This is veeeeeeeeeeeeeeeeery memory inefficient...
			Configuration neighbour = conf.copy();
			neighbour.setSimulation(sim);
			neighbour.apply(a);
			neighbours.add(new AStar.Neighbour<Configuration>(neighbour, 1));
		}
		return neighbours;
	}

}
