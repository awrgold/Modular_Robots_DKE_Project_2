package all.continuous;

import java.util.ArrayList;

import org.joml.Vector3d;

import javafx.geometry.Point3D;
import physics.Body;
import physics.FloorGeometry;
import physics.Physics;
import physics.PhysicsSimulation;

import static all.continuous.Main.VALIDATE_EVERYTHING;

import java.util.ArrayList;

public class Simulation {
    private Terrain terrain;
    private Configuration start;
    private Configuration goal;

    private boolean complete = false;

    private ModuleAlgorithm algorithm;

    private ArrayList<Configuration> timeStep;
    
    PhysicsSimulation physSim;

    public ArrayList<Configuration> getTimeStep() {
		return timeStep;
	}

	public Simulation(Terrain terrain, Configuration start, Configuration goal){
        this.terrain = terrain;
        this.start = start;
        this.goal = goal;
        
        this.physSim = new PhysicsSimulation();
        
        this.physSim.addBody(new Body(0, new FloorGeometry(0)));
        for (Obstacle obs : terrain.getObstacles())  this.physSim.addBody(obs);
        for (Agent agent : start.getAgents()) this.physSim.addBody(agent);

        terrain.setSimulation(this);
        start.setSimulation(this);
        goal.setSimulation(this);

        timeStep = new ArrayList<>();
        timeStep.add(start);
    }

    public void apply(Configuration configuration, Action action){
        //applies a given action to a given configuration
        configuration.apply(action);
    }


    public void apply(Configuration configuration, ArrayList<Action> actions){
        //applies a set of actions to a given configuration in the given order (WITHOUT ENDING THE TURN!)
        for (Action action: actions) {
            apply(configuration,action);
        }
    }

    //TODO maybe: implement a method that applies the actions of more than one turn to a given configuration, and returns a set of the resulting configurations

    public void apply(Action action){
        //applies an action to the current simulation timestep
        getCurrentConfiguration().apply(action);
    }
    
	public void applyPhysical(AgentAction action) {
		getCurrentConfiguration().applyPhysical(action);
	}

    public void applyAll(ArrayList<Action> actions){
        //applies a set of actions to the current simulation timestep in the given order (WITHOUT ENDING THE TURN!)
        for (Action action: actions) {
            apply(action);
        }
    }

    //TODO maybe: implement a method that applies the actions of more than one turn to the current timestep (and progresses through timesteps)

    public ArrayList<Action> getAllValidActions(){
        return getCurrentConfiguration().getAllValidActions();
    }
    
    public ArrayList<AgentAction> getAllPhysicalActions(){
        return getCurrentConfiguration().getAllPhysicalActions();
    }

    public ArrayList<Configuration> run(){
    	if (this.algorithm == null)
    		throw new IllegalStateException("No algorithm has been set");
    	endTurn();
        while(!complete){
            algorithm.takeTurn();
            endTurn();
            if (hasGoalBeenReached()) finish();
        }
        return timeStep;
    }

    public boolean hasGoalBeenReached() {
		for (Agent a : goal.agents) {
			if (getCurrentConfiguration().agents.stream().noneMatch((a2) -> a.getLocation().distance(a2.getLocation()) < 0.1)){
			    return false;
            }
		}
		return true;
	}

	public Terrain getTerrain(){
        return terrain;
    }

    public Configuration getCurrentConfiguration(){
        return timeStep.get(timeStep.size()-1);
    }

    public Configuration getGoalConfiguration() {
    	return this.goal;
    }

    public void endTurn() {
        if(VALIDATE_EVERYTHING) timeStep.get(timeStep.size()-1).validate();
        
//        boolean wasGood = true;
//        for (int i = 0; i < getCurrentConfiguration().agents.size(); i++) {
//        	getCurrentConfiguration().agents.get(i).setIndex(i);
//        	if (Math.abs(getCurrentConfiguration().agents.get(i).getPosition().x - Math.round(getCurrentConfiguration().agents.get(i).getPosition().x)) > 0.1 || 
//					Math.abs(getCurrentConfiguration().agents.get(i).getPosition().z - Math.round(getCurrentConfiguration().agents.get(i).getPosition().z)) > 0.1 ) {
//				wasGood = false;
//			}
//		}
        
        for (int i=0; i<getCurrentConfiguration().agents.size(); i++) {
        	Agent a = getCurrentConfiguration().getAgent(i);
        	Vector3d vec = a.getPosition();
        	Point3D posNew = new Point3D(vec.x, vec.y, vec.z);	
        	Point3D posOld = a.getLocation();
//        	if (posOld.distance(posNew) > 0.1 && 
//        			Math.abs(posOld.distance(posNew) - (
//        					Math.abs(posNew.getX()-posOld.getX()) + 
//        					Math.abs(posNew.getY()-posOld.getY()) + 
//        					Math.abs(posNew.getZ()-posOld.getZ())))  < 0.1) {
//        				System.out.println("wefewfefw");
//        			}
        	if (Math.abs(a.getVelocity().x) > 1.7061 ||
        			Math.abs(a.getVelocity().z) > 1.7061)
        		System.out.println("wefewfefw");
        }
        
        physSim.tick(1.0);
        
        for (int i=0; i<getCurrentConfiguration().agents.size(); i++) {
        	Agent a = getCurrentConfiguration().getAgent(i);
        	Vector3d vec = a.getPosition();
        	Point3D posNew = new Point3D(vec.x, vec.y, vec.z);
        	Point3D posOld = a.getLocation();
//        	if (posOld.distance(posNew) > Math.sqrt(2))
//        		System.out.println("noo");
        }
        
//        if(wasGood)
//        {
//        for (int i = 0; i < getCurrentConfiguration().agents.size(); i++) {
//        	getCurrentConfiguration().agents.get(i).setIndex(i);
//			if (Math.abs(getCurrentConfiguration().agents.get(i).getPosition().x - Math.round(getCurrentConfiguration().agents.get(i).getPosition().x)) > 0.1 || 
//					Math.abs(getCurrentConfiguration().agents.get(i).getPosition().z - Math.round(getCurrentConfiguration().agents.get(i).getPosition().z)) > 0.1 ) {
//				System.out.println("ewfewfwef");
//			}
//		}
//        }
        
        for (Agent agent : timeStep.get(timeStep.size()-1).getAgents()) this.physSim.removeBody(agent);
        Configuration newTimeStep = timeStep.get(timeStep.size()-1).copy();
        newTimeStep.setSimulation(this);
        
        timeStep.add(newTimeStep);
        for (Agent agent : timeStep.get(timeStep.size()-1).getAgents()) {
        	Vector3d vel = agent.getVelocity();
        	agent.setVelocity(new Vector3d(0, vel.y, 0));
        	this.physSim.addBody(agent);
        }

        //newTimeStep.resolveFalling();
    }

	public void finish() {
		this.complete = true;
	}

	public void setAlgorithm(ModuleAlgorithm algorithm) {
		this.algorithm = algorithm;
	}
}
