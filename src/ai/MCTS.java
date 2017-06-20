package ai;

import java.util.ArrayList;

import all.continuous.*;

public class MCTS extends ModuleAlgorithm
{

	private boolean DEBUG = false;
	private boolean DEBUG2 = false;
	private boolean DEBUG3 = false;
	private boolean DEBUG4 = false;
	private boolean DEBUG5=false;
	ArrayList<Action> path = new ArrayList<Action>();
	ArrayList<MCTSNode> nodePath = new ArrayList<MCTSNode>();
	private static int counter=0;
	private static int turnCounter=0;
	private static int height=0;
	public MCTS(Simulation sim) {
		super(sim);
		// TODO Auto-generated constructor stub
		if(DEBUG)
			System.out.println("CONSTRUCTION OF MCTS");


	}

	public void mainMCTS(Simulation sim){

		MCTSNode root = new MCTSNode(sim.getCurrentConfiguration());
		root.addVisit();
		expand(root);
		for(int i = 0; i < root.getChildren().size(); i++)
		{
			if(DEBUG)
				System.out.println("simulate child numb "+i);

			simulate(root.getChildren().get(i));
		}


		if(DEBUG3)
			System.out.println("root child score : "+root.getChildren().get(0).getScore());

		int counter1 = 0 ;
		//TO BE LOOPED UNTIL GOAL IS FOUND
		while(counter1<3000)
		{
			root.addVisit();
			//if(DEBUG)
			System.out.println("counter : "+counter1);

			MCTSNode next = select(root);
			if(next != null){
				while(next.getChildren().size()!=0)
				{
					MCTSNode next2 = select(next);
					next = next2;
				}
				if(next == null)
					System.out.println("next is nul!!");
				//if it has already been visited
				//if(next.getVisits() > 0)
				//{
				expand(next);

				for(int i = 0; i < next.getChildren().size(); i++)
				{
					if(DEBUG)
						System.out.println("simulate child numb "+i);

					simulate(next.getChildren().get(i));
				}
				//}
			/*else
			{

				simulate(next);
			}*/

				backUp(next, next.getChildren());
			}

			counter1++;
		}

		while(root.getChildren().size()>0)
		{
			/*for(int i =0; i<root.getChildren().size(); i++)
			{
				System.out.println("child Score : "+root.getChildren().get(i).getScore());
			}
			*/
			if(DEBUG3)
				System.out.println("Start computing the path");

			MCTSNode next = bestValueChild(root);
			if(DEBUG5)
				System.out.println("add node to path : ");
			nodePath.add(next);


			Action a = next.getAction();
			path.add(a);

			if(DEBUG3)
				System.out.println("next children size : "+next.getChildren().size());

			root = next;


			if(DEBUG3)
				System.out.println("root children size : "+root.getChildren().size());
		}

		if(DEBUG3)
			System.out.println("path size : "+path.size());


	}

	public MCTSNode bestValueChild(MCTSNode parent)
	{
		ArrayList<MCTSNode> children = parent.getChildren();
		int min=Integer.MAX_VALUE;
		MCTSNode best = null;
		for(int i=0; i<children.size(); i++)
		{
			if(DEBUG3)
				System.out.println("child value : "+children.get(i).getScore());

			if(children.get(i).getScore()<min && children.get(i).getScore()!=Integer.MIN_VALUE)
			{
				if(DEBUG3)
					System.out.println("update best value child");
				min = children.get(i).getScore();
				best = children.get(i);
			}
		}

		return best;
	}

	public MCTSNode bestVisitsChild(MCTSNode parent)
	{
		ArrayList<MCTSNode> children = parent.getChildren();
		int max=Integer.MIN_VALUE;
		MCTSNode best = null;
		for(int i=0; i<children.size(); i++)
		{
			if(DEBUG3)
				System.out.println("child value : "+children.get(i).getScore());

			if(children.get(i).getVisits()>max)
			{
				if(DEBUG3)
					System.out.println("update best value child");
				max = children.get(i).getScore();
				best = children.get(i);
			}
		}

		return best;
	}


