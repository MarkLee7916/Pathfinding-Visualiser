package visualizer;

import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ThreadLocalRandom;

public class Controller implements Observer {
	private final View view;

	private final Position start;

	private final Position goal;

	// Contains the pathfinding algorithm
	private final Pathfinding pathfinding;

	// Vertical and horizontal length of the grid
	private final int size;

	public Controller() {
		int defaultSpeed = 50;
		size = 100;

		start = new Position(1, 1);
		goal = new Position(size - 2, size - 2);

		pathfinding = new Pathfinding(size, defaultSpeed);
		pathfinding.addObserver(this);

		view = new View(size, start, goal);
		view.addObserver(this);
	}

	public void run() {
		pathfinding.AStar(start, goal);
	}

	@Override
	// Recieves an update from either model or view
	public void update(Observable observable, Object obj) {
		if (observable instanceof Pathfinding)
			updateView(obj);
		else
			updateModel(obj);
	}

	private void updateModel(Object obj) {
		switch (view.getUpdateType()) {

		case WALL:
			addPositionToWall(obj);
			break;
		case REMOVE_WALL:
			removePositionFromWall(obj);
			break;
		case START:
			pathfinding.start();
			break;
		case STOP:
			pathfinding.stop();
			break;
		case CHANGE_SPEED:
			updateSpeed(obj);
			break;
		case CLEAR_WALLS:
			pathfinding.clearWall();
			break;
		case GENERATE_MAZE:
			generateRandomMaze();
			break;
		default:
			throw new AssertionError("Enum + " + view.getUpdateType() + " doesn't match with any types");
		}
	}

	private void updateView(Object obj) {
		switch (pathfinding.getUpdateType()) {

		case EXPLORING:
			updateTileColour(obj, TileType.EXPLORED);
			break;
		case PATH_FOUND:
			colourFinalPath(obj);
			break;
		case NO_PATH_FOUND:
			view.noPathFoundMessage();
			break;
		default:
			throw new AssertionError("Enum + " + pathfinding.getUpdateType() + " doesn't match with any types");
		}
	}

	// Create a random series of walls between the start and end points
	private void generateRandomMaze() {
		for (int row = 0; row < size; row++)
			for (int column = 0; column < size; column++)
				if (randomProbability(4) && !isStartOrGoalTile(row, column)) {
					view.updateTileColour(TileType.WALL, row, column);
					pathfinding.addPositionToWall(new Position(row, column));
				}
	}

	private boolean isStartOrGoalTile(int row, int column) {
		return (row == start.row() && column == start.column()) || (row == goal.row() && column == goal.column());
	}

	// An input of 3 renders a 1/3 chance of randomProbability() returning True
	private static boolean randomProbability(int chance) {
		return ThreadLocalRandom.current().nextInt(chance) == 1;
	}

	private void updateSpeed(Object obj) {
		Integer speed = (Integer) obj;

		pathfinding.setSpeed(speed);
	}

	private void removePositionFromWall(Object obj) {
		Position pos = (Position) obj;

		pathfinding.removePositionFromWall(pos);
	}

	private void addPositionToWall(Object obj) {
		Position pos = (Position) obj;

		pathfinding.addPositionToWall(pos);
	}

	// Extract final path from pointers and updates the tile colours with
	private void colourFinalPath(Object obj) {
		Position pos = (Position) obj;

		while (pos.getNext() != null) {
			updateTileColour(pos, TileType.PATH);
			pos = pos.getNext();
		}

		updateTileColour(pos, TileType.PATH);
	}

	private void updateTileColour(Object obj, TileType tileType) {
		Position pos = (Position) obj;

		if (!pos.equals(start) && !pos.equals(goal) || tileType == TileType.PATH)
			view.updateTileColour(tileType, pos.row(), pos.column());
	}
}
