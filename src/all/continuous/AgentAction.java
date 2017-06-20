package all.continuous;

import javafx.geometry.Point3D;

public abstract class AgentAction {
	public final Agent agent;
	
	public AgentAction(Agent agent) {
		this.agent = agent;
	}

	public abstract void apply();
}
