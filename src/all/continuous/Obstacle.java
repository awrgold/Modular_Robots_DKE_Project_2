package all.continuous;

import java.util.ArrayList;
import java.util.List;

import javafx.geometry.Point3D;

public class Obstacle extends Cube{

    public Obstacle(float id, Point3D location){
        this.id = id;
        this.location = location;
    }
    
  	//Check if the obstacle is supported by another obstacle underneath it
  	public boolean isSupportedFromBottom(Configuration config, Terrain terrain)
    {
  		ArrayList<Obstacle> alignedObstacles = this.getAlignedObstacles(terrain.getObstacles()); 
  		
		for(int i=0; i<alignedObstacles.size(); i++)
		{
			if(alignedObstacles.get(i).getLocation().getZ() == (this.getLocation().getZ() -1))
				return true;
		}
		return false; 
	}
  	
	//Check if the obstacle has another obstacle on top of it
  	public boolean isSupportedFromTop(Configuration config, Terrain terrain)
	{
  		ArrayList<Obstacle> alignedObstacles = this.getAlignedObstacles(terrain.getObstacles()); 
  		
		for(int i=0; i<alignedObstacles.size(); i++)
		{
			if(alignedObstacles.get(i).getLocation().getZ() == (this.getLocation().getZ() +1))
				return true;
		}
		return false; 
	}
  	
  //From all obstacles in the current configuration, return the ones that are aligned (same x and y)
  	public ArrayList<Obstacle> getAlignedObstacles(List<Obstacle> list)
  	{
  		ArrayList<Obstacle> alignedObstacles = new ArrayList<Obstacle>();
  		
  		for(int i=0 ; i<list.size(); i++)
  		{
  			if(list.get(i).getLocation().getX() == this.getLocation().getX())
  			{
  				if(list.get(i).getLocation().getY() == this.getLocation().getY())
  					alignedObstacles.add(list.get(i));
  			}
  		}
  		
  		return alignedObstacles; 
  	}
}
