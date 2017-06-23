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
    	for(int i=0; i<sim.getCurrentConfiguration().getAgents().size();i++){
    		System.out.println("agent : "+i+" "+printVector(sim.getCurrentConfiguration().getAgent(i).getPosition()));
    	}
    	//print agent couples
    	for(int i=0; i<agentCouples.size(); i++){
    		System.out.println("agent : "+agentCouples.get(i).getIndex1()+ " "+printVector(agentCouples.get(i).getAgent1(sim).getPosition()));
    		System.out.println("agent : "+agentCouples.get(i).getIndex2()+ " "+printVector(agentCouples.get(i).getAgent2(sim).getPosition()));
    	}
        //if the agent is on its own path, move randomly
        if (couple.getPathNumber() == -1){
        	if(DEBUG)
        		System.out.println("random movement");
        	
            AgentAction action = randomMove(couple);
            //add position to the agent's paths
            if(action!=null)
            {
            	Agent agent = sim.getCurrentConfiguration().getAgent(action.index);
            	agent.addPath(getDestination(action, sim.getCurrentConfiguration()));
            	if(DEBUG)
            		System.out.println("agent velocity : "+agent.getVelocity());
            sim.applyPhysical(action);
            }
        }

        //else, follow path
        else{
        	if(DEBUG)
        		System.out.println("follow path!!");
        	
            ArrayList<AgentAction> validMoves1 = sim.getCurrentConfiguration().getPhysicalActions(couple.getAgent1(sim));
            deleteNotCoupleMoves(couple, 1, validMoves1, sim);
            ArrayList<AgentAction> validMoves2 = sim.getCurrentConfiguration().getPhysicalActions(couple.getAgent2(sim));
            deleteNotCoupleMoves(couple,2,  validMoves2, sim);

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
        for(int i=0; i<validMoves.size(); i++){
        	System.out.println("move : "+getDestination(validMoves.get(i), sim.getCurrentConfiguration()));
        }


        deleteNotCoupleMoves(couple,agentChoice, validMoves, sim);
        
        if(DEBUG)
            System.out.println("valid moves size : "+validMoves.size());
       
        int rand2 = (int)(Math.random()*validMoves.size());
        //if i can move the chosen agent
        if(validMoves.size() !=0) {
            AgentAction action = validMoves.get(rand2);
            if(DEBUG)
            	System.out.println("chosen action : "+getDestination(action, sim.getCurrentConfiguration()));
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
            deleteNotCoupleMoves(couple, agentChoice,  validMoves, sim);
            
            if(DEBUG)
            	System.out.println("new agent pos : "+printVector(agent.getPosition()));
            if(DEBUG)
                System.out.println("valid moves size new agent: "+validMoves.size());

            if(validMoves.size()!=0){
            rand2 = (int)(Math.random()*validMoves.size());
            AgentAction action = validMoves.get(rand2);
            if(DEBUG)
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
    		if(DEBUG)
    			System.out.println("action destination : "+getDestination(actions.get(i), sim.getCurrentConfiguration()));
    		if(isOnTopOfNotCoupleAgent(ac, agentIndex, getDestination(actions.get(i), sim.getCurrentConfiguration()), sim))
    		{
    			toDelete.add(actions.get(i));
    			if(DEBUG)
    				System.out.println("remove action");
    		}
    	}
    	
    	actions.removeAll(toDelete);
    }
    
    public boolean isOnTopOfNotCoupleAgent(AgentCouple ac, int agentIndex,Point3D loc, Simulation sim){
    	//it's not on top on anything
    	/*if(loc.getY()<0.9)
    	{
    		if(DEBUG)
    			System.out.println("not a top position");
    		return false; 
    	}
    	
    	else{
    		Vector3d vector1 = ac.getAgent1(sim).getPosition();
    		Point3D locAgent1 = new Point3D(vector1.x, vector1.y, vector1.z);
    		
    		Vector3d vector2 = ac.getAgent2(sim).getPosition();
    		Point3D locAgent2 = new Point3D(vector2.x, vector2.y, vector2.z);
    		
    		//if(Math.abs(loc.getX()-locAgent1.getX())>1 ||  )
    		
    		if((loc.getX() != locAgent1.getX() || loc.getZ() != locAgent1.getZ()) && (loc.getX() != locAgent2.getX() || loc.getZ()!=locAgent2.getZ()))
    		{
    			if(DEBUG)
    				System.out.println("MOVE TO BE DELETED");
    			return true;
    		}
    		
    		return false; 
    	}*/
    	
    	Agent notMovingAgent =null;
    	if(agentIndex==1)
    		notMovingAgent = ac.getAgent2(sim);
    	else
    		notMovingAgent = ac.getAgent1(sim);
    	
    	Point3D agentPos = notMovingAgent.getPointPosition();
    	
    	//check if the result position disconnects it from its pair (loc is new location, agentPos is the module taht doesn't move)
    	if(Math.abs(loc.getX()-agentPos.getX()) >1 || Math.abs(loc.getY()-agentPos.getY())>1 || Math.abs(loc.getZ()-agentPos.getZ())>1){
    		return true;
    	}
    	
    	//if not disconnect, if it's not somewhere high, it's not a problem
    	else if(loc.getY()<1)
			return false;
    	
    	//now check if it's on top of another agent
		for(int i=0; i<agentCouples.size(); i++){
			
			
			if(agentCouples.get(i).getIndex1()!=ac.getIndex1() && agentCouples.get(i).getIndex2()!=ac.getIndex2()){
				Point3D agent1Loc = agentCouples.get(i).getAgent1(sim).getPointPosition();
				Point3D agent2Loc = agentCouples.get(i).getAgent2(sim).getPointPosition();
				//if it's on top of the first agent
				if(Math.abs(agent1Loc.getX()-loc.getX())<EPSILON
						&& Math.abs(agent1Loc.getZ()-loc.getZ())<EPSILON
						&& Math.abs(agent1Loc.getY()-loc.getY())>0.9
						&& Math.abs(agent1Loc.getY()-loc.getY())<1.1){
				return true;	
				}
				if(Math.abs(agent2Loc.getX()-loc.getX())<EPSILON
						&& Math.abs(agent2Loc.getZ()-loc.getZ())<EPSILON
						&& Math.abs(agent2Loc.getY()-loc.getY())>0.9
						&& Math.abs(agent2Loc.getY()-loc.getY())<1.1){
				return true;	
				}
				
			}
		}
    	
    	
    	return false;
    	//if it's on top of smth, check if it's an agent that is not in the couple
    	/*for(int i=0; i<agentCouples.size(); i++){
    		if(DEBUG)
        		System.out.println("couple analysed : "+agentCouples.get(i).getIndex1()+" "+agentCouples.get(i).getIndex2());
    		
    		if(agentCouples.get(i).getIndex1()!=ac.getIndex1() && agentCouples.get(i).getIndex2()!=ac.getIndex2()){
	    		
    			
    			Vector3d vector1 = agentCouples.get(i).getAgent1(sim).getPosition();
	    		Point3D locAgent1 = new Point3D(vector1.x, vector1.y, vector1.z);
	    		if(DEBUG)
	    			System.out.println("agent 1 loc : "+locAgent1);
	    		if (Math.abs(loc.getX() - locAgent1.getX()) <EPSILON &&
	                    Math.abs(loc.getZ() - locAgent1.getZ()) <EPSILON &&
	                    Math.abs(loc.getY() - locAgent1.getY()) >0.99 &&
	                    Math.abs(loc.getY() - locAgent1.getY())<1.01){
	    			if(DEBUG)
	    				System.out.println("MOVE TO BE DELETED");
	    			return true; 
	    		}
	    		
	    		Vector3d vector2 = agentCouples.get(i).getAgent2(sim).getPosition();
	    		Point3D locAgent2 = new Point3D(vector2.x, vector2.y, vector2.z);
	    		if(DEBUG)
	    			System.out.println("agent 2 loc : "+locAgent2);
	    		if (Math.abs(loc.getX() - locAgent2.getX()) <EPSILON &&
	                    Math.abs(loc.getZ() - locAgent2.getZ()) <EPSILON &&
	                    Math.abs(loc.getY() - locAgent2.getY()) >0.99 &&
	                    Math.abs(loc.getY() - locAgent2.getY())<1.01){
	    			if(DEBUG)
	    				System.out.println("MOVE TO BE DELETED");
	    			return true; 
	    		}
	    	}
    	}*/
    }

    /*END OF HELPER METHODS*/

    @Override
    public void takeTurn(){
    	if(iterations>100)
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
