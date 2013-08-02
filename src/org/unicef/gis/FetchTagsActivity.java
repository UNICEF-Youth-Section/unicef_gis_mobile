package org.unicef.gis;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ProgressBar;

public class FetchTagsActivity extends Activity {
	ProgressBar progress;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fetch_tags);
		
		progress = (ProgressBar) findViewById(R.id.fetch_tags_activity_progress);
	}
}
