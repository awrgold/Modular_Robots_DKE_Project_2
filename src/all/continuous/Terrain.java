package all.continuous;

import java.util.ArrayList;

public class Terrain {
    Simulation simulation;
    ArrayList<Obstacle> obstacles;

    public Terrain(ArrayList<Obstacle> obstacles){
        this.obstacles = obstacles;

        for (int i = 0; i < obstacles.size(); i++) {
            obstacles.get(i).index = i;
        }
    }

    public ArrayList<Obstacle> getObstacles(){
        return obstacles;
    }

    public void setSimulation(Simulation sim){
        this.simulation = sim;
    }
}
