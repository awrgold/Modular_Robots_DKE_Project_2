package all.continuous;

import javafx.geometry.Point3D;
import java.util.ArrayList;
import static all.continuous.Direction.NONE;

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
        actions.add(new Action(agent.getIndex(), new Point3D(0,0,0), NONE));
        if(agent.hasMoved()) return actions;

        ArrayList<ArrayList<Agent>> attachedAgents = getAttachedAgentsInDirections(agent);

        boolean[] allowedDirections = getAllowedDirections(attachedAgents);

        double[] maxDistance = getMaxDistances(agent, allowedDirections, attachedAgents);

        // foreach direction: determineMaxDistance;

        // create actions out of directions and distances;

        //TODO: implement

        return actions;
    }

    public double[] getMaxDistances(Agent agent, boolean[] allowedDirections,ArrayList<ArrayList<Agent>> attachedAgents){
        double[] maxDistances = new double[6];

        for (int i = 0; i < 6; i++) {
            if(allowedDirections[i]){
                Point3D oldLoc = agent.getLocation();
                Point3D newLoc = oldLoc;

                Agent tester = new Agent(-1, agent.getLocation());

                if(i==0){ //LEFT
                    Point3D movement = new Point3D(-1,0,0);
                    ArrayList<CubeCouple> allCollisions = new ArrayList<>();

                    double modifier = -1;

                    do {
                        tester.setLocation(agent.getLocation().add(movement));

                        allCollisions = getAllCollisions(tester, false);

                        for (CubeCouple cc: allCollisions){
                            if(cc.getCube2().equals(agent)) allCollisions.remove(cc); //IMPORTANT!!!!!!!!!!
                            else if (modifier + Math.abs(agent.getXDist(cc.getCube2())) < 1) modifier += 1 - Math.abs(agent.getXDist(cc.getCube2()));
                        }

                        movement = new Point3D(modifier,0,0);
                        if(modifier >= 0) break;
                    } while (allCollisions.size()!=0);

                    maxDistances[i] = modifier;
                } else if (i==1){
                    Point3D movement = new Point3D(1,0,0);
                    ArrayList<CubeCouple> allCollisions = new ArrayList<>();

                    double modifier = 1;

                    do {
                        tester.setLocation(agent.getLocation().add(movement));

                        allCollisions = getAllCollisions(tester, false);

                        for (CubeCouple cc: allCollisions){
                            if(cc.getCube2().equals(agent)) allCollisions.remove(cc); //IMPORTANT!!!!!!!!!!
                            else if (modifier + Math.abs(agent.getXDist(cc.getCube2())) < 1) modifier -= 1 - Math.abs(agent.getXDist(cc.getCube2()));
                        }
                    } while (allCollisions.size()!=0);
                }

                switch(i) {
                    case 0: { //LEFT

                        break;
                    }
                    case 1: //RIGHT
                        newLoc = new Point3D(agent.getLocation().getX() + 1, agent.getLocation().getY(), agent.getLocation().getZ());
                        break;
                    case 2: //BACK
                        newLoc = new Point3D(agent.getLocation().getX(), agent.getLocation().getY() - 1, agent.getLocation().getZ());
                        break;
                    case 3: //FORWARD
                        newLoc = new Point3D(agent.getLocation().getX(), agent.getLocation().getY() + 1, agent.getLocation().getZ());
                        break;
                    case 4: //DOWN
                        newLoc = new Point3D(agent.getLocation().getX(), agent.getLocation().getY(), agent.getLocation().getZ() - 1);
                        break;
                    case 5: //UP
                        newLoc = new Point3D(agent.getLocation().getX(), agent.getLocation().getY(), agent.getLocation().getZ() + 1);
                        break;
                }


                /*
                while(getAllCollisions.size() != 0){

                }
                */

            }
            else maxDistances[i] = 0;
        }

        //TODO: add checks for not-collision problems (e.g hanging in the air)

        return maxDistances;
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

    public ArrayList<ArrayList<Agent>> getAttachedAgentsInDirections(Agent agent){
        ArrayList<ArrayList<Agent>> fin = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            fin.add(new ArrayList<>());
        }

        for (Agent agent2: agents){
            if(!agent.equals(agent2)){
                Direction dir = agent.isAttachedTo(agent2);
                if(dir!=null) {
                    switch (dir){
                        case LEFT: fin.get(0).add(agent2); break;
                        case RIGHT: fin.get(1).add(agent2); break;
                        case BACKWARD: fin.get(2).add(agent2); break;
                        case FORWARD: fin.get(3).add(agent2); break;
                        case DOWNWARD: fin.get(4).add(agent2); break;
                        case UPWARD: fin.get(5).add(agent2); break;
                    }
                }
            }
        }

        return fin;
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
}
