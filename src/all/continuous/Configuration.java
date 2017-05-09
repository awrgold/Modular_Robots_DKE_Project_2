package all.continuous;

import java.util.ArrayList;
import java.util.List;

import all.continuous.CollisionUtil.Collision;
import javafx.geometry.Point3D;

public class Configuration {
    Simulation simulation;
    ArrayList<Agent> agents;

    public Configuration(ArrayList<Agent> agents){
        this.agents = agents;

        for (int i = 0; i < agents.size(); i++) {
            agents.get(i).setIndex(i);
        }
    }

    public void apply(Action action){
    	Agent agent = agents.get(action.agentIndex);
    	agent.move(agent.location.add(action.movement));
    }

    public ArrayList<Action> getAllValidActions(){
        ArrayList<Action> actions = new ArrayList<>();

        for (Agent agent: this.getAgents()) {
            actions.addAll(getAllValidActions(agent));
        }

        return actions;
    }

    // TODO: Separate grounded method to simplify code
    public ArrayList<Action> getAllValidActions(Agent agent){
        ArrayList<Action> actions = new ArrayList<>();
        
        // Attempt movement in all directions
        for (Point3D dir : Direction.DIRECTIONS) {
        	// Determine the perpendicular directions (needed to determine groundedness)
        	Point3D[] perpDirs = Direction.getPerpDirs(dir);
        	
        	// Determine whether the agent is grounded for the current direction
        	List<Point3D> groundedDirs = new ArrayList<>();
        	for (Point3D perpDir : perpDirs) {
        		Collision c =  CollisionUtil.castRay(simulation, new Ray(PositionUtil.center(agent.location), perpDir), 0.01, 0.1+World.VOXEL_SIZE/2.0, 0.01+World.VOXEL_SIZE/2.0, agent);
        		if (c.type == CollisionType.AGENT) {
        			groundedDirs.add(perpDir);
        			break;
        		}
        	}
        	
        	// If the agent is grounded, attempt movement in the current direction
        	if (groundedDirs.size() > 0) {
        		// Determine the maximum new position
        		Collision max = CollisionUtil.castRayCube(simulation, new Ray(agent.location, dir), agent);
        		
        		if (max.location.distance(agent.location) < 0.01) continue; // If the agent can't move in this direction, try the next one
        		
        		// Determine whether the agent remains grounded after the movement
        		boolean remainsGrounded = false;
        		for (Point3D perpDir : groundedDirs) {
            		Collision c =  CollisionUtil.castRay(simulation, new Ray(PositionUtil.center(max.location), perpDir), 0.01, 0.1+World.VOXEL_SIZE/2.0, 0.05+World.VOXEL_SIZE/2.0, agent);
            		if (c.type == CollisionType.AGENT) {
            			remainsGrounded = true;
            		} else {
            			// Not grounded here, but it might be possible to move diagonally
            			determineDiagAction(actions, agent, max.location, perpDir);
            		}
            	}
        		
        		// If the agent remains grounded, it can move in the current direction
        		if (remainsGrounded) {
        			actions.add(new Action(agent.index, max.location.subtract(agent.location)));
        		}
        	}
        }

        return actions;
    }

	private void determineDiagAction(ArrayList<Action> actions, Agent agent, Point3D location, Point3D dir) {
		Collision max = CollisionUtil.castRayCube(simulation, new Ray(location, dir), agent);
		
		Point3D[] perpDirs = Direction.getPerpDirs(dir);
  		boolean grounded = false;
		for (Point3D perpDir : perpDirs) {
    		Collision c =  CollisionUtil.castRay(simulation, new Ray(PositionUtil.center(max.location), perpDir), 0.01, 0.1+World.VOXEL_SIZE/2.0, 0.05+World.VOXEL_SIZE/2.0, agent);
    		if (c.type == CollisionType.AGENT) {
    			grounded = true;
    			break;
    		}
    	}
		
		// If the agent remains grounded after this diagonal movement, add it to the actions list
		if (grounded) {
			actions.add(new Action(agent.index, max.location.subtract(agent.location)));
		}
	}

	public ArrayList<Action> getAllValidActions(int agentIndex){
        return getAllValidActions(getAgent(agentIndex));
    }

    public ArrayList<Agent> getAgents(){
        return agents;
    }

    public Agent getAgent(int i){
        return agents.get(i);
    }

    public ArrayList<CubeCouple> getAllCollisions(Agent agent){
        ArrayList<CubeCouple> collisions = new ArrayList<>();

        for (Obstacle obstacle:simulation.getTerrain().getObstacles()) {
            if(agent.isCollidingWith(obstacle)) collisions.add(new CubeCouple(agent,obstacle));
        }

        boolean found = false;

        for (Agent agent2: agents) {
            if(found){
                if(agent.isCollidingWith(agent2)) collisions.add(new CubeCouple(agent,agent2));
            }

            if(agent2 == agent) found = true;
        }

        return collisions;
    }

    public ArrayList<CubeCouple> getAllCollisions(){
        ArrayList<CubeCouple> collisions = new ArrayList<>();

        for (Agent agent:agents) {
            for (CubeCouple cc: getAllCollisions(agent)) {
                collisions.add(cc);
            }
        }

        return collisions;
    }

    public void setSimulation(Simulation sim){
        this.simulation = sim;
    }

    public boolean isValidConfiguration(){
        return (getAllCollisions().size()==0);
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
}
