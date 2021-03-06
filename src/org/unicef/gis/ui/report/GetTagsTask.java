package org.unicef.gis.ui.report;

import java.util.List;

import org.unicef.gis.infrastructure.data.UnicefGisStore;
import org.unicef.gis.model.Tag;

import android.content.Context;
import android.os.AsyncTask;

public class GetTagsTask extends AsyncTask<Void, Void, List<Tag>> {
	private final IGetTagsCallback callback;
	private final Context context;

	public GetTagsTask(Context context, IGetTagsCallback callback) {
		this.callback = callback;
		this.context = context;
	}

	@Override
	protected List<Tag> doInBackground(Void... arg0) {
		UnicefGisStore store = new UnicefGisStore(context);
		return store.retrieveTags();
	}

	@Override
	protected void onPostExecute(List<Tag> result) {
		callback.onGetTagsResult(result);
	}
}
