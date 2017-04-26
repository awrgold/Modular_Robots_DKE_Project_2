package all.continuous;

import java.util.ArrayList;

import static all.continuous.Main.VALIDATE_EVERYTHING;

public class Simulation {
    private Terrain terrain;
    private Configuration start;
    private Configuration goal;

    private boolean complete = false;

    //private Algorithm algorithm;

    private ArrayList<Configuration> timeStep;

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

    /*

    TODO: decide what to do with this shit

    public ArrayList<Configuration> run(){
        while(!complete){
            simulate(algorithm.getNextAction());
        }
        return timeStep;
    }*/

    public Terrain getTerrain(){
        return terrain;
    }

    public Configuration getCurrentConfiguration(){
        return timeStep.get(timeStep.size()-1);
    }

    public void endTurn() throws InvalidStateException {
        if(VALIDATE_EVERYTHING) timeStep.get(timeStep.size()-1).validate();

        Configuration newTimeStep = timeStep.get(timeStep.size()-1).copy();
        newTimeStep.setSimulation(this);
        timeStep.add(newTimeStep);

        //TODO: make falling always get calculated first
    }
}
