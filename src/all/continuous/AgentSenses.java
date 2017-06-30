package all.continuous;

import javafx.geometry.Point3D;
import org.joml.Vector3d;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;

/**
 * Created by God on the 8th day, and it was good...
 */
public class AgentSenses {

    //private Simulation sim;
    private AgentCouple couple;
    //private Configuration conf;
    private Point3D agent1Loc;
    private Point3D agent2Loc;
    private static boolean DEBUG = false;
    private static boolean DEBUG2=false;
    private static boolean DEBUG3=false;

    public AgentSenses(AgentCouple couple){
        //this.sim = sim;
        //this.conf = sim.getCurrentConfiguration();
        this.couple = couple;
    }

    public void updateAgentsLoc(Simulation sim){
        Vector3d vector1 = couple.getAgent1(sim).getPosition();
        agent1Loc = new Point3D(vector1.x, vector1.y, vector1.z);
        Vector3d vector2 = couple.getAgent2(sim).getPosition();
        agent2Loc = new Point3D(vector2.x, vector2.y, vector2.z);
    }

    public AgentAction makeDecision(ArrayList<CollisionUtil.Collision> collisions, Simulation sim) {

        //TODO: *** ABSOLUTELY MUST FIGURE OUT WHEN TO TELL AN AGENT TO CHANGE DIRECTIONS WHEN APPROACHING AN OBSTACLE. IS IT AT DISTANCE 3 WHEN SMELL TAKES OVER?
        //TODO: *** MUST HAVE A METHOD TO OVERRIDE MOVEMENT WHEN A PHEROMONE TRAIL IS DETECTED NEARBY

        // Loop through collisions and organize decision based on type (priority queue of sorts)
        ArrayList<Point3D> seenObjects = new ArrayList<>();
        double distanceToClosestObject = Double.MAX_VALUE;
        Point3D closestObject = null;
        Agent movingAgent =null;
        int agentChoice =0;
        
        //80% of the time, return null
        //20% of the time, follow the collision
        //otherwise agents just gather together too much
        double rand = Math.random();
       if(rand<0.2)
        	return null;
       // else
        //{
	        if(DEBUG)
	        	System.out.println("collision size : "+collisions.size());
	        // add the locations of each collision it finds
	        for (int i = 0; i < collisions.size(); i++) {
	
	        	if(DEBUG)
	        		System.out.println(collisions.get(i).type);
	            if (collisions != null){
	                if (collisions.get(i).type == CollisionType.AGENT) {
	                    seenObjects.add(collisions.get(i).getLocation());
	                }
	               // || collisions.get(i).type == CollisionType.OBSTACLE
	            }
	            else{
	                System.out.println("No collisions detected");
	                /**
	                 * MOVE RANDOMLY???
	                 *
	                 */
	                return null;
	                //means that another type of movement will be chosen
	            }
	        }
	        
	        if(seenObjects.size()==0)
	        	return null;
	
	        if(DEBUG)
	        	System.out.println("seens objects : "+seenObjects.size());
	        // find the closest object.
	        // if multiple objects are both the same distance, pick one at random.
	        for (int i = 0; i < seenObjects.size(); i++) {
	            if (getManhattanDistanceBetween(agent1Loc, seenObjects.get(i)) < distanceToClosestObject) {
	            	if(DEBUG)
	            		System.out.println("agent 1 closer");
	                distanceToClosestObject = getManhattanDistanceBetween(agent1Loc, seenObjects.get(i));
	                closestObject = seenObjects.get(i);
	                movingAgent = couple.getAgent1(sim);
	                agentChoice = 1;
	
	            }
	            else if (getManhattanDistanceBetween(agent2Loc, seenObjects.get(i)) < distanceToClosestObject) {
	            	if(DEBUG)
	            		System.out.println("agent 1 closer");
	                distanceToClosestObject = getManhattanDistanceBetween(agent2Loc, seenObjects.get(i));
	                closestObject = seenObjects.get(i);
	                movingAgent = couple.getAgent2(sim);
	                agentChoice = 2;
	                
	
	            } else if (getManhattanDistanceBetween(agent1Loc, seenObjects.get(i)) == distanceToClosestObject) {
	                double coinToss = Math.random();
	                if (coinToss < .5) {
	                    distanceToClosestObject = getManhattanDistanceBetween(agent1Loc, seenObjects.get(i));
	                    closestObject = seenObjects.get(i);
	                    movingAgent = couple.getAgent1(sim);
	                    agentChoice = 1;
	                } else {
	                    i++;
	                }
	            } else if (getManhattanDistanceBetween(agent2Loc, seenObjects.get(i)) == distanceToClosestObject) {
	                double coinToss = Math.random();
	                if (coinToss < .5) {
	                    distanceToClosestObject = getManhattanDistanceBetween(agent2Loc, seenObjects.get(i));
	                    closestObject = seenObjects.get(i);
	                    movingAgent = couple.getAgent2(sim);
	                    agentChoice = 2;
	
	                } else {
	                    i++;
	                }
	            }
	        }
	
	        ArrayList<AgentAction> validMoves=null;
	        if(DEBUG)
	        	System.out.println("agent choice :"+agentChoice);
	        if(agentChoice==1)
	        	validMoves = sim.getCurrentConfiguration().getPhysicalActions(couple.getAgent1(sim));
	        else if(agentChoice==2)
	        	validMoves = sim.getCurrentConfiguration().getPhysicalActions(couple.getAgent2(sim));
	
	        Pheromones pher = new Pheromones(sim);
	        pher.deleteNotCoupleMoves(couple, agentChoice, validMoves, sim);
	        pher.applyMovesWeight(validMoves, couple);
	        AgentAction bestAction = null;
	        for (int i = 0; i < validMoves.size(); i++){
	            double bestDist=Double.MAX_VALUE;
	            if (getManhattanDistanceBetween(getDestination(validMoves.get(i), sim), closestObject) <= bestDist){
	                bestAction = validMoves.get(i);
	                bestDist = getManhattanDistanceBetween(getDestination(validMoves.get(i), sim), closestObject);
	            }
	        }


        // from here, determine which agent in AgentCouple is further from the closest object.
        // Choose the move that brings it closest in Manhattan distance to the closest object
        // Move it
        // Repeat until a new closest object is found.


        //sim.applyPhysical(agentAction action)
	        return bestAction;
    }

