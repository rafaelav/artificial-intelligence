/*
 * Artificial Intelligence and Multi-Agent Systems
 * Denmarks Tehnical University
 * 
 * Blue Ducks
 * Spring 2013
 */
package dk.dtu.ai.blueducks;

import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.print.attribute.standard.Finishings;

import dk.dtu.ai.blueducks.actions.Action;
import dk.dtu.ai.blueducks.actions.NoOpAction;
import dk.dtu.ai.blueducks.goals.GoToBoxGoal;
import dk.dtu.ai.blueducks.goals.Goal;
import dk.dtu.ai.blueducks.goals.MoveBoxGoal;
import dk.dtu.ai.blueducks.heuristics.GoToBoxHeuristic;
import dk.dtu.ai.blueducks.heuristics.MoveBoxHeuristic;
import dk.dtu.ai.blueducks.map.LevelMap;
import dk.dtu.ai.blueducks.map.State;
import dk.dtu.ai.blueducks.planner.AStarSearch;
import dk.dtu.ai.blueducks.planner.GoalPlanner;
import dk.dtu.ai.blueducks.planner.GoalSplitter;
import dk.dtu.ai.blueducks.planner.GoalPlanner.GoalCost;

/**
 * The Class Agent.
 */
public class Agent {

	/** The id. */
	private int id;

	public int getId() {
		return id;
	}

	/** The color. */
	private String color;

	/** The goal planner. */
	private GoalPlanner goalPlanner;

	/** The goal splitter. */
	private GoalSplitter goalSplitter;

	/** The subgoals. */
	List<Goal> subgoals;

	/** The current goal. */
	Goal currentGoal;

	/** The current subgoal index. */
	int currentSubgoalIndex;

	/** The current position in path. */
	int currentPositionInPath;

	/** The Constant logger. */
	private final Logger log;

	/**
	 * Instantiates a new agent.
	 * 
	 * @param id the id
	 * @param color the color
	 */
	public Agent(char id, String color) {
		this.id = id - '0';
		this.color = color;
		this.goalPlanner = new GoalPlanner(this);
		this.goalSplitter = new GoalSplitter();
		this.log = Logger.getLogger("Agent " + id);
	}

	/**
	 * Gets the color.
	 * 
	 * @return the color
	 * @returns the color of the agent
	 */
	public String getColor() {
		return color;
	}

	/**
	 * Gets the current goal.
	 * 
	 * @return the current goal
	 */
	public Goal getCurrentGoal() {
		return currentGoal;
	}

	/**
	 * Compute the plan states and actions for a given goal starting from a given state.
	 * 
	 * @param agentState the agent state
	 */
	private List<State> computePlanStates(Goal goal, State agentState) {
		List<State> path = null;
		if (goal instanceof GoToBoxGoal) {
			GoToBoxGoal gtbGoal = (GoToBoxGoal) goal;
			path = AStarSearch.<State, GoToBoxGoal> getBestPath(agentState, gtbGoal, new GoToBoxHeuristic());
		} else if (goal instanceof MoveBoxGoal) {
			MoveBoxGoal mbGoal = (MoveBoxGoal) goal;
			path = AStarSearch.<State, MoveBoxGoal> getBestPath(agentState, mbGoal, new MoveBoxHeuristic());
		}
		return path;
	}

	/**
	 * Request the plan of the agent. As a response to this method, the agent should respond with a
	 * proposal using {@link MotherOdin#appendPlan(Agent, List)}.<br/>
	 * <br/>
	 * If the agent has no plan, he must append a plan with at least one {@link NoOpAction}.
	 */
	public void requestPlan() {
		// Build the subgoals
		Goal newGoal = MotherOdin.getInstance().getGoalForAgent(this);
		if (this.currentGoal == null || !this.currentGoal.equals(newGoal)) {
			this.currentGoal = MotherOdin.getInstance().getGoalForAgent(this);
			if (log.isLoggable(Level.INFO))
				log.info("Planning for new goal: " + currentGoal);
			this.subgoals = goalSplitter.getSubgoal(currentGoal, this);
			if (log.isLoggable(Level.FINEST))
				log.finest("Subgoals generated: " + subgoals);
			currentSubgoalIndex = 0;
		} else {
			if (log.isLoggable(Level.INFO))
				log.info("Planning for existing goal: " + currentGoal);
		}

		// If there are no more subgoals that need to be satisfied
		if (currentSubgoalIndex >= this.subgoals.size()) {
			log.info("Finished planning for all subgoals.");
			MotherOdin.getInstance().finishedTopLevelGoal(this, this.currentGoal);
			return;
		}

		// Replan
		State agentState = new State(LevelMap.getInstance().getCellForAgent(this), null, null, this);
		agentState.setBoxes(LevelMap.getInstance().getCurrentState().getBoxes());
		Goal subgoal = subgoals.get(currentSubgoalIndex++);
		if (log.isLoggable(Level.FINER))
			log.finer("\tCurrent subgoal: " + subgoal);
		List<State> plan = computePlanStates(subgoal, agentState);
		plan.remove(0);
		List<Action> actions = new LinkedList<Action>();
		for (State s : plan)
			actions.add((Action) s.getEdgeFromPrevNode());
		if (log.isLoggable(Level.FINEST))
			log.finest("Generated plan actions: " + actions);

		MotherOdin.getInstance().appendPlan(this, actions);
	}

	/**
	 * Request goals proposals. As a response to this method, the agent should respond with a
	 * proposal using {@link MotherOdin#addAgentGoalsProposal(Agent, java.util.PriorityQueue)}.
	 */
	public void requestGoalsProposals() {
		log.fine("Request for Goals Proposals received.");
		PriorityQueue<GoalCost> proposals = goalPlanner.computeGoalCosts(MotherOdin.getInstance()
				.getTopLevelGoals());
		if (log.isLoggable(Level.FINEST))
			log.finest("Goals proposals: " + proposals);
		MotherOdin.getInstance().addAgentGoalsProposal(this, proposals);
	}

	@Override
	public String toString() {
		return "Agent" + id;
	}
}
