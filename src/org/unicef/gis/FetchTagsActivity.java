package org.unicef.gis;

import org.unicef.gis.infrastructure.Network;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FetchTagsActivity extends Activity {
	private ProgressBar progress;
	private TextView feedback;
	private Button goToNetworkSettings;
	private Button retry;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fetch_tags);
		
		loadControls();		
		
		goToNetworkSettings.setText(R.string.go_to_network_settings);
		retry.setText(R.string.retry);
	}
	
	@Override 
	protected void onResume() {
		super.onResume();
		checkConnectivity();
	}
	
	public void editNetworkSettings(View view){
		startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
	}
	
	public void retry(View view) {
		checkConnectivity();
	}

	private void checkConnectivity() {
		Network network = new Network(this);
		if (!network.connected()) {
			feedback.setText(R.string.fetch_tags_not_connected);
			progress.setVisibility(View.GONE);
			goToNetworkSettings.setVisibility(View.VISIBLE);
			retry.setVisibility(View.GONE);			
		} else {
			feedback.setText(R.string.wait_configuring);
			progress.setVisibility(View.VISIBLE);
			goToNetworkSettings.setVisibility(View.GONE);
			retry.setVisibility(View.GONE);
			
			//If connectivity is alright, fetch the tags
			fetchTags();
		}	
	}

	private void fetchTags() {
		FetchTagsTask fetchTagsTask = new FetchTagsTask(this);
		fetchTagsTask.execute();		
	}

	private void loadControls() {
		progress = (ProgressBar) findViewById(R.id.fetch_tags_activity_progress);
		feedback = (TextView) findViewById(R.id.fetch_tags_feedback);
		goToNetworkSettings = (Button) findViewById(R.id.fetch_tags_go_to_network_settings);
		retry = (Button) findViewById(R.id.fetch_tags_retry);
	}

	public void onFetchTagsResult(boolean success) {
		if (success){
			startActivity(new Intent(this, MyReportsActivity.class));
		} else {
			feedback.setText(R.string.something_wrong_fetching_tags);
			progress.setVisibility(View.GONE);
			goToNetworkSettings.setVisibility(View.GONE);
			retry.setVisibility(View.VISIBLE);
		}
	}
}
