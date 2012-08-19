package me.dje.life;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.SystemClock;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;

public class LifeWallpaper extends WallpaperService {
	public static final String TAG = "LifeWallpaper";
	public static final String SHARED_PREFS_NAME = "droid_life_preferences";

	@Override
	public Engine onCreateEngine() {
		return new LifeEngine(this);
	}
	
	class LifeEngine extends Engine implements Runnable, 
			SharedPreferences.OnSharedPreferenceChangeListener {
		
		/* Configs */
		private int cSize = 10, cFPS = 10;
		private boolean wrapped = false;
		
		private CellularGrid cg = null;
		private ThemeManager theme = null;
		private Handler handler;		
		private Context context;
		
		/**
		 * Creates a new instance of the engine.
		 */
		public LifeEngine(Context context) {
			super();
			this.context = context;
			handler = new Handler();
			theme = new ThemeManager(this.context);
			SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME, 
					0);
			prefs.registerOnSharedPreferenceChangeListener(this);
			this.onSharedPreferenceChanged(prefs, null);
			setTouchEventsEnabled(true);
			run();
		}
		
		/**
		 * Run the animation.
		 */
		public void run() {
			final SurfaceHolder holder = this.getSurfaceHolder();
			Canvas c = null;
			long startTime = SystemClock.uptimeMillis();
			
			try {
				c = holder.lockCanvas();
				if(c != null) {
					if(cg == null) {
						try {
						cg = new CellularGrid(
								new CellularMapFile(context, "life.points")
									.getPoints("gosper"),
								(int)Math.ceil(c.getWidth()/cSize), 
								(int)Math.ceil(c.getHeight()/cSize),
								this.wrapped);
						} catch(Exception e) {
							Log.e(TAG, "Broken: " + e);
						}
						//cg.randomize();
						theme.setTheme(null, cg);
						theme.clear(c);
					}
					
					theme.draw(c);
					cg.step();
				}
				
			} catch(IllegalArgumentException e) {
				Log.d(TAG, "Caught exception: " + e);
			} finally {
				try {
					if(c != null) holder.unlockCanvasAndPost(c);
				} catch(IllegalArgumentException iae) {
					// This is necessary for some rotation weirdness 
				}
			}
			
			handler.removeCallbacks(this);
			if (this.isVisible()) {
				long delay = (1000 / cFPS) - (SystemClock.uptimeMillis() 
						- startTime);
				handler.postDelayed(this, (delay > 0)? delay : 1);
			}

		}
		
		/**
		 * 
		 */
		@Override
		public void onVisibilityChanged(boolean visible) {
			super.onVisibilityChanged(visible);
			if(visible) {
				this.run();
			} else {
				handler.removeCallbacks(this);
			}
		}
		
		/**
		 * Receive the touch event.
		 */
		public void onTouchEvent(MotionEvent m) {
			cg.set((int)m.getX() / cSize, (int)m.getY() / cSize, 4);
		}
		
		public void onSurfaceChanged(SurfaceHolder holder, int format, 
				int width, int height) {
			if(this.theme != null) {
				this.theme.rotate(width, height);
			}
		}

		@Override
		public void onSharedPreferenceChanged(SharedPreferences prefs, 
				String key) {
			if(key == null || key.compareTo("size") == 0) {
				// Reset this only if there is a change
				int size = Integer.parseInt(prefs.getString("size", "20"));
				if(size != cSize) {
					this.cSize = size;
					this.cg = null;
				}
			}
			
			this.cFPS = Integer.parseInt(prefs.getString("speed", "20"));
			
			boolean wrapped = prefs.getBoolean("wrapped", false);
			if(wrapped != this.wrapped) {
				if(this.cg != null)
					this.cg = null;
				this.wrapped = wrapped;
			}
			String themeName = prefs.getString("theme", "DefaultTheme");
			
			// This call is required initially before setRandom
			theme.setTheme(themeName, cg);
			
			if(themeName.compareTo("Random") == 0) {
				theme.setRandom(true, this.cFPS);
			}
		}
	}
}
