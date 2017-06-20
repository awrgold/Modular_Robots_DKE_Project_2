package all.continuous;

import javafx.geometry.Point3D;

import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.List;

public class Agent extends Cube {
    private boolean moved = false;
    private double speed;
    private Point3D intermediateGoal;
    private ArrayList<Point3D> path;

    public Agent(float id, Point3D location){
    	super(1);
        this.id = id;
        this.location = location;
        setPosition(new Vector3d(location.getX(), location.getY(), location.getZ()));
        
        this.intermediateGoal = null;
        this.path=new ArrayList<Point3D>();
        path.add(location);
    }

    public Agent copy(){
    	Vector3d pos = getPosition();
        Agent newAgent = new Agent(this.id, new Point3D(Math.round(pos.x*10.0)/10.0, Math.round(pos.y*10.0)/10.0, Math.round(pos.z*10.0)/10.0));
        newAgent.index = this.index;
        newAgent.intermediateGoal = this.intermediateGoal;
        newAgent.path = this.path;
        newAgent.setVelocity(getVelocity());
        return newAgent;
    }

    public void move(Point3D location) throws InvalidMoveException{
    	if(location == null) throw new InvalidMoveException("Moving into 'null', somehow");
        if(!moved){
            this.location = location;
            this.moved = true;
        } else throw new InvalidMoveException("Tried to move an agent that has already moved this turn!");
    }
    
    @Override
    public String toString() {
    	return String.format("Agent %s at %s", id, location);
    }

    public boolean hasMoved(){return moved;};

    public void setLocation(Point3D location){
        this.location = location;
    }
    
    public double getSpeed()
    {
    	return speed; 
    }

    public void setSpeed(double speed)
    {
    	this.speed=speed; 
    }

    public Point3D getIntermediateGoal(){
    	return intermediateGoal;
	}

	public void setIntermediateGoal(Point3D goal){
    	intermediateGoal = goal;
	}
	
	public void addPath(Point3D location)
	{
		this.path.add(location);
	}
	
	public ArrayList<Point3D> getPath()
	{
		return path;
	}
    
    /*THE NEXT 6 METHODS ARE USED FOR THE GRAVITY FALL //see physics class*/
  	
  	//Check if the agent is supported by another agent or an obstacle underneath it
  	public boolean isSupportedFromBottom(Configuration config, Terrain terrain)
    {
  		ArrayList<Agent> alignedAgents = this.getAlignedAgents(config.getAgents()); 
  		ArrayList<Obstacle> alignedObstacles = this.getAlignedObstacles(terrain.getObstacles()); 
  		
		for(int i=0; i<alignedAgents.size(); i++)
		{
			if(alignedAgents.get(i).getLocation().getZ() == (this.getLocation().getZ() -1))
				return true;
		}
		for(int i=0; i<alignedObstacles.size(); i++)
		{
			if(alignedObstacles.get(i).getLocation().getZ() == (this.getLocation().getZ() -1))
				return true;
		}
		return false; 
	}
	
  	//Check if the agent has another agent or an obstacle on top of it
  	public boolean isSupportedFromTop(Configuration config, Terrain terrain)
	{
  		ArrayList<Agent> alignedAgents = this.getAlignedAgents(config.getAgents()); 
  		ArrayList<Obstacle> alignedObstacles = this.getAlignedObstacles(terrain.getObstacles()); 
  		
		for(int i=0; i<alignedAgents.size(); i++)
		{
			if(alignedAgents.get(i).getLocation().getZ() == (this.getLocation().getZ() +1))
				return true;
		}
		for(int i=0; i<alignedObstacles.size(); i++)
		{
			if(alignedObstacles.get(i).getLocation().getZ() == (this.getLocation().getZ() +1))
				return true;
		}
		return false; 
	}
  	
  //Check if the agent is supported by another agent or an obstacle underneath it and return that cube
  	public ArrayList<Cube> getBottomCubes(Configuration config, Terrain terrain)
	{
  		ArrayList<Agent> alignedAgents = this.getAlignedAgents(config.getAgents()); 
  		ArrayList<Obstacle> alignedObstacles = this.getAlignedObstacles(terrain.getObstacles()); 
  		
  		ArrayList<Cube> bottom = new ArrayList<Cube>();
  		
		for(int i=0; i<alignedAgents.size(); i++)
		{
			if(alignedAgents.get(i).getLocation().getZ() == (this.getLocation().getZ() -1))
				bottom.add(alignedAgents.get(i));
		}
		for(int i=0; i<alignedObstacles.size(); i++)
		{
			if(alignedObstacles.get(i).getLocation().getZ() == (this.getLocation().getZ() -1))
				bottom.add(alignedObstacles.get(i));
		}
		return bottom;
	}
	
