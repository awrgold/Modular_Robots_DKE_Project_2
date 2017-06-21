package all.continuous;

import javafx.geometry.Point3D;

import java.util.ArrayList;

import org.joml.Vector3d;

/**
 * Created by God on the 8th day, and it was good...
 */
public class Pheromones extends ModuleAlgorithm {

    private ArrayList<AgentCouple> agentCouples = new ArrayList<>();
    SuperAgent superAgent;
    private static boolean DEBUG = true; 
    private static int iterations = 0; 

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
                            if(DEBUG)
                            	System.out.println("agent couple created");
                }
            }
        }
    }

    public void moveCouple(AgentCouple couple){
        //if the agent is on its own path, move randomly
        if (couple.getPathNumber() == -1){
        	if(DEBUG)
        		System.out.println("random movement");
        	
            AgentAction action = randomMove(couple);
            //add position to the agent's paths
            action.agent.addPath(getDestination(action, sim.getCurrentConfiguration()));
            sim.applyPhysical(action);
        }

        //else, follow path
        else{
        	if(DEBUG)
        		System.out.println("follow path!!");
        	
            ArrayList<AgentAction> validMoves1 = sim.getCurrentConfiguration().getPhysicalActions(couple.getAgent1());
            ArrayList<AgentAction> validMoves2 = sim.getCurrentConfiguration().getPhysicalActions(couple.getAgent2());

            Point3D nextLoc = superAgent.getActiveTrail(couple.getPathNumber()).get(couple.getPositionInPath()+1);

            boolean found = false;
            for(int i=0; i<validMoves1.size(); i++){
                if(getDestination(validMoves1.get(i), sim.getCurrentConfiguration()).equals(nextLoc)){
                	AgentAction action = validMoves1.get(i);
                	couple.getAgent2().addPath(getDestination(validMoves1.get(i), sim.getCurrentConfiguration()));
                	sim.applyPhysical(action);
                    break;
                }
            }

            if(found==false){
                for(int i=0; i<validMoves2.size(); i++){
                    if(getDestination(validMoves2.get(i), sim.getCurrentConfiguration()).equals(nextLoc)){
                        AgentAction action = validMoves2.get(i);
                        couple.getAgent2().addPath(getDestination(validMoves2.get(i), sim.getCurrentConfiguration()));
                        sim.applyPhysical(action);
                        break;
                    }
                }
            }


            couple.setPositionInPath(couple.getPositionInPath()+1);
        }
    }

    public AgentAction randomMove(AgentCouple couple){

    	//randomly choose which agent to choose

        double rand = Math.random();
        Agent agent;
        int agentChoice =0;
        if(rand<0.5) {
            if(DEBUG)
                System.out.println("first agent chosen");

            agent=couple.getAgent1();
            agentChoice=1;
       }
        else {
            if(DEBUG)
                System.out.println("second agent chosen");
            agent=couple.getAgent2();
            agentChoice=2;
        }
        
        if(DEBUG)
        {
        	System.out.println("agent 1 pos : "+printVector(couple.getAgent1().getPosition()));
        	System.out.println("agent 2 pos : "+printVector(couple.getAgent2().getPosition()));
        }
        //get all valid moves for that agent
        ArrayList<AgentAction> validMoves = sim.getCurrentConfiguration().getPhysicalActions(agent);
        
        if(DEBUG)
            System.out.println("valid moves size : "+validMoves.size());
       
        int rand2 = (int)(Math.random()*validMoves.size());
        //if i can move the chosen agent
        if(validMoves.size() !=0) {
            AgentAction action = validMoves.get(rand2);
            return action;
        }
        //if I cannot move the chosen agent
        else{
            if(agentChoice==1){
                agent = couple.getAgent2();
                if(DEBUG)
                	System.out.println("from agent 1 to 2");}
            else{
            	agent = couple.getAgent1();
            	if(DEBUG)
            		System.out.println("from agent 2 to 1");
            }
                

            validMoves = sim.getCurrentConfiguration().getPhysicalActions(agent);
            
            if(DEBUG)
            	System.out.println("new agent pos : "+printVector(agent.getPosition()));
            if(DEBUG)
                System.out.println("valid moves size new agent: "+validMoves.size());

            rand2 = (int)(Math.random()*validMoves.size());
            AgentAction action = validMoves.get(rand2);
            return action;
        }
    }

    /*HELPER METHODS*/
    public Point3D getDestination(AgentAction action, Configuration config ){
    	
    	Configuration configCopy = config.copy();
    	configCopy.applyPhysical(action);
    	return action.agent.getLocation();
    }
    public String printVector(Vector3d vector){
    	return "x : "+vector.x+", y: "+vector.y+", z: "+vector.z;
    }


    @Override
    public void takeTurn(){
    	if(iterations>300)
           sim.finish();

        if(iterations==0){
            pheromoneAlgorithm();
        }
        
        if(DEBUG)
        	System.out.println("TURN : "+iterations);
        if(DEBUG){
        	for(int i=0; i<agentCouples.size(); i++){
        		System.out.println("turn start agent 1 : "+printVector(agentCouples.get(i).getAgent1().getPosition()));
        		System.out.println("turn start agent 2 : "+printVector(agentCouples.get(i).getAgent2().getPosition()));
        	}
        }
        	

        // if goal is reached
        superAgent.isGoalReached(agentCouples, sim.getGoalConfiguration().getLocations());

            // do (reconfiguration into goal space, make sure both agents sit in goal)

        superAgent.checkPheromoneTrail();
        

        superAgent.mergeTrails();

        for(int i=0; i<agentCouples.size();i++){
            moveCouple(agentCouples.get(i));
        }

        iterations++;
    }


}
