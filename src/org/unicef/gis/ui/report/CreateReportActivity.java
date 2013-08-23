package org.unicef.gis.ui.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.unicef.gis.MyReportsActivity;
import org.unicef.gis.R;
import org.unicef.gis.infrastructure.ILocationServiceConsumer;
import org.unicef.gis.infrastructure.LocationService;
import org.unicef.gis.infrastructure.UnicefGisStore;
import org.unicef.gis.infrastructure.image.Camera;
import org.unicef.gis.model.Tag;
import org.unicef.gis.ui.AlertDialogFragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class CreateReportActivity extends Activity implements ILocationServiceConsumer, IGetTagsCallback {
	private static final String GET_TAGS_FRAGMENT_TAG = "get_tags_fragment";
	
	private GetTagsTaskFragment getTagsFragment;
	private ChooseTagsFragment tagsFragment;
	private ReportSummaryFragment reportSummaryFragment;	
	
	private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 11;
	private static final int SUMMARY_VIEW_THUMBNAIL_FACTOR = 4;
	
	private static final String BUNDLE_IMAGE_FILE = "bundle_key_image_file_path";
	private static final String BUNDLE_CHOSEN_TAGS = "bundle_key_chosen_tags";
	private static final String BUNDLE_DESCRIPTION = "bundle_key_description";
	private static final String BUNDLE_CURRENT_STEP = "bundle_key_current_step";
	
	private static final String STEP_PIC = "step_pic";
	private static final String STEP_TAG = "step_tag";
	private static final String STEP_SUMMARY = "step_summary";
	
	private String currentStep = STEP_PIC;
	
	private LocationService locationService;
	
	private Bitmap imageThumbnail = null;
	private File imageFile;
	private Location location = null;	
	
	private List<Tag> availableTags = null;
	private ArrayList<String> chosenTags = null;
	
	private String description = null;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("CreateReportActivity", "onCreate");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_report);
				
		startLoadingTags();
		
		loadFragments();		
		loadLocationService();				
	}
	
	private void startLoadingTags() {
		Log.d("CreateReportActivity", "startLoadingTags");
		
		FragmentManager fm = getFragmentManager();
		getTagsFragment = (GetTagsTaskFragment) fm.findFragmentByTag(GET_TAGS_FRAGMENT_TAG);

		if (getTagsFragment == null) {
			getTagsFragment = new GetTagsTaskFragment();
			fm.beginTransaction().add(getTagsFragment, GET_TAGS_FRAGMENT_TAG).commit();
		} else {
			availableTags = getTagsFragment.getAvailableTags();
		}
	}

	private void tryToTakePicture() {
		Log.d("CreateReportActivity", "tryToTakePicture");
		
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
	protected void onSaveInstanceState(Bundle outState) {
		Log.d("CreateReportActivity", "onSaveInstanceState");
		
		if (imageFile != null)
			outState.putString(BUNDLE_IMAGE_FILE, imageFile.getAbsolutePath());
		
		String fragmentChosenTags = tagsFragment.getChosenTags() == null ? "null" : tagsFragment.getChosenTags().toString();
		Log.d("CreateReportActivity", "Fragment chosen tags: " + fragmentChosenTags);
		
		String chosenTags = getChosenTags() == null ? "null" : getChosenTags().toString();
		Log.d("CreateReportActivity", "Activity chosen tags: " + chosenTags);
	
		if (currentStep.equals(STEP_TAG))
			outState.putStringArrayList(BUNDLE_CHOSEN_TAGS, tagsFragment.getChosenTags());
		else
			outState.putStringArrayList(BUNDLE_CHOSEN_TAGS, getChosenTags());
		
		String desc = reportSummaryFragment.getReportDescription() == null ? "null" : reportSummaryFragment.getReportDescription();
		Log.d("CreateReportActivity", "Fragment chosen desc: " + desc);
		
		String descAct = description == null ? "null" : description;
		Log.d("CreateReportActivity", "Activity chosen desc: " + descAct);
		
		if (currentStep.equals(STEP_SUMMARY)){
			Log.d("CreateReportActivity", "Storing fragment desc");
			outState.putString(BUNDLE_DESCRIPTION, reportSummaryFragment.getReportDescription());
		}
		else {
			Log.d("CreateReportActivity", "Storing activity desc");
			outState.putString(BUNDLE_DESCRIPTION, description);
		}	
		
		outState.putString(BUNDLE_CURRENT_STEP, currentStep);
		
		super.onSaveInstanceState(outState);		
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		Log.d("CreateReportActivity", "onRestoreInstanceState");
		
		super.onRestoreInstanceState(savedInstanceState);
		
		String savedImageFilePath = savedInstanceState.getString(BUNDLE_IMAGE_FILE);
		if (savedImageFilePath != null) 
			imageFile = new File(savedImageFilePath);
		
		setChosenTags(savedInstanceState.getStringArrayList(BUNDLE_CHOSEN_TAGS));		
		tagsFragment.setChosenTags(getChosenTags());
		
		description = savedInstanceState.getString(BUNDLE_DESCRIPTION);
		reportSummaryFragment.setReportDescription(description);
		
		String restoredStep = savedInstanceState.getString(BUNDLE_CURRENT_STEP);
		currentStep = restoredStep;
	
		if (currentStep == null) {
			currentStep = STEP_PIC;
		}

		Log.d("CreateReportActivity", "Current step:" + (currentStep == null ? "null" : currentStep));
	}
	
	@Override
	protected void onStart() {
		Log.d("CreateReportActivity", "onStart");
		super.onStart();
		locationService.start();
	}
	
	@Override
	protected void onStop() {
		Log.d("CreateReportActivity", "onStop");
		locationService.stop();		
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		Log.d("CreateReportActivity", "onResume");
		Log.d("Current step:", currentStep == null ? "null" : currentStep);
		
		servicesConnected();
		
		if (currentStep == null || currentStep.equals(STEP_PIC))
			tryToTakePicture();
		else if (currentStep.equals(STEP_TAG))
			moveToTagStep(null);
		else if (currentStep.equals(STEP_SUMMARY))
			moveToSummaryStep(null);
		
		super.onResume();
	}
	
	private void loadLocationService() {
		Log.d("CreateReportActivity", "loadLocationService");
		locationService = new LocationService(this, this);
	}

	private void loadFragments() {
		Log.d("CreateReportActivity", "loadFragments");
		tagsFragment = new ChooseTagsFragment();
		
		if (availableTags != null) 
			tagsFragment.setAvailableTags(availableTags);
		
		reportSummaryFragment = new ReportSummaryFragment();
	}	
	
	private boolean servicesConnected() {
		Log.d("CreateReportActivity", "servicesConnected");
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
	  Log.d("CreateReportActivity", "showErrorDialog");
	  GooglePlayServicesUtil.getErrorDialog(code, this, REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("CreateReportActivity", "onActivityResult");
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
		
		currentStep = STEP_TAG;
		Log.d("Current step:", currentStep == null ? "null" : currentStep);
	}
	
	private void handleConnectionFailureResolutionRequest(int resultCode, Intent data) {
		Log.d("CreateReportActivity", "handleConnectionFailureResolutionRequest");
		if (resultCode == RESULT_CANCELED) {
			Toast.makeText(this, "Google Play Services must be installed.", Toast.LENGTH_SHORT).show();
	        finish();
		}
		return;		
	}

	private void moveToAnotherStep(Fragment current, Fragment newFragment, String step) {
		Log.d("CreateReportActivity", "moveToAnotherStep");
		currentStep = step;
		
		FragmentTransaction tx = getFragmentManager().beginTransaction();
		
		if (current == null) {
			tx.add(R.id.fragment_container, newFragment);
		} else {
			tx.replace(R.id.fragment_container, newFragment);
			tx.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
			tx.addToBackStack(null);
		}	
		
		tx.commit();
	}
	
	private void moveToTagStep(Fragment current) {
		Log.d("CreateReportActivity", "moveToTagStep");
		moveToAnotherStep(current, tagsFragment, STEP_TAG);
	}
	
	private void moveToSummaryStep(ChooseTagsFragment current) {
		Log.d("CreateReportActivity", "moveToSummaryStep");
		moveToAnotherStep(current, reportSummaryFragment, STEP_SUMMARY);
	}

	public void onTagsChosen(View view) {
		Log.d("CreateReportActivity", "onTagsChosen");
		setChosenTags(tagsFragment.getChosenTags());
		moveToSummaryStep(tagsFragment);			
	}

	public Bitmap getTakenPictureThumbnail(ImageView imageView) {
		Log.d("CreateReportActivity", "getTakenPictureThumbnail");
		if (imageThumbnail == null){
			Camera camera = new Camera(this);
			imageThumbnail = camera.getThumbnail(imageFile, SUMMARY_VIEW_THUMBNAIL_FACTOR);
		}
		
		return imageThumbnail;
	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		Log.d("CreateReportActivity", "onConnectionFailed");
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
		Log.d("CreateReportActivity", "onConnected");
		locationService.playServicesConnected();        
	}

	@Override
	public void onDisconnected() {
		Log.d("CreateReportActivity", "onDisconnected");
		locationService.playServicesDisconnected();		
	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d("CreateReportActivity", "onLocationChanged");
		this.location = location;
		reportSummaryFragment.setLocation(this.location);
	}
	
	public void onSaveReport(View view) {
		Log.d("CreateReportActivity", "onSaveReport");
		if (!validate()) return;
		
		saveReport();
		
		finish();
	}

	private void saveReport() {
		Log.d("CreateReportActivity", "saveReport");
		String description = reportSummaryFragment.getReportDescription();
		List<String> tags = getChosenTags();
		
		Camera camera = new Camera(this);		
		UnicefGisStore store = new UnicefGisStore(this);
		
		store.saveReport(description, location, camera.getUri(imageFile), tags);		
	}

	private boolean validate() {
		Log.d("CreateReportActivity", "validate");
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
		Log.d("CreateReportActivity", "showDescriptionMissingDialog");
		showAlertDialog(R.string.description_missing, R.string.description_missing_prompt, "description_missing");
	}

	private void showLocationMissingDialog() {
		Log.d("CreateReportActivity", "showLocationMissingDialog");
		showAlertDialog(R.string.location_missing, R.string.location_missing_prompt, "location_missing");
	}
	
	private void showAlertDialog(int title, int prompt, String tag) {
		Log.d("CreateReportActivity", "showAlertDialog");
		AlertDialogFragment dialog = new AlertDialogFragment();
		dialog.setTitle(title);
		dialog.setPrompt(prompt);
		dialog.show(getFragmentManager(), tag);
	}

	public ArrayList<String> getChosenTags() {
		Log.d("CreateReportActivity", "getChosenTags");
		return chosenTags;
	}

	@Override
	public void onGetTagsResult(List<Tag> result) {
		Log.d("CreateReportActivity", "onGetTagsResult");
		availableTags = result;
		tagsFragment.setAvailableTags(result);
		tagsFragment.setChosenTags(getChosenTags());
	}

	private void setChosenTags(ArrayList<String> chosenTags) {
		Log.d("CreateReportActivity", "setChosenTags");
		
		String chosenTagsDesc = chosenTags == null ? "null" : chosenTags.toString();		
		Log.d("CreateReportActivity", "chosenTags: " + chosenTags == null ? "null" : chosenTagsDesc);
		
		this.chosenTags = chosenTags;
	}
}
