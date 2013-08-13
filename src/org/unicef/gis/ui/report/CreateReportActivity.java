package org.unicef.gis.ui.report;

import org.unicef.gis.MyReportsActivity;
import org.unicef.gis.R;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

public class CreateReportActivity extends Activity {
	private ChooseTagsFragment tagsFragment;
	private ReportSummaryFragment reportSummaryFragment;

	private final int TAKE_PICTURE_INTENT = 10;
	private Bitmap image;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_report);
		
		if (savedInstanceState != null)
			return;
		
		tagsFragment = new ChooseTagsFragment();
		reportSummaryFragment = new ReportSummaryFragment();
		
		dispatchTakePictureIntent();
	}
	
	private void dispatchTakePictureIntent() {
		startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PICTURE_INTENT);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_CANCELED)
			startActivity(new Intent(this, MyReportsActivity.class));
		
		if (requestCode != TAKE_PICTURE_INTENT && resultCode != RESULT_OK) 
			return;
		
		Bundle extras = data.getExtras();
	    image = (Bitmap) extras.get("data");
	    moveToTagStep(null);
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

	public Bitmap getTakenPicture() {
		return image;
	}
}
