package all.continuous;

import javafx.geometry.Point3D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        }
    }

    public void apply(Action action){
    	Agent agent = agents.get(action.getAgent());
    	agent.move(action.getDestination());
    }
    
    public void applyPhysical(AgentAction action) {
    	action.apply();
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
    	ArrayList<AgentAction> actions = new ArrayList<>();
    	
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
             			determineDiagPhysicalAction(actions, agent, max.location, perpDir, dir);
             		}
             	}
         		
         		// If the agent remains grounded, it can move in the current direction
         		if (dir != Direction.UP && remainsGrounded) {
         			double impulseMag = calculateImpulseForDist(max.location.subtract(agent.getLocation()).magnitude());
         			actions.add(new ImpulseAction(agent, new Vector3d(dir.getX(), dir.getY(), dir.getZ()).mul(impulseMag)));
         		}
         	}
         }
         return actions;
    }
    
    private double calculateImpulseForDist(double dist) {
    	return -0.0024955 * dist + 0.0360 * dist*dist;
    }
    
    private void determineDiagPhysicalAction(ArrayList<AgentAction> actions, Agent agent, Point3D location, Point3D dir, Point3D primDir) {
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
			actions.add(new ClimbAction(agent, primDir, dir));
		}
	}
    
    public static void main(String[] args) {
    	ArrayList<Agent> agents = new ArrayList<>();
    	agents.add(new Agent(0, new Point3D(World.VOXEL_SIZE, 0, 0)));
    	//agents.add(new Agent(1, new Point3D(World.VOXEL_SIZE, World.VOXEL_SIZE, 0)));
    	agents.add(new Agent(2, new Point3D(World.VOXEL_SIZE, World.VOXEL_SIZE*2, 0)));
    	
    	Configuration conf =  new Configuration(agents);
    	
    	Simulation sim = new Simulation(new Terrain(new ArrayList<>()), conf, new Configuration(agents));
    	sim.setAlgorithm(new RandomAlgorithm(sim));
    	
//    	for (int i=0; i<1000; i++) {
//    		sim.physSim.tick(0.1);
//    		System.out.printf("%s\t%s\n", i*0.1, sim.getCurrentConfiguration().getAgent(2).getPosition().y);
//    	}
    	sim.endTurn();
    	
    	System.out.println(sim.getCurrentConfiguration().getAgents());
    	System.out.println(sim.getCurrentConfiguration().getPhysicalActions(sim.getCurrentConfiguration().getAgent(1)));
    	
		sim.endTurn();
		sim.endTurn();
		sim.endTurn();
    	
    	System.out.println(sim.getCurrentConfiguration().getAgents());
    	System.out.println(sim.getCurrentConfiguration().getPhysicalActions(sim.getCurrentConfiguration().getAgent(1)));
    	//System.out.println(conf.getAllValidActions(agents.get(2)));
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
	    		Point3D loc = a.getLocation();
	    		hashes[i] = loc.hashCode();
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
}
