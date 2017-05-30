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
public class DisplayWindow implements UIWindow {

	public boolean renderModules = 	 true;
	public boolean renderGoals = 	 true;
	public boolean renderInits = 	 false;
	public boolean renderObstacles = true;
	
	public DisplayWindow() {
	}

	@Override
	public void layout(NkContext ctx) {
		String title = "Display";

		try ( MemoryStack stack = stackPush() ) {
			NkRect rect = NkRect.mallocStack(stack);
			
			if ( nk_begin(
					ctx,
					title, 
					nk_rect(Display.getInstance().getSize().x - 260, 20, 240, 280, rect),
					NK_WINDOW_BORDER | NK_WINDOW_MOVABLE | NK_WINDOW_SCALABLE | NK_WINDOW_MINIMIZABLE | NK_WINDOW_TITLE
					) ) {
				
				nk_layout_row_dynamic(ctx, 30, 1);
			    renderModules = nk_check_label(ctx, "Modules", renderModules);
			    nk_layout_row_dynamic(ctx, 30, 1);
			    renderGoals = nk_check_label(ctx, "Goals", renderGoals);
			    nk_layout_row_dynamic(ctx, 30, 1);
			    renderInits = nk_check_label(ctx, "Inits", renderInits);
			    nk_layout_row_dynamic(ctx, 30, 1);
			    renderObstacles =nk_check_label(ctx, "Obstacles", renderObstacles);

			}
			nk_end(ctx);
		}
	}



}