    //}


    public AgentAction visualSearch(Simulation sim){
    	updateAgentsLoc(sim);
    	Configuration conf = sim.getCurrentConfiguration();
    	double sightDist=10;
        // Cast Ray in Left/Right/Forward search field from front agent in "agents"

        // if Agent 1 is in front of Agent 2, cast rays from Agent 1 to L/R/F
        if (agent1Loc.getX() > agent2Loc.getX()){

        	
            // Store all collisions in an arraylist to loop through
            ArrayList<CollisionUtil.Collision> collisions = new ArrayList<>();
            

            Point3D forwardDirOne = new Point3D(agent1Loc.getX() + 1.0, agent1Loc.getY(), agent1Loc.getZ());
            Point3D forwardDirTwo = new Point3D(agent2Loc.getX() - 1.0, agent2Loc.getY(), agent2Loc.getZ());
            Ray forwardRayOne = new Ray(agent1Loc, Direction.FRONT);
            Ray forwardRayTwo = new Ray(agent2Loc, Direction.FRONT);
            CollisionUtil.Collision agent1ColForward = CollisionUtil.castRay(conf, forwardRayOne, 0.25, sightDist, 1, couple.getAgent1(sim));
            CollisionUtil.Collision agent2ColForward = CollisionUtil.castRay(conf, forwardRayTwo, 0.25, sightDist, 1, couple.getAgent1(sim));

            Point3D leftDirOne = new Point3D(agent1Loc.getX(), agent1Loc.getY(), agent1Loc.getZ() - 1.0);
            Point3D leftDirTwo = new Point3D(agent2Loc.getX(), agent2Loc.getY(), agent2Loc.getZ() - 1.0);
            Ray leftRayOne = new Ray(agent1Loc, Direction.LEFT);
            Ray leftRayTwo = new Ray(agent1Loc, Direction.LEFT);
            CollisionUtil.Collision agent1ColLeft = CollisionUtil.castRay(conf, leftRayOne, 0.25, sightDist, 1, couple.getAgent1(sim));
            CollisionUtil.Collision agent2ColLeft = CollisionUtil.castRay(conf, leftRayTwo, 0.25, sightDist, 1, couple.getAgent1(sim));

            Point3D rightDirOne = new Point3D(agent1Loc.getX(), agent1Loc.getY(), agent1Loc.getZ() + 1.0);
            Point3D rightDirTwo = new Point3D(agent2Loc.getX(), agent2Loc.getY(), agent2Loc.getZ() + 1.0);
            Ray rightRayOne = new Ray(agent1Loc, Direction.RIGHT);
            Ray rightRayTwo = new Ray(agent1Loc, Direction.RIGHT);
            CollisionUtil.Collision agent1ColRight = CollisionUtil.castRay(conf, rightRayOne, 0.25, sightDist, 1.0, couple.getAgent1(sim));
            CollisionUtil.Collision agent2ColRight = CollisionUtil.castRay(conf, rightRayTwo, 0.25, sightDist, 1.0, couple.getAgent1(sim));

            collisions.add(agent1ColForward);
            collisions.add(agent2ColForward);
            collisions.add(agent1ColLeft);
            collisions.add(agent2ColLeft);
            collisions.add(agent1ColRight);
            collisions.add(agent2ColRight);

            return makeDecision(collisions, sim);
        }

        // if Agent 2 is in front of Agent 1, cast rays from Agent 2 to L/R/F
        else if (agent2Loc.getX() > agent1Loc.getX()){

            ArrayList<CollisionUtil.Collision> collisions = new ArrayList<>();


            Point3D forwardDirOne = new Point3D(agent1Loc.getX() - 1.0, agent1Loc.getY(), agent1Loc.getZ());
            Point3D forwardDirTwo = new Point3D(agent2Loc.getX() + 1.0, agent2Loc.getY(), agent2Loc.getZ());
            Ray forwardRayOne = new Ray(agent1Loc, Direction.FRONT);
            Ray forwardRayTwo = new Ray(agent2Loc, Direction.FRONT);
            CollisionUtil.Collision agent1ColForward = CollisionUtil.castRay(conf, forwardRayOne, 0.25, sightDist, 1, couple.getAgent1(sim));
            CollisionUtil.Collision agent2ColForward = CollisionUtil.castRay(conf, forwardRayTwo, 0.25, sightDist, 1, couple.getAgent1(sim));

            Point3D leftDirOne = new Point3D(agent1Loc.getX(), agent1Loc.getY(), agent1Loc.getZ() - 1);
            Point3D leftDirTwo = new Point3D(agent2Loc.getX(), agent2Loc.getY(), agent2Loc.getZ() - 1);
            Ray leftRayOne = new Ray(agent1Loc, Direction.LEFT);
            Ray leftRayTwo = new Ray(agent1Loc, Direction.LEFT);
            CollisionUtil.Collision agent1ColLeft = CollisionUtil.castRay(conf, leftRayOne, 0.25, sightDist, 1, couple.getAgent1(sim));
            CollisionUtil.Collision agent2ColLeft = CollisionUtil.castRay(conf, leftRayTwo, 0.25, sightDist, 1, couple.getAgent1(sim));

            Point3D rightDirOne = new Point3D(agent1Loc.getX(), agent1Loc.getY(), agent1Loc.getZ() + 1);
            Point3D rightDirTwo = new Point3D(agent2Loc.getX(), agent2Loc.getY(), agent2Loc.getZ() + 1);
            Ray rightRayOne = new Ray(agent1Loc, Direction.RIGHT);
            Ray rightRayTwo = new Ray(agent1Loc, Direction.RIGHT);
            CollisionUtil.Collision agent1ColRight = CollisionUtil.castRay(conf, rightRayOne, 0.25, sightDist, 1, couple.getAgent1(sim));
            CollisionUtil.Collision agent2ColRight = CollisionUtil.castRay(conf, rightRayTwo, 0.25, sightDist, 1, couple.getAgent1(sim));


            collisions.add(agent1ColForward);
            collisions.add(agent2ColForward);
            collisions.add(agent1ColLeft);
            collisions.add(agent2ColLeft);
            collisions.add(agent1ColRight);
            collisions.add(agent2ColRight);

            return makeDecision(collisions, sim);

        }

        // if Agent 1 is to the right of Agent 2, cast rays from Agent 1 to R/F/B
        else if (agent1Loc.getZ() > agent2Loc.getZ()){

            ArrayList<CollisionUtil.Collision> collisions = new ArrayList<>();


            Point3D forwardDirOne = new Point3D(agent1Loc.getX(), agent1Loc.getY(), agent1Loc.getZ() + 1);
            Point3D forwardDirTwo = new Point3D(agent2Loc.getX(), agent2Loc.getY(), agent2Loc.getZ() - 1);
            Ray forwardRayOne = new Ray(agent1Loc, Direction.FRONT);
            Ray forwardRayTwo = new Ray(agent2Loc, Direction.FRONT);
            CollisionUtil.Collision agent1ColForward = CollisionUtil.castRay(conf, forwardRayOne, 0.25, sightDist, 1, couple.getAgent1(sim));
            CollisionUtil.Collision agent2ColForward = CollisionUtil.castRay(conf, forwardRayTwo, 0.25, sightDist, 1, couple.getAgent1(sim));

            Point3D leftDirOne = new Point3D(agent1Loc.getX() + 1, agent1Loc.getY(), agent1Loc.getZ());
            Point3D leftDirTwo = new Point3D(agent2Loc.getX() + 1, agent2Loc.getY(), agent2Loc.getZ());
            Ray leftRayOne = new Ray(agent1Loc, Direction.LEFT);
            Ray leftRayTwo = new Ray(agent1Loc, Direction.LEFT);
            CollisionUtil.Collision agent1ColLeft = CollisionUtil.castRay(conf, leftRayOne, 0.25, sightDist, 1, couple.getAgent1(sim));
            CollisionUtil.Collision agent2ColLeft = CollisionUtil.castRay(conf, leftRayTwo, 0.25, sightDist, 1, couple.getAgent1(sim));

            Point3D rightDirOne = new Point3D(agent1Loc.getX() - 1, agent1Loc.getY(), agent1Loc.getZ());
            Point3D rightDirTwo = new Point3D(agent2Loc.getX() - 1, agent2Loc.getY(), agent2Loc.getZ());
            Ray rightRayOne = new Ray(agent1Loc, Direction.RIGHT);
            Ray rightRayTwo = new Ray(agent1Loc, Direction.RIGHT);
            CollisionUtil.Collision agent1ColRight = CollisionUtil.castRay(conf, rightRayOne, 0.25, sightDist, 1, couple.getAgent1(sim));
            CollisionUtil.Collision agent2ColRight = CollisionUtil.castRay(conf, rightRayTwo, 0.25, sightDist, 1, couple.getAgent1(sim));


            collisions.add(agent1ColForward);
            collisions.add(agent2ColForward);
            collisions.add(agent1ColLeft);
            collisions.add(agent2ColLeft);
            collisions.add(agent1ColRight);
            collisions.add(agent2ColRight);

            return makeDecision(collisions,sim);


        }

        // if Agent 1 is to the left of Agent 2, cast rays from Agent 1 to R/F/B
        else if (agent2Loc.getZ() > agent1Loc.getZ()){

            ArrayList<CollisionUtil.Collision> collisions = new ArrayList<>();


            Point3D forwardDirOne = new Point3D(agent1Loc.getX(), agent1Loc.getY(), agent1Loc.getZ() - 1);
            Point3D forwardDirTwo = new Point3D(agent2Loc.getX(), agent2Loc.getY(), agent2Loc.getZ() + 1);
            Ray forwardRayOne = new Ray(agent1Loc, Direction.FRONT);
            Ray forwardRayTwo = new Ray(agent2Loc, Direction.FRONT);
            CollisionUtil.Collision agent1ColForward = CollisionUtil.castRay(conf, forwardRayOne, 0.25, sightDist, 1, couple.getAgent1(sim));
            CollisionUtil.Collision agent2ColForward = CollisionUtil.castRay(conf, forwardRayTwo, 0.25,sightDist, 1, couple.getAgent1(sim));

            Point3D leftDirOne = new Point3D(agent1Loc.getX() + 1, agent1Loc.getY(), agent1Loc.getZ());
            Point3D leftDirTwo = new Point3D(agent2Loc.getX() + 1, agent2Loc.getY(), agent2Loc.getZ());
            Ray leftRayOne = new Ray(agent1Loc, Direction.LEFT);
            Ray leftRayTwo = new Ray(agent1Loc, Direction.LEFT);
            CollisionUtil.Collision agent1ColLeft = CollisionUtil.castRay(conf, leftRayOne, 0.25, sightDist, 1, couple.getAgent1(sim));
            CollisionUtil.Collision agent2ColLeft = CollisionUtil.castRay(conf, leftRayTwo, 0.25, sightDist, 1, couple.getAgent1(sim));

            Point3D rightDirOne = new Point3D(agent1Loc.getX() - 1, agent1Loc.getY(), agent1Loc.getZ());
            Point3D rightDirTwo = new Point3D(agent2Loc.getX() - 1, agent2Loc.getY(), agent2Loc.getZ());
            Ray rightRayOne = new Ray(agent1Loc, Direction.RIGHT);
            Ray rightRayTwo = new Ray(agent1Loc, Direction.RIGHT);
            CollisionUtil.Collision agent1ColRight = CollisionUtil.castRay(conf, rightRayOne, 0.25,sightDist, 1, couple.getAgent1(sim));
            CollisionUtil.Collision agent2ColRight = CollisionUtil.castRay(conf, rightRayTwo, 0.25, sightDist, 1, couple.getAgent1(sim));


            collisions.add(agent1ColForward);
            collisions.add(agent2ColForward);
            collisions.add(agent1ColLeft);
            collisions.add(agent2ColLeft);
            collisions.add(agent1ColRight);
            collisions.add(agent2ColRight);

            return makeDecision(collisions,sim);


        }
        else{
            if (agent1Loc.getY() < agent2Loc.getY()){

                ArrayList<CollisionUtil.Collision> collisions = new ArrayList<>();

                Point3D forwardDir = new Point3D(agent1Loc.getX() + 1, agent1Loc.getY(), agent1Loc.getZ());
                Ray forwardRay = new Ray(agent1Loc, Direction.FRONT);
                CollisionUtil.Collision colForward = CollisionUtil.castRay(conf, forwardRay, 0.25, sightDist, 1, couple.getAgent1(sim));

                Point3D leftDir = new Point3D(agent1Loc.getX(), agent1Loc.getY(), agent1Loc.getZ() - 1);
                Ray leftRay = new Ray(agent1Loc, Direction.LEFT);
                CollisionUtil.Collision colLeft = CollisionUtil.castRay(conf, leftRay, 0.25, sightDist, 1, couple.getAgent1(sim));

                Point3D rightDir = new Point3D(agent1Loc.getX(), agent1Loc.getY(), agent1Loc.getZ() + 1);
                Ray rightRay = new Ray(agent1Loc, Direction.RIGHT);
                CollisionUtil.Collision colRight = CollisionUtil.castRay(conf, rightRay, 0.25, sightDist, 1, couple.getAgent1(sim));

                Point3D backDir = new Point3D(agent1Loc.getX() - 1, agent1Loc.getY(), agent1Loc.getZ());
                Ray backRay = new Ray(agent1Loc, Direction.BACK);
                CollisionUtil.Collision colBack = CollisionUtil.castRay(conf, backRay, 0.25, sightDist, 1, couple.getAgent1(sim));


                collisions.add(colForward);
                collisions.add(colLeft);
                collisions.add(colRight);
                collisions.add(colBack);

                return makeDecision(collisions,sim);


            }else{

                ArrayList<CollisionUtil.Collision> collisions = new ArrayList<>();

                Point3D forwardDir = new Point3D(agent2Loc.getX() + 1, agent2Loc.getY(), agent2Loc.getZ());
                Ray forwardRay = new Ray(agent2Loc, Direction.FRONT);
                CollisionUtil.Collision colForward = CollisionUtil.castRay(conf, forwardRay, 0.25, sightDist, 1, couple.getAgent1(sim));

                Point3D leftDir = new Point3D(agent2Loc.getX(), agent2Loc.getY(), agent2Loc.getZ() - 1);
                Ray leftRay = new Ray(agent2Loc, leftDir);
                CollisionUtil.Collision colLeft = CollisionUtil.castRay(conf, leftRay, 0.25, sightDist, 1, couple.getAgent1(sim));

                Point3D rightDir = new Point3D(agent2Loc.getX(), agent2Loc.getY(), agent2Loc.getZ() + 1);
                Ray rightRay = new Ray(agent2Loc, rightDir);
                CollisionUtil.Collision colRight = CollisionUtil.castRay(conf, rightRay, 0.25, sightDist, 1, couple.getAgent1(sim));

                Point3D backDir = new Point3D(agent2Loc.getX() - 1, agent2Loc.getY(), agent2Loc.getZ());
                Ray backRay = new Ray(agent2Loc, backDir);
                CollisionUtil.Collision colBack = CollisionUtil.castRay(conf, backRay, 0.25, sightDist, 1, couple.getAgent1(sim));


                collisions.add(colForward);
                collisions.add(colLeft);
                collisions.add(colRight);
                collisions.add(colBack);

                return makeDecision(collisions,sim);

            }
        }
    }
    
