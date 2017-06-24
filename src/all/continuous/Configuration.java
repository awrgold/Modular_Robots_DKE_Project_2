package all.continuous;

import javafx.geometry.Point3D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import org.joml.Vector3d;

import all.continuous.CollisionUtil.Collision;
import javafx.geometry.Point3D;

import static all.continuous.CollisionUtil.castRayCube;
import static all.continuous.CollisionUtil.castRayCubeFalling;

public class Configuration {
	Simulation simulation;
	ArrayList<Agent> agents;

	public Configuration(ArrayList<Agent> agents) {
		this.agents = agents;

		for (int i = 0; i < agents.size(); i++) {
			agents.get(i).setIndex(i);
//			if (agents.get(i).getLocation().getX() != Math.floor(agents.get(i).getLocation().getX()) || agents.get(i).getLocation().getZ() != Math.floor(agents.get(i).getLocation().getZ())) {
//				System.out.println("ewfewfwef");
//			}
		}
	}

	public void apply(Action action){
		Agent agent = agents.get(action.getAgent());
		agent.move(action.getDestination());
	}

	public void applyPhysical(AgentAction action) {
		Agent a = getAgent(action.index);
		if (a.moved) 
			throw new IllegalStateException("no cheating!");
		action.apply(a);
		a.moved = true;
	}

	public ArrayList<Action> getAllValidActions(){
		ArrayList<Action> actions = new ArrayList<>();

		for (Agent agent: this.getAgents()) {
			actions.addAll(getAllValidActions(agent));
		}

		return actions;
	}

	public ArrayList<AgentAction> getAllPhysicalActions() {
		ArrayList<AgentAction> actions = new ArrayList<>();

		for (Agent agent: this.getAgents()) {
			actions.addAll(getPhysicalActions(agent));
		}

		return actions;
	}