	//SELECT
	public double selectPolicy(MCTSNode node){

		if(DEBUG3)
			System.out.println("node score : "+node.getScore()+" and visits "+node.getVisits());
		double selectScore=0;

		if(node.getVisits()==0)
		{
			//selectScore = node.getScore()  - Math.sqrt(4)*Math.sqrt(Math.log(node.getParent().getVisits())/1);
			selectScore = node.getScore()  - 100;
			//selectScore = Double.MIN_VALUE;
		}
		//to be maximized
		//double selectScore = node.getScore() + Math.sqrt(2)*Math.sqrt(Math.log(node.getParent().getVisits())/node.getVisits());

		//to be minimized
		else
		{
			if(DEBUG)
				System.out.println("sqrt : "+Math.sqrt(2));
			if(DEBUG)
				System.out.println("UCTS second part"+Math.sqrt(Math.log(node.getParent().getVisits())/node.getVisits()));
			selectScore = node.getScore() - Math.sqrt(1/5)*Math.sqrt(Math.log(node.getParent().getVisits())/node.getVisits());
		}
		if(DEBUG3)
			System.out.println("node SELECT score : "+selectScore);

		return selectScore;
	}

	public MCTSNode select(MCTSNode origin){

		if(DEBUG)
			System.out.println("SELECT");

		ArrayList<MCTSNode> children = origin.getChildren();
		/*if(DEBUG)
			System.out.println("children size "+children.size());*/
		//MAX SELECTION
		/*double max = -1000000; //CHANGED BY BOBBY
		MCTSNode maxNode = null;

		for(int i = 0; i < children.size(); i++){
			double selectScore = selectPolicy(children.get(i));
			if(DEBUG2)
				System.out.println("select policy : "+selectScore);

			if(DEBUG)
				System.out.println("current max value"+ max);
			if(selectScore > max){
				if(DEBUG)
					System.out.println("update the max");
				max = selectScore;
				maxNode = children.get(i);
			}
		}*/

		//MIN SELECTION
		double min = 1000000; //CHANGED BY BOBBY
		MCTSNode minNode = null;

		for(int i = 0; i < children.size(); i++){
			double selectScore = selectPolicy(children.get(i));
			if(DEBUG2)
				System.out.println("select policy : "+selectScore);

			if(DEBUG)
				System.out.println("current max value"+ min);
			if(selectScore < min){
				if(DEBUG)
					System.out.println("update the min");
				min = selectScore;
				minNode = children.get(i);
			}
		}


		if(DEBUG3)
			System.out.println("Chosen node has policy : "+min);

		//maxNode.addVisit();
		//return maxNode;
		minNode.addVisit();
		return minNode;
	}

	//EXPAND
	public void expand(MCTSNode origin){
		if(DEBUG3)
			System.out.println("EXPAND");

		ArrayList<Action> validActions = origin.getConfiguration().getAllValidActions();

		for(int i = 0; i < validActions.size(); i++){

			Configuration configCopy = origin.getConfiguration().copy();
			configCopy.apply(validActions.get(i));

			MCTSNode child = new MCTSNode(configCopy);
			child.setAction(validActions.get(i));

			origin.addChild(child);

			if(DEBUG)
				System.out.println("expanded chil score : "+child.getScore());

		}


		if(DEBUG)
			System.out.println("origin now has "+origin.getChildren().size()+ " children");
	}

	//SIMULATE
	public void simulate(MCTSNode origin){
		if(DEBUG)
			System.out.println("SIMULATE");

		//origin.addVisit();
		Configuration currentConfig = origin.getConfiguration();


		long t = System.nanoTime();
		long end = t + 100000;

		while(System.nanoTime() < end){

			Configuration nextConfig = currentConfig.copy();
			currentConfig = nextConfig;

			/*if(DEBUG)
			{
				if(origin == null)
					System.out.println("node is null");
				if(origin.getConfiguration() == null)
					System.out.println("config is null");
				System.out.println("number of agents : "+origin.getConfiguration().getAgents().size());
				System.out.println(origin.getConfiguration().getAgent(0).getLocation() + " "+ origin.getConfiguration().getAgent(1).getLocation());
			}*/
			ArrayList<Action> validActions = currentConfig.getAllValidActions();
			int size = validActions.size();
			int random = (int) Math.random()*size;

			currentConfig.apply(validActions.get(random));

		}

		int score = estimateScore(currentConfig);
		if(isSameAsAParent(origin))
		{
			score = score*10;
		}
		origin.setScore(score);

		if(DEBUG3)
			System.out.println("END OF SIMULATION, score : "+score);
	}

