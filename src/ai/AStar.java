package ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Set;

public class AStar {

	@FunctionalInterface
	interface AStarScoreCalculator<T> {
		public float calculate(T node, T goal);
	}

	static class Neighbour<T> {
		public final T node;
		public final float distance;
		public Neighbour(T node, float distance) {
			this.node = node;
			this.distance = distance;
		}
	}

	@FunctionalInterface
	interface AStarNeighbourFinder<T> {
		public List<Neighbour<T>> findNeighbours(T node);
	}

	public static <T> List<T> aStar(AStarScoreCalculator<T> heuristic, AStarNeighbourFinder<T> neighFind, T start, T goal) {
		Set<T> visited = new HashSet<>();

		Map<T, T> cameFrom = new HashMap<>();
		Map<T, Float> gScore = new HashMap<>();
		Map<T, Float> fScore = new HashMap<>();
		
		PriorityQueue<T> visible = new PriorityQueue<>((a, b) -> fScore.get(a) < fScore.get(b) ? -1 : 1);
		Set<T> visibleSet = new HashSet<>();

		visible.add(start);
		gScore.put(start, 0.0f);
		fScore.put(start, heuristic.calculate(start, goal));

		int iter = 0;
		while (!visible.isEmpty()) {
			T current = visible.poll();
			if (current.equals(goal))
				break;

			visibleSet.remove(current);
			visible.remove(current);
			visited.add(current);

			for (Neighbour<T> neighbour : neighFind.findNeighbours(current)) {
				if (visited.contains(neighbour.node))
					continue;

				float score = gScore.get(current) + neighbour.distance;
				
				if (visibleSet.contains(neighbour.node) && gScore.containsKey(neighbour.node) && score >= gScore.get(neighbour.node))
					continue;

				cameFrom.put(neighbour.node, current);
				gScore.put(neighbour.node, score);
				fScore.put(neighbour.node, score + heuristic.calculate(neighbour.node, goal));
				
				if (!visibleSet.contains(neighbour.node)) {
					visibleSet.add(neighbour.node);
					visible.add(neighbour.node);
				}
			}

//			Configuration conf = (Configuration) current;
//			for (Agent a : conf.getAgents()) {
//				dos.writeUTF(a.getIndex() + ":" + a.getLocation() + ", ");
//			}
//			dos.writeUTF("\n");

			iter++;

						if (iter > 9000)
							break;

			System.out.println(iter);
		}

		Optional<T> finalGoal = cameFrom.containsKey(goal) ? cameFrom.keySet().stream().filter((node) -> node.equals(goal)).findFirst() : 
			cameFrom.keySet().stream().reduce((a, b) -> heuristic.calculate(a, goal) < heuristic.calculate(b, goal) ? a : b);
		if (!finalGoal.isPresent()) return new ArrayList<>();
		T current = finalGoal.get();
		List<T> path = new ArrayList<>();
		while (current != start) {
			path.add(current);
			current = cameFrom.get(current);
		}

		path.add(start);
		Collections.reverse(path);

		return path;
	}
}