  	//Check if the agent has another agent or an obstacle on top of it and return that cube
  	public ArrayList<Cube> getTopCubes(Configuration config, Terrain terrain)
	{
  		ArrayList<Agent> alignedAgents = this.getAlignedAgents(config.getAgents()); 
  		ArrayList<Obstacle> alignedObstacles = this.getAlignedObstacles(terrain.getObstacles()); 
  		
  		ArrayList<Cube> top = new ArrayList<Cube>();
  		
		for(int i=0; i<alignedAgents.size(); i++)
		{
			if(alignedAgents.get(i).getLocation().getZ() == (this.getLocation().getZ() +1))
				top.add(alignedAgents.get(i));
		}
		for(int i=0; i<alignedObstacles.size(); i++)
		{
			if(alignedObstacles.get(i).getLocation().getZ() == (this.getLocation().getZ() +1))
				top.add(alignedObstacles.get(i));
		}
		return top; 
	}
  	
  	//From all agents in the current configuration, return the ones that are aligned (same x and y)
  	public ArrayList<Agent> getAlignedAgents(ArrayList<Agent> agents)
  	{
  		ArrayList<Agent> alignedAgents = new ArrayList<Agent>();
  		
  		for(int i=0 ; i<agents.size(); i++)
  		{
  			if(agents.get(i).getLocation().getX() == this.getLocation().getX())
  			{
  				if(agents.get(i).getLocation().getY() == this.getLocation().getY())
  					alignedAgents.add(agents.get(i));
  			}
  		}
  		return alignedAgents; 
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
  	
  	public double getTouchBottom(Configuration config, Terrain terrain)
  	{
  		if(this.isSupportedFromBottom(config, terrain))
  		{
  			ArrayList<Cube> bottom = this.getBottomCubes(config, terrain); 
  			double area=0; 
  			//for each cube underneath it
  			for(int i=0; i<bottom.size(); i++)
  			{
  				//X-difference
  	  			double x = Math.abs(bottom.get(i).getLocation().getX() - this.getLocation().getX()); 
  	  			//take the part where the cubes match (size of cube ==1)
  	  			x = (1-x);
  	  			
  	  			//Y-difference
  	  			double y = Math.abs(bottom.get(i).getLocation().getY() - this.getLocation().getY());
  	  			//take the part where the cubes match (size of cube ==1)
  	  			y=(1-y); 
  	  			
  	  			area+=(x*y);
  			}
  			
  			return area; 
  		
  		}
  		
  		else 
  			return 0; 
  	}
  	
  	public double getTouchTop(Configuration config, Terrain terrain)
  	{
  		if(this.isSupportedFromTop(config, terrain))
  		{
  			ArrayList<Cube> top = this.getTopCubes(config, terrain); 
  			double area=0; 
  			//for each cube underneath it
  			for(int i=0; i<top.size(); i++)
  			{
  				//X-difference
  	  			double x = Math.abs(top.get(i).getLocation().getX() - this.getLocation().getX()); 
  	  			//take the part where the cubes match (size of cube ==1)
  	  			x = (1-x);
  	  			
  	  			//Y-difference
  	  			double y = Math.abs(top.get(i).getLocation().getY() - this.getLocation().getY());
  	  			//take the part where the cubes match (size of cube ==1)
  	  			y=(1-y); 
  	  			
  	  			area+=(x*y);
  			}
  			
  			return area; 
  		
  		}
  		else 
  			return 0; 
    }

    //TODO: Turn this into a real method. Class *aStar* should have a Point3D location to pass as a goal.
    public double getManhattanDistanceTo(Point3D goal){
  		double distance = 0;

  		double xDiff = Math.abs(goal.getX()-this.location.getX());
  		double yDiff = Math.abs(goal.getY()-this.location.getY());
  		double zDiff = Math.abs(goal.getZ()-this.location.getZ());

  		distance += (xDiff + yDiff + zDiff);

  		return distance;
	}

	public boolean isConnected(Configuration conf) {
		for (Point3D dir : Direction.DIRECTIONS) {
			CollisionUtil.Collision c = CollisionUtil.castRayCube(conf, new Ray(this.location, dir), 0.24, 1.0, 0.0, this);
			if (c.type == CollisionType.AGENT)
				return true;
		}
		return false;
	}

}
