package me.dje.life;

import java.util.Random;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Paint;

public class CircleTheme extends LifeBaseTheme {
	public static final String TAG = "CircleTheme";
	private Paint bgPaint, fgPaint;
	private Paint discoPaint[];
	private Random rand;
	private Bitmap bg;
	private boolean redraw;
	
	public CircleTheme(Context context, CellularGrid grid) {
		super(context, grid);
		rand = new Random();
		redraw = false;
	}
	
	public void onSharedPreferenceChanged(SharedPreferences prefs, String arg1) {
		super.onSharedPreferenceChanged(prefs, arg1);
		
		bgPaint = createPaint(this.getBackground());
		if(prefs.getBoolean("disco", false)) {
			if(discoPaint == null) {
				discoPaint = new Paint[3];
				discoPaint[0] = createPaint(0xD00000);
				discoPaint[1] = createPaint(0x00D000);
				discoPaint[2] = createPaint(0x0000D0);
			}
		} else {
			discoPaint = null;
			fgPaint = createPaint(this.getForeground());
		}
		
	}
	
	public void clear(Canvas c) {
		c.drawPaint(this.bgPaint);
		Canvas bgC = new Canvas(bg);
		bgC.drawPaint(this.bgPaint);
		redraw = true;
	}
	
	public void draw(Canvas c) {
		if(bg == null || bg.getWidth() != c.getWidth()) {
			bg = Bitmap.createBitmap(c.getWidth(), c.getHeight(), 
					Bitmap.Config.ARGB_8888);
			this.clear(c);
		}
		Canvas tmpCanvas = new Canvas(bg);
		Paint p = fgPaint;
		
		int cellWidth = tmpCanvas.getWidth() / grid.getWidth();
		int cellHeight = tmpCanvas.getHeight() / grid.getHeight();
		boolean portrait = (tmpCanvas.getWidth() < tmpCanvas.getHeight());
		
		for(int x = 0; x < grid.getWidth(); x++) {
			for(int y = 0; y < grid.getHeight(); y++) {
				if(redraw || grid.changed(x, y)) {
					if(discoPaint != null) {
						int i = rand.nextInt(discoPaint.length);
						p = discoPaint[i];
					}
					if(portrait) {
						int left = (x * cellWidth);
						int top = (y * cellWidth);
						if(grid.get(x, y))
							tmpCanvas.drawCircle(left + (cellWidth / 2), 
									top + (cellWidth / 2), 
									cellWidth / 2 - 1, p);
						else
							tmpCanvas.drawRect(left, top, left + cellWidth, 
									top + cellWidth, bgPaint);
					} else {
						int left = (x * cellWidth);
						int top = (y * cellWidth);
						if(grid.get(x, y))
							tmpCanvas.drawCircle(left + (cellWidth / 2), 
									top + (cellWidth / 2), 
									cellWidth / 2 - 1, p);
						else
							tmpCanvas.drawRect(left, top, left + cellWidth, 
									top + cellWidth, bgPaint);
					}
				}
			}
		}
		redraw = false;
		c.drawBitmap(bg, 0, 0, fgPaint);
	}
	
	/**
	 * Change the bg Bitmap and schedule a redraw.
	 */
	public void rotate(int width, int height) {
		//TODO: fancy rotation matrix
		bg = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		Canvas bgC = new Canvas(bg);
		bgC.drawPaint(this.bgPaint);
		redraw = true;
	}
	
	private Paint createPaint(int color) {
		Paint p = new Paint();
		p.setColor(color);
		p.setAlpha(0xFF);
		if(this.getBlurred()) {
			p.setMaskFilter(new BlurMaskFilter(3, 
					BlurMaskFilter.Blur.NORMAL));
		}
		return p;
	}

}
