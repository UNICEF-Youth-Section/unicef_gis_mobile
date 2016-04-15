package org.unicef.gis.ui;

import java.util.List;

import org.unicef.gis.infrastructure.ServerUrlPreferenceNotSetException;
import org.unicef.gis.infrastructure.UnicefGisApi;
import org.unicef.gis.infrastructure.data.UnicefGisStore;
import org.unicef.gis.model.Tag;

import android.os.AsyncTask;

public class FetchTagsTask extends AsyncTask<Void, Void, Integer> {
	public static final Integer RESULT_SUCCESS = 0;
	public static final Integer SERVER_URL_PREFERENCE_NOT_SET = 1;
	
	private final FetchTagsActivity context;
	
	public FetchTagsTask(FetchTagsActivity context) {
		this.context = context;
	}
	
	@Override
	protected Integer doInBackground(Void... arg0) {
		try {
			UnicefGisApi api = new UnicefGisApi(context);
			List<Tag> tags = api.getTags();
			
			UnicefGisStore store = new UnicefGisStore(context);
			store.saveTags(tags);			
		} catch (ServerUrlPreferenceNotSetException e) {
			return SERVER_URL_PREFERENCE_NOT_SET;
		}
		
		return RESULT_SUCCESS;
	}
	
	@Override
    protected void onPostExecute(Integer result) {
        context.onFetchTagsResult(result);
    }
}
