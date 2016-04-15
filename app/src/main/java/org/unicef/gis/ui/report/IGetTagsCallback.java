package org.unicef.gis.ui.report;

import java.util.List;

import org.unicef.gis.model.Tag;

public interface IGetTagsCallback {
	public void onGetTagsResult(List<Tag> result);
}
