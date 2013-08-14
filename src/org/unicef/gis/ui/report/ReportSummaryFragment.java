package org.unicef.gis.ui.report;

import org.unicef.gis.R;

import android.app.Fragment;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ReportSummaryFragment extends Fragment {
	private View view;
	private ImageView imageView;
	private TextView locationView;
	private ProgressBar progressBar;
	private TextView reportDescription;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_summary, container, false);
		
		loadControls();
		
		getActivity().setTitle(R.string.save_report);
		
		setupImage();
		
		return view;
	}

	private void loadControls() {
		if (getActivity() == null) return;
		
		imageView = (ImageView) view.findViewById(R.id.summary_picture); 
		locationView = (TextView) view.findViewById(R.id.summary_location_display);
		progressBar = (ProgressBar) view.findViewById(R.id.summary_progress_bar);
		reportDescription = (TextView) view.findViewById(R.id.summary_title);
	}

	private void setupImage() {
		if (getActivity() == null) return;
		if (!(getActivity() instanceof CreateReportActivity)) return;
		
		CreateReportActivity activity = (CreateReportActivity) getActivity();
		
		imageView.setImageBitmap(activity.getTakenPicture());		
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