	public ArrayList<AgentAction> getPhysicalActions(Agent agent) {
		if (this.getAgents().stream().noneMatch((a) -> a == agent))
			throw new IllegalArgumentException("Agent does not exist in this configuration");
		ArrayList<AgentAction> actions = new ArrayList<>();
		
		List<Collision> upCols =  CollisionUtil.isCollidingCubeMult(this, agent.location.add(Direction.UP.multiply(0.01)), agent);
		if (upCols.stream().anyMatch((col) -> col.type == CollisionType.AGENT)) return actions;

		if (CollisionUtil.castRay(this, new Ray(PositionUtil.center(agent.location), Direction.UP), 
				0.01, 0.1+World.VOXEL_SIZE/2.0, 0.01+World.VOXEL_SIZE/2.0, agent).type == CollisionType.AGENT)
			return actions;

		// Attempt movement in all directions
		for (Point3D dir : Direction.DIRECTIONS) {
			// Determine the perpendicular directions (needed to determine groundedness)
			Point3D[] perpDirs = Direction.getPerpDirs(dir);

			// Determine whether the agent is grounded for the current direction
			List<Point3D> groundedDirs = new ArrayList<>();
			List<Collision> groundedCollisions = new ArrayList<>();
			for (Point3D perpDir : perpDirs) {
				if (perpDir == Direction.UP) continue;
				Collision c =  CollisionUtil.castRay(this, new Ray(PositionUtil.center(agent.location), perpDir), 0.01, 0.1+World.VOXEL_SIZE/2.0, 0.01+World.VOXEL_SIZE/2.0, agent);
				if (c.type == CollisionType.AGENT) {
					groundedDirs.add(perpDir);
				}
				
//				List<Collision> cols =  CollisionUtil.isCollidingCubeMult(this, agent.location.add(perpDir.multiply(0.01)), agent);
//				if (cols.stream().anyMatch((col) -> col.type == CollisionType.AGENT))
//					groundedDirs.add(perpDir);
			
//				if (c.type != CollisionType.AGENT && cols.stream().anyMatch((col) -> col.type == CollisionType.AGENT))
//					System.out.println("ewfewf");
//					
//				groundedCollisions.addAll(cols);
			}

			// If the agent is grounded, attempt movement in the current direction
			if (groundedDirs.size() > 0) {
				// Determine the maximum new position
				Collision max = castRayCube(this, new Ray(agent.location, dir), agent);
				Point3D delta = max.location.subtract(agent.location);

				if (max.location.distance(agent.location) < 0.01) continue; // If the agent can't move in this direction, try the next one
				
				if (dir != Direction.UP) {
					for (int i=1; i<10; i++) {
						for (Point3D perpDir : groundedDirs) {
							// TODO: Optimize vector calcs
							Collision c =  CollisionUtil.castRay(this, new Ray(PositionUtil.center(agent.location).add(delta.multiply(0.1*i)), perpDir), 0.01, 0.1+World.VOXEL_SIZE/2.0, 0.05+World.VOXEL_SIZE/2.0, agent, true);
							Point3D left = PositionUtil.center(agent.location).add(delta.multiply(0.1*i).subtract(dir.multiply(World.VOXEL_SIZE/2+0.03)));
							Point3D right = PositionUtil.center(agent.location).add(delta.multiply(0.1*i).add(dir.multiply(World.VOXEL_SIZE/2+0.03)));
							Collision cLeft = CollisionUtil.castRay(this, new Ray(left, perpDir), 0.01, 0.1+World.VOXEL_SIZE/2.0, 0.05+World.VOXEL_SIZE/2.0, agent, true);
							Collision cRight = CollisionUtil.castRay(this, new Ray(right, perpDir), 0.01, 0.1+World.VOXEL_SIZE/2.0, 0.05+World.VOXEL_SIZE/2.0, agent, true);
							if ((c.type == CollisionType.AGENT || c.type == CollisionType.OBSTACLE) && cLeft.collided != c.collided && cRight.collided != c.collided) {
								double impulseMag = calculateImpulseForDist(delta.multiply(0.1*i).magnitude());
								//actions.add(new ImpulseAction(agent.getIndex(), new Vector3d(dir.getX(), dir.getY(), dir.getZ()).mul(impulseMag)));
							}
						}
					}
				}

				// Determine whether the agent remains grounded after the movement
				boolean remainsGrounded = false;
				for (Point3D perpDir : groundedDirs) {
//					List<Collision> cols =  CollisionUtil.isCollidingCubeMult(this, max.location.add(perpDir.multiply(0.01)), agent);
//					if (cols.stream().anyMatch((col) -> col.type == CollisionType.AGENT)) {
//						remainsGrounded = true;
//					} else if (dir == Direction.UP || perpDir == Direction.UP || perpDir == Direction.DOWN) {
//						determineDiagPhysicalAction(actions, agent, max.location, perpDir, dir);
//					}
					Collision c =  CollisionUtil.castRay(this, new Ray(PositionUtil.center(max.location), perpDir), 0.01, 0.1+World.VOXEL_SIZE/2.0, 0.05+World.VOXEL_SIZE/2.0, agent);
					if (c.type == CollisionType.AGENT) {
						remainsGrounded = true;
					} else if (dir == Direction.UP || perpDir == Direction.UP || perpDir == Direction.DOWN) {
						// Not grounded here, but it might be possible to move diagonally
						determineDiagPhysicalAction(actions, agent, max.location, perpDir, dir);
					}
				}

				// If the agent remains grounded, it can move in the current direction
				if (dir != Direction.UP && remainsGrounded) {
					double impulseMag = calculateImpulseForDist(max.location.subtract(agent.getLocation()).magnitude());
					actions.add(new ImpulseAction(agent.getIndex(), new Vector3d(dir.getX(), dir.getY(), dir.getZ()).mul(impulseMag)));
				}
			}
		}
		return actions;
	}
	
	private static Map<Double, Double> distToImpulse = new HashMap<>();
	
	static {
		distToImpulse.put(0.0, 0.0);
		distToImpulse.put(0.1, 0.5);
		distToImpulse.put(0.2, 0.75);
		distToImpulse.put(0.3, 0.9);
		distToImpulse.put(0.4, 1.05);
		distToImpulse.put(0.5, 1.2);
		distToImpulse.put(0.6, 1.3);
		distToImpulse.put(0.7, 1.4);
		distToImpulse.put(0.8, 1.5);
		distToImpulse.put(0.9, 1.6);
		distToImpulse.put(1.0, 1.7);
	}

	private double calculateImpulseForDist(double dist) {
		//return distToImpulse.get(Math.max(1.0, Math.min(0.0, Math.round(dist*10.0)/10.0)));
		return 1.7077117958726 * Math.sqrt(dist) - 0.0875062498601*dist + 0.0858603965234 * dist*dist;
	}

