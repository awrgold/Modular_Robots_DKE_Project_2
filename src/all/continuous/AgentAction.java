package all.continuous;

import javafx.geometry.Point3D;

public abstract class AgentAction {
	public final int index;
	
	public AgentAction(int index) {
		this.index = index;
	}

	public abstract void apply(Agent agent);
}
