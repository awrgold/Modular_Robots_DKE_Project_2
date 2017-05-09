package all.continuous.gfx;

import java.io.IOException;
import java.util.HashMap;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import all.continuous.IOUtil;
import all.continuous.exceptions.ShaderException;

public class ShaderManager {
	private final static Logger LOGGER = Logger.getLogger(ShaderManager.class.getName());
	
	private static ShaderManager shaderMan;
	public static ShaderManager getInstance() {
		if (shaderMan == null)
			shaderMan = new ShaderManager();
		return shaderMan;
	}
	
	private ShaderManager() {}
	
	private Stack<String> shaderStack = new Stack<>();
	
	private HashMap<String, ShaderProgram> shaders = new HashMap<>();
	private String currentShader;
	
	public void addShader(String key, ShaderProgram program) {
		shaders.put(key, program);
		LOGGER.log(Level.INFO, String.format("Added shader %s", key));
	}
	
	public void addShader(String key, String vertPath, String fragPath) throws ShaderException, IOException {
		addShader(key, new ShaderProgram(IOUtil.loadFile(vertPath), IOUtil.loadFile(fragPath)));
		
		String lastShader = currentShader;
		setShader(key);
		if (lastShader != null) {
			setShader(lastShader);
		}
	}
	
	public void setShader(String key) {
		this.currentShader = key;
		getShader().bind();
	}
	
	public String getShaderKey() {
		return this.currentShader;
	}
	
	public ShaderProgram getShader() {
		return shaders.get(this.currentShader);
	}
	
	public void pushShader(String newShader) {
		shaderStack.push(this.currentShader);
		setShader(newShader);
	}
	
	public void popShader() {
		setShader(shaderStack.pop());
	}
}
