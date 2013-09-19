package org.unicef.gis;

import java.util.List;

import org.unicef.gis.infrastructure.RoutesResolver;
import org.unicef.gis.infrastructure.ServerUrlPreferenceNotSetException;
import org.unicef.gis.infrastructure.data.UnicefGisStore;
import org.unicef.gis.model.Report;
import org.unicef.gis.model.couchdb.ReportLoader;
import org.unicef.gis.ui.report.CreateReportActivity;
import org.unicef.gis.ui.report.ReportRowAdapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.couchbase.cblite.router.CBLURLStreamHandlerFactory;

public class MyReportsActivity extends ListActivity implements LoaderCallbacks<List<Report>> {	
	{
		CBLURLStreamHandlerFactory.registerSelfIgnoreError();
    }
	
	private static final int LOADER_ID = 111; 
	
	private TextView emptyView;
	private Button newReportButton;
	private ReportRowAdapter dbAdapter;
	private ProgressBar progressBar;
	
	//There seems to be a bug in the way loaders interact with the activity lifecycle, 
	//which forces us to call loaders onCreate, so we use this flag in order not to
	//restart loaders twice when we come back to this activity from another one without
	//recreating it.
	private boolean justCreated = false;
		
	private Account dummyAccount;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_reports);
		
		justCreated = true;
						
		loadControls();
		
		emptyView.setText(R.string.no_reports);
		newReportButton.setText(R.string.new_report);	
		
		if (checkAddressPreference() && checkTags()){			
			setupAdapter();										
			refreshData();			
			initDummyAccount();
			scheduleSync();
		}
	}

	private void scheduleSync() {
        ContentResolver.addPeriodicSync(
                dummyAccount,
                "org.unicef.gis.provider",
                new Bundle(),
                5);
	}

	private void initDummyAccount() {
		dummyAccount = new Account("dummyaccount", "unicef-gis.org");
		AccountManager accountManager = (AccountManager) this.getSystemService(ACCOUNT_SERVICE);
		accountManager.addAccountExplicitly(dummyAccount, null, null);		
	}
	
	private void refreshData() {
		displaySpinningWheelWhileLoading();
		dbAdapter.notifyDataSetChanged();
		getListView().invalidateViews();
		
		LoaderManager lm = getLoaderManager();
		if (lm.getLoader(LOADER_ID) != null) {        	
			lm.restartLoader(LOADER_ID, null, this);
		} else {
			lm.initLoader(LOADER_ID, null, this);        	
		}
	}
	
	@Override
	protected void onResume() {
		if (!justCreated)
			refreshData();			

		justCreated = false;
						
		super.onResume();
	}
	
	public void startCreateReportActivity(View view) {
		startActivity(new Intent(this, CreateReportActivity.class));
	}
	
	private void setupAdapter() {
		dbAdapter = new ReportRowAdapter(this, R.layout.row_report);
		setListAdapter(dbAdapter);				
	}

	private void displaySpinningWheelWhileLoading() {
		// Create a progress bar to display while the list loads
        progressBar = new ProgressBar(this);
        progressBar.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        progressBar.setIndeterminate(true);
        
        // Must add the progress bar to the root of the layout
        ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.addView(progressBar);
	}

	private void loadControls() {
		newReportButton = (Button) findViewById(R.id.my_reports_new_report);
		emptyView = (TextView) findViewById(R.id.my_reports_empty_message);		
	}

	private boolean checkTags() {
		UnicefGisStore store = new UnicefGisStore(this);
		
		if (!store.tagsHaveBeenFetched()) {
			startActivity(new Intent(this, FetchTagsActivity.class));
			return false;
		}	
		
		return true;
	}

	private boolean checkAddressPreference() {
		RoutesResolver routes = new RoutesResolver(this);
		try {
			routes.getBaseUrl();
			return true;
		} catch (ServerUrlPreferenceNotSetException e) {
			startActivity(new Intent(this, ConfigureServerUrlActivity.class));
			return false;
		} 	
	}

	@Override
	public Loader<List<Report>> onCreateLoader(int id, Bundle args) {
        return new ReportLoader(getApplicationContext());
	}

	@Override
	public void onLoaderReset(Loader<List<Report>> loader) {
		dbAdapter.clear();
	}

	@Override
	public void onLoadFinished(Loader<List<Report>> loader, List<Report> reports) {
		dbAdapter.clear();
		dbAdapter.addAll(reports);
		
		ViewGroup root = (ViewGroup) findViewById(android.R.id.content);
        root.removeView(progressBar);
	}
}
