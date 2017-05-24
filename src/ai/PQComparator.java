package ai;

import java.util.Comparator;
import all.continuous.*;

/**
 * Created by God on the 8th day, and it was good...
 */
public class PQComparator implements Comparator<Agent> {

    public int compare(Agent one, Agent two){
        double distOne = one.getManhattanDistanceTo(one.getIntermediateGoal());
        double distTwo = two.getManhattanDistanceTo(two.getIntermediateGoal());
        if(distOne > distTwo){
            return 1;
        }
        else if(distOne == distTwo){
            return 0;
        }
        else return -1;
    }

}