	private void determineDiagPhysicalAction(ArrayList<AgentAction> actions, Agent agent, Point3D location, Point3D dir, Point3D primDir) {
		Collision max = castRayCube(this, new Ray(location, dir), agent);
		
		if (max.location.distance(location) < 0.99) return;

		Point3D[] perpDirs = Direction.getPerpDirs(dir);
		boolean grounded = false;
		for (Point3D perpDir : perpDirs) {
//			List<Collision> cols =  CollisionUtil.isCollidingCubeMult(this, max.location.add(perpDir.multiply(0.01)), agent);
//			if (cols.stream().anyMatch((col) -> col.type == CollisionType.AGENT)) grounded = true;
			Collision c =  CollisionUtil.castRay(this, new Ray(PositionUtil.center(max.location), perpDir), 0.01, 0.1+World.VOXEL_SIZE/2.0, 0.05+World.VOXEL_SIZE/2.0, agent);
			if (c.type == CollisionType.AGENT) {
				grounded = true;
				break;
			}
//			
//			if (cols.stream().anyMatch((col) -> col.type == CollisionType.AGENT) != (c.type == CollisionType.AGENT)) {
//				System.out.println("ewfewf");
//			}
		}

		// If the agent remains grounded after this diagonal movement, add it to the actions list
		if (grounded) {
			actions.add(new ClimbAction(agent.getIndex(), primDir, dir));
		}
	}

	public static void mainot(String[] args) {
		ArrayList<Agent> agents = new ArrayList<>();
		agents.add(new Agent(0, new Point3D(0, 0, 0)));
		//agents.add(new Agent(1, new Point3D(World.VOXEL_SIZE, World.VOXEL_SIZE, 0)));
		//agents.add(new Agent(2, new Point3D(World.VOXEL_SIZE, World.VOXEL_SIZE*2, 0)));

		Configuration conf =  new Configuration(agents);

		Simulation sim = new Simulation(new Terrain(new ArrayList<>()), conf, new Configuration(agents));
		sim.setAlgorithm(new RandomAlgorithm(sim));

		//    	for (int i=0; i<1000; i++) {
		//    		sim.physSim.tick(0.1);
		//    		System.out.printf("%s\t%s\n", i*0.1, sim.getCurrentConfiguration().getAgent(2).getPosition().y);
		//    	}
		
		for (double strength=0.1; strength<=1.9; strength+=0.1) {
			Vector3d pos = agents.get(0).getPosition();
			pos.x = 0;
			agents.get(0).setVelocity(new Vector3d());
			agents.get(0).setPosition(pos);
			agents.get(0).applyImpulse(new Vector3d(strength, 0, 0));
			for (int i=0; i<1; i++) {
				sim.physSim.tick(1.0);
			}
			//System.out.printf("Impulse strength of %s gives a relative movement of %s\n", strength, a.getPosition().x);
			//System.out.printf("distToImpulse.put(%s, %s);\n", Math.round(agents.get(0).getPosition().x*10.0)/10.0, Math.round(strength*1000.0)/1000.0);
			System.out.printf("%s\t%s\n", agents.get(0).getPosition().x, strength);
		}

//		System.out.println(sim.getCurrentConfiguration().getAgents());
//		System.out.println(sim.getCurrentConfiguration().getPhysicalActions(sim.getCurrentConfiguration().getAgent(1)));
//
//		sim.endTurn();
//		sim.endTurn();
//		sim.endTurn();
//
//		System.out.println(sim.getCurrentConfiguration().getAgents());
//		System.out.println(sim.getCurrentConfiguration().getPhysicalActions(sim.getCurrentConfiguration().getAgent(1)));
//		//System.out.println(conf.getAllValidActions(agents.get(2)));
	}

