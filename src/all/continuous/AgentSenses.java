package all.continuous;

import javafx.geometry.Point3D;
import org.joml.Vector3d;
import org.lwjgl.system.CallbackI;

import java.util.ArrayList;

/**
 * Created by God on the 8th day, and it was good...
 */
public class AgentSenses {

    private Simulation sim;
    private AgentCouple agents;
    private Configuration conf;
    private Point3D agent1Loc;
    private Point3D agent2Loc;
    private static boolean DEBUG = false;

    public AgentSenses(Simulation sim, Configuration conf, AgentCouple agents){
        this.sim = sim;
        this.conf = conf;
        this.agents = agents;
    }

    public void updateAgentsLoc(){
        Vector3d vector1 = agents.getAgent1(sim).getPosition();
        agent1Loc = new Point3D(vector1.x, vector1.y, vector1.z);
        Vector3d vector2 = agents.getAgent2(sim).getPosition();
        agent2Loc = new Point3D(vector2.x, vector2.y, vector2.z);
    }

    public AgentAction makeDecision(ArrayList<CollisionUtil.Collision> collisions) {

        //TODO: *** ABSOLUTELY MUST FIGURE OUT WHEN TO TELL AN AGENT TO CHANGE DIRECTIONS WHEN APPROACHING AN OBSTACLE. IS IT AT DISTANCE 3 WHEN SMELL TAKES OVER?
        //TODO: *** MUST HAVE A METHOD TO OVERRIDE MOVEMENT WHEN A PHEROMONE TRAIL IS DETECTED NEARBY

        // Loop through collisions and organize decision based on type (priority queue of sorts)
        ArrayList<Point3D> seenObjects = new ArrayList<>();
        double distanceToClosestObject = Double.MAX_VALUE;
        Point3D closestObject = null;
        Agent movingAgent =null;

        // add the locations of each collision it finds
        for (int i = 0; i < collisions.size(); i++) {
            if (collisions != null){
                if (collisions.get(i).type == CollisionType.AGENT) {
                    seenObjects.add(collisions.get(i).getLocation());
                }
                if (collisions.get(i).type == CollisionType.OBSTACLE) {
                    seenObjects.add(collisions.get(i).getLocation());
                }
            }
            else{
                System.out.println("No collisions detected");
                /**
                 * MOVE RANDOMLY???
                 */
            }
        }

        // find the closest object.
        // if multiple objects are both the same distance, pick one at random.
        for (int i = 0; i < seenObjects.size(); i++) {
            if (getManhattanDistanceBetween(agent1Loc, seenObjects.get(i)) < distanceToClosestObject) {
                distanceToClosestObject = getManhattanDistanceBetween(agent1Loc, seenObjects.get(i));
                closestObject = seenObjects.get(i);
                movingAgent = agents.getAgent1(sim);

            } else if (getManhattanDistanceBetween(agent2Loc, seenObjects.get(i)) < distanceToClosestObject) {
                distanceToClosestObject = getManhattanDistanceBetween(agent2Loc, seenObjects.get(i));
                closestObject = seenObjects.get(i);
                movingAgent = agents.getAgent2(sim);

            } else if (getManhattanDistanceBetween(agent1Loc, seenObjects.get(i)) == distanceToClosestObject) {
                double coinToss = Math.random();
                if (coinToss < .5) {
                    distanceToClosestObject = getManhattanDistanceBetween(agent1Loc, seenObjects.get(i));
                    closestObject = seenObjects.get(i);
                    movingAgent = agents.getAgent1(sim);
                } else {
                    i++;
                }
            } else if (getManhattanDistanceBetween(agent2Loc, seenObjects.get(i)) == distanceToClosestObject) {
                double coinToss = Math.random();
                if (coinToss < .5) {
                    distanceToClosestObject = getManhattanDistanceBetween(agent2Loc, seenObjects.get(i));
                    closestObject = seenObjects.get(i);
                    movingAgent = agents.getAgent2(sim);

                } else {
                    i++;
                }
            }
        }

        ArrayList<AgentAction> validMoves = sim.getCurrentConfiguration().getPhysicalActions(movingAgent);


        for (int i = 0; i < validMoves.size(); i++){
            AgentAction bestAction = null;
            if (getManhattanDistanceBetween(validMoves.get(i), closestObject) <= bestAction){
                bestAction = validMoves.get(i);
            }
             movingAgent.move(bestAction.getLocation());
        }


        // from here, determine which agent in AgentCouple is further from the closest object.
        // Choose the move that brings it closest in Manhattan distance to the closest object
        // Move it
        // Repeat until a new closest object is found.


        //sim.applyPhysical(agentAction action)

    }


