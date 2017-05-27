package physics;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joml.Matrix3dc;
import org.joml.Matrix3fc;
import org.joml.Matrix3x2dc;
import org.joml.Matrix3x2fc;
import org.joml.Matrix4dc;
import org.joml.Matrix4fc;
import org.joml.Matrix4x3dc;
import org.joml.Matrix4x3fc;
import org.joml.Quaterniond;
import org.joml.Quaterniondc;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.joml.Vector3fc;

public class PhysicsSimulation {
	private static final double STEP_SIZE = 0.01;

	private static final Vector3dc GRAVITY = new Vector3d(0, -9.81, 0).toImmutable();
	
	private List<Body> bodies = new ArrayList<>();
	
	private double accumulator;
	
	public PhysicsSimulation() {
		
	}
	
	public Body addBody(Body b) {
		this.bodies.add(b);
		return b;
	}
	
	public void removeBody(Body b) {
		this.bodies.remove(b);
	}
	
	public void tick(double trueDelta) {
		accumulator += trueDelta;
		
		while (accumulator > STEP_SIZE) {
			update(STEP_SIZE);
			accumulator -= STEP_SIZE;
		}
	}

	private void update(double delta) {
		for (int i=0; i<bodies.size(); i++) {
			Body a = bodies.get(i);
			for (int j=i+1; j<bodies.size(); j++) {
				Body b = bodies.get(j);
				Manifold manifold = new Manifold(a, b);
				
				if (PhysicsUtil.AABBvsAABB(manifold)) {
					resolveCollision(a, b, manifold);
					positionalCorrection(a, b, manifold);
				}
			}
		}
		
		// Update bodies
		for (Body b : bodies) {
			b.applyForce(GRAVITY);
			b.update(delta);
		}
	}

	private void resolveCollision(Body a, Body b, Manifold manifold) {
		Vector3d relVel = b.getVelocity().sub(a.getVelocity(), new Vector3d());
		double velAlongNormal = relVel.dot(manifold.normal);
		
		if (velAlongNormal > 0) return;
		
		double e = Math.min(a.getRestitution(), b.getRestitution());
		double impulseMag = -(1 + e) * velAlongNormal;
		impulseMag /= a.getInvMass() + b.getInvMass();
		
		Vector3d impulse = manifold.normal.mul(impulseMag, new Vector3d());
		
		a.getVelocity().sub(impulse.mul(a.getInvMass()));
		b.getVelocity().sub(impulse.mul(b.getInvMass()));
		
		// Handle friction
		Vector3d tangent = relVel.sub(manifold.normal.mul(relVel.dot(manifold.normal), new Vector3d()), new Vector3d());
		tangent.normalize();
		
		double frictionImpulseMag = -(relVel.dot(tangent)); // FIXME: Could I not just not normalize the tangent...
		frictionImpulseMag /= a.getInvMass() + b.getInvMass();
		
		double mu = Math.sqrt(Math.pow(a.getStaticFrictionCoeff(), 2) + Math.pow(b.getStaticFrictionCoeff(), 2));
		
		Vector3d frictionImpulse;
		
		if (Math.abs(frictionImpulseMag) < impulseMag * mu) {
			frictionImpulse = tangent.mul(frictionImpulseMag, new Vector3d());
		} else {
			double dynfric = Math.sqrt(Math.pow(a.getDynamicFrictionCoeff(), 2) + Math.pow(b.getDynamicFrictionCoeff(), 2));
			frictionImpulse = tangent.mul(-impulseMag * dynfric);
		}
		
		a.getVelocity().sub(frictionImpulse.mul(a.getInvMass()));
		b.getVelocity().sub(frictionImpulse.mul(b.getInvMass()));
	}
	
	private void positionalCorrection(Body a, Body b, Manifold manifold) {
		final double percent = 0.2;
		final double slop = 0.01;
		
		Vector3d correction = manifold.normal.mul(Math.max(manifold.penetration - slop, 0.0) / (a.getInvMass() + b.getInvMass()) * percent, new Vector3d());
		
		a.setPosition(a.getPosition().sub(correction.mul(a.getInvMass(), new Vector3d()), new Vector3d()));
		b.setPosition(b.getPosition().sub(correction.mul(b.getInvMass(), new Vector3d()), new Vector3d()));
	}
}
