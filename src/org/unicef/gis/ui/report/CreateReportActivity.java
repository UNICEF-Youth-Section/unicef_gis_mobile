package org.unicef.gis.ui.report;

import org.unicef.gis.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;

public class CreateReportActivity extends Activity {
	private ChooseTagsFragment tagsFragment;
	private ReportSummaryFragment reportSummaryFragment; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_report);
		
		if (savedInstanceState != null)
			return;
		
		tagsFragment = new ChooseTagsFragment();
		reportSummaryFragment = new ReportSummaryFragment();
		
		moveToTagStep(null);
		
		setTitle(R.string.tag_your_report);
	}
	
	private void moveToAnotherStep(Fragment current, Fragment newFragment) {
		FragmentTransaction tx = getFragmentManager().beginTransaction();
		
		if (current == null)		
			tx.add(R.id.fragment_container, newFragment);
		else
			tx.replace(R.id.fragment_container, newFragment);
			
		tx.commit();
	}
	
	private void moveToTagStep(Fragment current) {
		moveToAnotherStep(current, tagsFragment);
	}
	
	private void moveToSummaryStep(ChooseTagsFragment current) {
		moveToAnotherStep(current, reportSummaryFragment);
	}

	public void onTagsChosen(View view) {
		moveToSummaryStep(tagsFragment);			
	}
}
