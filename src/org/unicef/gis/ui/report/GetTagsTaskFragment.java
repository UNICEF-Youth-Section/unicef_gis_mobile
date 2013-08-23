package org.unicef.gis.ui.report;

import java.util.List;

import org.unicef.gis.model.Tag;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

public class GetTagsTaskFragment extends Fragment implements IGetTagsCallback {
	
	private GetTagsTask task;
	private IGetTagsCallback callbacks;
	
	private List<Tag> availableTags = null;
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.callbacks = (IGetTagsCallback) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
		
		task = new GetTagsTask(getActivity().getApplicationContext(), this);
		task.execute();
	}

	@Override
	public void onDetach() {
		super.onDetach();
		callbacks = null;
	}

	@Override
	public void onGetTagsResult(List<Tag> result) {
		availableTags = result;
		
		if (callbacks != null)
			callbacks.onGetTagsResult(result);
	}

	public List<Tag> getAvailableTags() {
		return availableTags;
	}
}
