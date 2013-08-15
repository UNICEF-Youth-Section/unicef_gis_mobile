package org.unicef.gis.ui.report;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.unicef.gis.MyReportsActivity;
import org.unicef.gis.R;
import org.unicef.gis.infrastructure.Camera;
import org.unicef.gis.infrastructure.ILocationServiceConsumer;
import org.unicef.gis.infrastructure.LocationService;
import org.unicef.gis.infrastructure.UnicefGisStore;
import org.unicef.gis.model.Tag;
import org.unicef.gis.ui.AlertDialogFragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class CreateReportActivity extends Activity implements ILocationServiceConsumer {	
	private ChooseTagsFragment tagsFragment;
	private ReportSummaryFragment reportSummaryFragment;	
	
	private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 11;
	
	private LocationService locationService;
	
	private Bitmap imageThumbnail = null;
	private File imageFile;
	private Location location = null;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_report);
				
		loadFragments();		
		loadLocationService();
		
		tryToTakePicture();
	}

	private void tryToTakePicture() {
		try {
			Camera camera = new Camera(this);		
			imageFile = camera.takePicture();
		} catch (IOException e) {
			showAlertDialog(R.string.configuration_problem, R.string.configuration_problem_prompt, "configuration_problem");
			e.printStackTrace();
			finish();
		}
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		locationService.start();
	}
	
	@Override
	protected void onStop() {
		locationService.stop();		
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		servicesConnected();
	}
	
	private void loadLocationService() {
		locationService = new LocationService(this, this);
	}

	private void loadFragments() {
		tagsFragment = new ChooseTagsFragment();
		reportSummaryFragment = new ReportSummaryFragment();
	}	
	
	private boolean servicesConnected() {
		// Check that Google Play services is available        
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        
        if (status != ConnectionResult.SUCCESS) {
          if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
            showErrorDialog(status);
          } else {
            Toast.makeText(this, "This device is not supported.", Toast.LENGTH_LONG).show();
            finish();
          }
          
          return false;
        }
        
        return true;
    }	
	
	private void showErrorDialog(int code) {
	  GooglePlayServicesUtil.getErrorDialog(code, this, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_RECOVER_PLAY_SERVICES){
			handleConnectionFailureResolutionRequest(resultCode, data);
			return;
		}
					
		if (resultCode == RESULT_CANCELED)
			startActivity(new Intent(this, MyReportsActivity.class));
		
		if (requestCode != Camera.TAKE_PICTURE_INTENT && resultCode != RESULT_OK) 
			return;		
		
		Camera camera = new Camera(this);
		camera.addPicToGallery(imageFile);
				
	    moveToTagStep(null);
	}
	
	private void handleConnectionFailureResolutionRequest(int resultCode, Intent data) {
		if (resultCode == RESULT_CANCELED) {
			Toast.makeText(this, "Google Play Services must be installed.", Toast.LENGTH_SHORT).show();
	        finish();
		}
		return;		
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

	public Bitmap getTakenPictureThumbnail(ImageView imageView) {
		if (imageThumbnail == null){
			Camera camera = new Camera(this);
			imageThumbnail = camera.getThumbnail(imageFile, imageView.getWidth(), imageView.getHeight());
		}
		
		return imageThumbnail;
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		/*
         * Google Play services can resolve some errors it detects.
         * If the error has a resolution, try sending an Intent to
         * start a Google Play services activity that can resolve
         * error.
         */
        if (result.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                result.startResolutionForResult(
                        this,
                        REQUEST_CODE_RECOVER_PLAY_SERVICES);
                /*
                 * Thrown if Google Play services canceled the original
                 * PendingIntent
                 */
            } catch (IntentSender.SendIntentException e) {
                // Log the error
                e.printStackTrace();
            }
        } else {
            /*
             * If no resolution is available, display a dialog to the
             * user with the error.
             */
            showErrorDialog(result.getErrorCode());
        }
	}

	@Override
	public void onConnected(Bundle connectionHint) {
		locationService.playServicesConnected();        
	}

	@Override
	public void onDisconnected() {
		locationService.playServicesDisconnected();		
	}

	@Override
	public void onLocationChanged(Location location) {
		this.location = location;
		reportSummaryFragment.setLocation(this.location);
	}
	
	public void onSaveReport(View view) {
		if (!validate()) return;
		
		saveReport();
		
		finish();
	}

	private void saveReport() {
		String description = reportSummaryFragment.getReportDescription();
		List<Tag> tags = null;
		
		Camera camera = new Camera(this);		
		UnicefGisStore store = new UnicefGisStore(this);
		
		store.saveReport(description, location, camera.getUri(imageFile), tags);
		
		finish();
	}

	private boolean validate() {
		if (location == null) {
			showLocationMissingDialog();
			return false;
		} else if (reportSummaryFragment.getReportDescription().isEmpty()) {
			showDescriptionMissingDialog();
			return false;
		}
			
		return true;
	}

	private void showDescriptionMissingDialog() {
		showAlertDialog(R.string.description_missing, R.string.description_missing_prompt, "description_missing");
	}

	private void showLocationMissingDialog() {
		showAlertDialog(R.string.location_missing, R.string.location_missing_prompt, "location_missing");
	}
	
	private void showAlertDialog(int title, int prompt, String tag) {
		AlertDialogFragment dialog = new AlertDialogFragment();
		dialog.setTitle(title);
		dialog.setPrompt(prompt);
		dialog.show(getFragmentManager(), tag);
	}
}