    public void visualSearch(Configuration conf, Simulation sim){
        // Cast Ray in Left/Right/Forward search field from front agent in "agents"

        // if Agent 1 is in front of Agent 2, cast rays from Agent 1 to L/R/F
        if (agent1Loc.getX() > agent2Loc.getX()){

            // Store all collisions in an arraylist to loop through
            ArrayList<CollisionUtil.Collision> collisions = new ArrayList<>();

            Point3D forwardDirOne = new Point3D(agent1Loc.getX() + 1.0, agent1Loc.getY(), agent1Loc.getZ());
            Point3D forwardDirTwo = new Point3D(agent2Loc.getX() - 1.0, agent2Loc.getY(), agent2Loc.getZ());
            Ray forwardRayOne = new Ray(agent1Loc, forwardDirOne);
            Ray forwardRayTwo = new Ray(agent2Loc, forwardDirTwo);
            CollisionUtil.Collision agent1ColForward = CollisionUtil.castRay(conf, forwardRayOne, 0.25, 20.0, 1, agents.getAgent1(sim));
            CollisionUtil.Collision agent2ColForward = CollisionUtil.castRay(conf, forwardRayTwo, 0.25, 20.0, 1, agents.getAgent1(sim));

            Point3D leftDirOne = new Point3D(agent1Loc.getX(), agent1Loc.getY(), agent1Loc.getZ() - 1.0);
            Point3D leftDirTwo = new Point3D(agent2Loc.getX(), agent2Loc.getY(), agent2Loc.getZ() - 1.0);
            Ray leftRayOne = new Ray(agent1Loc, leftDirOne);
            Ray leftRayTwo = new Ray(agent1Loc, leftDirTwo);
            CollisionUtil.Collision agent1ColLeft = CollisionUtil.castRay(conf, leftRayOne, 0.25, 20.0, 1, agents.getAgent1(sim));
            CollisionUtil.Collision agent2ColLeft = CollisionUtil.castRay(conf, leftRayTwo, 0.25, 20.0, 1, agents.getAgent1(sim));

            Point3D rightDirOne = new Point3D(agent1Loc.getX(), agent1Loc.getY(), agent1Loc.getZ() + 1.0);
            Point3D rightDirTwo = new Point3D(agent2Loc.getX(), agent2Loc.getY(), agent2Loc.getZ() + 1.0);
            Ray rightRayOne = new Ray(agent1Loc, rightDirOne);
            Ray rightRayTwo = new Ray(agent1Loc, rightDirTwo);
            CollisionUtil.Collision agent1ColRight = CollisionUtil.castRay(conf, rightRayOne, 0.25, 20.0, 1.0, agents.getAgent1(sim));
            CollisionUtil.Collision agent2ColRight = CollisionUtil.castRay(conf, rightRayTwo, 0.25, 20.0, 1.0, agents.getAgent1(sim));

            collisions.add(agent1ColForward);
            collisions.add(agent2ColForward);
            collisions.add(agent1ColLeft);
            collisions.add(agent2ColLeft);
            collisions.add(agent1ColRight);
            collisions.add(agent2ColRight);

            makeDecision(collisions);
        }

        // if Agent 2 is in front of Agent 1, cast rays from Agent 2 to L/R/F
        else if (agent2Loc.getX() > agent1Loc.getX()){

            ArrayList<CollisionUtil.Collision> collisions = new ArrayList<>();


            Point3D forwardDirOne = new Point3D(agent1Loc.getX() - 1.0, agent1Loc.getY(), agent1Loc.getZ());
            Point3D forwardDirTwo = new Point3D(agent2Loc.getX() + 1.0, agent2Loc.getY(), agent2Loc.getZ());
            Ray forwardRayOne = new Ray(agent1Loc, forwardDirOne);
            Ray forwardRayTwo = new Ray(agent2Loc, forwardDirTwo);
            CollisionUtil.Collision agent1ColForward = CollisionUtil.castRay(conf, forwardRayOne, 0.25, 20.0, 1, agents.getAgent1(sim));
            CollisionUtil.Collision agent2ColForward = CollisionUtil.castRay(conf, forwardRayTwo, 0.25, 20.0, 1, agents.getAgent1(sim));

            Point3D leftDirOne = new Point3D(agent1Loc.getX(), agent1Loc.getY(), agent1Loc.getZ() - 1);
            Point3D leftDirTwo = new Point3D(agent2Loc.getX(), agent2Loc.getY(), agent2Loc.getZ() - 1);
            Ray leftRayOne = new Ray(agent1Loc, leftDirOne);
            Ray leftRayTwo = new Ray(agent1Loc, leftDirTwo);
            CollisionUtil.Collision agent1ColLeft = CollisionUtil.castRay(conf, leftRayOne, 0.25, 20.0, 1, agents.getAgent1(sim));
            CollisionUtil.Collision agent2ColLeft = CollisionUtil.castRay(conf, leftRayTwo, 0.25, 20.0, 1, agents.getAgent1(sim));

            Point3D rightDirOne = new Point3D(agent1Loc.getX(), agent1Loc.getY(), agent1Loc.getZ() + 1);
            Point3D rightDirTwo = new Point3D(agent2Loc.getX(), agent2Loc.getY(), agent2Loc.getZ() + 1);
            Ray rightRayOne = new Ray(agent1Loc, rightDirOne);
            Ray rightRayTwo = new Ray(agent1Loc, rightDirTwo);
            CollisionUtil.Collision agent1ColRight = CollisionUtil.castRay(conf, rightRayOne, 0.25, 20.0, 1, agents.getAgent1(sim));
            CollisionUtil.Collision agent2ColRight = CollisionUtil.castRay(conf, rightRayTwo, 0.25, 20.0, 1, agents.getAgent1(sim));


            collisions.add(agent1ColForward);
            collisions.add(agent2ColForward);
            collisions.add(agent1ColLeft);
            collisions.add(agent2ColLeft);
            collisions.add(agent1ColRight);
            collisions.add(agent2ColRight);

            makeDecision(collisions);

        }

        // if Agent 1 is to the right of Agent 2, cast rays from Agent 1 to R/F/B
        else if (agent1Loc.getZ() > agent2Loc.getZ()){

            ArrayList<CollisionUtil.Collision> collisions = new ArrayList<>();


            Point3D forwardDirOne = new Point3D(agent1Loc.getX(), agent1Loc.getY(), agent1Loc.getZ() + 1);
            Point3D forwardDirTwo = new Point3D(agent2Loc.getX(), agent2Loc.getY(), agent2Loc.getZ() - 1);
            Ray forwardRayOne = new Ray(agent1Loc, forwardDirOne);
            Ray forwardRayTwo = new Ray(agent2Loc, forwardDirTwo);
            CollisionUtil.Collision agent1ColForward = CollisionUtil.castRay(conf, forwardRayOne, 0.25, 20.0, 1, agents.getAgent1(sim));
            CollisionUtil.Collision agent2ColForward = CollisionUtil.castRay(conf, forwardRayTwo, 0.25, 20.0, 1, agents.getAgent1(sim));

            Point3D leftDirOne = new Point3D(agent1Loc.getX() + 1, agent1Loc.getY(), agent1Loc.getZ());
            Point3D leftDirTwo = new Point3D(agent2Loc.getX() + 1, agent2Loc.getY(), agent2Loc.getZ());
            Ray leftRayOne = new Ray(agent1Loc, leftDirOne);
            Ray leftRayTwo = new Ray(agent1Loc, leftDirTwo);
            CollisionUtil.Collision agent1ColLeft = CollisionUtil.castRay(conf, leftRayOne, 0.25, 20.0, 1, agents.getAgent1(sim));
            CollisionUtil.Collision agent2ColLeft = CollisionUtil.castRay(conf, leftRayTwo, 0.25, 20.0, 1, agents.getAgent1(sim));

            Point3D rightDirOne = new Point3D(agent1Loc.getX() - 1, agent1Loc.getY(), agent1Loc.getZ());
            Point3D rightDirTwo = new Point3D(agent2Loc.getX() - 1, agent2Loc.getY(), agent2Loc.getZ());
            Ray rightRayOne = new Ray(agent1Loc, rightDirOne);
            Ray rightRayTwo = new Ray(agent1Loc, rightDirTwo);
            CollisionUtil.Collision agent1ColRight = CollisionUtil.castRay(conf, rightRayOne, 0.25, 20.0, 1, agents.getAgent1(sim));
            CollisionUtil.Collision agent2ColRight = CollisionUtil.castRay(conf, rightRayTwo, 0.25, 20.0, 1, agents.getAgent1(sim));


            collisions.add(agent1ColForward);
            collisions.add(agent2ColForward);
            collisions.add(agent1ColLeft);
            collisions.add(agent2ColLeft);
            collisions.add(agent1ColRight);
            collisions.add(agent2ColRight);

            makeDecision(collisions);


        }

        // if Agent 1 is to the left of Agent 2, cast rays from Agent 1 to R/F/B
        else if (agent2Loc.getZ() > agent1Loc.getZ()){

            ArrayList<CollisionUtil.Collision> collisions = new ArrayList<>();


            Point3D forwardDirOne = new Point3D(agent1Loc.getX(), agent1Loc.getY(), agent1Loc.getZ() - 1);
            Point3D forwardDirTwo = new Point3D(agent2Loc.getX(), agent2Loc.getY(), agent2Loc.getZ() + 1);
            Ray forwardRayOne = new Ray(agent1Loc, forwardDirOne);
            Ray forwardRayTwo = new Ray(agent2Loc, forwardDirTwo);
            CollisionUtil.Collision agent1ColForward = CollisionUtil.castRay(conf, forwardRayOne, 0.25, 20.0, 1, agents.getAgent1(sim));
            CollisionUtil.Collision agent2ColForward = CollisionUtil.castRay(conf, forwardRayTwo, 0.25, 20.0, 1, agents.getAgent1(sim));

            Point3D leftDirOne = new Point3D(agent1Loc.getX() + 1, agent1Loc.getY(), agent1Loc.getZ());
            Point3D leftDirTwo = new Point3D(agent2Loc.getX() + 1, agent2Loc.getY(), agent2Loc.getZ());
            Ray leftRayOne = new Ray(agent1Loc, leftDirOne);
            Ray leftRayTwo = new Ray(agent1Loc, leftDirTwo);
            CollisionUtil.Collision agent1ColLeft = CollisionUtil.castRay(conf, leftRayOne, 0.25, 20.0, 1, agents.getAgent1(sim));
            CollisionUtil.Collision agent2ColLeft = CollisionUtil.castRay(conf, leftRayTwo, 0.25, 20.0, 1, agents.getAgent1(sim));

            Point3D rightDirOne = new Point3D(agent1Loc.getX() - 1, agent1Loc.getY(), agent1Loc.getZ());
            Point3D rightDirTwo = new Point3D(agent2Loc.getX() - 1, agent2Loc.getY(), agent2Loc.getZ());
            Ray rightRayOne = new Ray(agent1Loc, rightDirOne);
            Ray rightRayTwo = new Ray(agent1Loc, rightDirTwo);
            CollisionUtil.Collision agent1ColRight = CollisionUtil.castRay(conf, rightRayOne, 0.25, 20.0, 1, agents.getAgent1(sim));
            CollisionUtil.Collision agent2ColRight = CollisionUtil.castRay(conf, rightRayTwo, 0.25, 20.0, 1, agents.getAgent1(sim));


            collisions.add(agent1ColForward);
            collisions.add(agent2ColForward);
            collisions.add(agent1ColLeft);
            collisions.add(agent2ColLeft);
            collisions.add(agent1ColRight);
            collisions.add(agent2ColRight);

            makeDecision(collisions);


        }
        else{
            if (agent1Loc.getY() < agent2Loc.getY()){

                ArrayList<CollisionUtil.Collision> collisions = new ArrayList<>();

                Point3D forwardDir = new Point3D(agent1Loc.getX() + 1, agent1Loc.getY(), agent1Loc.getZ());
                Ray forwardRay = new Ray(agent1Loc, forwardDir);
                CollisionUtil.Collision colForward = CollisionUtil.castRay(conf, forwardRay, 0.25, 20.0, 1, agents.getAgent1(sim));

                Point3D leftDir = new Point3D(agent1Loc.getX(), agent1Loc.getY(), agent1Loc.getZ() - 1);
                Ray leftRay = new Ray(agent1Loc, leftDir);
                CollisionUtil.Collision colLeft = CollisionUtil.castRay(conf, leftRay, 0.25, 20.0, 1, agents.getAgent1(sim));

                Point3D rightDir = new Point3D(agent1Loc.getX(), agent1Loc.getY(), agent1Loc.getZ() + 1);
                Ray rightRay = new Ray(agent1Loc, rightDir);
                CollisionUtil.Collision colRight = CollisionUtil.castRay(conf, rightRay, 0.25, 20.0, 1, agents.getAgent1(sim));

                Point3D backDir = new Point3D(agent1Loc.getX() - 1, agent1Loc.getY(), agent1Loc.getZ());
                Ray backRay = new Ray(agent1Loc, backDir);
                CollisionUtil.Collision colBack = CollisionUtil.castRay(conf, backRay, 0.25, 20.0, 1, agents.getAgent1(sim));


                collisions.add(colForward);
                collisions.add(colLeft);
                collisions.add(colRight);
                collisions.add(colBack);

                makeDecision(collisions);


            }else{

                ArrayList<CollisionUtil.Collision> collisions = new ArrayList<>();

                Point3D forwardDir = new Point3D(agent2Loc.getX() + 1, agent2Loc.getY(), agent2Loc.getZ());
                Ray forwardRay = new Ray(agent2Loc, forwardDir);
                CollisionUtil.Collision colForward = CollisionUtil.castRay(conf, forwardRay, 0.25, 20.0, 1, agents.getAgent1(sim));

                Point3D leftDir = new Point3D(agent2Loc.getX(), agent2Loc.getY(), agent2Loc.getZ() - 1);
                Ray leftRay = new Ray(agent2Loc, leftDir);
                CollisionUtil.Collision colLeft = CollisionUtil.castRay(conf, leftRay, 0.25, 20.0, 1, agents.getAgent1(sim));

                Point3D rightDir = new Point3D(agent2Loc.getX(), agent2Loc.getY(), agent2Loc.getZ() + 1);
                Ray rightRay = new Ray(agent2Loc, rightDir);
                CollisionUtil.Collision colRight = CollisionUtil.castRay(conf, rightRay, 0.25, 20.0, 1, agents.getAgent1(sim));

                Point3D backDir = new Point3D(agent2Loc.getX() - 1, agent2Loc.getY(), agent2Loc.getZ());
                Ray backRay = new Ray(agent2Loc, backDir);
                CollisionUtil.Collision colBack = CollisionUtil.castRay(conf, backRay, 0.25, 20.0, 1, agents.getAgent1(sim));


                collisions.add(colForward);
                collisions.add(colLeft);
                collisions.add(colRight);
                collisions.add(colBack);

                makeDecision(collisions);

            }
        }
    }

    public boolean isActiveTrailNearby(Configuration conf, Simulation sim){
        // loop through all the squares around AgentCouple

        // determine if any of these contain an active pheromone trail

        // if true, abandon eyesight and move straight towards pheromone trail

        // once in pheromone trail, follow trail to goal.
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
