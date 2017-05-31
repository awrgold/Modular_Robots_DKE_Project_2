package all.continuous.gfx;

import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;

import all.continuous.ModuleAlgorithm;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

/**
 * Created by Roel on 18-03-17.
 */
public class AlgorithmWindow implements UIWindow {
	
	private List<Class> algorithms = new ArrayList<>();
	private int index = 0;
	
	public AlgorithmWindow() {
	}
	
	public <T extends ModuleAlgorithm> void addAlgorithm(Class<T> algorithm) {
		this.algorithms.add(algorithm);
	}

	@Override
	public void layout(NkContext ctx) {
		String title = "Algorithm";

		try ( MemoryStack stack = stackPush() ) {
			NkRect rect = NkRect.mallocStack(stack);
			
			if ( nk_begin(
					ctx,
					title, 
					nk_rect(Display.getInstance().getSize().x - 260, 260, 240, 280, rect),
					NK_WINDOW_BORDER | NK_WINDOW_MOVABLE | NK_WINDOW_SCALABLE | NK_WINDOW_MINIMIZABLE | NK_WINDOW_TITLE
					) ) {
				
			    for (int i=0; i<algorithms.size(); i++) {
			    	nk_layout_row_dynamic(ctx, 30, 1);
			    	if (nk_option_label(ctx, algorithms.get(i).getSimpleName(), i == index)) index = i;
			    }
			}
			nk_end(ctx);
		}
	}

	public Class getCurrent() {
		return this.algorithms.get(index);
	}



}