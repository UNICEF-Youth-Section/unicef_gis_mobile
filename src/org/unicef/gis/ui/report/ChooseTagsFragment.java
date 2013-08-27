package org.unicef.gis.ui.report;

import java.util.ArrayList;
import java.util.List;

import org.unicef.gis.R;
import org.unicef.gis.model.Tag;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ProgressBar;

public class ChooseTagsFragment extends Fragment implements IChooseTagsCallbacks {		
	private GridView gridView;
	private ProgressBar spinnningWheel;
	private View view;
	private Button next;
	private ToggleTagAdapter adapter;
		
	private IChooseTagsCallbacks callbacks = null;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			callbacks = (IChooseTagsCallbacks) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement IChooseTagsCallbacks.");
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
		Log.d("ChooseTagsFragment", "onCreateView");
		Log.d("ChooseTagsFragment", "object: " + System.identityHashCode(this));
		
		view = inflater.inflate(R.layout.fragment_choose_tags, container, false);
		
		getActivity().setTitle(R.string.tag_your_report);
				
		loadControls();
		
		next.setText(R.string.next);		
		
		gridView.setEmptyView(spinnningWheel);
		
		setupAdapter();
				
		return view;
	}		
	
	@Override
	public void onResume() {
		refreshData();
		super.onResume();
	}
	
	private void loadControls() {
		spinnningWheel = (ProgressBar) view.findViewById(R.id.choose_tags_progress);		
		gridView = (GridView) view.findViewById(R.id.choose_tags_gridview);
		next = (Button) view.findViewById(R.id.choose_tags_next);
	}
	
	private void setupAdapter() {
		Log.d("ChooseTagsFragment", "setting up adapter");
		
		adapter = new ToggleTagAdapter(this);
		gridView.setAdapter(adapter);			
	}
	
	private void refreshData() {
		CreateReportActivity cra = (CreateReportActivity) getActivity();		
		if (cra == null) return;
		
		setAvailableTags(cra.getAvailableTags());
		setChosenTags(cra.getChosenTags());
	}

	public ArrayList<String> getChosenTags() {		
		if (adapter == null) return null;				
		return adapter.getChosenTags();
	}

	public void setAvailableTags(List<Tag> availableTags) {
		if (adapter != null)
			adapter.setAvailableTags(availableTags);
	}

	public void setChosenTags(ArrayList<String> chosenTags) {		
		if (adapter != null)
			adapter.setChosenTags(chosenTags);
	}

	@Override
	public void chosenTagsChanged(ArrayList<String> chosenTags) {		
		if (callbacks != null){
			callbacks.chosenTagsChanged(chosenTags);
		}
	}	
}
