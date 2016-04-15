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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tag other = (Tag) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}
}
