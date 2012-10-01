package me.dje.life;

import java.util.Iterator;
import java.util.Random;

import me.dje.graph.GraphBuilder;
import me.dje.graph.Vertex;
import android.graphics.Point;

/**
 * CellularGrid is the data model of Droid Life. It provides the engine for 
 * stepping through the cellular automation and provides the ruleset.
 * @author dylan
 *
 */
public class CellularGrid {
	private int width, height;
	private boolean wrap;
	private boolean grid[], prev[];
	private boolean changeMap[];
	private Random rand;
	
	/**
	 * Create a grid with the given map file.
	 * @param map
	 * @param width
	 * @param height
	 * @param wrap
	 */
	public CellularGrid(CellularMapFile.Map map, int width, int height, 
			boolean wrap) {
		this.width = width;
		this.height = height;
		this.prev = null;
		this.grid = new boolean[width * height];
		this.changeMap = new boolean[width * height];
		this.wrap = wrap;
		rand = new Random();
		for(CellularMapFile.Point p : map) {
			grid[(p.y * width) + p.x] = true;
		}
	}
	
	public CellularGrid(int width, int height, boolean wrap) {
		this.width = width;
		this.height = height;
		this.prev = null;
		this.grid = new boolean[width * height];
		this.changeMap = new boolean[width * height];
		rand = new Random();
		this.wrap = wrap;
	}
	
	public CellularGrid(int width, int height) {
		this(width, height, true);
	}
	
	public CellularGrid(int width, int height, CellularGrid map) {
		this(width, height);
		
		if(map.width == this.width && map.height == this.height) {
			// Copy
			for(int i = 0; i < grid.length; i++) grid[i] = map.grid[i];
		} else if(map.width == this.height && map.height == this.width) {
			// Rotate
			for(int x = 0; x < this.width; x++) {
				for(int y = 0; y < this.height; y++) {
					grid[(y * height) + x] = map.grid[(x * width) + y];
				}
			}
		}
	}
	
	private int calculate_value(int x, int y) {
		int val = 0;
		for(int xm = -1; xm < 2; xm++) {
			for(int ym = -1; ym < 2; ym++) {
				int xp = x + xm;
				int yp = y + ym;
				if(wrap) {
					if(xp < 0) xp = this.width + xm;
					else if(xp >= this.width) xp = xm;
					if(yp < 0) yp = this.height + ym;
					else if(yp >= this.height) yp = ym;
				}
				if( (xm != 0 || ym != 0)
						&& xp >= 0 && xp < this.width
						&& yp >= 0 && yp < this.height
						&& grid[(yp * width) + xp])
					val++;	
			}
		}
		return val;
	}
	
	/**
	 * Step to the next frame in the simulation.
	 */
	public void step() {
		boolean next[];
		if(this.prev == null)
			next = new boolean[width * height];
		else
			next = this.prev;

		int flipper = 0;
		for(int x = 0; x < width; x++) {
			for(int y = 0; y < height; y++) {
				int val = calculate_value(x, y);
				changeMap[(y * width) + x] = false;
				if(val == 3) {
					next[(y * width) + x] = true;
					if(!grid[(y * width) + x]) 
						changeMap[(y * width) + x] = true;
				} else if(val < 2 || val > 3) {
					next[(y * width) + x] = false;
					if(grid[(y * width) + x]) 
						changeMap[(y * width) + x] = true;
				} else {
					next[(y * width) + x] = grid[(y * width) + x];
				}
			}
			
		}
		this.prev = this.grid;
		this.grid = next;
	}
	
	/**
	 * Step ahead a specified number of times.
	 * @param n The number of steps
	 */
	public void step(int n) {
		for(int i = 0; i < n; i++)
			step();
	}
	
	/**
	 * Randomize the whole grid.
	 */
	public void randomize() {
		for(int i = 0; i < grid.length; i++) {
			grid[i] = rand.nextBoolean();
		}
	}
	
	/**
	 * Get the status of the specified cell.
	 * @param x The horizontal position
	 * @param y The vertical position
	 * @return The value of the cell
	 */
	public boolean get(int x, int y) {
		return grid[(y * width) + x];
	}
	
	/**
	 * Determine if a cell changed in the last step.
	 * @param x The horizontal position
	 * @param y The vertical position
	 * @return True if the cell has changed
	 */
	public boolean changed(int x, int y) {
		return changeMap[(y * width) + x];
	}
	
	/**
	 * Set the cell at the specified coordinate.
	 * @param x The horizontal postion
	 * @param y The vertical position
	 */
	public void set(int x, int y) {
		if(x >= 0 && x < width && y >= 0 && y < height)
			grid[(y * width) + x] = true;
	}
	
	/**
	 * Randomly set a square section of the grid. This is for implementing a
	 * touch based interface.
	 * @param x The horizontal centre position
	 * @param y The vertical centre position
	 * @param r The width of the square
	 * @deprecated Needs to be changed to a more appropriate name.
	 */
	public void set(int x, int y, int r) {
		for(int xp = x - r; xp < x + r; xp++) {
			for(int yp = y - r; yp < y + r; yp++) {
				if(xp >= 0 && xp < width && yp >= 0 && yp < height)
					grid[(yp * width) + xp] = this.rand.nextBoolean();

			}
		}
		set(x, y);
	}
	
	/**
	 * Get the width of the grid.
	 * @return The width as an integer
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Get the height of the grid.
	 * @return The height as an integer
	 */
	public int getHeight() {
		return height;
	}
	
	//  TODO
	public void analyze() {
		//GraphSet<Point,String> gs = new GraphSet<Point,String>(new GridBuild());
	}
	
	class GridBuild extends GraphBuilder<Point,String> {
		private int x = 0, y = 0;
		private Point node = null;
		@Override
		public void build(Vertex<Point, String> v,
				Iterator<Vertex<Point, String>> set) {
			while(set.hasNext()) {
				Vertex<Point, String> vert = set.next();
				if(true) {
					v.addEdge(vert, "");
				}
			}
		}
		
		public Point next() {
			node = null;
			if(hasNext()) {
				return node;
			}
			
			return null;
		}
		
		public boolean hasNext() {
			if(node != null) return true;
			for(; x < width; x++) {
				for(; y < height; y++) {
					if(grid[(y * width) + x]) {
						node = new Point(x, y);
					}
				}
			}
			return false;
		}
		
	}
	



	
}
