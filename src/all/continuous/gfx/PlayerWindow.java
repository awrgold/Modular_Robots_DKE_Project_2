package all.continuous.gfx;

import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.system.MemoryStack.stackPush;

import org.lwjgl.glfw.GLFW;

/**
 * Created by Roel on 18-03-17.
 */
public class PlayerWindow implements UIWindow {
	private Runnable onRun;
	private Runnable onStop;

	private int speed = 1;
	private int slider;

	int max;
	private int t;
	private boolean playing;
	
	ObjectType type = ObjectType.MODULE;

	public PlayerWindow() {
	}

	public void setCallback(Runnable onRun) {
		this.onRun = onRun;
	}

	public void setCallbackStop(Runnable onStop) {
		this.onStop = onStop;
	}
	
	public int getCurrent() {
		return slider;
	}
	
	public ObjectType getType() {
		return type;
	}

	@Override
	public void layout(NkContext ctx, int x, int y) {
		if (playing) { // TODO: Use delta time you fuckin' idiot
			t++;
			if (t % (10/speed) == 0) {
				if (slider < max) {
					slider += 1;
				} else {
					slider = max;
					playing = false;
				}
			}
		}
		
		String title = "Player";

		try ( MemoryStack stack = stackPush() ) {
			NkRect rect = NkRect.mallocStack(stack);
			
			if ( nk_begin(
					ctx,
					title, 
					nk_rect(x, y, 240, 280, rect),
					NK_WINDOW_BORDER | NK_WINDOW_MOVABLE | NK_WINDOW_SCALABLE | NK_WINDOW_MINIMIZABLE | NK_WINDOW_TITLE
					) ) {
				
				nk_layout_row_dynamic(ctx, 10, 1);
				nk_layout_row_dynamic(ctx, 30, 3);
				if (nk_button_text(ctx, "Prev")) {
					if (slider > 0) slider--;
				}
				nk_label(ctx, " Frame: " + slider, NK_TEXT_LEFT);
				if (nk_button_text(ctx, "Next")) {
					if (slider < max-1) slider++;
				}
				nk_layout_row_dynamic(ctx, 10, 1);
				nk_layout_row_dynamic(ctx, 30, 3);
				if (nk_button_text(ctx, "Slower")) {
					if (speed > 1) speed--;
				}
				nk_label(ctx, " Speed: " + speed, NK_TEXT_LEFT);
				if (nk_button_text(ctx, "Faster")) {
					if (speed < 10) speed++;
				}
				nk_layout_row_dynamic(ctx, 10, 1);
				nk_layout_row_dynamic(ctx, 50, 1);
				if (nk_button_text(ctx, playing ? "Stop" : "Play")) {
					playing = !playing;
					t = 0;
				}
				nk_layout_row_dynamic(ctx, 10, 1);
				nk_layout_row_dynamic(ctx, 35, 1);
				if (nk_button_text(ctx, "Reset")) {
					playing = false;
					t = 0;
					slider = 0;
				}
				
				nk_layout_row_dynamic(ctx, 30, 3);
			    if (nk_option_label(ctx, "mod", type == ObjectType.MODULE)) type = ObjectType.MODULE;
			    if (nk_option_label(ctx, "goal", type == ObjectType.GOAL)) type = ObjectType.GOAL;
			    if (nk_option_label(ctx, "obs", type == ObjectType.OBSTACLE)) type = ObjectType.OBSTACLE;

				nk_layout_row_dynamic(ctx, 10, 1);
				nk_layout_row_dynamic(ctx, 35, 1);
				if (nk_button_text(ctx, "Compute")) {
					playing = false;
					t = 0;
					slider = 0;
					this.onRun.run();
				}
				
				nk_layout_row_dynamic(ctx, 10, 1);
				nk_layout_row_dynamic(ctx, 35, 1);
				if (nk_button_text(ctx, "Stop")) {
					playing = false;
					t = 0;
					slider = 0;
					this.onStop.run();
				}
				
				nk_layout_row_dynamic(ctx, 10, 1);
				nk_layout_row_dynamic(ctx, 35, 1);
				nk_label(ctx, Display.computing ? "COMPUTING!" : "", NK_TEXT_ALIGN_LEFT);
				nk_layout_row_dynamic(ctx, 10, 1);
				nk_layout_row_dynamic(ctx, 35, 1);
				nk_label(ctx, Display.error, NK_TEXT_ALIGN_LEFT);
			}
			nk_end(ctx);
		}
	}



}