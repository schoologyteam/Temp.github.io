package student;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import game.ScramState;
import game.HuntState;
import game.Explorer;
import game.Node;
import game.NodeStatus;

public class Indiana extends Explorer {

	private long startTime= 0;    // start time in milliseconds

	/** Get to the orb in as few steps as possible. Once you get there, 
	 * you must return from the function in order to pick
	 * it up. If you continue to move after finding the orb rather 
	 * than returning, it will not count.
	 * If you return from this function while not standing on top of the orb, 
	 * it will count as a failure.
	 * 
	 * There is no limit to how many steps you can take, but you will receive
	 * a score bonus multiplier for finding the orb in fewer steps.
	 * 
	 * At every step, you know only your current tile's ID and the ID of all 
	 * open neighbor tiles, as well as the distance to the orb at each of these tiles
	 * (ignoring walls and obstacles). 
	 * 
	 * In order to get information about the current state, use functions
	 * currentLocation(), neighbors(), and distanceToOrb() in HuntState.
	 * You know you are standing on the orb when distanceToOrb() is 0.
	 * 
	 * Use function moveTo(long id) in HuntState to move to a neighboring 
	 * tile by its ID. Doing this will change state to reflect your new position.
	 * 
	 * A suggested first implementation that will always find the orb, but likely won't
	 * receive a large bonus multiplier, is a depth-first search. Some
	 * modification is necessary to make the search better, in general.*/
	@Override public void huntOrb(HuntState state) {
		//TODO 1: Get the orb
		HashSet<Long> visited = new HashSet<Long>();
		dfs2(state, visited);

	}


	/**
	 * Performs a dfs search to get to the orb
	 * Suppose Indiana is standing at node returnPoint. Node returnPoint is marked as visited and then
	 * the method returns if the orb is at node returnPoint. If not, we check the neighbors of
	 * node returnPoint. We conduct a dfs for neighbor n of returnPoint, and if the orb is found the method returns
	 * and stops searching other neighbors. If the orb is not found, Indiana moves back
	 * to returnPoint and continues search other unvisited neighbors.
	 */
	private void dfs(HuntState IndysNode, HashSet<Long> visited)
	{	
		//adds the current node to the HashSet of visited nodes
		visited.add(IndysNode.currentLocation());
		Long returnPoint = IndysNode.currentLocation();

		//when Indiana reaches the orb stop the search
		if(IndysNode.distanceToOrb()==0)
			return;

		//performs dfs accordingly to each of the neighbors
		for(NodeStatus n : IndysNode.neighbors()){
			if(!visited.contains(n.getId())){
				IndysNode.moveTo(n.getId());
				dfs(IndysNode,visited);
				if(IndysNode.distanceToOrb()==0)
					return;
				IndysNode.moveTo(returnPoint);
			}	
		}
	}


	/**
	 * An optimized version of dfs2
	 * @param IndysNode - Takes the HuntState which will show Indy's current node
	 * @param visited - HashSet of visited nodes
	 * Performs dfs but when Indy considers his neighbors, he prioritizes
	 * neighbors with a closer Manhattan distance to the orb
	 */
	private void dfs2(HuntState IndysNode, HashSet<Long> visited){
		//adds the current node to the HashSet of visited nodes
		visited.add(IndysNode.currentLocation());
		Long returnPoint = IndysNode.currentLocation();

		//when Indiana reaches the orb stop the search
		if(IndysNode.distanceToOrb()==0)
			return;

		//performs dfs with a prioritized list of neighbors
		ArrayList<NodeStatus> neighbors = (ArrayList) IndysNode.neighbors();
		for(NodeStatus n : prioritizeNeighbors(neighbors)){
			if(!visited.contains(n.getId())){
				IndysNode.moveTo(n.getId());
				dfs2(IndysNode,visited);
				if(IndysNode.distanceToOrb()==0)
					return;
				IndysNode.moveTo(returnPoint);
			}	
		}
	}


