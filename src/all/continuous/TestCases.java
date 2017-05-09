package all.continuous;

import javafx.geometry.Point3D;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class TestCases {
    public static void basicTestCollisionDetection(){
        //EXPECTED OUTPUT: true false true

        Agent agent1 = new Agent(0, new Point3D(2,3,4));
        Agent agent2 = new Agent(1, new Point3D(2.5,3.5,4.5));
        Agent agent3 = new Agent(2, new Point3D(3,4,5));

        System.out.print(agent1.isCollidingWith(agent2) + " "); //true
        System.out.print(agent1.isCollidingWith(agent3) + " "); //false
        System.out.print(agent2.isCollidingWith(agent3) + " "); //true
    }

    public static void collisionDetectionTest2(){
        Agent agent1 = new Agent(0, new Point3D(0,0,0));
        Agent agent2 = new Agent(1, new Point3D(1,0,0));
        Agent agent3 = new Agent(2, new Point3D(1.- 1000*Double.MIN_VALUE, 0, 0));

        double test = 1- Double.MIN_NORMAL;

        System.out.printf("%.200f", Double.MIN_NORMAL);

        System.out.println();

        System.out.printf("%.200f", 1000000*Double.MIN_NORMAL);

        System.out.println();

        System.out.printf("%.200f", 1000000*Double.MIN_VALUE);

        System.out.println();

        System.out.printf("%.100f", test);

        System.out.println();

        System.out.print(agent1.isCollidingWith(agent3) + " "); //false
    }

    public static void basicTestAgentMovement() throws InvalidMoveException, InvalidStateException {

        /* EXPECTED OUTPUT:
        2, 3, 4
        7, 8, 9
        5, 6, 7
        InvalidMoveException
         */

        Agent agent1 = new Agent(0, new Point3D(2,3,4));

        ArrayList<Agent> agents = new ArrayList<>();
        agents.add(agent1);

        ArrayList<Obstacle> obstacles = new ArrayList<>();

        Agent goalAgent = new Agent(0, new Point3D(7,6,5));
        ArrayList<Agent> agentsGoal = new ArrayList<>();
        agentsGoal.add(goalAgent);

        Simulation sim = new Simulation(new Terrain(obstacles), new Configuration(agents), new Configuration(agentsGoal));

        System.out.println(sim.getCurrentConfiguration().getAgents().get(0).getLocation());

        sim.getCurrentConfiguration().getAgents().get(0).move(new Point3D(7,8,9));

        System.out.println(sim.getCurrentConfiguration().getAgents().get(0).getLocation());

        sim.endTurn();

        sim.getCurrentConfiguration().getAgents().get(0).move(new Point3D(5,6,7));

        System.out.println(sim.getCurrentConfiguration().getAgents().get(0).getLocation());

        sim.getCurrentConfiguration().getAgents().get(0).move(new Point3D(0,0,0));

    }

    public static void basicTestAutomaticStateValidation() throws InvalidMoveException, InvalidStateException {

        /*EXPECTED OUTPUT:
        Agent 0(ID:0.0) and obstacle 0(ID:0.0)
        Agent 0(ID:0.0) and agent 1(ID:2.5)
        Agent 1(ID:2.5) and obstacle 0(ID:0.0)
        */

        Agent agent1 = new Agent(0, new Point3D(2,3,4));

        ArrayList<Agent> agents = new ArrayList<>();
        agents.add(agent1);
        agent1 = null;

        Agent agent2 = new Agent((float) 2.5, new Point3D(4,5,6));
        agents.add(agent2);
        agent2 = null;

        Obstacle obstacle = new Obstacle(0, new Point3D(4,5,6));
        ArrayList<Obstacle> obstacles = new ArrayList<>();
        obstacles.add(obstacle);
        obstacle = null;

        Agent goalAgent = new Agent(0, new Point3D(7,6,5));
        ArrayList<Agent> agentsGoal = new ArrayList<>();
        agentsGoal.add(goalAgent);
        goalAgent = null;

        Simulation sim = new Simulation(new Terrain(obstacles), new Configuration(agents), new Configuration(agentsGoal));
        obstacles = null;
        agents = null;
        agentsGoal = null;

        sim.getCurrentConfiguration().getAgent(0).move(new Point3D(4,5,6));
        sim.endTurn();
    }

    public static void basicTestCopyingAndData() throws InvalidMoveException, InvalidStateException {
        Agent agent1 = new Agent(0, new Point3D(2,3,4));
        Agent agent2 = new Agent(1, new Point3D(100,200,300));

        ArrayList<Agent> agents = new ArrayList<>();
        agents.add(agent1);
        agents.add(agent2);

        Obstacle obstacle = new Obstacle(0, new Point3D(4,5,6));
        ArrayList<Obstacle> obstacles = new ArrayList<>();
        obstacles.add(obstacle);

        Simulation sim = new Simulation(new Terrain(obstacles), new Configuration(agents), new Configuration(agents));

        sim.getCurrentConfiguration().getAgent(0).move(new Point3D(17,11,12));
        sim.getCurrentConfiguration().getAgent(1).move(new Point3D(200,3,150));

        sim.endTurn();

        sim.getCurrentConfiguration().getAgent(0).move(new Point3D(200,3,150));
        sim.getCurrentConfiguration().getAgent(1).move(new Point3D(17,11,12));

        sim.endTurn();

        System.out.println("kekeke");
    }
}
