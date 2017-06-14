package all.continuous;

import java.util.ArrayList;

import physics.Physics;

import static all.continuous.Main.VALIDATE_EVERYTHING;

import java.util.ArrayList;

public class Simulation {
    private Terrain terrain;
    private Configuration start;
    private Configuration goal;

    private boolean complete = false;

    private ModuleAlgorithm algorithm;

    private ArrayList<Configuration> timeStep;

    public ArrayList<Configuration> getTimeStep() {
		return timeStep;
	}

	public Simulation(Terrain terrain, Configuration start, Configuration goal){
        this.terrain = terrain;
        this.start = start;
        this.goal = goal;

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
			if (getCurrentConfiguration().agents.stream().noneMatch((a2) -> a.getLocation().equals(a2.getLocation()))) return false;
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

        Configuration newTimeStep = timeStep.get(timeStep.size()-1).copy();
        newTimeStep.setSimulation(this);
        timeStep.add(newTimeStep);

        newTimeStep.resolveFalling();
    }

	public void finish() {
		this.complete = true;
	}

	public void setAlgorithm(ModuleAlgorithm algorithm) {
		this.algorithm = algorithm;
	}
}
