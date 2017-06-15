package all.continuous;

import java.util.Arrays;

import org.joml.Vector3d;

import javafx.geometry.Point3D;

public class ClimbAction extends AgentAction {
	public final Point3D primaryDir;
	public final Point3D secondaryDir;

	public ClimbAction(Agent agent, Point3D primaryDir, Point3D secondaryDir) {
		super(agent);
		if (Arrays.stream(Direction.DIRECTIONS).filter((dir) -> dir.equals(primaryDir) || dir.equals(secondaryDir)).count() != 2)
			throw new IllegalArgumentException("primaryDir and secondaryDir must be two unique directions (unit vectors)");
		
		if (primaryDir == Direction.DOWN)
			throw new IllegalArgumentException("primaryDir cannot be down");
		
		if (primaryDir != Direction.UP && secondaryDir != Direction.DOWN)
			throw new IllegalArgumentException("secondaryDir cannot be down if primaryDir is not up");
		
		this.primaryDir = primaryDir;
		this.secondaryDir = secondaryDir;
	}

	@Override
	public void apply() {
		Point3D loc = agent.getLocation();
		Point3D dest = loc.add(primaryDir).add(secondaryDir);
		agent.setPosition(new Vector3d(dest.getX(), dest.getY(), dest.getZ()));
	}

}
