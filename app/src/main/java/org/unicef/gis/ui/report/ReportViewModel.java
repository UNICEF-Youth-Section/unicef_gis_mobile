package org.unicef.gis.ui.report;

import java.io.File;
import java.util.ArrayList;

import org.unicef.gis.infrastructure.image.Camera;

import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

public class ReportViewModel {	
	public File imageFile;
	public Location location = null;		
	public ArrayList<String> chosenTags = null;	
	public String description = null;
	public boolean postToTwitter = false;
	public boolean postToFacebook = false;
	
	public Uri getImageUri() {
		return Camera.getUri(imageFile);
	}

	public void restoreInstanceState(Bundle savedInstanceState) {
		String savedImageFilePath = savedInstanceState.getString(CreateReportActivityConstants.BUNDLE_IMAGE_FILE);
		if (savedImageFilePath != null) 
			imageFile = new File(savedImageFilePath);
		
		chosenTags = savedInstanceState.getStringArrayList(CreateReportActivityConstants.BUNDLE_CHOSEN_TAGS);				
		description = savedInstanceState.getString(CreateReportActivityConstants.BUNDLE_DESCRIPTION);
		
		postToTwitter = savedInstanceState.getBoolean(CreateReportActivityConstants.BUNDLE_POST_TO_TWITTER, false);
		postToFacebook = savedInstanceState.getBoolean(CreateReportActivityConstants.BUNDLE_POST_TO_FACEBOOK, false);
	}

	public void saveInstanceState(Bundle outState) {
		if (imageFile != null)
			outState.putString(CreateReportActivityConstants.BUNDLE_IMAGE_FILE, imageFile.getAbsolutePath());
		
		outState.putStringArrayList(CreateReportActivityConstants.BUNDLE_CHOSEN_TAGS, chosenTags);		
		outState.putString(CreateReportActivityConstants.BUNDLE_DESCRIPTION, description);
		
		outState.putBoolean(CreateReportActivityConstants.BUNDLE_POST_TO_TWITTER, postToTwitter);
		outState.putBoolean(CreateReportActivityConstants.BUNDLE_POST_TO_FACEBOOK, postToFacebook);
	}
}
