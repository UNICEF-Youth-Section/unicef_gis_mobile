package org.unicef.gis.ui.report;

import org.unicef.gis.R;

import android.app.Activity;
import android.os.Bundle;

public class CreateReportActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_report);
		
		if (savedInstanceState != null)
			return;
		
		ChooseTagsFragment tagsFragment = new ChooseTagsFragment();
		getFragmentManager().beginTransaction().add(R.id.fragment_container, tagsFragment).commit();
		
		setTitle(R.string.tag_your_report);
	}
}
