package all.continuous;

import java.util.Comparator;

/**
 * Created by bvsla on 5/23/2017.
 */
public class RobotHeightComparator implements Comparator<Agent> {
    public int compare(Agent x, Agent y){
        if(x.getLocation().getZ() < y.getLocation().getZ()) return -1;
        if(y.getLocation().getZ() < x.getLocation().getZ()) return 1;
        return 0;
    }
}
