package org.unicef.gis.ui.report;

import java.util.List;

import org.unicef.gis.model.Tag;

public interface IGetTagsTaskFragmentCallbacks {
	void onPostExecute(List<Tag> tags);
}