    public AgentAction smell(SuperAgent superAgent, Simulation sim){
    	updateAgentsLoc(sim);
    	//loop through positions around the couple (any of the 2 agents)
    	ArrayList<Point3D> neighbours = getNeighboursToAnalyse(agent1Loc);
    	//check if any of those positions is part of an active trails
    	AgentAction bestAction=null;
    	if(DEBUG2)
			System.out.println(neighbours.size());
    	
    	//PRIORITY CHECK
    	//1) GOAL CHECK
    	//2) ACTIVE TRAIL CHECK
    	//3) AGENT CHECK
    	//4) INACTIVE TRAIL CHECK
    	
    	//go through neighbours to find if one of them is a goal
    	for(int i=0; i<neighbours.size(); i++){
    		
    		if(isAGoal(neighbours.get(i), sim)){
    			if(DEBUG3)
    				System.out.println("neighbour is a goal");
    			bestAction = findClosestAction(neighbours.get(i), sim);
    			if(DEBUG2)
    			{
    				if(bestAction==null)
    					System.out.println("best action is null");
    				else
    					System.out.println("agent to move : "+bestAction.index);
    			}
    			return bestAction;
    		}
    	}
    	
    	//go through neighbours to find if on eof them is an active trail
    	for(int i=0; i<neighbours.size(); i++){
    		
    		if(isInActiveTrail(superAgent, neighbours.get(i))){
    			if(DEBUG3)
    				System.out.println("neighbour is on an active trail");
    			bestAction = findClosestAction(neighbours.get(i), sim);
    			return bestAction;
    		}
    	}
    	
    	//go through neighbours to find if one of them is an agent
    	for(int i=0; i<neighbours.size(); i++){
    		if(isAnAgent(neighbours.get(i), sim)){
    			if(DEBUG3)
    				System.out.println("neighbour is an agent");
    			//go opposite direction
    			bestAction=findNotCloserAction(neighbours.get(i), sim);
    			return bestAction;
    		}
    	}
    	
    	for(int i=0; i<neighbours.size(); i++){
    		if(isOnInactiveTrail(superAgent, neighbours.get(i))){
    			if(DEBUG3)
    				System.out.println("neighbour is on an inactive trail");
    			//go opposite direction
    			bestAction=findNotCloserAction(neighbours.get(i), sim);
    			return bestAction;
    		}
    	}
    	
    	
    	
    	return null;
    	//if yes, move toward that point
    	//else return null
    }
    
