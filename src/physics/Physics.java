package physics;

import java.util.ArrayList;

import all.continuous.*;
import javafx.geometry.Point3D;

public class Physics
{
	static final double TIMESHOT = 0.1; 
	static final double GRAVITY = 9.81;
	static final double STATFRICTION = 0.2; 
	static final double KINFRICTION = 0.3; 
	
	
	/*On a specific configuration, make all the agents that need to fall fall*/
	public static void allAgentsFall(Configuration config, Terrain terrain)
	{
		ArrayList<Agent> agents = config.getAgents(); 
		for(int i=0; i<agents.size(); i++)
		{
			if(!agents.get(i).isSupportedFromBottom(config,terrain))
			{
				fall(agents.get(i), config, terrain); 
			}
		}
	}
	
	/*This method is called when a block is "flying"
	 * ie, there is no block supporting it*/
	public static void fall(Agent agent, Configuration config, Terrain terrain)
	{
			//d(t) = vi*t + a*t/2
			double dist = (agent.getSpeed()*TIMESHOT) +(GRAVITY*TIMESHOT*TIMESHOT/2); 
			//vf(t) = vi+a*t
			double newSpeed = agent.getSpeed() + TIMESHOT*GRAVITY;
			
			//update position
			Point3D newP = new Point3D(agent.getLocation().getX(),agent.getLocation().getY(), agent.getLocation().getZ()-dist);
			//move the agent
			try
			{agent.move(newP);}
			catch(InvalidMoveException e){}; 
			//update speed
			agent.setSpeed(newSpeed);

	}
	
	//THIS METHOD SHOULD KNOW WHICH SIDE TO RETURN (or return all of them in a list)
	public static Point3D returnFinalFallPos(Point3D pos, Configuration config, Terrain terrain)
	{
		
	}
	
	//THIS METHOD SHOULD KNOW WHICH SIDE TO RETURN (or return all of them in a list)
	public static ArrayList<Point3D> returnFinal4FallPos(Point3D pos, Configuration config, Terrain terrain)
	{
		
	}
	
	public static double calculateStaticFriction(Agent agent, Configuration config, Terrain terrain)
	{
		double staticF = 0 ; 
		double touchingBottomPart =0; 
		double touchingTopPart = 0; 
		
		
		if(agent.isSupportedFromBottom(config, terrain))
		{
			touchingBottomPart = agent.getTouchBottom(config, terrain); 
			
		}
		
		if(agent.isSupportedFromTop(config, terrain))
		{
			touchingTopPart = agent.getTouchTop(config, terrain);
		}
		
		staticF += (STATFRICTION*GRAVITY*touchingBottomPart*100) + (STATFRICTION*GRAVITY*touchingTopPart*100);

		return staticF; 
	}
	
	public static double calculateKineticFriction(Agent agent, Configuration config, Terrain terrain)
	{
		double dynF = 0 ; 
		double touchingBottomPart =0; 
		double touchingTopPart = 0; 
		
		
		if(agent.isSupportedFromTop(config, terrain))
		{
			touchingBottomPart = agent.getTouchBottom(config, terrain); 
			
		}
		
		if(agent.isSupportedFromTop(config, terrain))
		{
			touchingTopPart = agent.getTouchTop(config, terrain);
		}
		
		dynF += (STATFRICTION*GRAVITY*touchingBottomPart*100) + (STATFRICTION*GRAVITY*touchingTopPart*100);

		return dynF; 
	}
	
	public static double calculateImpulse(Agent agent, Configuration config, Terrain terrain)

	{
		double staticF = calculateStaticFriction(agent, config, terrain);
		double kineticF = calculateKineticFriction(agent, config, terrain); 
		
		double impulse = staticF+kineticF;
		
		return impulse;    
		
	}

	
}