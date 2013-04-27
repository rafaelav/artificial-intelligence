package dk.dtu.ai.blueducks.heuristics;

import dk.dtu.ai.blueducks.goals.GoToBoxGoal;
import dk.dtu.ai.blueducks.map.State;

public class GoToBoxHeuristic implements Heuristic<State, GoToBoxGoal> {


	@Override
	public float getHeuristicValue(State state, GoToBoxGoal goal) {
		float distance = Math.abs(goal.getTo().x - state.getAgentCell().x)
				+ Math.abs(goal.getTo().y - state.getAgentCell().y);

		return distance;
	}

}
