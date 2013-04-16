/*
 * Artificial Intelligence and Multi-Agent Systems
 * Denmarks Tehnical University
 * 
 * Blue Ducks
 * Spring 2013
 */
package dk.dtu.ai.blueducks;

import java.util.ArrayList;
import java.util.HashMap;

import dk.dtu.ai.blueducks.actions.Action;
import dk.dtu.ai.blueducks.map.Cell;
import dk.dtu.ai.blueducks.map.CellContent;
import dk.dtu.ai.blueducks.map.Direction;
import dk.dtu.ai.blueducks.map.LevelMap;

public class Agent extends CellContent {

	private char id;
	private String color;

	/**
	 * Instantiates a new agent.
	 * 
	 * @param initialCell the initial cell
	 * @param id the id
	 * @param color the color
	 */
	public Agent(Cell initialCell, char id, String color) {
		super(initialCell);
		this.id = id;
		this.color = color;
	}

	/**
	 * Move.
	 * 
	 * @param direction the direction
	 * @return the string
	 */
	public String move(Direction direction) {
		// return MoveAction(this, direction).toCommandString();
		return null;
	}

	/* ??????????????????? */
	public String nextAction() {
		return move(Direction.N);
	}

	public ArrayList<Cell> computeDesires() {
		return LevelMap.getInstance().getGoals();
	}

	public HashMap<Cell, Integer> computeScore() {
		HashMap<Cell, Integer> goalsScore = new HashMap<Cell, Integer>();
		ArrayList<Cell> goals = computeDesires();
		for (Cell cell : goals) {
			goalsScore.put(cell, computeScore(cell));
		}

		return goalsScore;
	}

	private Integer computeScore(Cell cell) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	public Action getNextAction() {
		//TODO
		return null;
	}
	
	public void triggerReplanning() {
		//TODO
	}
	

	public char getId() {
		return id;
	}

	public void setId(char id) {
		this.id = id;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

}
