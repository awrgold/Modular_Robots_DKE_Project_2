package all.continuous.gfx;



import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by Roel on 19-03-17.
 */
public class VoxelRenderer {
    private static final float VOXEL_SIZE = 1.0f;
    private static final float delta = 0.05f;
    /*
    private final Mesh cubeMesh;
    private final Mesh floorMesh;
    private int[][][] data, oldData;
    private boolean[][][] goals;
    private HashMap<Integer, Vector4f> colours = new HashMap<>();
	private Mesh goalMesh;
	
    public VoxelRenderer() {
        this.cubeMesh = ShapeFactory.genBox(delta, delta, delta, 1.0008f-delta*2, 1.0008f-delta*2, 1.0008f-delta*2);
        this.goalMesh = ShapeFactory.genBox(0, 0, 0, 1, 1, 1);
        setColour(1, new Vector4f(0.9f, 0.65f, 0.13f, 0.84f));
        for (int i=0; i<28; i++)
            setColour(3+i, new Vector4f((float)Math.random(), (float)Math.random(), (float)Math.random(), 1.0f));

        setColour(2, new Vector4f(0.9f, 0.1f, 0.1f, 0.5f));

        this.floorMesh = ShapeFactory.genBox(-64, -1, -64, 128, 1, 128);
    }

    public void setData(int[][][] data) {
        if (this.data != null && this.data.length == data.length && this.data[0].length == data[0].length)
            this.oldData = this.data;
        else
            this.oldData = null;
        this.data = data;
    }
    
    public void setData(World w) {
        int[][][] state = w.state;

        int[][][] newData = new int[state.length][][];
        for (int x=0; x<newData.length; x++) {
            newData[x] = new int[state[x].length][];
            for (int y=0; y<newData[x].length; y++) {
                newData[x][y] = new int[state[x][y].length];
                for (int z=0; z<newData[x][y].length; z++) {
                    int v = state[x][y][z];
                    if (v >= 0) {
                        newData[x][y][z] = 3 + v;
                    } else if (v == -1) {
                        newData[x][y][z] = 1;
                    }
                }
            }
        }

        this.goals = new boolean[newData.length][newData[0].length][newData[0][0].length];
        if (w.goals != null) {
            for (Point3D goal : w.goals) {
                goals[goal.getX()][goal.getY()][goal.getZ()] = true;
            }
        }
        for (Agent a : w.agents) {
            Point3D goal = a.goal;
            goals[goal.getX()][goal.getY()][goal.getZ()] = true;
        }

        this.setData(newData);
    }

    public void setColour(int key, Vector4f colour) {
        colours.put(key, colour);
    }

    public void render() {
    	// Render all opaque voxels
        for (int x=0; x<data.length; x++) {
            for (int y=0; y<data[x].length; y++) {
                for (int z=0; z<data[x][y].length; z++) {
                    int voxel = data[x][y][z];
                    if (voxel > 0) {
                        if (colours.containsKey(voxel) && colours.get(voxel).w < 0.99f) continue;
                        if (x > 0 && x < data.length-1 && // TODO: Add nicer voxel rendering
                                y > 0 && y < data[x].length-1 &&
                                z > 0 && z < data[x][y].length-1 &&
                                data[x-1][y][z] > 0 &&
                                data[x+1][y][z] > 0 &&
                                data[x][y+1][z] > 0 &&
                                data[x][y][z-1] > 0 &&
                                data[x][y][z+1] > 0) continue;
                        MVP.translate(x * VOXEL_SIZE, z * VOXEL_SIZE, y * VOXEL_SIZE);
                        {
                            Vector4f colour;
                            if (colours.containsKey(voxel)) {
                                colour = colours.get(voxel);
                            } else {
                                colour = new Vector4f(1, 1, 1, 1);;
                            }
                            if (this.oldData != null && this.oldData[x][y][z] != this.data[x][y][z]) {
                                colour = colour.add(0.5f, -0.6f, -0.6f, 0, new Vector4f());
                            }
                            ShaderManager.getInstance().getShader().setVector4("colour", colour);
                            this.cubeMesh.draw();
                        }
                        MVP.translate(-x * VOXEL_SIZE, -z * VOXEL_SIZE, -y * VOXEL_SIZE);
                    }
                }
            }
        }

        ShaderManager.getInstance().getShader().setVector4("colour",
                new Vector4f(1, 1, 1, 1));
        
        // Render floor mesh
        floorMesh.draw();

        for (int x=0; x<data.length; x++) {
            for (int y=0; y<data[x].length; y++) {
                for (int z=0; z<data[x][y].length; z++) {
                    int voxel = data[x][y][z];
                    if (voxel > 0) {
                        if (colours.containsKey(voxel) && colours.get(voxel).w >= 0.99f) continue;
                        if (x > 0 && x < data.length-1 && // TODO: Add nicer voxel rendering
                                y > 0 && y < data[x].length-1 &&
                                z > 0 && z < data[x][y].length-1 &&
                                data[x-1][y][z] > 0 &&
                                data[x+1][y][z] > 0 &&
                                data[x][y+1][z] > 0 &&
                                data[x][y][z-1] > 0 &&
                                data[x][y][z+1] > 0) continue;
                        MVP.translate(x * VOXEL_SIZE, z * VOXEL_SIZE, y * VOXEL_SIZE);
                        {
                            Vector4f colour;
                            if (colours.containsKey(voxel)) {
                                colour = colours.get(voxel);
                            } else {
                                colour = new Vector4f(1, 1, 1, 1);;
                            }
                            if (this.oldData != null && this.oldData[x][y][z] != this.data[x][y][z]) {
                                colour = colour.add(0.3f, -0.1f, -0.1f ,0, new Vector4f());
                            }
                            ShaderManager.getInstance().getShader().setVector4("colour", colour);
                            this.cubeMesh.draw();
                        }
                        MVP.translate(-x * VOXEL_SIZE, -z * VOXEL_SIZE, -y * VOXEL_SIZE);
                    }
                }
            }
        }

        // Rnder goals
        for (int x=0; x<goals.length; x++) {
            for (int y=0; y<goals[x].length; y++) {
                for (int z=0; z<goals[x][y].length; z++) {
                    boolean hasGoal = goals[x][y][z];
                    if (hasGoal) {
                        MVP.translate(x * VOXEL_SIZE, z * VOXEL_SIZE, y * VOXEL_SIZE);
                        {
                            ShaderManager.getInstance().getShader().setVector4("colour", colours.get(2));
                            this.goalMesh.draw();
                        }
                        MVP.translate(-x * VOXEL_SIZE, -z * VOXEL_SIZE, -y * VOXEL_SIZE);
                    }
                }
            }
        }


    }

    public void randomizeData() {
        Random rand = new Random();
        int n = rand.nextInt(5)+2;
        data = new int[n][n][n];
        for (int x=0; x<data.length; x++) {
            int[][] plane = data[x];
            for (int y = 0; y < plane.length; y++) {
                int[] row = plane[y];
                for (int z = 0; z < row.length; z++) {
                    int voxel = row[z];
                    if (rand.nextBoolean()) {
                        row[z] = 1;
                    }
                }
            }
        }
    }

    public void genWorld() {
        Random rand = new Random();
        float h = rand.nextFloat()-1;
        int n = 14;
        int[][][] newData = new int[n][n][n];
        for (int x=0; x<newData.length; x++) {
            int[][] plane = newData[x];
            for (int y = 0; y < plane.length; y++) {
                int[] row = plane[y];
                for (int z = 0; z < row.length; z++) {
                    if (y*0.8 < Math.cos(x)+Math.sin(z)+2+h) {
                        row[z] = 1;
                    } else if (y*0.66 < Math.cos(x)+Math.sin(z)+2+h) {
                        row[z] = 2;
                    }
                }
            }
        }

        setData(newData);
    }*/
}
