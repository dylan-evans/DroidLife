package me.dje.life;

import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Manage the theme.
 * @author dylan
 *
 */
public class ThemeManager {
	public static String DEFAULT_THEME = "CircleTheme";
	public static String THEMES[] = {"CircleTheme", "ClassicTheme", "AndroidTheme"};
	private CellularGrid grid;
	private LifeBaseTheme currentTheme;
	private String currentThemeName;
	private Context context;
	private Paint black;
	private boolean random;
	private int tick, max, fps;

	
	/**
	 * 
	 * @param context
	 */
	public ThemeManager(Context context, boolean random) {
		this.context = context;
		black = new Paint();
		black.setColor(0);
		black.setAlpha(0xFF);
		this.random = random;
		this.tick = 0;
		this.max = 0;
		this.fps = 25;
	}
	
	public ThemeManager(Context context) {
		this(context, false);
	}
	
	/**
	 * Set the current theme by name. This method also unregisters any old
	 * themes from listening to preferences.
	 * @param themeName The name of the theme.
	 * @param grid The current grid.
	 * @return The created LifeBaseTheme
	 */
	public LifeBaseTheme setTheme(String themeName, CellularGrid grid) {
		if(grid == null) {
			return null;
		}
		this.grid = grid;
		SharedPreferences prefs = context.getSharedPreferences(
				LifeWallpaper.SHARED_PREFS_NAME, 0);
		if(themeName == null) {
			themeName = prefs.getString("theme", DEFAULT_THEME);
		}
		if(currentTheme != null) {
			prefs.unregisterOnSharedPreferenceChangeListener(
					this.currentTheme);
		} 
		
		currentTheme = createThemeByName(themeName, grid);
		currentThemeName = themeName;
		return currentTheme;
	}
	
	/**
	 * Clear the canvas if any theme exists.
	 * @param c The canvas to draw on
	 */
	public void clear(Canvas c) {
		if(currentTheme != null) {
			c.drawPaint(black);
			currentTheme.draw(c);
		}
	}
	
	/**
	 * Draw the graphics onto the specified canvas.
	 * @param c The canvas to draw on
	 */
	public void draw(Canvas c) {
		tick++;
		if(random && tick > (max * fps)) {
			randomTheme();
		}
		if(currentTheme != null) {
			currentTheme.draw(c);
		}
	}
	
	/**
	 * Calls the rotation method of the current theme.
	 */
	public void rotate(int width, int height) {
		if(currentTheme != null) {
			currentTheme.rotate(width, height);
		}
	}
	
	/**
	 * Create a theme as specified by the given string. This function provides
	 * the essential kludge of mapping preference strings into classes.
	 * @param themeName The name of the theme
	 * @param grid The CellularGrid
	 * @return The created theme
	 */
	private LifeBaseTheme createThemeByName(String themeName, CellularGrid grid) {
		LifeBaseTheme t = null;
		if(themeName.compareTo("Random") == 0) {
			return null;
		}
		if(themeName == null) {
			t = new CircleTheme(context, grid);
		} else if(themeName.compareTo(ClassicTheme.TAG) == 0) {
			t = new ClassicTheme(context,grid);
		} else if(themeName.compareTo(AndroidTheme.TAG) == 0) {
			t = new AndroidTheme(context, grid);			
		} else {
			t = new CircleTheme(context, grid);
		}
		
		return t;
	}
	
	private void randomTheme() {
		Random r = new Random();
		this.setTheme(THEMES[r.nextInt(THEMES.length)], grid);
		max = r.nextInt(120) + 30;
		tick = 0;
	}

	/**
	 * Get the name of the currently set theme.
	 * @return The name of the theme.
	 */
	public String getCurrentThemeName() {
		return currentThemeName;
	}

	public boolean isRandom() {
		return random;
	}

	public void setRandom(boolean random, int fps) {
		this.fps = fps;
		this.random = random;
		if(grid != null) randomTheme();
	}
	
}
