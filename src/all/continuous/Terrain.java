package all.continuous;

import java.util.ArrayList;
import java.util.List;

public class Terrain {
    Simulation simulation;
    List<Obstacle> obstacles;

    public Terrain(List<Obstacle> obstacles){
        this.obstacles = obstacles;

        for (int i = 0; i < obstacles.size(); i++) {
            obstacles.get(i).index = i;
        }
    }

    public List<Obstacle> getObstacles(){
        return obstacles;
    }

    public void setSimulation(Simulation sim){
        this.simulation = sim;
    }
    
}
