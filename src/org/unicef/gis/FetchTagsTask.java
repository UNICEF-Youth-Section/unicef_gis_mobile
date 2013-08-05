package org.unicef.gis;

import java.util.List;

import org.unicef.gis.infrastructure.UnicefGisApi;
import org.unicef.gis.infrastructure.UnicefGisStore;
import org.unicef.gis.model.Tag;

import android.os.AsyncTask;

public class FetchTagsTask extends AsyncTask<Void, Void, Boolean> {
	final FetchTagsActivity context;
	
	public FetchTagsTask(FetchTagsActivity context) {
		this.context = context;
	}
	
	@Override
	protected Boolean doInBackground(Void... arg0) {
		UnicefGisApi api = new UnicefGisApi(context);
		List<Tag> tags = api.getTags();
		
		UnicefGisStore store = new UnicefGisStore(context);
		store.saveTags(tags);
		
		return true;
	}
	
	@Override
    protected void onPostExecute(Boolean result) {
        context.onFetchTagsResult(result);
    }
}
