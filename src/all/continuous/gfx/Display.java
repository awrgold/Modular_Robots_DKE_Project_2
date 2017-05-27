package all.continuous.gfx;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.lwjgl.nuklear.NkAllocator;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkMouse;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;
import org.lwjgl.system.MemoryStack;

import ai.AStarAlgorithm;
import ai.AStarGreedyAlgorithm;
import ai.CooperativeAStar;
import ai.SimpleAI;
import all.continuous.Agent;
import all.continuous.Configuration;
import all.continuous.GreedyAlgorithm;
import all.continuous.RandomAlgorithm;
import all.continuous.Simulation;
import all.continuous.exceptions.DisplayInitException;
import all.continuous.exceptions.InvalidStateException;
import all.continuous.exceptions.ShaderException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

public class Display {
	private final static Logger LOGGER = Logger.getLogger(Display.class.getName());
	
	private static Display display;

    public static Display getInstance() {
		if (display == null)
			display = new Display();
		return display;
	}
	
	private Display() {}
	
	private long windowHandle;

    public GLCapabilities caps;
	
	public void show() {
		glfwShowWindow(windowHandle);
		
		caps = GL.createCapabilities();
		
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
	}
	
	public void init(int width, int height, String title) throws DisplayInitException {
		GLFWErrorCallback.createPrint(System.err).set();
		
		if (!glfwInit()) {
			LOGGER.log(Level.SEVERE, "GLFW could not be initialized");
			throw new DisplayInitException();
		}
		
		glfwWindowHint(GLFW_STENCIL_BITS, 4);
		glfwWindowHint(GLFW_SAMPLES, 4);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
		glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
		glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
		glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
		glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
		
		windowHandle = glfwCreateWindow(width, height, title, NULL, NULL);
		if (windowHandle == NULL) {
			LOGGER.log(Level.SEVERE, "Window could not be created");
			throw new DisplayInitException();
		}
		
		glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
			if (this.keyCallback != null) this.keyCallback.invoke(window, key, scancode, action, mods);
		});
		
		glfwMakeContextCurrent(windowHandle);
		glfwSwapInterval(1);
	}

	public boolean isKeyDown(int key) {
	    return glfwGetKey(windowHandle, key) == GLFW_PRESS;
    }

    public boolean isButtonDown(int button) {
	    return glfwGetMouseButton(windowHandle, button) == GLFW_PRESS;
    }

    public void setMouseLocked(boolean b) {
	    if (b) {
	        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        }
	    else glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

	public void update() {
        glfwSwapBuffers(windowHandle);
    }

	public void input(UIContext uiContext) {
		if (uiContext != null) {
		    NkContext ctx = uiContext.getContext();
            try ( MemoryStack stack = stackPush() ) {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);

                glfwGetWindowSize(windowHandle, w, h);
                uiContext.width = w.get(0);
                uiContext.height = h.get(0);

                glfwGetFramebufferSize(windowHandle, w, h);
                uiContext.display_width = w.get(0);
                uiContext.display_height = h.get(0);
            }

            nk_input_begin(ctx);
            glfwPollEvents();

            NkMouse mouse = ctx.input().mouse();
            if ( mouse.grab() )
                glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
            else if ( mouse.grabbed() ) {
                float prevX = mouse.prev().x();
                float prevY = mouse.prev().y();
                glfwSetCursorPos(windowHandle, prevX, prevY);
                mouse.pos().x(prevX);
                mouse.pos().y(prevY);
            } else if ( mouse.ungrab() )
                glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);

            nk_input_end(ctx);
        } else {
            glfwPollEvents();


        }
        try ( MemoryStack stack = stackPush() ) {
            IntBuffer width = stack.mallocInt(1);
            IntBuffer height = stack.mallocInt(1);

            glfwGetWindowSize(windowHandle, width, height);
            glViewport(0, 0, width.get(0), height.get(0));
        }
	}

	public void destroy() {
		glfwDestroyWindow(windowHandle);
	}

	public boolean shouldClose() {
		return glfwWindowShouldClose(windowHandle);
	}
	
	public void setSize(Vector2f size) {
		glfwSetWindowSize(windowHandle, (int) size.x, (int) size.y);
		updateViewport();
		
		MVP.ortho(this);
	}

	public void updateViewport() {
		try ( MemoryStack stack = stackPush() ) {
			IntBuffer w = stack.mallocInt(1);
			IntBuffer h = stack.mallocInt(1);

			glfwGetFramebufferSize(windowHandle, w, h);
			glViewport(0, 0, w.get(0), h.get(0));
		}
	}

    public Vector2f getMouseCoords(Vector2f coords) {
	    try (MemoryStack stack = stackPush()) {
	        DoubleBuffer x = stack.mallocDouble(1);
            DoubleBuffer y = stack.mallocDouble(1);

            glfwGetCursorPos(windowHandle, x, y);
            coords.x = (float)x.get(0);
            coords.y = (float)y.get(0);
        }
        return coords;
    }

	public Vector2f getSize() {
		Vector2f size = new Vector2f();
		try (MemoryStack stack = stackPush()) {
			IntBuffer pWidth = stack.mallocInt(1);
			IntBuffer pHeight = stack.mallocInt(1);

			glfwGetWindowSize(windowHandle, pWidth, pHeight);
			
			size.x = pWidth.get(0);
			size.y = pHeight.get(0);
		}
		return size;
	}

	public NkContext genNKContext(UIContext uiContext) {
		/*
		 * Copyright LWJGL. All rights reserved.
		 * License terms: https://www.lwjgl.org/license
		 */
	    NkAllocator ALLOCATOR = uiContext.ALLOCATOR;
		NkContext ctx = NkContext.create();
		glfwSetScrollCallback(windowHandle, (window, xoffset, yoffset) -> {
			if (!uiContext.isActive() && this.scrollCallback != null) this.scrollCallback.invoke(window, xoffset, yoffset);
			nk_input_scroll(ctx, (float)yoffset);
		});
		glfwSetCharCallback(windowHandle, (window, codepoint) -> nk_input_unicode(ctx, codepoint));
		glfwSetKeyCallback(windowHandle, (window, key, scancode, action, mods) -> {
			if (this.keyCallback != null) this.keyCallback.invoke(window, key, scancode, action, mods);
			boolean press = action == GLFW_PRESS;
			switch ( key ) {
				case GLFW_KEY_DELETE:
					nk_input_key(ctx, NK_KEY_DEL, press);
					break;
				case GLFW_KEY_ENTER:
					nk_input_key(ctx, NK_KEY_ENTER, press);
					break;
				case GLFW_KEY_TAB:
					nk_input_key(ctx, NK_KEY_TAB, press);
					break;
				case GLFW_KEY_BACKSPACE:
					nk_input_key(ctx, NK_KEY_BACKSPACE, press);
					break;
				case GLFW_KEY_UP:
					nk_input_key(ctx, NK_KEY_UP, press);
					break;
				case GLFW_KEY_DOWN:
					nk_input_key(ctx, NK_KEY_DOWN, press);
					break;
				case GLFW_KEY_HOME:
					nk_input_key(ctx, NK_KEY_TEXT_START, press);
					nk_input_key(ctx, NK_KEY_SCROLL_START, press);
					break;
				case GLFW_KEY_END:
					nk_input_key(ctx, NK_KEY_TEXT_END, press);
					nk_input_key(ctx, NK_KEY_SCROLL_END, press);
					break;
				case GLFW_KEY_PAGE_DOWN:
					nk_input_key(ctx, NK_KEY_SCROLL_DOWN, press);
					break;
				case GLFW_KEY_PAGE_UP:
					nk_input_key(ctx, NK_KEY_SCROLL_UP, press);
					break;
				case GLFW_KEY_LEFT_SHIFT:
				case GLFW_KEY_RIGHT_SHIFT:
					nk_input_key(ctx, NK_KEY_SHIFT, press);
					break;
				case GLFW_KEY_LEFT_CONTROL:
				case GLFW_KEY_RIGHT_CONTROL:
					if ( press ) {
						nk_input_key(ctx, NK_KEY_COPY, glfwGetKey(window, GLFW_KEY_C) == GLFW_PRESS);
						nk_input_key(ctx, NK_KEY_PASTE, glfwGetKey(window, GLFW_KEY_P) == GLFW_PRESS);
						nk_input_key(ctx, NK_KEY_CUT, glfwGetKey(window, GLFW_KEY_X) == GLFW_PRESS);
						nk_input_key(ctx, NK_KEY_TEXT_UNDO, glfwGetKey(window, GLFW_KEY_Z) == GLFW_PRESS);
						nk_input_key(ctx, NK_KEY_TEXT_REDO, glfwGetKey(window, GLFW_KEY_R) == GLFW_PRESS);
						nk_input_key(ctx, NK_KEY_TEXT_WORD_LEFT, glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS);
						nk_input_key(ctx, NK_KEY_TEXT_WORD_RIGHT, glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS);
						nk_input_key(ctx, NK_KEY_TEXT_LINE_START, glfwGetKey(window, GLFW_KEY_B) == GLFW_PRESS);
						nk_input_key(ctx, NK_KEY_TEXT_LINE_END, glfwGetKey(window, GLFW_KEY_E) == GLFW_PRESS);
					} else {
						nk_input_key(ctx, NK_KEY_LEFT, glfwGetKey(window, GLFW_KEY_LEFT) == GLFW_PRESS);
						nk_input_key(ctx, NK_KEY_RIGHT, glfwGetKey(window, GLFW_KEY_RIGHT) == GLFW_PRESS);
						nk_input_key(ctx, NK_KEY_COPY, false);
						nk_input_key(ctx, NK_KEY_PASTE, false);
						nk_input_key(ctx, NK_KEY_CUT, false);
						nk_input_key(ctx, NK_KEY_SHIFT, false);
					}
					break;
			}
		});
		glfwSetCursorPosCallback(windowHandle, (window, xpos, ypos) -> nk_input_motion(ctx, (int)xpos, (int)ypos));
		glfwSetMouseButtonCallback(windowHandle, (window, button, action, mods) -> {
			if (!uiContext.isActive() && this.mouseCallback != null) this.mouseCallback.invoke(window, button, action, mods);
			try ( MemoryStack stack = stackPush() ) {
				DoubleBuffer cx = stack.mallocDouble(1);
				DoubleBuffer cy = stack.mallocDouble(1);

				glfwGetCursorPos(window, cx, cy);

				int x = (int)cx.get(0);
				int y = (int)cy.get(0);

				int nkButton;
				switch ( button ) {
					case GLFW_MOUSE_BUTTON_RIGHT:
						nkButton = NK_BUTTON_RIGHT;
						break;
					case GLFW_MOUSE_BUTTON_MIDDLE:
						nkButton = NK_BUTTON_MIDDLE;
						break;
					default:
						nkButton = NK_BUTTON_LEFT;
				}
				nk_input_button(ctx, nkButton, x, y, action == GLFW_PRESS);
			}
		});

		nk_init(ctx, ALLOCATOR, null);
		ctx.clip().copy((handle, text, len) -> {
			if ( len == 0 )
				return;

			try ( MemoryStack stack = stackPush() ) {
				ByteBuffer str = stack.malloc(len + 1);
				memCopy(text, memAddress(str), len);
				str.put(len, (byte)0);

				glfwSetClipboardString(windowHandle, str);
			}
		});
		ctx.clip().paste((handle, edit) -> {
			long text = nglfwGetClipboardString(windowHandle);
			if ( text != NULL )
				nnk_textedit_paste(edit, text, nnk_strlen(text));
		});

		return ctx;
	}
	
	private GLFWKeyCallbackI keyCallback;
	private GLFWMouseButtonCallbackI mouseCallback;
	private GLFWScrollCallbackI scrollCallback;

	public void setKeyListener(GLFWKeyCallbackI callback) {
		this.keyCallback = callback;
	}
	
	public void setMouseButtonListener(GLFWMouseButtonCallbackI callback) {
		this.mouseCallback = callback;
	}
	
	public void setScrollListener(GLFWScrollCallbackI callback) {
		this.scrollCallback = callback;
	}

	public long getWindowHandle() {
		return this.windowHandle;
	}
	
	// TODO: Clean this shit up
	public static Simulation sim = null;
	public static boolean computing = false;
	public static PlayerWindow window;
	
	public static Thread currentThread;

	public static String error = "";
	
	public static void main(String[] args) {
        Display disp = null;
		try {
            // Retrieve the display instance
            disp = Display.getInstance();
            // Initialize the display
            disp.init(640, 480, "Test");
            // Show and activate the display
            disp.show();

            // Load tow basic shaders
            ShaderManager.getInstance().addShader("basic", "vert.glsl", "frag.glsl");
            ShaderManager.getInstance().addShader("phong", "phong_vert.glsl", "phong_frag.glsl");

            // Initialize the ui context
            UIContext cont = new UIContext();

            // Set the projection matrix to a perspective projection matrix
            MVP.perspective(60, 640.0f / 480.0f);

            // Randomly initialize the voxel renderer (which will render our voxel world)
            VoxelRenderer vr = new VoxelRenderer();
            //vr.randomizeData();

            // Create a controllable camera

            // Set the clear colour to something that sort of resembles the sky
            glClearColor(0.1f, 0.6f, 0.9f, 1.0f);
            
            WorldRenderer wr = new WorldRenderer();
            
            
            
            // Add a window to the ui
			window = new PlayerWindow();
			Runnable simCalcFunc = () -> {
				try {
					sim = wr.createSimulation();
					sim.setAlgorithm(new AStarAlgorithm(sim));
					sim.run();
					window.max = sim.getTimeStep().size()-1;
					wr.animateTo(sim.getTimeStep().get(0));
				} catch (Exception e) {
					e.printStackTrace();
					error = e.getClass().getName() + ": " + e.getMessage();
				} finally {
					computing = false;
				}
			};
			Thread current;
			window.setCallback(() -> {
				computing = true;
				currentThread = new Thread(simCalcFunc);
				currentThread.start();
			});
			window.setCallbackStop(() -> {
				currentThread.interrupt();
				computing = false;
			});
            cont.addWindow(window);
            
            

            int lastCurrent = -1;

            // Loop until the display is closed
            while (!disp.shouldClose()) {
                // Retrieve input (and relay it to the UI context)
                disp.input(cont);

                // Clear the colour and depth buffers
                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

                // Reset gl state (get messed up by the ui context)
                ShaderManager.getInstance().setShader("phong");
                disp.updateViewport();
                glEnable(GL_DEPTH_TEST);

                if (!computing) {
	                if (lastCurrent != window.getCurrent() && sim != null)
	                	wr.animateTo(sim.getTimeStep().get(window.getCurrent()));
					lastCurrent = window.getCurrent();
                }

                // Use user input to move the camera and set the view matrix
                // accordingly
                // Render the world
                //vr.render();
				
				
				if (!cont.isActive())
					wr.input();
				wr.update();
				
				glEnable(GL_BLEND);
				glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
				glEnable(GL_CULL_FACE);  
				glCullFace(GL_FRONT);
				wr.render();

                // Render the ui
                cont.render();
                // Swap buffers
                disp.update();
            }
        } catch (DisplayInitException e) {
            e.printStackTrace();
        } catch (ShaderException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        	if (currentThread != null) currentThread.interrupt();
            // Destroy the display if necessary
            if (disp != null) disp.destroy();
            
            System.exit(0);
        }
    }

	public float getAspectRatio() {
		Vector2f size = getSize();
		return size.x/size.y;
	}


}
