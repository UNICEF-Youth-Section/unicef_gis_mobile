package org.unicef.gis;

import java.util.List;

import org.unicef.gis.infrastructure.RoutesResolver;
import org.unicef.gis.infrastructure.ServerUrlPreferenceNotSetException;
import org.unicef.gis.infrastructure.UnicefGisStore;
import org.unicef.gis.model.Tag;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class MyReportsActivity extends Activity {
	private TextView statusView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_reports);
		
		statusView = (TextView) findViewById(R.id.my_reports_status);
		
		checkAddressPreference();
		checkTags();
		deleteMeDisplayTags();
	}

	private void deleteMeDisplayTags() {
		ReadAllTagsTask task = new ReadAllTagsTask(this);
		task.execute();
	}
	
	public void onReadAllTagsResult(List<Tag> tags) {
		StringBuffer sb = new StringBuffer();
		for (Tag tag : tags) {
			sb.append(tag.getValue() + " ");
		}
		statusView.setText(sb.toString());
	}

	private void checkTags() {
		UnicefGisStore store = new UnicefGisStore(this);
		
		if (!store.tagsHaveBeenFetched())
			startActivity(new Intent(this, FetchTagsActivity.class));
	}

	private void checkAddressPreference() {
		RoutesResolver routes = new RoutesResolver(this);
		try {
			routes.getBaseUrl();
		} catch (ServerUrlPreferenceNotSetException e) {
			startActivity(new Intent(this, ConfigureServerUrlActivity.class));
		} 	
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RequestCodes.CONFIG_SERVER_URL) {
			statusView.setText("SERVER URL SET");
		}
	}
}
