package me.dje.life;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Use android icons as cells.
 * @author dylan
 *
 */
public class AndroidTheme extends LifeBaseTheme {
	public static String TAG = "AndroidTheme";
	private Paint bgOddPaint, bgEvenPaint, fgPaint;
	private Bitmap android;
	public AndroidTheme(Context context, CellularGrid grid) {
		super(context, grid);
		android = BitmapFactory.decodeResource(context.getResources(),
				R.drawable.ic_android);
	}

	@Override
	public void clear(Canvas c) {
		
	}

	@Override
	public void draw(Canvas c) { 
		int cWidth = c.getWidth() / grid.getWidth();
		for(int x = 0; x < grid.getWidth(); x++) {
			for(int y = 0; y < grid.getHeight(); y++) {
				Paint p;
				if(((x & 1) == 1 && (y & 1) == 1) || (x & 1) != 1 && (y & 1) != 1) {
					p = bgOddPaint;
				} else {
					p = bgEvenPaint;
				}
				c.drawRect(x * cWidth, y * cWidth, (x+1) * cWidth, (y+1) * cWidth, p);
				if(grid.get(x, y)) {
					Rect src = new Rect(0, 0, android.getWidth(), 
							android.getHeight());
					Rect dest = new Rect(x * cWidth, y * cWidth, (x+1) * cWidth, (y+1) * cWidth);
					c.drawBitmap(android, src, dest, fgPaint);
					
				}
			}
		}

	}
	
	public void onSharedPreferenceChanged(SharedPreferences prefs, String opt) {
		super.onSharedPreferenceChanged(prefs, opt);
		
		fgPaint = new Paint();
		bgOddPaint = new Paint();
		bgOddPaint.setColor(this.getBackground());
		bgOddPaint.setAlpha(0xFF);
		float[] hsv = new float[3];
		Color.colorToHSV(this.getBackground(), hsv);
		if(hsv[2] < 0.5) {
			hsv[2] += 0.15;
		} else {
			hsv[2] -= 0.15;
		}
		bgEvenPaint = new Paint();
		bgEvenPaint.setColor(Color.HSVToColor(hsv));
		bgEvenPaint.setAlpha(0xff);
	}

}
