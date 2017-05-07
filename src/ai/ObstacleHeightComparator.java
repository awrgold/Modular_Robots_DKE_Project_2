package ai;

import java.util.Comparator;

import all.continuous.Obstacle;

public class ObstacleHeightComparator implements Comparator<Obstacle>
{

	@Override
	public int compare(Obstacle o1, Obstacle o2) {
		
		if(o1.getLocation().getZ() > o2.getLocation().getZ())
		{
			return 1; 
		}
		
		else if(o1.getLocation().getZ() == o2.getLocation().getZ())
		{
			return 0;
		}
		
		else
		{
			return -1; 
		}
	}
	
}