package org.unicef.gis;

import org.unicef.gis.R;
import org.unicef.gis.infrastructure.UnicefGisStore;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class ConfigureServerUrlActivity extends Activity {
	
	private TextView welcome;
	private EditText editUrl;
	private Button buttonSave;
	
	@Override
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_configure_server_url);
		
		welcome = (TextView) findViewById(R.id.configure_server_url_welcome);
		welcome.setText(R.string.welcome);
		
		editUrl = (EditText) findViewById(R.id.configure_server_url_edit_url);
		editUrl.setText(R.string.example_url);
		
		buttonSave = (Button) findViewById(R.id.configure_server_url_button_save);
		buttonSave.setText(R.string.save_address);
	};
	
	public void saveAddress(View view) {
		UnicefGisStore store = new UnicefGisStore(this);
		store.saveAddress(editUrl.getText().toString());
		
		startActivity(new Intent(this, FetchTagsActivity.class));
	}
}
