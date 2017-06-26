package all.continuous;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class RandomAlgorithm extends ModuleAlgorithm {

	private Random rand;
	
	public RandomAlgorithm(Simulation sim) {
		super(sim);
		this.rand = new Random();
	}
	
	@Override
	public void takeTurn() {
		List<AgentAction> actions = this.sim.getAllPhysicalActions();
		int actionCount = actions.size();
		if (actionCount <= 0) {
			sim.finish();
			return;
		}
		sim.applyPhysical(actions.get(rand.nextInt(actionCount)));
		
		if (rand.nextFloat() < 0.0001f) {
			sim.finish();
		}
	}
	
}
