package all.continuous;

import java.util.ArrayList;

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
        //TODO: implement
    }

    public ArrayList<Action> getAllValidActions(){
        ArrayList<Action> actions = new ArrayList<>();

        for (Agent agent: this.getAgents()) {
            for (Action action: getAllValidActions(agent)) {
                actions.add(action);
            }
        }

        return actions;
    }



    public ArrayList<Action> getAllValidActions(Agent agent){
        ArrayList<Action> actions = new ArrayList<>();

        //TODO: implement

        return actions;
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
