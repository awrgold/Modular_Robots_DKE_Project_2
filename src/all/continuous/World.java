package all.continuous;

public class World {
	public static final double VOXEL_SIZE = 1.0;
	
    int width;
    int length;
    int height;

    private Terrain terrain;
    private Configuration configuration;
    
    public Terrain getTerrain() { return this.terrain; }
}