    public AgentAction findClosestAction(Point3D goal, Simulation sim){
    	double minDistance = Integer.MAX_VALUE;
    	int agentChoice = 0;
    	
        ArrayList<AgentAction>validMoves1 = sim.getCurrentConfiguration().getPhysicalActions(couple.getAgent1(sim));
        ArrayList<AgentAction> validMoves2 = sim.getCurrentConfiguration().getPhysicalActions(couple.getAgent2(sim));

        if(DEBUG2){
        	System.out.println("valid moves 1 size : "+validMoves1.size());
        	System.out.println("valid moves 2 size : "+validMoves2.size());
        }
        Pheromones pher = new Pheromones(sim);
        pher.deleteNotCoupleMoves(couple, 1, validMoves1, sim);
        pher.deleteNotCoupleMoves(couple, 2, validMoves2, sim);
        pher.applyMovesWeight(validMoves1, couple);
        pher.applyMovesWeight(validMoves2, couple);
        if(DEBUG2){
        	System.out.println("valid moves 1 size : "+validMoves1.size());
        	System.out.println("valid moves 2 size : "+validMoves2.size());
        }
        AgentAction bestAction = null;
        double bestDist=Double.MAX_VALUE;
        for (int i = 0; i < validMoves1.size(); i++){
            //double bestDist=Double.MAX_VALUE;
            if (getManhattanDistanceBetween(getDestination(validMoves1.get(i), sim), goal) < bestDist){
                bestAction = validMoves1.get(i);
                bestDist = getManhattanDistanceBetween(getDestination(validMoves1.get(i), sim), goal);
            }
            else if(getManhattanDistanceBetween(getDestination(validMoves1.get(i), sim), goal)==bestDist){
            	double coin = Math.random();
            	if(coin<0.5){
            		bestAction = validMoves1.get(i);
            		bestDist=getManhattanDistanceBetween(getDestination(validMoves1.get(i), sim), goal);
            	}
            		
            }
        }
        
        for (int i = 0; i < validMoves2.size(); i++){
            // bestDist=Double.MAX_VALUE;
            if (getManhattanDistanceBetween(getDestination(validMoves2.get(i), sim), goal) < bestDist){
                bestAction = validMoves2.get(i);
                bestDist = getManhattanDistanceBetween(getDestination(validMoves2.get(i), sim), goal);
            }
            
            else if(getManhattanDistanceBetween(getDestination(validMoves2.get(i), sim), goal)==bestDist){
            	double coin = Math.random();
            	if(coin<0.5){
            		bestAction = validMoves2.get(i);
            		bestDist=getManhattanDistanceBetween(getDestination(validMoves2.get(i), sim), goal);
            	}
            		
            }
        }
        
        return bestAction;
    }
    
