package all.continuous;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javafx.geometry.Point3D;

public class GreedyAlgorithm extends ModuleAlgorithm {

	private Random rand;
	
	public GreedyAlgorithm(Simulation sim) {
		super(sim);
		this.rand = new Random();
	}
	
	private double agentDist(Point3D point, Agent agent) {
		return agent.getLocation().distance(point);
	}

	private double calcScore(Action action) {
		double score = 0.0;
		boolean shouldDouble = false;
		for (Agent agent : sim.getCurrentConfiguration().agents) {
			final Point3D current = action.getAgent() == agent.index ? action.getDestination() : agent.getLocation();
			Agent goalAgent = sim.getGoalConfiguration().agents.stream().reduce((a, b) -> agentDist(current, a) < agentDist(current, b) ? a : b).get();
			
			double dist = agentDist(current, goalAgent);
			score += dist;
			
			if (action.getAgent() == agent.index && explored.get(agent.id).contains(current)) {
				shouldDouble = true;
			}
		}
		return score * (shouldDouble ? 1.5 : 1);
	}
	
	private int steps = 0;
	private Map<Float, Set<Point3D>> explored = new HashMap<>();
	
	@Override
	public void takeTurn() {
		sim.getCurrentConfiguration().agents.stream().forEach((x) -> {
			if (!explored.containsKey(x.id)) explored.put(x.id, new HashSet<>());
			explored.get(x.id).add(x.getLocation());
		});
		List<Action> actions = this.sim.getAllValidActions();
		Action bestAction = actions.stream().reduce((a, b) -> calcScore(a) > calcScore(b) ? b : a).get();
		sim.apply(bestAction);
		steps++;
		if (steps > 60) sim.finish();
	}
	
}
