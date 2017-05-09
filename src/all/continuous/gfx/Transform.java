package all.continuous.gfx;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javafx.geometry.Point3D;

public class Transform {
	public Vector3f position;
	public Quaternionf rotation;
	
	public Transform() {
		this.position = new Vector3f();
		this.rotation = new Quaternionf();
	}
	
	public Transform(Transform transform) {
		this.position = new Vector3f(transform.position);
		this.rotation = new Quaternionf(transform.rotation);
	}

	public void setPosition(float x, float y, float z) {
		this.position.x = x;
		this.position.y = y;
		this.position.z = z;
	}
	
	public void rotateX(float theta) {
		this.rotation.rotateX(theta);
	}
	public void rotateY(float theta) {
		this.rotation.rotateY(theta);
	}
	public void rotateZ(float theta) {
		this.rotation.rotateZ(theta);
	}
	
	public Matrix4f getMatrix() {
		Matrix4f result = new Matrix4f();
		result.translate(position);
		result.rotate(rotation);
		return result;
	}

	public Point3D toPoint3D() {
		return new Point3D(position.x, position.y, position.z);
	}
}
