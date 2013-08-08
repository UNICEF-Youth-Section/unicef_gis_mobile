package org.unicef.gis.ui.report;

import org.unicef.gis.R;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

public class ChooseTagsFragment extends Fragment {	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_choose_tags, container, false);
		
		GridView gridview = (GridView) view.findViewById(R.id.choose_tags_gridview);
	    gridview.setAdapter(new ToggleTagAdapter(view.getContext()));
		
		return view;
	}
}
