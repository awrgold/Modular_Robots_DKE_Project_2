package all.continuous;

import org.joml.Vector3dc;

public class ImpulseAction extends AgentAction {
	public final Vector3dc impulse;
	
	public ImpulseAction(Agent agent, Vector3dc impulse) {
		super(agent);
		if (!((impulse.x() != 0 && impulse.y() == 0 && impulse.z() == 0) ||
				(impulse.x() == 0 && impulse.y() != 0 && impulse.z() == 0) ||
				(impulse.x() == 0 && impulse.y() == 0 && impulse.z() != 0)
				))
			throw new IllegalArgumentException("Impulse must have exactly one non-zero component");
		this.impulse = impulse;
	}

	@Override
	public void apply() {
		agent.applyImpulse(impulse);
	}
}
