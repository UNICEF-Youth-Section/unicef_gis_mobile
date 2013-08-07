package org.unicef.gis;

import org.unicef.gis.infrastructure.RoutesResolver;
import org.unicef.gis.infrastructure.ServerUrlPreferenceNotSetException;
import org.unicef.gis.infrastructure.UnicefGisStore;
import org.unicef.gis.infrastructure.data.UnicefGisDbContract;

import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MyReportsActivity extends ListActivity implements LoaderCallbacks<Cursor> {	
	private TextView emptyView;
	private Button newReportButton;
	private SimpleCursorAdapter dbAdapter;
	private View originalEmptyView;
	private ProgressBar progressBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_reports);
				
		loadControls();
		
		emptyView.setText(R.string.no_reports);
		newReportButton.setText(R.string.new_report);
		
		displaySpinningWheelWhileLoading();
		
		checkAddressPreference();
		checkTags();
		
		setupAdapter();
	}
	
	private void setupAdapter() {
		String[] fromColumns = { UnicefGisDbContract.Report.COLUMN_NAME_TITLE };
		int[] toViews = { android.R.id.text1 };
		
		dbAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_expandable_list_item_1, null, fromColumns, toViews, 0);
		setListAdapter(dbAdapter);
		
		getLoaderManager().initLoader(0, null, this);
	}

	private void displaySpinningWheelWhileLoading() {
		originalEmptyView = getListView().getEmptyView();
		
		// Create a progress bar to display while the list loads
        progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        progressBar.setIndeterminate(true);
        getListView().setEmptyView(progressBar);
        
        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBar);
	}

	private void loadControls() {
		newReportButton = (Button) findViewById(R.id.my_reports_new_report);
		emptyView = (TextView) findViewById(R.id.my_reports_empty_message);		
	}

	private void checkTags() {
		UnicefGisStore store = new UnicefGisStore(this);
		
		if (!store.tagsHaveBeenFetched())
			startActivity(new Intent(this, FetchTagsActivity.class));
	}

	private void checkAddressPreference() {
		RoutesResolver routes = new RoutesResolver(this);
		try {
			routes.getBaseUrl();
		} catch (ServerUrlPreferenceNotSetException e) {
			startActivity(new Intent(this, ConfigureServerUrlActivity.class));
		} 	
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, 
        		UnicefGisDbContract.Report.CONTENT_URI,
                UnicefGisDbContract.Report.DEFAULT_PROJECTION, 
                UnicefGisDbContract.Selections.ALL, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		dbAdapter.swapCursor(data);
		getListView().setEmptyView(originalEmptyView);
		
		ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.removeView(progressBar);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		dbAdapter.swapCursor(null);		
	}
}
