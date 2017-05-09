package ai;
import all.continuous.Agent;
import java.util.Collections;
import java.util.ArrayList;

/**
 * Created by God on the 8th day, and it was good...
 */

@SuppressWarnings("unchecked")

public class PQ {

    private ArrayList<Agent> Q;
    private int qSize;

    public PQ(){
        Q = new ArrayList<>();
    }

    // Initialize the priority queue, placing the agents into the queue UNSORTED
    public void initialize(ArrayList<Agent> agents){
        for(int i = 1; i < agents.size(); i++){
            Q.add(agents.get(i));
        }
        prioritySearch();
    }

    // Sort the list, placing the agents FURTHEST from the current GOAL at the *TOP* of the list
    public void prioritySearch(){
        Agent priority = Q.get(0);
        for(int i = 0; i < Q.size(); i++){
            if (Q.get(i).getManhattanDistanceTo(aStar.getGoal()) >= priority.getManhattanDistanceTo(aStar.getGoal())){
                priority = Q.get(i);
            }
        }
    }

    public void priorityCompare(){
        int in, out;
        for (out = Q.size() - 1; out > 0; out--){
            for(in = 0; in < out; in++){
                if(Q.get(in).getManhattanDistanceTo(aStar.getGoal()) > Q.get(in + 1).getManhattanDistanceTo(aStar.getGoal())){
                    swap(in, in + 1);
                }
            }
        }
    }

    public Agent getPriority(){
        return Q.get(0);
    }

    public Agent getAgentAtPos(ArrayList<Agent> agents, int index){
        return agents.get(index);
    }

    public int getQSize(){
        return Q.size();
    }

    private void swap(int one, int two){
        Collections.swap(Q, one, two);
    }


}
