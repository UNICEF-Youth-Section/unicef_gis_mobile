package org.unicef.gis.ui.report;

import java.util.ArrayList;
import java.util.List;

import org.unicef.gis.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;
import android.widget.ToggleButton;

public class ChooseTagsFragment extends Fragment {	
	private GridView gridView;
	private ProgressBar spinnningWheel;
	private View view;
	private Button next;
	private ToggleTagAdapter adapter;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		view = inflater.inflate(R.layout.fragment_choose_tags, container, false);
		
		getActivity().setTitle(R.string.tag_your_report);
		
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
		adapter = new ToggleTagAdapter(view.getContext());		
		gridView.setAdapter(adapter);			
		adapter.refresh();
	}

	public List<String> getChosenTags() {
		ArrayList<String> tags = new ArrayList<String>();
		
		for (int i = 0; i < adapter.getCount(); i++) {
			if (((ToggleButton)(gridView.getChildAt(i))).isChecked())
				tags.add(adapter.getItem(i).getValue());				
		}
		
		return tags;
	}
}