	public int estimateScore(Configuration config){

		ArrayList<Agent> agents = config.getAgents();
		ArrayList<Agent> goals = config.getSimulation().getGoalConfiguration().getAgents();
		int totalManhattanDistance = 0;

		for(int i = 0; i < agents.size() ; i++){
			if(DEBUG)
				System.out.println("updtae MANHATTANDISTANCE");
			totalManhattanDistance += agents.get(i).getManhattanDistanceTo(goals.get(i).getLocation());
		}

		//FIRST TRY, PUT IN NEGATIVE
		totalManhattanDistance = (int) (totalManhattanDistance/agents.size());
		//SQECOND TRY, substract from 10000
		//totalManhattanDistance = (int)((100) -(totalManhattanDistance/agents.size()));

		return totalManhattanDistance;
	}
	//BACK UP
	public void backUp(MCTSNode start, ArrayList<MCTSNode> ends){
		if(DEBUG3)
			System.out.println("BACKUP");
		int newHeight=0;
		int totalScore = 0;
		for(int i = 0; i < ends.size(); i++){
			totalScore+=ends.get(i).getScore();
		}
		totalScore = totalScore/ends.size();
		start.setScore(totalScore);
		if(DEBUG4)
			System.out.println("new score : "+start.getScore());

		if(DEBUG)
			System.out.println("start score : "+start.getScore());

		while(start.getParent() != null){
			//start.getParent().addScore(start.getScore());
			totalScore=0;
			for(int i=0; i<start.getParent().getChildren().size(); i++)
			{
				totalScore+=start.getParent().getChildren().get(i).getScore();
			}
			totalScore = totalScore/start.getParent().getChildren().size();
			start.getParent().setScore(totalScore);
			if(DEBUG4)
				System.out.println("new score : "+start.getScore());
			start = start.getParent();
			newHeight++;
			if(DEBUG)
				System.out.println("backed up score parent "+start.getScore());
		}

		if(newHeight>height)
		{
			height=newHeight;
			if(DEBUG4)
				System.out.println("HEIGHT : "+height);
		}

	}

	//for the path finding, check if a certain config has already been chosen
	public boolean isSameAsAParent(MCTSNode node)
	{
		ArrayList<Agent> nodeAgents = node.getConfiguration().getAgents();
		if(DEBUG5)
			System.out.println("NODE AGENTS");
		if(DEBUG5)
		{for(int i=0; i<nodeAgents.size(); i++)
		{System.out.println(nodeAgents.get(i).getLocation());}}


		while(node.getParent() != null)
		{
			ArrayList<Agent> pathAgents = node.getParent().getConfiguration().getAgents();

			/*if(DEBUG5)
			{
				for(int w=0; w<pathAgents.size(); w++)
				{System.out.println("PATH AGENTS NUMBER "+i);
					System.out.println(pathAgents.get(w).getLocation());
					}
				}*/

			int agentCounter=0;
			for(int j=0; j<nodeAgents.size(); j++)
			{
				int count2=0;
				for(int k=0; k<pathAgents.size(); k++)
				{
					if(DEBUG5)
						System.out.println("analyse : "+nodeAgents.get(j).getLocation()+ "and "+ pathAgents.get(k).getLocation());
					if(nodeAgents.get(j).getLocation().equals(pathAgents.get(k).getLocation()))
					{

						count2++;
						if(DEBUG5)
							System.out.println("count2 : "+count2);
					}


				}
				if(count2>=1)
				{
					agentCounter++;
					if(DEBUG5)
						System.out.println("agentCounter : "+agentCounter);
				}

			}
			if(agentCounter==pathAgents.size())
			{
				if(DEBUG5)
					System.out.println("RETURN TRUE");
				return true;
			}

			node = node.getParent();
		}

		return false;
	}


	@Override
	public void takeTurn() {

		if(turnCounter==0)
		{
			mainMCTS(sim);
			turnCounter++;
		}
		// TODO Auto-generated method stub
		if(counter<path.size())
		{
			sim.apply(path.get(counter));
			counter++;
			turnCounter++;
		}
		else
		{
			sim.finish();
		}



	}
}