	// TODO: Separate grounded method to simplify code
	public ArrayList<Action> getAllValidActions(Agent agent){
		ArrayList<Action> actions = new ArrayList<>();

		if (CollisionUtil.castRay(this, new Ray(PositionUtil.center(agent.location), Direction.UP), 
				0.01, 0.1+World.VOXEL_SIZE/2.0, 0.01+World.VOXEL_SIZE/2.0, agent).type == CollisionType.AGENT)
			return actions;

		// Attempt movement in all directions
		for (Point3D dir : Direction.DIRECTIONS) {
			// Determine the perpendicular directions (needed to determine groundedness)
			Point3D[] perpDirs = Direction.getPerpDirs(dir);

			// Determine whether the agent is grounded for the current direction
			List<Point3D> groundedDirs = new ArrayList<>();
			for (Point3D perpDir : perpDirs) {
				if (perpDir == Direction.UP) continue;
				Collision c =  CollisionUtil.castRay(this, new Ray(PositionUtil.center(agent.location), perpDir), 0.01, 0.1+World.VOXEL_SIZE/2.0, 0.01+World.VOXEL_SIZE/2.0, agent);
				if (c.type == CollisionType.AGENT) {
					groundedDirs.add(perpDir);
				}
			}

			// If the agent is grounded, attempt movement in the current direction
			if (groundedDirs.size() > 0) {
				// Determine the maximum new position
				Collision max = castRayCube(this, new Ray(agent.location, dir), agent);

				if (max.location.distance(agent.location) < 0.01) continue; // If the agent can't move in this direction, try the next one

				// Determine whether the agent remains grounded after the movement
				boolean remainsGrounded = false;
				for (Point3D perpDir : groundedDirs) {
					Collision c =  CollisionUtil.castRay(this, new Ray(PositionUtil.center(max.location), perpDir), 0.01, 0.1+World.VOXEL_SIZE/2.0, 0.05+World.VOXEL_SIZE/2.0, agent);
					if (c.type == CollisionType.AGENT) {
						remainsGrounded = true;
					} else if (dir == Direction.UP || dir == Direction.DOWN || perpDir == Direction.UP || perpDir == Direction.DOWN) {
						// Not grounded here, but it might be possible to move diagonally
						determineDiagAction(actions, agent, max.location, perpDir);
					}
				}

				// If the agent remains grounded, it can move in the current direction
				if (dir != Direction.UP && remainsGrounded) {
					actions.add(new Action(agent.index, max.location));
				}
			}
		}
		return actions;
	}

	private void determineDiagAction(ArrayList<Action> actions, Agent agent, Point3D location, Point3D dir) {
		Collision max = castRayCube(this, new Ray(location, dir), agent);

		Point3D[] perpDirs = Direction.getPerpDirs(dir);
		boolean grounded = false;
		for (Point3D perpDir : perpDirs) {
			Collision c =  CollisionUtil.castRay(this, new Ray(PositionUtil.center(max.location), perpDir), 0.01, 0.1+World.VOXEL_SIZE/2.0, 0.05+World.VOXEL_SIZE/2.0, agent);
			if (c.type == CollisionType.AGENT) {
				grounded = true;
				break;
			}
		}

		// If the agent remains grounded after this diagonal movement, add it to the actions list
		if (grounded) {
			actions.add(new Action(agent.index, max.location));
		}
	}

	public ArrayList<Agent> getAgents(){
		return agents;
	}

	public Agent getAgent(int i){
		return agents.get(i);
	}

	public ArrayList<CubeCouple> getAllCollisions(Agent agent, boolean fast){
		ArrayList<CubeCouple> collisions = new ArrayList<>();

		for (Cube cube: getPotentialCollisions(agent,fast)){
			if(agent.isCollidingWith(cube)) collisions.add(new CubeCouple(agent,cube));
		}

		/*for (Obstacle obstacle:simulation.getTerrain().getObstacles()) {
            if(agent.isCollidingWith(obstacle)) collisions.add(new CubeCouple(agent,obstacle));
        }

        if(fast){
            boolean found = false;

            for (Agent agent2: agents) {
                if(found){
                    if(agent.isCollidingWith(agent2)) collisions.add(new CubeCouple(agent,agent2));
                }

                if(agent2 == agent) found = true;
            }
        }
        else {
            for(Agent agent2: agents){
                if(agent.isCollidingWith(agent2)) collisions.add(new CubeCouple(agent,agent2));
            }
        }*/
		return collisions;
	}

	public ArrayList<CubeCouple> getAllCollisions(){
		ArrayList<CubeCouple> collisions = new ArrayList<>();

		for (Agent agent:agents) {
			for (CubeCouple cc: getAllCollisions(agent, true)) {
				collisions.add(cc);
			}
		}

		return collisions;
	}

