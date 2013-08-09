package org.unicef.gis.ui.report;

import org.unicef.gis.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ProgressBar;

public class ChooseTagsFragment extends Fragment {	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_choose_tags, container, false);
		
		ProgressBar spinnningWheel = (ProgressBar) view.findViewById(R.id.choose_tags_progress);
		
		GridView gridView = (GridView) view.findViewById(R.id.choose_tags_gridview);
		gridView.setEmptyView(spinnningWheel);        
		gridView.setAdapter(new ToggleTagAdapter(view.getContext()));
		
		return view;
	}
}
