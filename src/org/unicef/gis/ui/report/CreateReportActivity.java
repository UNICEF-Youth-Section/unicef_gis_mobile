package org.unicef.gis.ui.report;

import org.unicef.gis.MyReportsActivity;
import org.unicef.gis.R;
import org.unicef.gis.ui.AlertDialogFragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class CreateReportActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {	
	private ChooseTagsFragment tagsFragment;
	private ReportSummaryFragment reportSummaryFragment;	

	private static final int TAKE_PICTURE_INTENT = 10;
	private static final int REQUEST_CODE_RECOVER_PLAY_SERVICES = 11;
	
	// Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    
    private LocationClient locationClient;
    private LocationRequest locationRequest;
	
	private Bitmap image;
	private Location location = null;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_create_report);
				
		loadFragments();
		
		loadLocationService();		
		
		dispatchTakePictureIntent();
	}

	private void loadLocationService() {
		locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(UPDATE_INTERVAL);
		locationRequest.setFastestInterval(FASTEST_INTERVAL);
		
		locationClient = new LocationClient(this, this, this);
	}

	private void loadFragments() {
		tagsFragment = new ChooseTagsFragment();
		reportSummaryFragment = new ReportSummaryFragment();
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		locationClient.connect();
	}
	
	@Override
	protected void onStop() {
		if (locationClient.isConnected())
			locationClient.removeLocationUpdates(this);
		
		locationClient.disconnect();
		super.onStop();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		if (servicesConnected()) {
			
		}
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
	
	private void dispatchTakePictureIntent() {
		startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PICTURE_INTENT);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_RECOVER_PLAY_SERVICES){
			handleConnectionFailureResolutionRequest(resultCode, data);
			return;
		}
					
		if (resultCode == RESULT_CANCELED)
			startActivity(new Intent(this, MyReportsActivity.class));
		
		if (requestCode != TAKE_PICTURE_INTENT && resultCode != RESULT_OK) 
			return;
		
		Bundle extras = data.getExtras();
	    image = (Bitmap) extras.get("data");
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

	public Bitmap getTakenPicture() {
		return image;
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
        locationClient.requestLocationUpdates(locationRequest, this);
	}

	@Override
	public void onDisconnected() {
		locationClient.connect();
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
		// TODO Auto-generated method stub
		
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
