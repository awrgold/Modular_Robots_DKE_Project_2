package all.continuous.gfx;

public interface CameraControlState {
	public void keyCallback(long window, int key, int scancode, int action, int mods);
	public void mouseCallback(long window, int button, int action, int mods);
	public void scrollCallback(long window, double xOff, double yOff);
	public void update(boolean guiHovered);
	public void draw();
}