	/**Returns list of neighbors sorted according to increasing manhattan distance
	 * Precondition: n is not empty.**/
	private ArrayList<NodeStatus> prioritizeNeighbors(ArrayList<NodeStatus> n){
		Collections.sort(n, new Comparator<NodeStatus>(){
			public int compare(NodeStatus o1, NodeStatus o2) {
				return o1.compareTo(o2);
			}
		});

		return n;
	}

	/** 
	 * Get out the cavern before the ceiling collapses, trying to collect as much
	 * gold as possible along the way. Your solution must ALWAYS get out before time runs
	 * out, and this should be prioritized above collecting gold.
	 * 
	 * You now have access to the entire underlying graph, which can be accessed through ScramState.
	 * currentNode() and getExit() will return Node objects of interest, and getNodes()
	 * will return a collection of all nodes on the graph. 
	 * 
	 * Note that the cavern will collapse in the number of steps given by getStepsRemaining(),
	 * and for each step this number is decremented by the weight of the edge taken. You can use
	 * getStepsRemaining() to get the time still remaining, pickUpGold() to pick up any gold
	 * on your current tile (this will fail if no such gold exists), and moveTo() to move
	 * to a destination node adjacent to your current node.
	 * 
	 * You must return from this function while standing at the exit. Failing to do so before time
	 * runs out or returning from the wrong location will be considered a failed run.
	 * 
	 * You will always have enough time to escape using the shortest path from the starting
	 * position to the exit, although this will not collect much gold. For this reason, using 
	 * Dijkstra's to plot the shortest path to the exit is a good starting solution    */
	@Override public void scram(ScramState state) {
		//TODO 2: Get out of the cavern before it collapses, picking up gold along the way
		betterScram(state);
	}


	/**
	 * Uses djikstra's algorithm to exit
	 * @param state
	 */
	private void djikstraScram(ScramState state){
		List<Node> exitPath = Paths.shortestPath(state.currentNode(), state.getExit());
		exitPath.remove(0);
		for (Node n: exitPath){
			state.moveTo(n);
			if (n==state.getExit()) return;
			if (n.getTile().gold()>0){
				state.grabGold();
			}
		}
	}

	/**
	 * @param state - Gives information about the game in ScramState
	 * @param currentNode - Indy's current Node
	 * @return a list of nodes that gives the path to a reachable tile
	 * with the maximum gold while still being able to get out in time
	 */
	private List<Node> bigCoin(Node currentNode, ScramState state){

		//gets all the Nodes, puts them in a list
		//and sorts the list based on increasing gold values
		ArrayList<Node> nodeList = new ArrayList(state.allNodes());
		Collections.sort(nodeList, new Comparator<Node>(){
			public int compare(Node o1, Node o2) {
				return o2.getTile().gold() - (o1.getTile().gold());
			}
		});


		int steps = state.stepsLeft();
		int i = 0;
		List<Node> toGoldPath = new ArrayList();
		List<Node> toExitPath = new ArrayList();
		int pathDist = Integer.MAX_VALUE;

		//finds a path that allows Indiana to go to a reachable tile with the
		//most amount of gold while still being able to make out it in time
		while(pathDist > steps){
			toGoldPath = Paths.shortestPath(currentNode, nodeList.get(i));
			toExitPath = Paths.shortestPath(nodeList.get(i), state.getExit());
			pathDist = Paths.pathDistance(toGoldPath) + Paths.pathDistance(toExitPath);
			i++;
		}
		return toGoldPath;
	}

	/**
	 * Exits the scramState by going to reachable gold tiles in order
	 * starting from tiles with the most gold
	 * @param state
	 */
	private void betterScram(ScramState state){
		
		//picks up the gold from the current location
		if(state.currentNode().getTile().gold() > 0)
			state.grabGold();

		//continues to go to the next reachable tile with the most
		//amount of gold
		List<Node> path = bigCoin(state.currentNode(), state);
		while(path.size()>1){
			path.remove(0);
			for (Node n: path ){
				state.moveTo(n);
				if (n.getTile().gold()>0){
					state.grabGold();
				}
			}
			path = bigCoin(state.currentNode(), state);
		}
		
		//exits using djikstra
		djikstraScram(state);

	}



}
