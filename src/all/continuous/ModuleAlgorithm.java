package all.continuous;

public abstract class ModuleAlgorithm {
	protected Simulation sim;
	
	public ModuleAlgorithm(Simulation sim) {
		this.sim = sim;
	}
	
	public abstract void takeTurn();
}
