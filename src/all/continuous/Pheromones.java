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
    private static boolean DEBUG = false; 
    private static boolean DEBUG2=false;
    private static boolean DEBUG3=false;
    private static boolean DEBUG4=false;
    private static boolean DEBUG5 = false;
    private static boolean DEBUG6=false;
    
    private static int iterations = 0; 
    private int agentCoupleToMove =0;
    private static final double EPSILON = 0.001;

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
            		if(DEBUG)
            		{
            			System.out.println("agent "+i+" : "+printVector(agents.get(i).getPosition()));
            			System.out.println("agent "+j+" : "+printVector(agents.get(j).getPosition()));
            		}
            		//
            		if (Math.abs(agents.get(i).getCenter().getX() - agents.get(j).getCenter().getX()) == 1 &&
            				Math.abs(agents.get(i).getCenter().getY() - agents.get(j).getCenter().getY()) == 0 &&
            						Math.abs(agents.get(i).getCenter().getZ() - agents.get(j).getCenter().getZ()) == 0){
            			 AgentCouple couple = new AgentCouple(sim,i,j);
            			 agentCouples.add(couple);
            			 if(DEBUG)
            				 System.out.println("agent couple created : "+i+" "+j);
            		}
            		
            		else if(Math.abs(agents.get(i).getCenter().getX() - agents.get(j).getCenter().getX()) == 0 &&
            				Math.abs(agents.get(i).getCenter().getY() - agents.get(j).getCenter().getY()) == 1 &&
            						Math.abs(agents.get(i).getCenter().getZ() - agents.get(j).getCenter().getZ()) == 0){
            			AgentCouple couple = new AgentCouple(sim,i,j);
           			 agentCouples.add(couple);
           			 if(DEBUG)
           				 System.out.println("agent couple created : "+i+" "+j);
            		}
            		
            		else if(Math.abs(agents.get(i).getCenter().getX() - agents.get(j).getCenter().getX()) == 0 &&
            				Math.abs(agents.get(i).getCenter().getY() - agents.get(j).getCenter().getY()) == 0 &&
            						Math.abs(agents.get(i).getCenter().getZ() - agents.get(j).getCenter().getZ()) == 1){
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
    	
    	//print agents
    	if(DEBUG2){
	    	for(int i=0; i<sim.getCurrentConfiguration().getAgents().size();i++){
	    		System.out.println("agent : "+i+" "+printVector(sim.getCurrentConfiguration().getAgent(i).getPosition()));
	    	}
	    	//print agent couples
	    	for(int i=0; i<agentCouples.size(); i++){
	    		System.out.println("agent : "+agentCouples.get(i).getIndex1()+ " "+printVector(agentCouples.get(i).getAgent1(sim).getPosition()));
	    		System.out.println("agent : "+agentCouples.get(i).getIndex2()+ " "+printVector(agentCouples.get(i).getAgent2(sim).getPosition()));
	    	}
    	}
    	
    	AgentAction visualAction=null;
    	AgentAction smellAction = null;
    	//AgentSenses coupleSense = new AgentSenses(couple);
        //if the agent is on its own path, move randomly
    	if(couple.getPathNumber()==-1){
	    	visualAction = couple.getSense().visualSearch(sim);
	    	smellAction = couple.getSense().smell(superAgent, sim);
    	}

        //else, follow path
        if(couple.getPathNumber()!=-1){
        	if(DEBUG6)
        		System.out.println("follow path!!");
        	
            ArrayList<AgentAction> validMoves1 = sim.getCurrentConfiguration().getPhysicalActions(couple.getAgent1(sim));
            deleteNotCoupleMoves(couple, 1, validMoves1, sim);
            ArrayList<AgentAction> validMoves2 = sim.getCurrentConfiguration().getPhysicalActions(couple.getAgent2(sim));
            deleteNotCoupleMoves(couple,2,  validMoves2, sim);

            Point3D nextLoc = superAgent.getActiveTrail(couple.getPathNumber()).get(couple.getPositionInPath()+1);
            if(DEBUG4)
            	System.out.println("next pos :"+nextLoc);
            Vector3d nextVect = new Vector3d(nextLoc.getX(), nextLoc.getY(), nextLoc.getZ());

            boolean found = false;
            for(int i=0; i<validMoves1.size(); i++){
            	Point3D dest = getDestination(validMoves1.get(i), sim.getCurrentConfiguration());
                if(superAgent.isSameLocation(new Vector3d(dest.getX(), dest.getY(), dest.getZ()),nextVect)){
                	
                	AgentAction action = validMoves1.get(i);
                	couple.addCouplePath(getDestination(validMoves1.get(i), sim.getCurrentConfiguration()));
                	sim.applyPhysical(action);
                	found=true;
                    break;
                }
            }

            if(found==false){
                for(int i=0; i<validMoves2.size(); i++){
                	Point3D dest = getDestination(validMoves2.get(i), sim.getCurrentConfiguration());
                    if(superAgent.isSameLocation(new Vector3d(dest.getX(), dest.getY(), dest.getZ()),nextVect)){
                        AgentAction action = validMoves2.get(i);
                        couple.addCouplePath(getDestination(validMoves2.get(i), sim.getCurrentConfiguration()));
                        sim.applyPhysical(action);
                        found=true;
                        break;
                    }
                }
            }
            //if there is no possible move towards that spot, that means an agent is currently there
            if(found==false){
            	if(DEBUG6)
            		System.out.println("couldnt find a move to follow the path");
            		
            }

            //if we found a move
            else{
            	couple.setPositionInPath(couple.getPositionInPath()+1);
            }
        }
        
        else if(smellAction != null){
        	if(DEBUG6)
        		System.out.println("smell activated");
        	AgentAction action = smellAction;
        	couple.addCouplePath(getDestination(action, sim.getCurrentConfiguration()));
        	sim.applyPhysical(action);
        }
        
        else if(visualAction != null){
        	if(DEBUG6)
        		System.out.println("visual search activated");
    		
        	couple.addCouplePath(getDestination(visualAction, sim.getCurrentConfiguration()));
        	sim.applyPhysical(visualAction);
    		
    	}
        
        else{
        	if(DEBUG6)
        		System.out.println("random movement");
        	
            AgentAction action = randomMove(couple);
            //add position to the agent's paths
            if(action!=null)
            {
            	//Agent agent = sim.getCurrentConfiguration().getAgent(action.index);
            	couple.addCouplePath(getDestination(action, sim.getCurrentConfiguration()));
            	/*if(DEBUG)
            		System.out.println("agent velocity : "+agent.getVelocity());*/
            	sim.applyPhysical(action);
            }
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
       if(DEBUG4)
       {
	        for(int i=0; i<validMoves.size(); i++){
	        	System.out.println("move : "+getDestination(validMoves.get(i), sim.getCurrentConfiguration()));
	        }
       }
        deleteNotCoupleMoves(couple,agentChoice, validMoves, sim);
        applyMovesWeight(validMoves, couple);
        if(DEBUG4)
        {
        	System.out.println("moves after deletion and addition");
 	        for(int i=0; i<validMoves.size(); i++){
 	        	System.out.println("move : "+getDestination(validMoves.get(i), sim.getCurrentConfiguration()));
 	        }
        }
        
        if(DEBUG)
            System.out.println("valid moves size : "+validMoves.size());
       
        int rand2 = (int)(Math.random()*validMoves.size());
        //if i can move the chosen agent
        if(validMoves.size() !=0) {
            AgentAction action = validMoves.get(rand2);
            if(DEBUG6)
            	System.out.println("chosen action : "+getDestination(action, sim.getCurrentConfiguration()));
            return action;
        }
        //if I cannot move the chosen agent
        else{
            if(agentChoice==1){
                agent = couple.getAgent2(sim);
                agentChoice=2;
                if(DEBUG)
                	System.out.println("from agent 1 to 2");}
            else{
            	agent = couple.getAgent1(sim);
            	agentChoice=1;
            	if(DEBUG)
            		System.out.println("from agent 2 to 1");
            }
                

            validMoves = sim.getCurrentConfiguration().getPhysicalActions(agent);
            if(DEBUG4)
            {
     	        for(int i=0; i<validMoves.size(); i++){
     	        	System.out.println("move : "+getDestination(validMoves.get(i), sim.getCurrentConfiguration()));
     	        }
            }
            deleteNotCoupleMoves(couple, agentChoice,  validMoves, sim);
            applyMovesWeight(validMoves, couple);
            if(DEBUG4)
            {
            	System.out.println("moves after deletion and addition");
     	        for(int i=0; i<validMoves.size(); i++){
     	        	System.out.println("move : "+getDestination(validMoves.get(i), sim.getCurrentConfiguration()));
     	        }
            }
            
            if(DEBUG)
            	System.out.println("new agent pos : "+printVector(agent.getPosition()));
            if(DEBUG)
                System.out.println("valid moves size new agent: "+validMoves.size());

            if(validMoves.size()!=0){
            rand2 = (int)(Math.random()*validMoves.size());
            AgentAction action = validMoves.get(rand2);
            if(DEBUG6)
            	System.out.println("chosen action : "+getDestination(action, sim.getCurrentConfiguration()));
            return action;}
            else
            	return null;
        }
    }

    /*HELPER METHODS*/
    public Point3D getDestination(AgentAction action, Configuration config ){
    	
    	Configuration configCopy = config.copy();
    	configCopy.applyPhysical(action);
    	Simulation sim2 = new Simulation(sim.getTerrain(), configCopy, sim.getGoalConfiguration());
    	sim2.endTurn();
    	Agent agent = configCopy.getAgent(action.index);
    	Vector3d pos = agent.getPosition();
    	return new Point3D(pos.x, pos.y, pos.z);
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
    
    public void deleteNotCoupleMoves(AgentCouple ac, int agentIndex,ArrayList<AgentAction> actions, Simulation sim){
    	if(DEBUG)
    		System.out.println("check for bumping into other couple moves");
    	ArrayList<AgentAction> toDelete = new ArrayList<>();
    	
    	for(int i=0; i<actions.size(); i++){ 
    		if(DEBUG3)
    			System.out.println("action destination : "+getDestination(actions.get(i), sim.getCurrentConfiguration()));
    		if(interactsNotCoupleAgent(ac, agentIndex, getDestination(actions.get(i), sim.getCurrentConfiguration()), sim))
    		{
    			toDelete.add(actions.get(i));
    			if(DEBUG3)
    				System.out.println("remove action");
    		}
    	}
    	
    	actions.removeAll(toDelete);
    }
    
    public boolean interactsNotCoupleAgent(AgentCouple ac, int agentIndex,Point3D loc, Simulation sim){
    
    	int locX = Math.round((float)loc.getX());
    	int locY = Math.round((float)loc.getY());
    	int locZ = Math.round((float)loc.getZ());
    	if(DEBUG)
    		System.out.println("x :"+locX+ " y "+locY);
    	Agent notMovingAgent =null;
    	if(agentIndex==1)
    		notMovingAgent = ac.getAgent2(sim);
    	else
    		notMovingAgent = ac.getAgent1(sim);
    	
    	Point3D agentPos = notMovingAgent.getPointPosition();
    	int agentX = Math.round((float)agentPos.getX());
    	int agentY = Math.round((float)agentPos.getY());
    	int agentZ = Math.round((float)agentPos.getZ());
    	
    	
    	if(DEBUG2)
    	{
    		System.out.println("future loc : "+loc); 
    		System.out.println("not moving agent loc : "+agentPos);
    	}
    	//check if the result position disconnects it from its pair (loc is new location, agentPos is the module taht doesn't move)
    	if(Math.abs(locX-agentX) >1 || Math.abs(locY-agentY)>1 || Math.abs(locZ-agentZ)>1){
    		if(DEBUG3)
    			System.out.println("one of the differences is bigger than 1");
    		return true;
    	}
    	else if(Math.abs(locX-agentX) ==1 &&  
    			Math.abs(locY-agentY)==1)
    	{
    		if(DEBUG3)
    			System.out.println("x and y of 1 difference");
    		return true;
    	}
    	else if(Math.abs(locZ-agentZ) ==1  && Math.abs(locY-agentY)==1){
    		if(DEBUG3)
    			System.out.println("y and z of 1 difference");
    		return true;
    	}
    		
    	else if(Math.abs(locX-agentX) ==1  && Math.abs(locZ-agentZ)==1)
    	{
    		if(DEBUG3)
    			System.out.println("x and z of 1 difference");
    		return true;
    	}
    
    	return false;
    	
    }
    
    public void applyMovesWeight(ArrayList<AgentAction> validMoves, AgentCouple couple){
    	if(DEBUG4)
    		System.out.println("moves weight apply");
    	ArrayList<AgentAction> toAdd = new ArrayList<>();  
    	int nonVisitedWeight=0;
    	if(couple.getCouplePath().size()<5)
    	 nonVisitedWeight = 3;
    	else
    		nonVisitedWeight = (int) couple.getCouplePath().size()*1/3;

    	//go through valid moves
    	for(int i=0; i<validMoves.size(); i++){
    		
    		//if a move has never been explored by the agent, add it some more times to the list to tweek the randomness
    		if(!couple.hasVisited(getDestination(validMoves.get(i), sim.getCurrentConfiguration()))){
    			
    			if(DEBUG6){
    				System.out.println("non visited move : "+getDestination(validMoves.get(i), sim.getCurrentConfiguration()));
    				System.out.println("add "+nonVisitedWeight+ "times");}
    			for(int j=0; j<nonVisitedWeight; j++){
    				/*if(DEBUG4)
    					System.out.println("validMoves added");*/
    				toAdd.add(validMoves.get(i));
    			}
    		}
    		//else don't do anything
    	}
    	
    	//add all the validMoves
    	validMoves.addAll(toAdd);
    	//Valid Moves are now updated based on the weights
    }

    /*END OF HELPER METHODS*/

    @Override
    public void takeTurn(){
    	if(iterations>700)
           sim.finish();

        if(iterations==0){
            pheromoneAlgorithm();
        }
        
        
        //if(DEBUG6)
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

        if(!agentCouples.get(agentCoupleToMove).hasReachedGoal())
        	moveCouple(agentCouples.get(agentCoupleToMove));
        
        if(agentCoupleToMove != agentCouples.size()-1)
        	agentCoupleToMove++;
        else
        	agentCoupleToMove=0;
        /*for(int i=0; i<agentCouples.size();i++){
        	if(!agentCouples.get(i).hasReachedGoal())
        		moveCouple(agentCouples.get(i));
        }*/

        iterations++;
    }


}
