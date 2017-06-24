package ai;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.sun.jmx.remote.internal.ArrayQueue;

import all.continuous.Agent;
import all.continuous.AgentAction;
import all.continuous.CollisionType;
import all.continuous.CollisionUtil;
import all.continuous.CollisionUtil.Collision;
import all.continuous.Configuration;
import all.continuous.Direction;
import all.continuous.ModuleAlgorithm;
import all.continuous.PositionUtil;
import all.continuous.Ray;
import all.continuous.Simulation;
import javafx.geometry.Point3D;

public class AStarGroupedAlgorithm extends ModuleAlgorithm {
	
	private static final int GROUP_SIZE = 3;
	private Random rand = new Random();

	public AStarGroupedAlgorithm(Simulation sim) {
		super(sim);
	}
	
	private Set<Set<Integer>> groups;
	private Queue<AgentAction> actions = new LinkedList<>();
	
	private Set<Integer> movingGroup;
	private List<AStarNode> currentPath;

	@Override
	public void takeTurn() {
		if (groups == null) { // Determine groups if none have been made yet
			groups = new HashSet<>();
			Configuration conf = sim.getCurrentConfiguration();
			int agentCount = conf.getAgents().size();
			Set<Integer> availableAgents = IntStream.range(0, agentCount).boxed().collect(Collectors.toSet());
			
			int currentAgentIndex = rand.nextInt(agentCount);
			availableAgents.remove(currentAgentIndex);
			
			Set<Integer> currentGroup = new HashSet<>();
			currentGroup.add(currentAgentIndex);
			
			while (!availableAgents.isEmpty()) {
				if (currentGroup.size() == GROUP_SIZE) {
					groups.add(currentGroup);
					if (availableAgents.size() < GROUP_SIZE) {
						groups.add(new HashSet<>(availableAgents));
						availableAgents.clear();
						break;
					}
					currentGroup = new HashSet<>();
					currentAgentIndex = new ArrayList<Integer>(availableAgents).get(rand.nextInt(availableAgents.size()));
					availableAgents.remove(currentAgentIndex);
					currentGroup.add(currentAgentIndex);
				}
				
				Agent currentAgent =  conf.getAgent(currentAgentIndex);
				Point3D currentAgentPos = currentAgent.getLocation();
				for (Point3D dir : Direction.DIRECTIONS) {
					Collision col = CollisionUtil.castRay(conf, new Ray(PositionUtil.center(currentAgentPos), dir), currentAgent);
					if (col.type == CollisionType.AGENT && availableAgents.contains(((Agent)col.collided).getIndex())) {
						currentGroup.add(((Agent)col.collided).getIndex());
						availableAgents.remove(((Agent) col.collided).getIndex());
						
						if (currentGroup.size() == GROUP_SIZE) {
							break;
						}
					}
				}
				
				currentAgentIndex = new ArrayList<Integer>(currentGroup).get(rand.nextInt(currentGroup.size()));
			}
			
			groups.add(currentGroup);
			System.out.println(groups);
			
			this.movingGroup = new ArrayList<Set<Integer>>(groups).get(rand.nextInt(groups.size()));
		}
		
		if (currentPath == null) {
			Configuration goal = sim.getGoalConfiguration();
			ArrayList<Agent> currentGoalAgents = new ArrayList<>();
	
			Configuration currentGoal = new Configuration(currentGoalAgents);
			for (Agent a : goal.getAgents()) {
				if (movingGroup.contains(a.getIndex())) {
					Agent newAgent = new Agent(a.getId(), a.getLocation());
					newAgent.setIndex(a.getIndex());
					currentGoalAgents.add(newAgent);
				} else {
					currentGoalAgents.add(null);
				}
			}
			currentPath = AStar.aStar(this::calculateScore, this::getNeighbours, new AStarNode(sim.getCurrentConfiguration(), null), new AStarNode(currentGoal, null)); // TODO: Use queue instead of list
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
		
		if (currentPath.isEmpty()) {
			if (groups.size() <= 1) sim.finish();
			else {
				groups.remove(this.movingGroup);
				this.movingGroup = new ArrayList<Set<Integer>>(groups).get(rand.nextInt(groups.size()));
				currentPath = null;
			}
		}
		
		//sim.finish();
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
		for (Integer agentIndex : movingGroup) {
			score += calculateAgentScore(agentIndex, node.conf, goal.conf);
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
		List<AStar.Neighbour<AStarNode>> neighbours = new ArrayList<>();
		for (Integer agentIndex : movingGroup) {
			List<AgentAction> actions = node.conf.getPhysicalActions(node.conf.getAgent(agentIndex));
			
			for (AgentAction a : actions) {
				// FIXME: This is veeeeeeeeeeeeeeeeery memory inefficient...
				if (node.conf.getAgent(a.index).hasMoved()) continue;
				Configuration neighbour = node.conf.copy();
				neighbour.applyPhysical(a);
				Simulation sim = new Simulation(this.sim.getTerrain(), neighbour, this.sim.getGoalConfiguration());
				
				sim.endTurn();
				neighbours.add(new AStar.Neighbour<AStarNode>(new AStarNode(sim.getCurrentConfiguration(), a), 1));
			}
		}
		
		return neighbours;
	}

}
