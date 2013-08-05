package org.unicef.gis;

import java.util.List;

import org.unicef.gis.infrastructure.UnicefGisApi;
import org.unicef.gis.model.Tag;

import android.os.AsyncTask;

public class FetchTagsTask extends AsyncTask<Void, Void, List<Tag>> {
	final FetchTagsActivity context;
	
	public FetchTagsTask(FetchTagsActivity context) {
		this.context = context;
	}
	
	@Override
	protected List<Tag> doInBackground(Void... arg0) {
		UnicefGisApi api = new UnicefGisApi(context);
		return api.getTags();
	}
	
	@Override
    protected void onPostExecute(final List<Tag> tags) {
        context.onFetchTagsResult(tags);
    }
}
