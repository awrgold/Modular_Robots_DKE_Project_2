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

public class AStarGreedyAlgorithm extends ModuleAlgorithm {

	private int currentAgentIndex = -1;
	private List<Configuration> currentPath;
	
	private Random rand = new Random();
	
	private int iter = 0;

	public AStarGreedyAlgorithm(Simulation sim) {
		super(sim);
	}

	@Override
	public void takeTurn() {
		if (currentAgentIndex == -1 || currentPath.isEmpty()) {
			Configuration goal = sim.getGoalConfiguration();
			currentAgentIndex = sim.getCurrentConfiguration().getAgents()
				.stream()
				.reduce((a, b) -> calculateAgentScore(a.getIndex(), sim.getCurrentConfiguration(), goal) > 
									calculateAgentScore(b.getIndex(), sim.getCurrentConfiguration(), goal) ? a : b).get().getIndex();
			if (Math.random() < 0.1) {
				currentAgentIndex = rand.nextInt(sim.getCurrentConfiguration().getAgents().size());
			} 
			currentPath = AStar.aStar(this::calculateScore, this::getNeighbours, sim.getCurrentConfiguration(), sim.getGoalConfiguration()); // TODO: Use queue instead of list
			if (currentPath.isEmpty()) {
				sim.finish();
				return;
			}
			currentPath.remove(0);
		}
		
		Point3D nextLocation = currentPath.get(0).getAgent(currentAgentIndex).getLocation();
		
		Action a = new Action(currentAgentIndex, nextLocation);
		
		sim.apply(a);
		
		currentPath.remove(0);
		
		iter++;
		if (iter > 40) sim.finish();
	}
	
	private float calculateAgentScore(int agentIndex, Configuration current, Configuration goal) {
		double dist = Double.MAX_VALUE;
		for (Agent a : goal.getAgents()) {
			double agentDist = a.getManhattanDistanceTo(current.getAgent(agentIndex).getLocation());
			dist = Math.min(agentDist, dist);
		}
		return (float) dist;
	}
	
	private float calculateScore(Configuration node, Configuration goal) {
		return calculateAgentScore(currentAgentIndex, node, goal);
	}
	
	private List<AStar.Neighbour<Configuration>> getNeighbours(Configuration conf) {
		List<Action> actions = conf.getAllValidActions(conf.getAgent(currentAgentIndex));
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
