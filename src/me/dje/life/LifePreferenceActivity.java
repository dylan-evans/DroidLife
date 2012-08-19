package me.dje.life;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class LifePreferenceActivity extends PreferenceActivity {
	public LifePreferenceActivity() {
		super();
	}
	
	@Override
	public void onCreate(Bundle state) {
		super.onCreate(state);
		PreferenceManager pm = this.getPreferenceManager();
		pm.setSharedPreferencesName(LifeWallpaper.SHARED_PREFS_NAME);
		this.addPreferencesFromResource(R.xml.preferences);
	}
}
