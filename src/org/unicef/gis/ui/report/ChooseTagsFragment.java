package org.unicef.gis.ui.report;

import java.util.ArrayList;
import java.util.List;

import org.unicef.gis.R;
import org.unicef.gis.model.Tag;

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
	private ToggleTagAdapter adapter;
	
	private List<Tag> availableTags;
	private ArrayList<String> chosenTags;
	
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
		setAvailableTags(availableTags);
		setChosenTags(chosenTags);
		
		gridView.setAdapter(adapter);			
	}

	public ArrayList<String> getChosenTags() {		
		if (adapter == null) return null;				
		return adapter.getChosenTags();
	}

	public void setAvailableTags(List<Tag> availableTags) {
		this.availableTags = availableTags;
		
		if (adapter != null)
			adapter.setAvailableTags(this.availableTags);
	}

	public void setChosenTags(ArrayList<String> restoredChosenTags) {
		this.chosenTags = restoredChosenTags;
				
		if (adapter != null)
			adapter.setChosenTags(chosenTags);
	}
}
