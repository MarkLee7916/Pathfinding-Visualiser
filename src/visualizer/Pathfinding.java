package visualizer;

import java.util.ArrayList;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class Pathfinding extends Observable {
	private boolean running;

	// Vertical and horizontal length of the grid
	private final int size;

	// How long the algorithm sleeps between turns
	private int speed;

	// List of positions that algorithm can't cross
	private final Set<Position> wall;

	// Flag to signal what view wants to notify controller about
	private UpdateType updateType;

	public Pathfinding(int si, int sp) {
		size = si;
		speed = sp;

		wall = new HashSet<>();
		updateType = UpdateType.EXPLORING;
	}

	// A* pathfinding based on a simple co-ordinate heuristic
	public void AStar(Position start, Position goal) {
		Queue<Position> positions = new PriorityQueue<>(Arrays.asList(start));
		Set<Position> visited = new HashSet<>(Arrays.asList(start));
		Position currentPos;

		while (!positions.isEmpty()) {
			if (running) {
				currentPos = positions.poll();

				if (!wall.contains(currentPos)) {
					notifyExploredTile(currentPos);

					for (Position pos : neighbours(currentPos)) {
						if (!visited.contains(pos)) {
							pos.computeHeuristicValue(goal);
							positions.add(pos);
							visited.add(pos);
							pos.setNext(currentPos);
						}
					}

					// We found the path!
					if (currentPos.equals(goal)) {
						notifyFinalPath(currentPos);
						return;
					}
				}
			}

			threadSleep();
		}

		notifyNoPathFound();
	}

	private void notifyNoPathFound() {
		updateType = UpdateType.NO_PATH_FOUND;
		setChanged();
		notifyObservers();
	}

	// Tell controller that that a tile has been explored
	private void notifyExploredTile(Position pos) {
		updateType = UpdateType.EXPLORING;
		setChanged();
		notifyObservers(pos);
	}

	// Tell controller that that algorithm is finished and give it the final path
	private void notifyFinalPath(Position pos) {
		updateType = UpdateType.PATH_FOUND;
		setChanged();
		notifyObservers(pos);
	}

	private void threadSleep() {
		try {
			Thread.sleep(speed);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// Returns list of positions that are adjacent to the current position
	private List<Position> neighbours(Position pos) {
		List<Position> neighbours = new ArrayList<>();

		if (isInGrid(pos.row() + 1, pos.column()))
			neighbours.add(new Position(pos.row() + 1, pos.column()));
		if (isInGrid(pos.row() - 1, pos.column()))
			neighbours.add(new Position(pos.row() - 1, pos.column()));
		if (isInGrid(pos.row(), pos.column() + 1))
			neighbours.add(new Position(pos.row(), pos.column() + 1));
		if (isInGrid(pos.row(), pos.column() - 1))
			neighbours.add(new Position(pos.row(), pos.column() - 1));

		return neighbours;
	}

	private boolean isInGrid(int row, int column) {
		return row >= 0 && row < size && column >= 0 && column < size;
	}

	public void addPositionToWall(Position pos) {
		wall.add(pos);
	}

	public void removePositionFromWall(Position pos) {
		wall.remove(pos);
	}

	public void clearWall() {
		wall.clear();
	}

	public UpdateType getUpdateType() {
		return updateType;
	}

	public void setSpeed(int s) {
		speed = s;
	}

	public void stop() {
		running = false;
	}

	public void start() {
		running = true;
	}
}
