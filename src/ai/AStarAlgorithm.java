package ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import all.continuous.Action;
import all.continuous.Agent;
import all.continuous.AgentAction;
import all.continuous.Configuration;
import all.continuous.ModuleAlgorithm;
import all.continuous.Simulation;
import javafx.geometry.Point3D;

public class AStarAlgorithm extends ModuleAlgorithm {

	private List<AStarNode> currentPath;

	private Random rand = new Random();

	private int iter = 0;

	public AStarAlgorithm(Simulation sim) {
		super(sim);
	}

	@Override
	public void takeTurn() {
		if (currentPath == null) {
			Configuration goal = sim.getGoalConfiguration();
			currentPath = AStar.aStar(this::calculateScore, this::getNeighbours, new AStarNode(sim.getCurrentConfiguration(), null), new AStarNode(sim.getGoalConfiguration(), null)); // TODO: Use queue instead of list
			if (currentPath.isEmpty()) {
				sim.finish();
				return;
			}
			currentPath.remove(0);
		}

		AgentAction action = currentPath.get(0).action;
		if (action != null) {
			System.out.println("ITER: " + action.toString());
			sim.applyPhysical(action);
		}
		
		currentPath.remove(0);
		
		if (currentPath.isEmpty()) sim.finish();

		iter++;
	}
	
    private float manhattan(Point3D a, Point3D b) {
    	return (float) (Math.abs(a.getX()-b.getX()) + Math.abs(a.getY()-b.getY()) + Math.abs(a.getZ()-b.getZ()));
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
		float score = (float) manhattan(current.getAgent(agentIndex).getLocation(), goal.getAgent(agentIndex).getLocation()) * 2.2f;
		if (score > 0.001f) score += 1.2f;
		score += current.getAgent(agentIndex).isConnected(current) ? 0 : 8.5f;
		return score;
	}

	private float calculateScore(AStarNode node, AStarNode goal) {
		float score = 0;
		for (Agent agent : node.conf.getAgents()) {
			score += calculateAgentScore(agent.getIndex(), node.conf, goal.conf);
		}
		return score;
	}
	
	private class AStarNode {
		public final Configuration conf;
		public final AgentAction action;
		
		public AStarNode(Configuration conf, AgentAction action) {
			this.conf = conf;
			this.action = action;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof AStarNode)) return false;
			return ((AStarNode) obj).conf.equals(this.conf);
		}
		
		@Override
		public int hashCode() {
			return this.conf.hashCode();
		}
	}

	private List<AStar.Neighbour<AStarNode>> getNeighbours(AStarNode node) {
		List<AgentAction> actions = node.conf.getAllPhysicalActions();
		List<AStar.Neighbour<AStarNode>> neighbours = new ArrayList<>();
		for (AgentAction a : actions) {
			// FIXME: This is veeeeeeeeeeeeeeeeery memory inefficient...
			if (node.conf.getAgent(a.index).hasMoved()) continue;
			Configuration neighbour = node.conf.copy();
			neighbour.applyPhysical(a);
			Simulation sim = new Simulation(this.sim.getTerrain(), neighbour, this.sim.getGoalConfiguration());
			
			sim.endTurn();
			neighbours.add(new AStar.Neighbour<AStarNode>(new AStarNode(sim.getCurrentConfiguration(), a), 1));
		}
		return neighbours;
	}

}
