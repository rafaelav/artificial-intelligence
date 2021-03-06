package dk.dtu.ai.blueducks.goals;

import dk.dtu.ai.blueducks.map.Cell;
import dk.dtu.ai.blueducks.map.State;
import dk.dtu.ai.blueducks.planner.AStarNode;

public class GoToBoxGoal extends Goal {

	private Cell from;
	private Cell to;

	public GoToBoxGoal(Cell from, Cell to) {
		this.from = from;
		this.to = to;
	}

	public Cell getFrom() {
		return from;
	}

	public Cell getTo() {
		return to;
	}

	@Override
	public boolean isSatisfied(AStarNode node) {
		State state = (State) node;
		if(state.getAgentCell().getNeighbours().contains(to))
			return true;
		return false;
	}

	@Override
	public String toString() {
		return "GoToBoxGoal [from=" + from + ", to=" + to + "]";
	}

	
}
