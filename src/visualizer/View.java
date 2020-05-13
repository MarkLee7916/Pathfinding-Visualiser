package visualizer;

import java.awt.BorderLayout;

import java.awt.Color;
import java.awt.GridLayout;
import java.util.EnumMap;
import java.util.Observable;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class View extends Observable {
	// Allows us to access a tile given a position on the grid
	private final JButton[][] tiles;

	// Main frame that the GUI runs on
	private final JFrame frame;

	// Main panel that all tiles on the grid are placed on
	private final JPanel grid;

	// The grid's total area is SIZE * SIZE
	private final int size;

	// Options to control visualizer
	private final JPanel options;

	// Maps a tile's type onto the colour it's displayed in
	private final EnumMap<TileType, Color> colorToTileType;

	// Flag to signal what view wants to notify controller about
	private UpdateType updateType;

	private Position start, goal;

	public View(int si, Position st, Position g) {
		size = si;
		start = st;
		goal = g;

		colorToTileType = new EnumMap<>(TileType.class);
		buildMapping();

		frame = new JFrame("Pathfinding visualizer");
		grid = new JPanel(new GridLayout(0, size));

		options = new JPanel();
		setUpPlayerOptions();

		tiles = new JButton[size][size];
		setupGridButtons();
		addGridBehaviour();
		colourStartAndGoalTiles();

		addComponentsToFrame();
		configureFrame();
	}

	private void colourStartAndGoalTiles() {
		updateTileColour(TileType.START, start.row(), start.column());
		updateTileColour(TileType.GOAL, goal.row(), goal.column());
	}

	// Add tileType-colour pairs to EnumMap
	private void buildMapping() {
		colorToTileType.put(TileType.BLANK, Color.WHITE);
		colorToTileType.put(TileType.PATH, Color.CYAN);
		colorToTileType.put(TileType.START, Color.RED);
		colorToTileType.put(TileType.EXPLORED, Color.PINK);
		colorToTileType.put(TileType.GOAL, Color.GREEN);
		colorToTileType.put(TileType.WALL, Color.GRAY);
	}

	private void setUpPlayerOptions() {
		setupRunButton();
		setUpClearButton();
		setUpSpeedSlider();
		setUpGenerateMazeButton();
	}

	private void setUpGenerateMazeButton() {
		JButton mazeGen = new JButton("Random Maze");

		mazeGen.addActionListener(actionEvent -> {
			setChanged();
			updateType = UpdateType.GENERATE_MAZE;
			notifyObservers();
		});

		options.add(mazeGen);
	}

	// Set up button that removes all the walls from the grid
	private void setUpClearButton() {
		JButton clear = new JButton("Clear Walls");

		clear.addActionListener(actionEvent -> {
			setChanged();
			updateType = UpdateType.CLEAR_WALLS;
			notifyObservers();

			clearColour(TileType.WALL);
		});

		options.add(clear);
	}

	// Clears every tile in the grid that matches a given colour
	private void clearColour(TileType tileType) {
		for (int row = 0; row < size; row++)
			for (int column = 0; column < size; column++)
				if (tiles[row][column].getBackground() == colorToTileType.get(tileType))
					tiles[row][column].setBackground(colorToTileType.get(TileType.BLANK));

	}

	// Set up slider that allows user to configure the speed of the algorithm
	private void setUpSpeedSlider() {
		JSlider slider = new JSlider(1, 100);

		configureSliderSettings(slider);

		slider.addChangeListener(changeEvent -> {
			setChanged();
			updateType = UpdateType.CHANGE_SPEED;
			notifyObservers(slider.getValue());
		});

		options.add(slider);
	}

	private void configureSliderSettings(JSlider slider) {
		slider.setMajorTickSpacing(20);
		slider.setMinorTickSpacing(5);
		slider.setPaintTicks(true);
		slider.setInverted(true);
	}

	// Set up button that allows to user to start and stop the algorithm from
	// running
	private void setupRunButton() {
		JButton run = new JButton("Start");

		run.addActionListener(actionEvent -> {
			setChanged();

			if (run.getText().equals("Start")) {
				updateType = UpdateType.START;
				run.setText("Stop");
			} else {
				updateType = UpdateType.STOP;
				run.setText("Start");
			}

			notifyObservers();
		});

		options.add(run);
	}

	public UpdateType getUpdateType() {
		return updateType;
	}

	public void updateTileColour(TileType tileType, int row, int column) {
		tiles[row][column].setBackground(colorToTileType.get(tileType));
	}

	private void configureFrame() {
		frame.setSize(1000, 1000);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	private void addComponentsToFrame() {
		frame.getContentPane().add(BorderLayout.CENTER, grid);
		frame.getContentPane().add(BorderLayout.NORTH, options);
	}

	// Create buttons and add to panel
	private void setupGridButtons() {
		for (int row = 0; row < size; row++) {
			for (int column = 0; column < size; column++) {
				JButton button = new JButton();
				tiles[row][column] = button;
				grid.add(button);
				updateTileColour(TileType.BLANK, row, column);
			}
		}
	}

	private boolean isWallTile(int row, int column) {
		return tiles[row][column].getBackground() == colorToTileType.get(TileType.WALL);
	}

	private boolean isBlankTile(int row, int column) {
		return tiles[row][column].getBackground() == colorToTileType.get(TileType.BLANK);
	}

	private void addGridBehaviour() {
		for (int row = 0; row < size; row++)
			for (int column = 0; column < size; column++)
				addGridButtonBehaviour(row, column);
	}

	private void addGridButtonBehaviour(int row, int column) {
		tiles[row][column].addActionListener(actionEvent -> {
			if (isWallTile(row, column))
				eraseWall(row, column);
			else if (isBlankTile(row, column))
				newWall(row, column);
			else
				return;

			setChanged();
			notifyObservers(new Position(row, column));
		});
	}

	private void eraseWall(int row, int column) {
		updateTileColour(TileType.BLANK, row, column);
		updateType = UpdateType.REMOVE_WALL;
	}

	private void newWall(int row, int column) {
		updateTileColour(TileType.WALL, row, column);
		updateType = UpdateType.WALL;
	}

	public void noPathFoundMessage() {
		JOptionPane.showMessageDialog(null, "No path found");
	}
}
