package org.unicef.gis;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FetchTagsActivity extends Activity {
	ProgressBar progress;
	TextView feedback;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fetch_tags);
		
		loadControls();
		
		feedback.setText(R.string.wait_configuring);
	}

	private void loadControls() {
		progress = (ProgressBar) findViewById(R.id.fetch_tags_activity_progress);
		feedback = (TextView) findViewById(R.id.fetch_tags_feedback);
	}
}
