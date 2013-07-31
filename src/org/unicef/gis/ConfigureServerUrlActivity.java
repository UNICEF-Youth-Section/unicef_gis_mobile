package org.unicef.gis;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

public class ConfigureServerUrlActivity extends Activity {
	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configure_server_url);
	};
	
	public void handleClick(View view) {
		setResult(RESULT_OK, new Intent());
		finish();
	}
}
