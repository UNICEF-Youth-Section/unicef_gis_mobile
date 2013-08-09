package org.unicef.gis.ui.report;

import org.unicef.gis.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;

public class ChooseTagsFragment extends Fragment {	
	private GridView gridView;
	private ProgressBar spinnningWheel;
	private View view;
	private Button next;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_choose_tags, container, false);

		loadControls();
		
		next.setText(R.string.next);
		
		gridView.setEmptyView(spinnningWheel);        
		
		setupAdapter();
		
		return view;
	}
	
	private void loadControls() {
		spinnningWheel = (ProgressBar) view.findViewById(R.id.choose_tags_progress);		
		gridView = (GridView) view.findViewById(R.id.choose_tags_gridview);
		next = (Button) view.findViewById(R.id.choose_tags_next);
	}
	
	private void setupAdapter() {
		ToggleTagAdapter adapter = new ToggleTagAdapter(view.getContext());
		
		gridView.setAdapter(adapter);
			
		adapter.refresh();
	}
}