    public AgentAction findNotCloserAction(Point3D goal, Simulation sim){
    	double maxDistance = 0;
    	int agentChoice = 0;
    	
        ArrayList<AgentAction>validMoves1 = sim.getCurrentConfiguration().getPhysicalActions(couple.getAgent1(sim));
        ArrayList<AgentAction> validMoves2 = sim.getCurrentConfiguration().getPhysicalActions(couple.getAgent2(sim));

        if(DEBUG2){
        	System.out.println("valid moves 1 size : "+validMoves1.size());
        	System.out.println("valid moves 2 size : "+validMoves2.size());
        }
       
        AgentAction bestAction = null;
        double bestDist=0;
        for (int i = 0; i < validMoves1.size(); i++){
            //double bestDist=Double.MAX_VALUE;
            if (getManhattanDistanceBetween(getDestination(validMoves1.get(i), sim), goal) > bestDist){
                bestAction = validMoves1.get(i);
                bestDist = getManhattanDistanceBetween(getDestination(validMoves1.get(i), sim), goal);
            }
            else if(getManhattanDistanceBetween(getDestination(validMoves1.get(i), sim), goal)==bestDist){
            	double coin = Math.random();
            	if(coin<0.5){
            		bestAction = validMoves1.get(i);
            		bestDist=getManhattanDistanceBetween(getDestination(validMoves1.get(i), sim), goal);
            	}
            		
            }
        }
        
        
        for (int i = 0; i < validMoves2.size(); i++){
            // bestDist=Double.MAX_VALUE;
            if (getManhattanDistanceBetween(getDestination(validMoves2.get(i), sim), goal) > bestDist){
                bestAction = validMoves2.get(i);
                bestDist = getManhattanDistanceBetween(getDestination(validMoves2.get(i), sim), goal);
            }
            
            else if(getManhattanDistanceBetween(getDestination(validMoves2.get(i), sim), goal)==bestDist){
            	double coin = Math.random();
            	if(coin<0.5){
            		bestAction = validMoves2.get(i);
            		bestDist=getManhattanDistanceBetween(getDestination(validMoves2.get(i), sim), goal);
            	}
            		
            }
        }
        
        if(bestAction.index==1)
        	validMoves1.remove(bestAction);
        else
        validMoves2.remove(bestAction);
        
        Pheromones pher = new Pheromones(sim);
        pher.deleteNotCoupleMoves(couple, 1, validMoves1, sim);
        pher.deleteNotCoupleMoves(couple, 2, validMoves2, sim);
        pher.applyMovesWeight(validMoves1, couple);
        pher.applyMovesWeight(validMoves2, couple);
        if(DEBUG2){
        	System.out.println("valid moves 1 size : "+validMoves1.size());
        	System.out.println("valid moves 2 size : "+validMoves2.size());
        }
        double rand1 = Math.random();
        ArrayList<AgentAction> validMoves = null;
        if(validMoves1.size()==0)
        	validMoves=validMoves2;
        else if(validMoves2.size()==0)
        	validMoves=validMoves1; 
        else
        {
		    if(rand1<0.5)
		    	validMoves = validMoves1; 
		    else
		    	validMoves = validMoves2; 
        }
		    
		int rand2 = (int)(Math.random()*validMoves.size());
        	
        if(validMoves.size()!=0)
        	return validMoves.get(rand2);
        else
        	return null;
        
    }
    
