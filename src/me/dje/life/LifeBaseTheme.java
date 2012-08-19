package me.dje.life;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;

/**
 * 
 * @author dylan
 *
 */
public abstract class LifeBaseTheme 
		implements SharedPreferences.OnSharedPreferenceChangeListener {
	private int background,
				foreground;
	private boolean inverted = false, 
					blurred = false;

	protected CellularGrid grid;
	protected Context context;

	/**
	 * 
	 * @param context
	 * @param grid
	 */
	public LifeBaseTheme(Context context, CellularGrid grid) {
		this.grid = grid;
		this.context = context;
		
		SharedPreferences prefs = 
				context.getSharedPreferences(LifeWallpaper.SHARED_PREFS_NAME, 
						0);
		this.onSharedPreferenceChanged(prefs, null);
		prefs.registerOnSharedPreferenceChangeListener(this);
	}
	
	/**
	 * Change the current grid.
	 * @param grid The grid to use.
	 */
	public void reset(CellularGrid grid) {
		this.grid = grid;
	}
	
	/**
	 * Clear the current canvas.
	 * @param c The canvas to clear.
	 */
	abstract public void clear(Canvas c);
	
	/**
	 * Draw the graphics on the canvas.
	 * @param c The canvas to draw on.
	 */
	abstract public void draw(Canvas c);
	
	/**
	 * Optional rotation method called when the device is rotated.
	 * @param width The new width
	 * @param height The new height
	 */
	public void rotate(int width, int height) {
		
	}
	
	@Override
	public void onSharedPreferenceChanged(SharedPreferences prefs, String k) {
		background = prefs.getInt("background", 0);
		foreground = prefs.getInt("foreground", 0xFF11EE11);
		inverted = prefs.getBoolean("inverted", inverted);
		blurred  = prefs.getBoolean("blurred", blurred);
		
	}
	
	/**
	 * Get the background color as an integer.
	 * @return The colour
	 */
	public int getBackground() {
		return background;
	}
	
	/**
	 * Get the foreground color as an integer.
	 * @return The colour
	 */
	public int getForeground() {
		return foreground;
	}
	
	/**
	 * Get the state of the inverted preferences.
	 * @return The inverted preference.
	 */
	public boolean getInverted() {
		return inverted;
	}
	
	/**
	 * Get the blurred preference.
	 * @return The blurred preference.
	 */
	public boolean getBlurred() {
		return blurred;
	}
}
