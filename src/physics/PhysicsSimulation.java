package physics;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector3d;
import org.joml.Vector3dc;

public class PhysicsSimulation {
	private static final double STEP_SIZE = 0.005;

	private static final Vector3dc GRAVITY = new Vector3d(0, -1, 0).toImmutable();

	private List<Body> bodies = new ArrayList<>();

	private double accumulator;

	public PhysicsSimulation() {
		
	}

	public Body addBody(Body b) {
		this.bodies.add(b);
		b.attach(this);
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
			for (int j=0; j<bodies.size(); j++) {
				if (i == j) continue;
				Body b = bodies.get(j);
				Manifold manifold = new Manifold(a, b);

				if (PhysicsUtil.bodyVsBody(manifold)) {
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

		a.getVelocity().sub(impulse.mul(a.getInvMass(), new Vector3d()));
		b.getVelocity().add(impulse.mul(b.getInvMass(), new Vector3d()));

		// Handle friction
		Vector3d tangent = relVel.sub(manifold.normal.mul(relVel.dot(manifold.normal), new Vector3d()), new Vector3d());
		if (tangent.length() < 0.00001) return;
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

		a.getVelocity().sub(frictionImpulse.mul(a.getInvMass(), new Vector3d()));
		b.getVelocity().add(frictionImpulse.mul(b.getInvMass(), new Vector3d()));
	}

	private void positionalCorrection(Body a, Body b, Manifold manifold) {
		final double percent = 0.2;
		final double slop = 0.01;

		Vector3d correction = manifold.normal.mul(Math.max(manifold.penetration - slop, 0.0) / (a.getInvMass() + b.getInvMass()) * percent, new Vector3d());

		if (a.getInvMass() > 0) a.setPosition(a.getPosition().sub(correction.mul(a.getInvMass(), new Vector3d()), new Vector3d()));
		if (b.getInvMass() > 0) b.setPosition(b.getPosition().add(correction.mul(b.getInvMass(), new Vector3d()), new Vector3d()));
	}

	public static void main(String[] args) {
		PhysicsSimulation sim = new PhysicsSimulation();
		Body a = sim.addBody(new Body(1, new AABBGeometry(new Vector3d(-0.5, -0.5, -0.5), new Vector3d(0.5, 0.5, 0.5)))).setPosition(new Vector3d(0, 10, 0));
		//Body b = sim.addBody(new Body(0, new AABBGeometry(new Vector3d(-5, -0.5, -5), new Vector3d(5, 0.5, 5)))).setPosition(new Vector3d(0, 0, 0));
		Body b = sim.addBody(new Body(0, new FloorGeometry(0)));
		for (int i=0; i<1000; i++) {
			sim.tick(100);
			//System.out.printf("%s, %s\n", 0.02*i, a.getPosition().y);
		}

		for (double strength=0; strength<=10; strength+=0.5) {
			Vector3d pos = a.getPosition();
			pos.x = 0;
			a.setPosition(pos);
			a.applyImpulse(new Vector3d(strength, 0, 0));
			for (int i=0; i<1; i++) {
				sim.tick(0.05);
			}
			//System.out.printf("Impulse strength of %s gives a relative movement of %s\n", strength, a.getPosition().x);
			System.out.printf("%s\t%s\n", strength, a.getPosition().x);
		}

//		//System.out.printf("%s, %s\n", 0, a.getPosition().y);
//		for (double dynFric=0; dynFric<=2; dynFric+=0.25) {
//			for (double dynFric2=0; dynFric2<=2; dynFric2+=0.25) {
//				for (double strength=0; strength<=10; strength+=0.5) {
//					Vector3d pos = a.getPosition();
//					pos.x = 0;
//					a.setDynamicFrictionCoeff(dynFric);
//					b.setDynamicFrictionCoeff(dynFric2);
//					a.setPosition(pos);
//					a.applyImpulse(new Vector3d(strength, 0, 0));
//					for (int i=0; i<600; i++) {
//						sim.tick(0.05);
//					}
//					//System.out.printf("Impulse strength of %s gives a relative movement of %s\n", strength, a.getPosition().x);
//					System.out.printf("%s,%s,%s,%s\n", strength, dynFric, dynFric2, a.getPosition().x);
//				}
//			}
//		}
	}
}
