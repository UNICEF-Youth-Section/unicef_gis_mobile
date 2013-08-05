package org.unicef.gis;

import java.util.List;

import org.unicef.gis.infrastructure.UnicefGisStore;
import org.unicef.gis.model.Tag;

import android.os.AsyncTask;

public class ReadAllTagsTask extends AsyncTask<Void, Void, List<Tag>> {
	private final MyReportsActivity context;
	
	public ReadAllTagsTask(MyReportsActivity context){
		this.context = context;
	}
	
	@Override
	protected List<Tag> doInBackground(Void... params) {
		UnicefGisStore store = new UnicefGisStore(context);
		return store.retrieveTags();
	}
	
	@Override
	protected void onPostExecute(List<Tag> result) {
		context.onReadAllTagsResult(result);
	}

}
