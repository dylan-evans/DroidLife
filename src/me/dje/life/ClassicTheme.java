package me.dje.life;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

public class ClassicTheme extends LifeBaseTheme {
	public static final String TAG = "ClassicTheme";
	public Paint bgOddPaint, bgEvenPaint, fgPaint;

	public ClassicTheme(Context context, CellularGrid grid) {
		super(context, grid);
	}
	
	@Override
	public void clear(Canvas c) {
		int cWidth = c.getWidth() / grid.getWidth();
		for(int x = 0; x < grid.getWidth(); x++) {
			for(int y = 0; y < grid.getHeight(); y++) {
				Paint p;
				if(((x & 1) == 1 && (y & 1) == 1) || (x & 1) != 1 && (y & 1) != 1) {
					p = bgOddPaint;
				} else {
					p = bgEvenPaint;
				}
				Log.d(TAG, "paint: " + p);
				c.drawRect(x * cWidth, y * cWidth, (x+1) * cWidth, (y+1) * cWidth, p);
			}
		}
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
					c.drawRect(x * cWidth + 1, y * cWidth + 1, 
							(x+1) * cWidth - 1, (y+1) * cWidth - 1, fgPaint);
				}
			}
		}

	}
	
	public void onSharedPreferenceChanged(SharedPreferences prefs, String opt) {
		super.onSharedPreferenceChanged(prefs, opt);
		
		fgPaint = new Paint();
		fgPaint.setColor(this.getForeground());
		fgPaint.setAlpha(0xFF);
		if(this.getBlurred()) {
			fgPaint.setMaskFilter(new BlurMaskFilter(3, BlurMaskFilter.Blur.NORMAL));
		}
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