    public boolean isAGoal(Point3D loc, Simulation sim){
    	ArrayList<Agent> goals = sim.getGoalConfiguration().getAgents();
    	
    	for(int i=0; i<goals.size(); i++){
    		if(goals.get(i).getLocation().equals(loc)){
    			return true;
    		}
    	}
    	return false;
    }
    
    public boolean isAnAgent(Point3D loc, Simulation sim){
    	ArrayList<Agent> agents = sim.getCurrentConfiguration().getAgents();
    	for(int i=0; i<agents.size(); i++){
    		if(agents.get(i).getPointPosition().equals(loc))
    			return true;
    	}
    	return false; 
    }
    
    
    
    /*HELPer methods*/
    
    public ArrayList<Point3D> getNeighboursToAnalyse(Point3D loc){
    	//assume y==0
    	ArrayList<Point3D> neighbours = new ArrayList<>();
    	//x's
    	for(int i=-3; i<3; i++){
    		//z's
    		for(int j=-3; j<3; j++){
    			if(i!=0 && j!=0)
    				neighbours.add(new Point3D(loc.getX()+i, 0, loc.getZ()+j));
    			
    		}
    	}
    	return neighbours;
    }
    
    public boolean isOnInactiveTrail(SuperAgent superAgent,Point3D loc){
    	ArrayList<AgentCouple> agents = superAgent.getAgentCouples();
    	
    	for(int i=0; i<agents.size(); i++){
    		if(agents.get(i).getIndex1()!=couple.getIndex1() && agents.get(i).getIndex2()!=couple.getIndex2())
    		{	
    			for(int j=0; j<agents.get(i).getCouplePath().size(); j++){
	    			if(agents.get(i).getCouplePath().get(j).equals(loc))
	    				return true;
	    		}
    		}
    	}
    	return false;
    }
    
