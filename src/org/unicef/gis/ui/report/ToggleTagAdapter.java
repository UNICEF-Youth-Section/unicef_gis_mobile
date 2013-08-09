package org.unicef.gis.ui.report;

import java.util.List;

import org.unicef.gis.model.Tag;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ToggleButton;

public class ToggleTagAdapter extends BaseAdapter implements IGetTagsCallback {
	private final Context context;
	
	private List<Tag> tags;
	
	public ToggleTagAdapter(Context context) {
		this.context = context;
	}
	
	@Override
	public int getCount() {
		return tags == null ? 0 : tags.size();
	}

	@Override
	public Tag getItem(int position) {
		return tags.get(position);
	}

	@Override
	public long getItemId(int position) {
		return tags.get(position).hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ToggleButton toggle = new ToggleButton(context);
		
        if (convertView == null) {  // if it's not recycled, initialize some attributes
        	toggle = new ToggleButton(context);
        	toggle.setPadding(8, 8, 8, 8);
        	toggle.setText(getItem(position).getValue());
        	toggle.setTextOff(getItem(position).getValue());
        	toggle.setTextOn(getItem(position).getValue());
        } else {
        	toggle = (ToggleButton) convertView;
        }

        return toggle;
	}

	public void refresh() {
		GetTagsTask getTagsTask = new GetTagsTask(context, this); 
		getTagsTask.execute();
	}

	@Override
	public void onGetTagsResult(List<Tag> result) {
		tags = result;
		notifyDataSetChanged();
	}
}
