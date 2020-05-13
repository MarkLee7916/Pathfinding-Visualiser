package visualizer;

public class Position implements Comparable<Position> {
	private final int row;

	private final int column;

	// Value that A* bases it's order of precedence on
	private int heuristicValue;

	// Pointer to next position in path
	private Position next;

	public Position(int X, int Y) {
		row = X;
		column = Y;
	}

	public void computeHeuristicValue(Position goal) {
		heuristicValue = Math.abs(row - goal.row) + Math.abs(column - goal.column);
	}

	public void setNext(Position n) {
		next = n;
	}

	public Position getNext() {
		return next;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Position))
			return false;

		Position position = (Position) obj;

		return row == position.row && column == position.column;
	}

	@Override
	public int hashCode() {
		return row * 102523 + column;
	}

	@Override
	public String toString() {
		return "(" + row + ", " + column + ")";
	}

	public int row() {
		return row;
	}

	public int column() {
		return column;
	}

	@Override
	public int compareTo(Position pos) {
		return heuristicValue - pos.heuristicValue;
	}
}
