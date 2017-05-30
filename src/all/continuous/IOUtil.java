package all.continuous;

import org.lwjgl.BufferUtils;

import javafx.geometry.Point3D;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Roel on 18-03-17.
 */
public final class IOUtil {
	
	/*
	 *  SIMULATION SAVING AND LOADING
	 */
	
	public static void saveSimulation(String path, Simulation sim) throws IOException {
		saveSimulation(path, path+"_goal", path+"_terrain", sim);
	}
	
	public static void saveSimulation(String initialPath, String goalPath, String terrainPath, Simulation sim) throws IOException {
		saveConfig(initialPath, sim.getTimeStep().get(0));
		saveConfig(goalPath, sim.getGoalConfiguration());
		saveTerrain(terrainPath, sim.getTerrain());
	}

	private static void saveConfig(String path, Configuration conf) throws IOException {
		StringBuilder data = new StringBuilder();
		conf.getAgents().stream().forEach((a) -> {
			Point3D pos = a.getLocation();
			data.append(String.format("%s, %s, %s, %s\n", a.getId(), pos.getX(), pos.getY(), pos.getZ()));
		});
		Files.write(Paths.get(path), data.toString().getBytes());
	}

	private static void saveTerrain(String path, Terrain terrain) throws IOException {
		StringBuilder data = new StringBuilder();
		terrain.getObstacles().stream().forEach((a) -> {
			Point3D pos = a.getLocation();
			data.append(String.format("%s, %s, %s, %s\n", a.getId(), pos.getX(), pos.getY(), pos.getZ()));
		});
		Files.write(Paths.get(path), data.toString().getBytes());
	}
	
	public static Simulation loadSimulation(String path) throws IOException {
		return loadSimulation(path, path+"_goal", path+"_terrain");
	}
	
	public static Simulation loadSimulation(String initialPath, String goalPath, String terrainPath) throws IOException {
		Configuration initial = loadConfig(initialPath);
		Configuration goal = loadConfig(goalPath);
		Terrain terrain = loadTerrain(terrainPath);
		
		return new Simulation(terrain, initial, goal);
	}
	
	public static Configuration loadConfig(String path) throws IOException {
		ArrayList<Agent> agents = new ArrayList<>();
		Files.lines(Paths.get(path))
			.map((s) -> Arrays.stream(s.split(",\\s*"))
						.map((column) -> Float.parseFloat(column)).toArray(Float[]::new))
			.forEach((c) -> {agents.add(new Agent(c[0], new Point3D(c[1], c[2], c[3])));});
		return new Configuration(agents);
	}
	
	public static Terrain loadTerrain(String path) throws IOException {
		ArrayList<Obstacle> obstacles = new ArrayList<>();
		Files.lines(Paths.get(path))
			.map((s) -> Arrays.stream(s.split(",\\s*"))
						.map((column) -> Float.parseFloat(column)).toArray(Float[]::new))
			.forEach((c) -> {obstacles.add(new Obstacle(c[0], new Point3D(c[1], c[2], c[3])));});
		return new Terrain(obstacles);
	}

    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    public static String loadFile(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)));
    }

    /**
     * Reads the specified resource and returns the raw data as a ByteBuffer.
     *
     * @param resource   the resource to read
     * @param bufferSize the initial buffer size
     *
     * @return the resource data
     *
     * @throws IOException if an IO error occurs
     */
    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

        Path path = Paths.get(resource);
        if ( Files.isReadable(path) ) {
            try (SeekableByteChannel fc = Files.newByteChannel(path)) {
                buffer = BufferUtils.createByteBuffer((int)fc.size() + 1);
                while ( fc.read(buffer) != -1 ) ;
            }
        } else {
            try (
                    InputStream source = IOUtil.class.getClassLoader().getResourceAsStream(resource);
                    ReadableByteChannel rbc = Channels.newChannel(source)
            ) {
                buffer = BufferUtils.createByteBuffer(bufferSize);

                while ( true ) {
                    int bytes = rbc.read(buffer);
                    if ( bytes == -1 )
                        break;
                    if ( buffer.remaining() == 0 )
                        buffer = resizeBuffer(buffer, buffer.capacity() * 2);
                }
            }
        }

        buffer.flip();
        return buffer;
    }

}