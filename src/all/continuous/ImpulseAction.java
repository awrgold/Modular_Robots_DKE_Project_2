package all.continuous;

import org.joml.Vector3dc;

public class ImpulseAction extends AgentAction {
	public final Vector3dc impulse;
	
	public ImpulseAction(int index, Vector3dc impulse) {
		super(index);
		if (!((impulse.x() != 0 && impulse.y() == 0 && impulse.z() == 0) ||
				(impulse.x() == 0 && impulse.y() != 0 && impulse.z() == 0) ||
				(impulse.x() == 0 && impulse.y() == 0 && impulse.z() != 0)
				))
			throw new IllegalArgumentException("Impulse must have exactly one non-zero component");
		this.impulse = impulse;
	}

	@Override
	public void apply(Agent agent) {
		agent.applyImpulse(impulse);
	}
	
	@Override
	public String toString() {
		return String.format("Applying impulse of %s for agent %s", impulse, index);
	}
}