	private ArrayList<Cube> getPotentialCollisions(Agent agent, boolean fast) {
		ArrayList<Cube> potentialCollisions = new ArrayList<>();

		//TODO: make this smarter

		for (Obstacle obst:simulation.getTerrain().getObstacles()){
			potentialCollisions.add(obst);
		}

		if(fast){
			boolean found = false;
			for (Agent agent2: agents) {
				if(found) potentialCollisions.add(agent2);
				if(agent2.equals(agent)) found = true;
			}
		} else {
			for (Agent agent2: agents){
				potentialCollisions.add(agent2);
			}
		}

		return potentialCollisions;
	}

	public void setSimulation(Simulation sim){
		this.simulation = sim;
	}

	public boolean isValidConfiguration(){
		return (getAllCollisions().size()==0);
	}

	public boolean[] getAllowedDirections(ArrayList<ArrayList<Agent>> attachedAgents){
		//TODO: make this smarter by using ints instead of booleans: -1 for blocked, 0 for undecided (or there isn't an attached agent to support it), 1 for 'allowed'

		boolean[] directions = new boolean[6];

		if(attachedAgents.get(0).size()!=0 || attachedAgents.get(1).size()!=0){
			directions[2] = true;
			directions[3] = true;
			directions[4] = true;
			directions[5] = true;
		}

		if(attachedAgents.get(2).size()!=0 || attachedAgents.get(3).size()!=0){
			directions[0] = true;
			directions[1] = true;
			directions[4] = true;
			directions[5] = true;
		}

		if(attachedAgents.get(4).size() != 0 || attachedAgents.get(5).size()!=0){
			directions[0] = true;
			directions[1] = true;
			directions[2] = true;
			directions[3] = true;
		}

		return directions;
	}

	public void validate() throws InvalidStateException {
		String message = "";

		for (CubeCouple cc : getAllCollisions()) {
			message += (cc + "\n");
		}

		if(!message.equals("")) throw new InvalidStateException("Collision(s) detected:\n" + message);
	}

	public Configuration copy(){
		ArrayList<Agent> newAgents = new ArrayList<>();

		for (Agent agent: this.agents) {
			newAgents.add(agent.copy());
		}

		return new Configuration(newAgents);
	}

	public Simulation getSimulation(){
		return simulation;
	}

	private int hash;

	@Override
	public int hashCode() {
		if (hash == 0) {
			int[] hashes = new int[agents.size()];
			for (int i=0; i<agents.size(); i++) {
				Agent a = agents.get(i);
				if (a != null) {
					Point3D loc = a.getLocation();
					hashes[i] = loc.hashCode();
				} else {
					hashes[i] = 0;
				}
			}
			hash = Arrays.hashCode(hashes);
		}
		return hash;
	}

	private float manhattan(Point3D a, Point3D b) {
		return (float) (Math.abs(a.getX()-b.getX()) + Math.abs(a.getY()-b.getY()) + Math.abs(a.getZ()-b.getZ()));
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Configuration)) return false;
		Configuration b = (Configuration) obj;
		if (b.agents.size() != this.agents.size()) return false;
		for (Agent agent : agents) {
//			if (((b.getAgent(agent.getIndex()) == null)) || 
//					((b.getAgent(agent.getIndex()) != null) && (agent == null))) return false;
//			if (((b.getAgent(agent.getIndex()) == null) && (agent == null))) continue;
			if (agent == null || ((b.getAgent(agent.getIndex()) == null))) continue;
			if (manhattan(b.getAgent(agent.getIndex()).getLocation(), agent.getLocation()) > 0.01) return false;
		}
		return true;
	}

	public void resolveFalling() {
		PriorityQueue<Agent> queue = new PriorityQueue<>(agents.size(), new RobotHeightComparator());
		for (Agent agent: agents) {
			queue.add(agent);
		}

		while(!queue.isEmpty()){
			Agent agent = queue.poll();
			resolveFalling(agent);
		}
	}

	public void resolveFalling(Agent agent){
		double maxDist = 1;
		Collision coll = castRayCubeFalling(this, new Ray(agent.getLocation(), Direction.DOWN), 0.1, maxDist, 0, agent);

		if(coll.location != agent.getLocation() && coll.location != null){
			agent.move(coll.location);
		}
	}

	public ArrayList<Vector3d> getPositions(){
		ArrayList<Vector3d> loc = new ArrayList<>();

		for(int i =0; i<agents.size();i++){
			loc.add(agents.get(i).getPosition());
		}

		return loc;
	}

}
