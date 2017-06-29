package ai;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
	
	private Set<Integer> movingGroup;
	private List<AStarNode> currentPath;

	@Override
	public void takeTurn() {
		if (groups == null) { // Determine groups if none have been made yet
			groups = new HashSet<>();
			Configuration conf = sim.getCurrentConfiguration();
			int agentCount = conf.getAgents().size();
			Set<Integer> availableAgents = IntStream.range(0, agentCount).boxed().collect(Collectors.toSet());
			
			Configuration goal = sim.getGoalConfiguration();
			int agentIndex = sim.getCurrentConfiguration().getAgents()
					.stream()
					.reduce((a, b) -> calculateAgentScore(a.getIndex(), sim.getCurrentConfiguration(), goal) > 
										calculateAgentScore(b.getIndex(), sim.getCurrentConfiguration(), goal) ? a : b).get().getIndex();
			availableAgents.remove(agentIndex);
			
			Set<Integer> currentGroup = new HashSet<>();
			currentGroup.add(agentIndex);
			
			while (!availableAgents.isEmpty()) {
//				if (currentGroup.size() == GROUP_SIZE) {
//					groups.add(currentGroup);
//					if (availableAgents.size() <= GROUP_SIZE) {
//						break;
//					}
//					currentGroup = new HashSet<>();
//					currentAgentIndex = new ArrayList<Integer>(availableAgents).get(rand.nextInt(availableAgents.size()));
//					availableAgents.remove(currentAgentIndex);
//					currentGroup.add(currentAgentIndex);
//				}
				
				int groupSize = currentGroup.size();
				
				Set<Integer> newGroup = new HashSet<>(currentGroup);
				
				for (Integer currentAgentIndex : currentGroup) {
					Agent currentAgent =  conf.getAgent(currentAgentIndex);
					Point3D currentAgentPos = currentAgent.getLocation();
					for (Point3D dir : Direction.DIRECTIONS) {
						Collision col = CollisionUtil.castRay(conf, new Ray(PositionUtil.center(currentAgentPos), dir), currentAgent);
						if (col.type == CollisionType.AGENT && availableAgents.contains(((Agent)col.collided).getIndex())) {
							newGroup.add(((Agent)col.collided).getIndex());
							availableAgents.remove(((Agent) col.collided).getIndex());
							
							if (currentGroup.size() == GROUP_SIZE) {
								break;
							}
						}
					}
				}
				
				currentGroup = newGroup;
				
				if (currentGroup.size() == groupSize || currentGroup.size() >= GROUP_SIZE) {
					groups.add(currentGroup);
					if (availableAgents.size() == 0) break;
					agentIndex = availableAgents.stream()
							.reduce((a, b) -> calculateAgentScore(a, sim.getCurrentConfiguration(), goal) > 
												calculateAgentScore(b, sim.getCurrentConfiguration(), goal) ? a : b).get();
					availableAgents.remove(agentIndex);
					
					currentGroup = new HashSet<>();
					currentGroup.add(agentIndex);
				}
			}
			
			Set<Set<Integer>> finalGroups = new HashSet<>();
			
			for (Set<Integer> group : groups) {
				Set<Integer> finalGroup = new HashSet<>();
				for (Integer groupAgentID : group) {
					finalGroup.add(groupAgentID);
					Agent currentAgent =  conf.getAgent(groupAgentID);
					Point3D currentAgentPos = currentAgent.getLocation();
					for (Point3D dir : Direction.DIRECTIONS) {
						Collision col = CollisionUtil.castRay(conf, new Ray(PositionUtil.center(currentAgentPos), dir), currentAgent);
						if (col.type == CollisionType.AGENT && availableAgents.contains(((Agent)col.collided).getIndex())) {
							finalGroup.add(((Agent) col.collided).getIndex());
							availableAgents.remove(((Agent) col.collided).getIndex());
						}
					}
				}
				finalGroups.add(finalGroup);
			}
			
			this.groups = finalGroups;
			
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
			ArrayList<AgentAction> possibleActions = sim.getAllPhysicalActions();
			AgentAction actualAction = null;
			for (AgentAction posAction : possibleActions) {
				Configuration neighbour = sim.getCurrentConfiguration().copy();
				neighbour.applyPhysical(posAction);
				Simulation simNew = new Simulation(this.sim.getTerrain(), neighbour, this.sim.getGoalConfiguration());
				
				simNew.endTurn();
				if (simNew.getCurrentConfiguration().equals(currentPath.get(0).conf)) {
					actualAction = posAction;
					break;
				}
			}
			
			if (actualAction != null)
				sim.applyPhysical(actualAction);
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
		double dist = Double.MAX_VALUE;
		boolean lonelyAgent = false;
		for (Agent a : sim.getGoalConfiguration().getAgents()) {
//			if (CollisionUtil.isCollidingCubeMult(current, a.getLocation(), current.getAgent(agentIndex)).stream().anyMatch((col) -> col.type == CollisionType.AGENT))
//				continue;
			double agentDist = a.getManhattanDistanceTo(current.getAgent(agentIndex).getLocation());
			dist = Math.min(agentDist, dist);
			//if (current.getAllValidActions(a).size() == 0) lonelyAgent = true;
		}
		float score = (float) (dist + (lonelyAgent ? 20 : 0));
		//score *= current.getAgent(agentIndex).isConnected(current) ? 1 : 3f;
		score += current.getAgent(agentIndex).isConnected(current) ? 0 : 8.5f;
		return score;
//		float score = (float) manhattan(current.getAgent(agentIndex).getLocation(), goal.getAgent(agentIndex).getLocation()) * 2.2f;
//		//if (score > 0.001f) score += 1.2f;
//		score += current.getAgent(agentIndex).isConnected(current) ? 0 : 8.5f;
//		return score;
	}

	private float calculateScore(AStarNode node, AStarNode goal) {
		float score = 0;
		for (Integer agentIndex : movingGroup) {
			score += calculateAgentScore(agentIndex, node.conf, goal.conf);
		}
		if (score < movingGroup.size()*0.1) return 0;
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
