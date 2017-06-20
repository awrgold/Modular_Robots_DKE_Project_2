package all.continuous;

import javafx.geometry.Point3D;

import java.util.ArrayList;

/**
 * Created by God on the 8th day, and it was good...
 */
public class Pheromones extends ModuleAlgorithm {

    private ArrayList<AgentCouple> agentCouples = new ArrayList<>();
    SuperAgent superAgent;

    public Pheromones(Simulation sim)
    {
        super(sim);
    }

    public void pheromoneAlgorithm()
    {
        createAgentCouples();
        superAgent = new SuperAgent(agentCouples.size(), agentCouples);
    }

    public void createAgentCouples(){
        ArrayList<Agent> agents = sim.getCurrentConfiguration().getAgents();

        for (int i = 0; i < agents.size(); i++){
            for (int j = i; j < agents.size(); j++){
                if (Math.abs(agents.get(i).getCenter().getX() - agents.get(j).getCenter().getX()) == 1 ||
                        Math.abs(agents.get(i).getCenter().getY() - agents.get(j).getCenter().getY()) == 1 ||
                        Math.abs(agents.get(i).getCenter().getZ() - agents.get(j).getCenter().getZ()) == 1) {
                            AgentCouple couple = new AgentCouple(agents.get(i), agents.get(j));
                            agentCouples.add(couple);
                }
            }
        }
    }

    public void moveCouple(AgentCouple couple){
        //if the agent is on its own path, move randomly
        if (couple.getPathNumber() == -1){
            Action action = randomMove(couple);
            sim.apply(action);
        }

        //else, follow path
        else{
            ArrayList<Action> validMoves1 = sim.getCurrentConfiguration().getAllValidActions(couple.getAgent1());
            ArrayList<Action> validMoves2 = sim.getCurrentConfiguration().getAllValidActions(couple.getAgent2());

            Point3D nextLoc = superAgent.getActiveTrail(couple.getPathNumber()).get(couple.getPositionInPath()+1);

            boolean found = false;
            for(int i=0; i<validMoves1.size(); i++){
                if(validMoves1.get(i).getDestination().equals(nextLoc)){
                    Action action = validMoves1.get(i);
                    sim.apply(action);
                    break;
                }
            }

            if(found==false){
                for(int i=0; i<validMoves2.size(); i++){
                    if(validMoves2.get(i).getDestination().equals(nextLoc)){
                        Action action = validMoves2.get(i);
                        sim.apply(action);
                        break;
                    }
                }
            }


            couple.setPositionInPath(couple.getPositionInPath()+1);
        }
    }

    public Action randomMove(AgentCouple couple){

        //randomly choose which agent to choose
        double rand = Math.random();
        Agent agent;
        if(rand<0.5)
            agent=couple.getAgent1();
        else
            agent=couple.getAgent2();

        //get all valid moves for that agent
        ArrayList<Action> validMoves = sim.getCurrentConfiguration().getAllValidActions(agent);
        int rand2 = Math.round((float)Math.random()*validMoves.size());
        Action action = validMoves.get(rand2);

        return action;
    }



    @Override
    public void takeTurn(){
        // if goal is reached
        superAgent.isGoalReached(agentCouples, sim.getGoalConfiguration().getLocations());
            // do (reconfiguration into goal space, make sure both agents sit in goal)
        superAgent.checkPheromoneTrail();
        superAgent.mergeTrails();


        for(int i=0; i<agentCouples.size();i++){
            moveCouple(agentCouples.get(i));
        }


    }


}
