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

        int size = agents.size();
        
        for (int i = 0; i < size; i++){
            for (int j = i+1; j < size; j++){
            	if(i!=j && !isInACouple(i) && !isInACouple(j))
                {
            		if (Math.abs(agents.get(i).getCenter().getX() - agents.get(j).getCenter().getX()) == 1 ||
                        Math.abs(agents.get(i).getCenter().getY() - agents.get(j).getCenter().getY()) == 1 ||
                        Math.abs(agents.get(i).getCenter().getZ() - agents.get(j).getCenter().getZ()) == 1) {
                            AgentCouple couple = new AgentCouple(sim,i,j);
                            agentCouples.add(couple);
                            if(DEBUG)
                            	System.out.println("agent couple created : "+i+" "+j);
            		}
                }
            }
        }
        
        if(DEBUG)
        	System.out.println("agent couples size : "+agentCouples.size());
    }

    public void moveCouple(AgentCouple couple){
        //if the agent is on its own path, move randomly
        if (couple.getPathNumber() == -1){
        	if(DEBUG)
        		System.out.println("random movement");
        	
            AgentAction action = randomMove(couple);
            //add position to the agent's paths
            if(action!=null)
            {action.agent.addPath(getDestination(action, sim.getCurrentConfiguration()));
            sim.applyPhysical(action);}
        }

        //else, follow path
        else{
        	if(DEBUG)
        		System.out.println("follow path!!");
        	
            ArrayList<AgentAction> validMoves1 = sim.getCurrentConfiguration().getPhysicalActions(couple.getAgent1(sim));
            ArrayList<AgentAction> validMoves2 = sim.getCurrentConfiguration().getPhysicalActions(couple.getAgent2(sim));

            Point3D nextLoc = superAgent.getActiveTrail(couple.getPathNumber()).get(couple.getPositionInPath()+1);
            Vector3d nextVect = new Vector3d(nextLoc.getX(), nextLoc.getY(), nextLoc.getZ());

            boolean found = false;
            for(int i=0; i<validMoves1.size(); i++){
            	Point3D dest = getDestination(validMoves1.get(i), sim.getCurrentConfiguration());
                if(superAgent.isSameLocation(new Vector3d(dest.getX(), dest.getY(), dest.getZ()),nextVect)){
                	AgentAction action = validMoves1.get(i);
                	couple.getAgent1(sim).addPath(getDestination(validMoves1.get(i), sim.getCurrentConfiguration()));
                	sim.applyPhysical(action);
                    break;
                }
            }

            if(found==false){
                for(int i=0; i<validMoves2.size(); i++){
                	Point3D dest = getDestination(validMoves2.get(i), sim.getCurrentConfiguration());
                    if(superAgent.isSameLocation(new Vector3d(dest.getX(), dest.getY(), dest.getZ()),nextVect)){
                        AgentAction action = validMoves2.get(i);
                        couple.getAgent2(sim).addPath(getDestination(validMoves2.get(i), sim.getCurrentConfiguration()));
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
        //Choose which agent is gonna move
        if(rand<0.5) {
            if(DEBUG)
                System.out.println("first agent chosen");

            agent=couple.getAgent1(sim);
            agentChoice=1;
       }
        else {
            if(DEBUG)
                System.out.println("second agent chosen");
            agent=couple.getAgent2(sim);
            agentChoice=2;
        }
        
        if(DEBUG)
        {
        	System.out.println("agent 1 pos : "+printVector(couple.getAgent1(sim).getPosition()));
        	System.out.println("agent 2 pos : "+printVector(couple.getAgent2(sim).getPosition()));
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
                agent = couple.getAgent2(sim);
                if(DEBUG)
                	System.out.println("from agent 1 to 2");}
            else{
            	agent = couple.getAgent1(sim);
            	if(DEBUG)
            		System.out.println("from agent 2 to 1");
            }
                

            validMoves = sim.getCurrentConfiguration().getPhysicalActions(agent);
            
            if(DEBUG)
            	System.out.println("new agent pos : "+printVector(agent.getPosition()));
            if(DEBUG)
                System.out.println("valid moves size new agent: "+validMoves.size());

            if(validMoves.size()!=0){
            rand2 = (int)(Math.random()*validMoves.size());
            AgentAction action = validMoves.get(rand2);
            return action;}
            else
            	return null;
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
    
    public boolean isInACouple(int index){
    	for(int i=0; i<agentCouples.size(); i++){
    		if(agentCouples.get(i).getIndex1()==index || agentCouples.get(i).getIndex2()==index)
    		{
    			if(DEBUG)
    				System.out.println(index+" is in a couple");
    			return true;
    		}
    	}
    	if(DEBUG)
    		System.out.println(index+" is not in a couple");
    	return false; 
    }
    
   /* public void deleteNotCoupleMoves(ArrayList<AgentAction> actions){
    	
    	for(int i=0; i<actions.size(); i++){
    		if()
    	}
    }*/

    /*END OF HELPER METHODS*/

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
        		System.out.println("turn start agent 1 : "+printVector(agentCouples.get(i).getAgent1(sim).getPosition()));
        		System.out.println("turn start agent 2 : "+printVector(agentCouples.get(i).getAgent2(sim).getPosition()));
        	}
        }
        	

        // if goal is reached
        superAgent.isGoalReached(agentCouples, sim);

            // do (reconfiguration into goal space, make sure both agents sit in goal)

        superAgent.checkPheromoneTrail(sim);
        

        superAgent.mergeTrails(sim);

        for(int i=0; i<agentCouples.size();i++){
        	if(!agentCouples.get(i).hasReachedGoal())
        		moveCouple(agentCouples.get(i));
        }

        iterations++;
    }


}