    public boolean isInActiveTrail(SuperAgent superAgent, Point3D loc){
    	for(int i=0; i<superAgent.getActiveTrails().length; i++){
    		if(superAgent.getActiveTrails()[i]!=null){
	    		for(int j=0; j<superAgent.getActiveTrails()[i].size(); j++){
	    			if(loc.equals(superAgent.getActiveTrails()[i].get(j))){
	    				return true;
	    			}
	    		}
    		}
    		else
    			return false;
    	}
    	return false; 
    }
    
   public Point3D getDestination(AgentAction action, Simulation sim ){
    	Configuration config = sim.getCurrentConfiguration();
    	Configuration configCopy = config.copy();
    	configCopy.applyPhysical(action);
    	Simulation sim2 = new Simulation(sim.getTerrain(), configCopy, sim.getGoalConfiguration());
    	sim2.endTurn();
    	Agent agent = configCopy.getAgent(action.index);
    	Vector3d pos = agent.getPosition();
    	return new Point3D(pos.x, pos.y, pos.z);
    }
   
    public double getManhattanDistanceBetween(Point3D start, Point3D goal){
        double distance = 0;

        double xDiff = Math.abs(goal.getX()-start.getX());
        double yDiff = Math.abs(goal.getY()-start.getY());
        double zDiff = Math.abs(goal.getZ()-start.getZ());

        distance += (xDiff + yDiff + zDiff);

        return distance;
    }


}
