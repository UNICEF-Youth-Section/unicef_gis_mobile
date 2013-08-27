package org.unicef.gis.ui.report;

import java.util.ArrayList;
import java.util.List;

import org.unicef.gis.model.Tag;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ToggleButton;

public class ToggleTagAdapter extends BaseAdapter implements OnCheckedChangeListener {
	private IChooseTagsCallbacks callbacks = null;
	
	private ArrayList<String> chosenTags;
	private List<Tag> tags;
	
	public ToggleTagAdapter(IChooseTagsCallbacks callbacks) {
		this.callbacks = callbacks;
		chosenTags = new ArrayList<String>();
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
		Context context = parent.getContext();
		
		ToggleButton toggle = new ToggleButton(context);
		
        if (convertView == null) {  // if it's not recycled, initialize some attributes
        	toggle = new ToggleButton(context);
        	toggle.setPadding(8, 8, 8, 8);
        	toggle.setText(getItem(position).getValue());
        	toggle.setTextOff(getItem(position).getValue());
        	toggle.setTextOn(getItem(position).getValue());
        	
        	toggle.setOnCheckedChangeListener(this);
        	
        } else {
        	toggle = (ToggleButton) convertView;
        }
        
        toggle.setChecked(chosenTags.contains(getItem(position).getValue()));

        return toggle;
	}
	
	public void setAvailableTags(List<Tag> availableTags) {
		this.tags = availableTags;
		notifyDataSetChanged();
	}

	public void setChosenTags(ArrayList<String> chosenTags) {
		this.chosenTags = chosenTags == null ? new ArrayList<String>() : chosenTags;
		notifyDataSetChanged();
	}

	@Override
	public void onCheckedChanged(CompoundButton button, boolean isChecked) {
		if (isChecked && !chosenTags.contains(button.getText().toString()))
			chosenTags.add(button.getText().toString());
		
		if (!isChecked && chosenTags.contains(button.getText().toString()))
			chosenTags.remove(button.getText().toString());
		
		callbacks.chosenTagsChanged(chosenTags);
	}

	public ArrayList<String> getChosenTags() {
		return chosenTags;
	}
}
