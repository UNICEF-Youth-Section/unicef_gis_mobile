package org.unicef.gis.ui.report;

import org.unicef.gis.model.Tag;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ToggleButton;

public class ToggleTagAdapter extends BaseAdapter {
	private final Context context;
	
	private Tag[] tags = new Tag[] { new Tag("Foo"), new Tag("Bar"), new Tag("Baz") };
	
	public ToggleTagAdapter(Context context) {
		this.context = context;
	}
	
	@Override
	public int getCount() {
		return tags.length;
	}

	@Override
	public Object getItem(int position) {
		return tags[position];
	}

	@Override
	public long getItemId(int position) {
		return tags[position].hashCode();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ToggleButton toggle = new ToggleButton(context);
		
        if (convertView == null) {  // if it's not recycled, initialize some attributes
        	toggle = new ToggleButton(context);
        	//toggle.setLayoutParams(new GridView.LayoutParams(85, 85));
        	//toggle.setScaleType(ImageView.ScaleType.CENTER_CROP);
        	toggle.setPadding(8, 8, 8, 8);
        	toggle.setText(tags[position].getValue());
        } else {
        	toggle = (ToggleButton) convertView;
        }

        return toggle;
	}

}
