package visualizer;

public enum UpdateType {
	EXPLORING, PATH_FOUND, WALL, REMOVE_WALL, START, STOP, CHANGE_SPEED, CLEAR_WALLS, NO_PATH_FOUND, GENERATE_MAZE;

	public static UpdateType updateType;
}