package org.unicef.gis.ui.report;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.unicef.gis.R;
import org.unicef.gis.infrastructure.ILocationServiceConsumer;
import org.unicef.gis.infrastructure.LocationService;
import org.unicef.gis.infrastructure.data.UnicefGisStore;
import org.unicef.gis.infrastructure.image.Camera;
import org.unicef.gis.model.Tag;
import org.unicef.gis.ui.AlertDialogFragment;
import org.unicef.gis.ui.MyReportsActivity;

import android.app.ActionBar;
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

public class CreateReportActivity extends Activity implements ILocationServiceConsumer, IGetTagsCallback, IChooseTagsCallbacks, IReportSummaryCallbacks {
	private GetTagsTaskFragment getTagsFragment;
	private ChooseTagsFragment tagsFragment;
	private ReportSummaryFragment summaryFragment;	
	
	private String currentStep = CreateReportActivityConstants.STEP_PIC;
	
	private LocationService locationService;
	
	private ReportViewModel reportViewModel = null;
	
	private List<Tag> availableTags = null;
	
	public Bitmap imageThumbnail = null;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("CreateReportActivity", "onCreate");
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_report);
		
		reportViewModel = new ReportViewModel();
				
		startLoadingTags();
		
		loadFragments();		
		loadLocationService();	
		
		setupActionBar();
	}

	private void setupActionBar() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
	}
	
	@Override
	public boolean onNavigateUp() {
		onBackPressed();
		return true;
	}
	
	@Override
	public void onBackPressed() {
		if (atSummaryStep()){
			currentStep = CreateReportActivityConstants.STEP_TAG;	
			refreshTagsFragment();			
			moveToTagStep(CreateReportActivityConstants.STEP_SUMMARY);			
		} else if (atTaggingStep()) {
			currentStep = CreateReportActivityConstants.STEP_PIC;									
			tryToTakePicture();
		} else if (atTakePictureStep()) {
			finish();
		}				
	}

	private boolean atTakePictureStep() {
		return currentStep == null || currentStep.equals(CreateReportActivityConstants.STEP_PIC);
	}

	private boolean atTaggingStep() {
		return currentStep.equals(CreateReportActivityConstants.STEP_TAG);
	}

	private boolean atSummaryStep() {
		return currentStep.equals(CreateReportActivityConstants.STEP_SUMMARY);
	}

	private void refreshTagsFragment() {
		if (tagsFragment == null) return;

		tagsFragment.setAvailableTags(availableTags);
		tagsFragment.setChosenTags(reportViewModel.chosenTags);
	}
	
	private void startLoadingTags() {
		Log.d("CreateReportActivity", "startLoadingTags");
		
		FragmentManager fm = getFragmentManager();
		getTagsFragment = (GetTagsTaskFragment) fm.findFragmentByTag(CreateReportActivityConstants.GET_TAGS_FRAGMENT_TAG);

		if (getTagsFragment == null) {
			Log.d("CreateReportActivity", "fragment is null, starting tag retrieval");
			getTagsFragment = new GetTagsTaskFragment();
			fm.beginTransaction().add(getTagsFragment, CreateReportActivityConstants.GET_TAGS_FRAGMENT_TAG).commit();
		} else {
			Log.d("CreateReportActivity", "fragment exists, updating available tags");
			availableTags = getTagsFragment.getAvailableTags();
		}
	}

	private void tryToTakePicture() {
		Log.d("CreateReportActivity", "tryToTakePicture");
		
		try {
			Camera camera = new Camera(this);		
			reportViewModel.imageFile = camera.takePicture();
		} catch (IOException e) {
			showAlertDialog(R.string.configuration_problem, R.string.configuration_problem_prompt, "configuration_problem");
			e.printStackTrace();
			finish();
		}
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		reportViewModel.saveInstanceState(outState);		
		outState.putString(CreateReportActivityConstants.BUNDLE_CURRENT_STEP, currentStep);		
		super.onSaveInstanceState(outState);		
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		
		reportViewModel.restoreInstanceState(savedInstanceState);		
		
		if (availableTags == null)
			availableTags = getTagsFragment.getAvailableTags();
		
		currentStep = savedInstanceState.getString(CreateReportActivityConstants.BUNDLE_CURRENT_STEP);
		
		refreshTagsFragment();
		refreshSummaryFragment();
		
		if (currentStep == null)
			currentStep = CreateReportActivityConstants.STEP_PIC;
	}

	private void refreshSummaryFragment() {
		if (summaryFragment == null) return;
		
		summaryFragment.setReportDescription(reportViewModel.description);
		summaryFragment.setPostToTwitter(reportViewModel.postToTwitter);
		summaryFragment.setPostToFacebook(reportViewModel.postToFacebook);
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
	
	private void nullableLog(String key, Object nullable) {
		Log.d("CreateReportActivity", key + (nullable == null ? "null" : nullable.toString()));
	}
	
	@Override
	protected void onResume() {
		nullableLog("onResume: ", "executing");

		servicesConnected();
		
		if (atTakePictureStep()) {
			tryToTakePicture();
		} else if (atTaggingStep()) {
			refreshTagsFragment();
			moveToTagStep(CreateReportActivityConstants.STEP_TAG);
		} else if (atSummaryStep()) {
			refreshSummaryFragment();
			moveToSummaryStep(CreateReportActivityConstants.STEP_SUMMARY);
		}	
		
		super.onResume();
	}
	
	private void loadLocationService() {
		Log.d("CreateReportActivity", "loadLocationService");
		locationService = new LocationService(this, this);
	}

	private void loadFragments() {
		Log.d("CreateReportActivity", "loadFragments");
		
		FragmentManager fm = getFragmentManager();
		
		tagsFragment = (ChooseTagsFragment) fm.findFragmentByTag(CreateReportActivityConstants.CHOOSE_TAGS_FRAGMENT_TAG);
		if (tagsFragment == null)
			tagsFragment = new ChooseTagsFragment();		
		
		summaryFragment = (ReportSummaryFragment) fm.findFragmentByTag(CreateReportActivityConstants.SUMMARY_FRAGMENT_TAG);
		if (summaryFragment == null)
			summaryFragment = new ReportSummaryFragment();				
	}	
	
	private void servicesConnected() {
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
        }
    }	
	
	private void showErrorDialog(int code) {
	  Log.d("CreateReportActivity", "showErrorDialog");
	  GooglePlayServicesUtil.getErrorDialog(code, this, CreateReportActivityConstants.REQUEST_CODE_RECOVER_PLAY_SERVICES).show();
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("CreateReportActivity", "onActivityResult");
		if (requestCode == CreateReportActivityConstants.REQUEST_CODE_RECOVER_PLAY_SERVICES){
			handleConnectionFailureResolutionRequest(resultCode, data);
			return;
		}
					
		if (resultCode == RESULT_CANCELED || requestCode != Camera.TAKE_PICTURE_INTENT || resultCode != RESULT_OK)
			startActivity(new Intent(this, MyReportsActivity.class));
		
		Camera camera = new Camera(this);
		camera.addPicToGallery(reportViewModel.imageFile);
		
		currentStep = CreateReportActivityConstants.STEP_TAG;
	}
	
	private void handleConnectionFailureResolutionRequest(int resultCode, Intent data) {
		Log.d("CreateReportActivity", "handleConnectionFailureResolutionRequest");
		if (resultCode == RESULT_CANCELED) {
			Toast.makeText(this, "Google Play Services must be installed.", Toast.LENGTH_SHORT).show();
	        finish();
		}
		return;		
	}

	private void moveToAnotherStep(String fragmentTag, String newStep, String previousStep) {
		currentStep = newStep;
		
		FragmentManager fm = getFragmentManager();
		FragmentTransaction tx = fm.beginTransaction();		
		Fragment fragment = fm.findFragmentByTag(fragmentTag);		
		
		if (fragment == null) {
			if (atTaggingStep()){
				tx.add(R.id.fragment_container, tagsFragment, CreateReportActivityConstants.CHOOSE_TAGS_FRAGMENT_TAG);
			} else if (atSummaryStep()) {
				tx.add(R.id.fragment_container, summaryFragment, CreateReportActivityConstants.SUMMARY_FRAGMENT_TAG);
			}				
		} else if (!(previousStep.equals(currentStep))) {
			tx.replace(R.id.fragment_container, fragment);
		}	
		
		tx.commit();
	}
	
	private void moveToTagStep(String previousStep) {
		Log.d("CreateReportActivity", "moveToTagStep");
		moveToAnotherStep(CreateReportActivityConstants.CHOOSE_TAGS_FRAGMENT_TAG, CreateReportActivityConstants.STEP_TAG, previousStep);
	}
	
	private void moveToSummaryStep(String previousStep) {
		Log.d("CreateReportActivity", "moveToSummaryStep");
		refreshSummaryFragment();
		moveToAnotherStep(CreateReportActivityConstants.SUMMARY_FRAGMENT_TAG, CreateReportActivityConstants.STEP_SUMMARY, previousStep);
	}

	public void onTagsChosen(View view) {
		Log.d("CreateReportActivity", "onTagsChosen");
		moveToSummaryStep(currentStep);			
	}

	public Bitmap getTakenPictureThumbnail(ImageView imageView) {
		Log.d("CreateReportActivity", "getTakenPictureThumbnail");
		if (imageThumbnail == null){
			Camera camera = new Camera(this);
			imageThumbnail = camera.getThumbnail(reportViewModel.imageFile, CreateReportActivityConstants.SUMMARY_VIEW_THUMBNAIL_FACTOR);
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
                        CreateReportActivityConstants.REQUEST_CODE_RECOVER_PLAY_SERVICES);
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
		reportViewModel.location = location;
		summaryFragment.setLocation(reportViewModel.location);
	}
	
	public void onSaveReport(View view) {
		Log.d("CreateReportActivity", "onSaveReport");
		if (!validate()) return;
		
		saveReport();
		
		finish();
	}

	private void saveReport() {
		if (summaryFragment != null) {
			summaryFragment.onSavingReport();
		}
	
		UnicefGisStore store = new UnicefGisStore(this);		
		store.saveReport(reportViewModel);		
	}

	private boolean validate() {
		if (reportViewModel.location == null) {
			showLocationMissingDialog();
			return false;
		} else if (reportViewModel.description == null || reportViewModel.description.isEmpty()) {
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

	public ArrayList<String> getChosenTags() {
		return reportViewModel.chosenTags;
	}

	@Override
	public void onGetTagsResult(List<Tag> result) {
		availableTags = result;
		refreshTagsFragment();
	}

	private void setChosenTags(ArrayList<String> chosenTags) {
		Log.d("CreateReportActivity", "setChosenTags");
		reportViewModel.chosenTags = chosenTags;
	}

	public List<Tag> getAvailableTags() {
		return availableTags;
	}

	@Override
	public void chosenTagsChanged(ArrayList<String> chosenTags) {
		setChosenTags(chosenTags);
	}

	@Override
	public void descriptionChanged(String description) {
		reportViewModel.description = description;
	}

	public CharSequence getReportDescription() {
		return reportViewModel.description;
	}

	@Override
	public void postToTwitterChanged(boolean shouldPost) {
		reportViewModel.postToTwitter = shouldPost;
	}

	@Override
	public void postToFacebookChanged(boolean shouldPost) {
		reportViewModel.postToFacebook = shouldPost;
	}

	public boolean getPostToTwitter() {
		return reportViewModel.postToTwitter;
	}
	
	public boolean getPostToFacebook() {
		return reportViewModel.postToFacebook;
	}
}
