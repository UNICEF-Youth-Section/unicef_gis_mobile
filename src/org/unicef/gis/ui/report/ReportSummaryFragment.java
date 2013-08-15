package org.unicef.gis.ui.report;

import java.util.List;

import org.unicef.gis.R;

import android.app.Fragment;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ReportSummaryFragment extends Fragment {
	private View view;
	private ImageView imageView;
	private TextView locationView;
	private ProgressBar progressBar;
	private TextView reportDescription;
	private TextView tagsView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_summary, container, false);
		
		loadControls();
		
		getActivity().setTitle(R.string.save_report);		
		
		return view;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		if (reportDescription.requestFocus()) {
	        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
	        imm.showSoftInput(reportDescription, InputMethodManager.SHOW_IMPLICIT);
	    }
	}

	@Override
	public void onResume() {
		setupImage();
		refreshTags();
		super.onResume();
	}
	
	private void refreshTags() {
		if (getActivity() == null) return;
		if (!(getActivity() instanceof CreateReportActivity)) return;
		
		CreateReportActivity activity = (CreateReportActivity) getActivity();
		
		tagsView.setText(stringify(activity.getChosenTags()));
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
		if (getActivity() == null) return;
		
		imageView = (ImageView) view.findViewById(R.id.summary_picture); 
		locationView = (TextView) view.findViewById(R.id.summary_location_display);
		progressBar = (ProgressBar) view.findViewById(R.id.summary_progress_bar);
		reportDescription = (TextView) view.findViewById(R.id.summary_title);
		tagsView = (TextView) view.findViewById(R.id.summary_tags_chosen);
	}

	private void setupImage() {
		if (getActivity() == null) return;
		if (!(getActivity() instanceof CreateReportActivity)) return;
		
		CreateReportActivity activity = (CreateReportActivity) getActivity();
		
		imageView.setImageBitmap(activity.getTakenPictureThumbnail(imageView));		
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

	public String getReportDescription() {
		return reportDescription.getText().toString();
	}
}
