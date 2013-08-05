package org.unicef.gis.model;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

public class Tag {

	private final String value;
	
	public Tag(String value) {
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}

	public static List<Tag> listFromJSON(JSONArray jsonTags) {
		try {
			List<Tag> tags = new ArrayList<Tag>();
			
			for (int i = 0; i < jsonTags.length(); i++) {
				tags.add(new Tag(jsonTags.getString(i)));
			}
			
			return tags;	
		} catch (JSONException e) {
			Log.e("Tag", "listFromJSON: invalid JSON: " + jsonTags.toString());
			return null;
		}
	}
}
