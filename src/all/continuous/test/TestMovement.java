package all.continuous.test;

import java.util.ArrayList;
import java.util.List;

import all.continuous.Action;
import all.continuous.Agent;
import all.continuous.Configuration;
import all.continuous.Obstacle;
import all.continuous.Simulation;
import all.continuous.Terrain;
import javafx.geometry.Point3D;

public class TestMovement {
	public static void test1() {
		List<Obstacle> obstacles = new ArrayList<>();
		for (int x=0; x<5; x++) {
			for (int y=0; y<5; y++) {
				obstacles.add(new Obstacle(x*5+y, new Point3D(x, 0, y)));
			}
		}
		Terrain t = new Terrain(obstacles);
		
		ArrayList<Agent> agents = new ArrayList<>();
		agents.add(new Agent(0, new Point3D(0, 0, 0)));
		agents.add(new Agent(1, new Point3D(1, 0, 0)));
		agents.add(new Agent(2, new Point3D(1, 1, 0)));
		Simulation sim = new Simulation(t, new Configuration(agents), new Configuration(agents));
		
		for (Action a : sim.getAllValidActions()) {
			System.out.println(a);
		}
	}
	
	public static void main(String[] args) {
		test1();
	}
}
