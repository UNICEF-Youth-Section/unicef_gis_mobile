package org.unicef.gis.ui.report;

import org.unicef.gis.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ReportSummaryFragment extends Fragment {
	private View view;
	private ImageView imageView;

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
	}

	private void setupImage() {
		if (getActivity() == null) return;
		if (!(getActivity() instanceof CreateReportActivity)) return;
		
		CreateReportActivity activity = (CreateReportActivity) getActivity();
		
		imageView.setImageBitmap(activity.getTakenPicture());		
	}

}
