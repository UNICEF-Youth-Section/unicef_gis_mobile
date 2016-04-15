package org.unicef.gis.ui.report;

import java.util.ArrayList;
import java.util.List;

import org.unicef.gis.R;
import org.unicef.gis.infrastructure.CompileTimeSettings;

import android.app.Activity;
import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ReportSummaryFragment extends Fragment implements TextWatcher {
	private IReportSummaryCallbacks callbacks = null;
	
	private static final int SOCIAL_CONTROLS_VISIBILITY = CompileTimeSettings.SUPPORT_SOCIAL ? View.VISIBLE : View.INVISIBLE; 
	
	private View view;
	private ImageView imageView;
	private TextView locationView;
	private ProgressBar progressBar;
	private TextView reportDescription;
	private TextView tagsView;
	private Button saveReport;

	private CheckBox postToTwitterCheckbox;
	private CheckBox postToFacebookCheckbox;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			callbacks = (IReportSummaryCallbacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement IReportSummaryCallbacks.");
		}
	}
	
	@Override
	public void onDetach() {
		callbacks = null;
		super.onDetach();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_summary, container, false);
		
		loadControls();
		
		getActivity().setTitle(R.string.save_report);		
		
		return view;
	}
	
	@Override
	public void onResume() {		
		setupImage();
		refreshTags();
		refreshDescription();
		refreshSocialNetworkCheckboxes();
		super.onResume();
	}
	
	private void refreshSocialNetworkCheckboxes() {
		CreateReportActivity cra = (CreateReportActivity) getActivity();
		if (cra == null) return;		
	
		setPostToTwitter(cra.getPostToTwitter());
		setPostToFacebook(cra.getPostToFacebook());
	}
	
	private void refreshDescription() {
		CreateReportActivity cra = (CreateReportActivity) getActivity();
		if (cra == null) return;
		
		reportDescription.setText(cra.getReportDescription());
	}

	private void refreshTags() {		
		CreateReportActivity cra = (CreateReportActivity) getActivity();
		if (cra == null) return;
				
		ArrayList<String> tags = cra.getChosenTags();
		
		if (tags != null)
			tagsView.setText(stringify(cra.getChosenTags()));
	}

	private CharSequence stringify(List<String> chosenTags) {
		StringBuffer sb = new StringBuffer("");
		for (String string : chosenTags) {
			if (sb.length() != 0) 
				sb.append(", ");
			
			sb.append(string);
		}
		return sb.toString();
	}

	private void loadControls() {		
		imageView = (ImageView) view.findViewById(R.id.summary_picture); 
		locationView = (TextView) view.findViewById(R.id.summary_location_display);
		progressBar = (ProgressBar) view.findViewById(R.id.summary_progress_bar);
		
		reportDescription = (TextView) view.findViewById(R.id.summary_title);
		reportDescription.addTextChangedListener(this);
				
		tagsView = (TextView) view.findViewById(R.id.summary_tags_chosen);
		
		saveReport = (Button) view.findViewById(R.id.summary_done);
		
		postToTwitterCheckbox = (CheckBox) view.findViewById(R.id.summary_post_to_twitter);
		postToTwitterCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {			
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				notifyPostToTwitterChange(isChecked);
			}
		});				
		postToTwitterCheckbox.setVisibility(SOCIAL_CONTROLS_VISIBILITY);
		
		postToFacebookCheckbox = (CheckBox) view.findViewById(R.id.summary_post_to_facebook);
		postToFacebookCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				notifyPostToFacebookChange(isChecked);
			}
		});
		postToFacebookCheckbox.setVisibility(SOCIAL_CONTROLS_VISIBILITY);	
	}

	private void setupImage() {
		CreateReportActivity cra = (CreateReportActivity) getActivity();
		if (cra == null) return;
		
		imageView.setImageBitmap(cra.getTakenPictureThumbnail(imageView));		
	}

	public void setLocation(Location location) {
		// The fragment may not have been displayed yet even though locations maybe ready.
		if (locationView != null) {
			if (location == null) {
				locationView.setText(R.string.searching_location);
				progressBar.setVisibility(View.VISIBLE);
			}			
			else {
				locationView.setText("Lat: " + location.getLatitude() + ", Long: " + location.getLongitude());
				progressBar.setVisibility(View.GONE);
			}
		}			
	}

	public void setReportDescription(String description) {	
		Log.d("ReportSummaryFragment", "setReportDescription");
		Log.d("ReportSummaryFragment", String.valueOf(System.identityHashCode(this)));
		
		if (reportDescription != null){
			reportDescription.setText(description);
		}
	}

	@Override
	public void afterTextChanged(Editable text) {
		if (callbacks != null) {
			callbacks.descriptionChanged(text.toString());
		}
	}
	
	public void notifyPostToTwitterChange(boolean shouldPost) {
		if (callbacks != null)
			callbacks.postToTwitterChanged(shouldPost);
	}
	
	public void notifyPostToFacebookChange(boolean shouldPost) {
		if (callbacks != null)
			callbacks.postToFacebookChanged(shouldPost);
	}

	@Override
	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
	@Override
	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

	public void onSavingReport() {
		saveReport.setText(R.string.saving_report);
		saveReport.setEnabled(false);
	}

	public void setPostToTwitter(boolean postToTwitter) {
		if (postToTwitterCheckbox != null)
			postToTwitterCheckbox.setChecked(postToTwitter);
	}
	
	public void setPostToFacebook(boolean postToFacebook) {
		if (postToFacebookCheckbox != null)
			postToFacebookCheckbox.setChecked(postToFacebook);
	}
}
