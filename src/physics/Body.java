package physics;

import org.joml.Vector3d;
import org.joml.Vector3dc;

public class Body {
	private double mass;
	public double getInvMass() {
		if (this.mass == 0) return 0;
		return 1.0/this.mass;
	}
	
	private double restitution = 0.5; // FIXME: Test value
	public double getRestitution() { return this.restitution; }
	
	private Geometry geometry;
	public Geometry getGeometry() { return this.geometry; }
	
	private Vector3d position;
	public Vector3d getPosition() { return this.position; }
	public void setPosition(Vector3d position) {
		if (this.mass == 0) return;
		this.position = position;
	}
	private Vector3d velocity;
	public Vector3d getVelocity() { return this.velocity; }
	
	private double staticFrictionCoeff;
	public double getStaticFrictionCoeff() { return staticFrictionCoeff; }
	public void setStaticFrictionCoeff(double staticFrictionCoeff) { this.staticFrictionCoeff = staticFrictionCoeff; }
	private double dynamicFrictionCoeff;
	public double getDynamicFrictionCoeff() { return dynamicFrictionCoeff; }
	public void setDynamicFrictionCoeff(double dynamicFrictionCoeff) { this.dynamicFrictionCoeff = dynamicFrictionCoeff; }
	
	private Vector3d forces;
	
	// Exist reduce unnecessary instantiations
	private Vector3d acc;
	private Vector3d posDelta;
	
	public void applyImpulse(Vector3dc impulse) {
		this.velocity.add(impulse.mul(this.getInvMass(), new Vector3d()));
	}
	
	public void applyForce(Vector3dc gravity) {
		this.forces.add(gravity);
	}
	
	public Body(double mass, Geometry geometry) {
		this.mass = mass;
		this.geometry = geometry;
		
		this.forces = new Vector3d();
		
		acc = new Vector3d();
		posDelta = new Vector3d();
	}
	
	/**
	 * Update velocity and position
	 * 
	 * @param delta
	 */
	public void update(double delta) {
		if (this.mass == 0) return;
		
		acc = this.forces.mul(this.getInvMass(), acc);
		this.velocity.add(acc.mul(delta));
		this.position.add(this.velocity.mul(delta, posDelta));
		
		this.forces.mul(0);
	}
}
