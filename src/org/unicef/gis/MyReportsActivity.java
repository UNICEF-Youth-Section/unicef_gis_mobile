package org.unicef.gis;

import org.unicef.gis.infrastructure.RoutesResolver;
import org.unicef.gis.infrastructure.UnicefGisStore;

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
	}

	private void checkTags() {
		UnicefGisStore store = new UnicefGisStore(this);
		
		if (!store.tagsHaveBeenFetched())
			startActivity(new Intent(this, FetchTagsActivity.class));
	}

	private void checkAddressPreference() {
		RoutesResolver routes = new RoutesResolver(this);
		String baseUrl = routes.getBaseUrl(); 
		
		if (baseUrl == null || baseUrl.isEmpty())
			startActivity(new Intent(this, ConfigureServerUrlActivity.class));
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RequestCodes.CONFIG_SERVER_URL) {
			statusView.setText("SERVER URL SET");
		}
	}
}
