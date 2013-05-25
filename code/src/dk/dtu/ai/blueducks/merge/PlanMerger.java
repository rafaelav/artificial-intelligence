package dk.dtu.ai.blueducks.merge;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import dk.dtu.ai.blueducks.MotherOdin;
import dk.dtu.ai.blueducks.actions.Action;
import dk.dtu.ai.blueducks.actions.NoOpAction;
import dk.dtu.ai.blueducks.map.LevelMap;
import dk.dtu.ai.blueducks.map.MultiAgentState;

public class PlanMerger {

	private static final Logger log = Logger.getLogger(PlanMerger.class.getSimpleName());
	private static final boolean[][] mergeOptions;
	private static final int AGENTS_COUNT;
	private static final int NO_MORE_OPTIONS = -2;
	static {
		AGENTS_COUNT = LevelMap.getInstance().getAgentsList().size();
		mergeOptions = PlanMergeOptions.OPTIONS[AGENTS_COUNT - 1];
		log.config("Loaded options for " + AGENTS_COUNT + " agents.");
	}

	private static int getNextOptionIndex(int currentOptionsPos) {
		if (currentOptionsPos >= mergeOptions.length)
			return NO_MORE_OPTIONS;
		return currentOptionsPos + 1;
	}

	/**
	 * @param actions - line i contains the actions from the agent i's plan
	 * @param current - the node we are trying to expand
	 * @param step - the current depth of the plan (e.g. step 3 - we're trying to see what actions
	 *            from step 3 can be done)
	 * @return true if is successful in merging the plans or false otherwise
	 */
	// TODO - limit the go back
	// TODO - keep track of the most advanced point where it failed
	public static boolean mergePlans(Action[][] actions, short[] agentCurrentActionIndex,
			PlanMergeNode current, int step) {
		// getting current action for each agent
		Action[] agentsActions = new Action[AGENTS_COUNT];
		short agentsDone = 0;
		for (int agent = 0; agent < AGENTS_COUNT; agent++)
			if (agentCurrentActionIndex[agent] >= actions[agent].length) {
				agentsActions[agent] = new NoOpAction();
				agentsDone++;
			} else
				agentsActions[agent] = actions[agent][agentCurrentActionIndex[agent]];
		if (log.isLoggable(Level.FINE)) {
			log.fine("Merging plans step " + step + ". Current actions to merge: "
					+ Arrays.toString(agentsActions));
		}

		if (agentsDone == AGENTS_COUNT) {
			log.info("Plan merging complete.");
			List<LinkedList<Action>> mergedPlan = prepareResponse(current);
			if (log.isLoggable(Level.FINER))
				log.finer("Merged plan: " + mergedPlan);
			MotherOdin.getInstance().setMergedPlan(mergedPlan);
			return true;
		}

		// we will keep a list of pairs of actions which are conflicting in the state
		List<Conflict> conflictingAgents = new ArrayList<Conflict>();

		// keeps track if there is a conflict found when trying to execute the actions for this step
		boolean conflictFound;

		// we try to get all option of generated activeAgents
		boolean[] activeAgents;
		int currentOptionsIndex = 0;

		while (currentOptionsIndex != NO_MORE_OPTIONS) {
			activeAgents = mergeOptions[currentOptionsIndex];

			if (log.isLoggable(Level.FINEST))
				log.finest("Checking options: " + Arrays.toString(activeAgents));

			// at the beginning no conflict exists
			conflictFound = false;

			// we duplicate state
			MultiAgentState duplicatedState = new MultiAgentState(current.getState().getAgents(), current
					.getState().getBoxes());

			// we try to add actions of the active agents and see if we have conflicts
			for (short i = 0; i < agentsActions.length; i++) {
				if (activeAgents[i] == true) {
					Action agentIAction = agentsActions[i];
					// checking if action can be performed on the state
					if (agentIAction.isApplicable(duplicatedState)) {
						// if yes -> execute it
						agentIAction.execute(duplicatedState);
					} else {
						// the action is not applicable so it conflicts with another action
						if (log.isLoggable(Level.FINER)) {
							log.finer("Action not applicable: " + agentIAction + " in state "
									+ duplicatedState);

						}
						conflictFound = true;

						// if no -> check with which action is in conflict
						for (short j = 0; j < i; j++) {
							if (agentIAction.isInConflict(duplicatedState, agentsActions[j])) {
								// added the conflict between already executed action j
								conflictingAgents.add(new Conflict(j, i));
								if (log.isLoggable(Level.FINE))
									log.fine("Conflict found between agents: " + j + "x" + i);
							}
						}
					}
				}
			}

			// if we can move on to next step (no conflict was found at this one)
			if (conflictFound == false) {
				short[] nextAgentCurrentActionIndex = new short[agentCurrentActionIndex.length];
				Action[] currentSelectedActions = new Action[AGENTS_COUNT];
				for (int i = 0; i < AGENTS_COUNT; i++)
					if (activeAgents[i]) {
						nextAgentCurrentActionIndex[i] = (short) (agentCurrentActionIndex[i] + 1);
						currentSelectedActions[i] = agentsActions[i];
					} else {
						nextAgentCurrentActionIndex[i] = agentCurrentActionIndex[i];
						currentSelectedActions[i] = new NoOpAction();
					}

				log.info("Moving to next step after selecting: " + Arrays.toString(currentSelectedActions));
				log.info("Moving to next step with agent action indexes: "
						+ Arrays.toString(nextAgentCurrentActionIndex));
				PlanMergeNode next = new PlanMergeNode(duplicatedState, currentSelectedActions, current);
				return mergePlans(actions, nextAgentCurrentActionIndex, next, step + 1);
			} else {
				// get the next option
				currentOptionsIndex = getNextOptionIndex(currentOptionsIndex);
			}
		}
		log.info("Plan merge not possible.");
		return false;
	}

	private static List<LinkedList<Action>> prepareResponse(PlanMergeNode finalNode) {
		// return the plan...
		PlanMergeNode crt = finalNode;
		List<LinkedList<Action>> mergedPlan = new ArrayList<LinkedList<Action>>();
		for (int i = 0; i < AGENTS_COUNT; i++)
			mergedPlan.add(new LinkedList<Action>());

		// while there are nodes to go back to (and not the first node, because it doesn't have
		// stored actions which led to it)
		while (crt.getPrevNode() != null) {
			for (int i = 0; i < AGENTS_COUNT; i++)
				// in the plan of i we put the action of i that brought us to this state
				mergedPlan.get(i).push(crt.getPrevActions()[i]);

			// go back one more node in the planning
			crt = crt.getPrevNode();
		}
		return mergedPlan;
	}

	private static class Conflict {
		public short agent1;
		public short agent2;

		public Conflict(short agent1, short agent2) {
			super();
			this.agent1 = agent1;
			this.agent2 = agent2;
		}
	}
}