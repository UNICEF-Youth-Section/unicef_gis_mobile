package org.unicef.gis.ui;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.unicef.gis.R;
import org.unicef.gis.auth.Authenticator;
import org.unicef.gis.infrastructure.RoutesResolver;
import org.unicef.gis.infrastructure.ServerUrlPreferenceNotSetException;
import org.unicef.gis.infrastructure.data.UnicefGisStore;
import org.unicef.gis.model.Report;
import org.unicef.gis.model.couchdb.ReportLoader;
import org.unicef.gis.ui.report.CreateReportActivity;
import org.unicef.gis.ui.report.ReportRowAdapter;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.Loader;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.couchbase.cblite.router.CBLURLStreamHandlerFactory;

public class MyReportsActivity extends ListActivity implements LoaderCallbacks<List<Report>>, AccountManagerCallback<Bundle> {	
	{
		CBLURLStreamHandlerFactory.registerSelfIgnoreError();
    }
	
	private static final int LOADER_ID = 111; 
	
	private TextView emptyView;
	private Button newReportButton;
	private ReportRowAdapter dbAdapter;
	
	//There seems to be a bug in the way loaders interact with the activity lifecycle, 
	//which forces us to call loaders onCreate, so we use this flag in order not to
	//restart loaders twice when we come back to this activity from another one without
	//recreating it.
	private boolean justCreated = false;
			
	private AccountManager accountManager = null;

	private Timer timer;
			
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		
		setContentView(R.layout.activity_my_reports);
		
		accountManager = AccountManager.get(this);
		
		justCreated = true;
						
		loadControls();
		
		emptyView.setText(R.string.no_reports);
		newReportButton.setText(R.string.new_report);	
		
		if (checkAddressPreference() && checkTags()){			
			setupAdapter();										
			refreshData();									
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		openPreferences();
		return true;
	}
	
	private void openPreferences() {
		startActivity(new Intent(this, SettingsActivity.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.my_reports_actions, menu);
		
		return super.onCreateOptionsMenu(menu);
	}

	private void scheduleSync(Account account) {	
		ContentResolver.setSyncAutomatically(account, "org.unicef.gis.provider", true);
		
        ContentResolver.addPeriodicSync(
                account,
                "org.unicef.gis.provider",
                new Bundle(),
                5);
	}

	private void setupAccount() {
		Account[] accounts = accountManager.getAccountsByType(Authenticator.ACCOUNT_TYPE);
		
		if (accounts.length == 0) {
			Bundle options = new Bundle();
			options.putBoolean(AuthenticatorActivity.PARAM_SHOULD_AUTHENTICATE, false);
			
			accountManager.addAccount(Authenticator.ACCOUNT_TYPE, Authenticator.AUTH_TOKEN_TYPE, null, options, this, this, null);
		} else {			
			scheduleSync(accounts[0]);
		}		
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
		setupAccount();
		
		if (!justCreated)
			refreshData();			

		justCreated = false;
		
		setupRefreshTimer();
		
		super.onResume();
	}
	
	private void setupRefreshTimer() {
		timer = new Timer();
		timer.schedule(new TimerTask() {			
			@Override
			public void run() {
				runOnUiThread(new Runnable() {					
					@Override
					public void run() {
						refreshData();						
					}
				});
			}
		}, 30000, 30000);
	}

	@Override
	protected void onPause() {
		super.onPause();
		
		cancelRefreshTimer();
	}
	
	private void cancelRefreshTimer() {
		if (timer != null)
			timer.cancel();
		
		timer = null;
	}

	public void startCreateReportActivity(View view) {
		startActivity(new Intent(this, CreateReportActivity.class));
	}
	
	private void setupAdapter() {
		dbAdapter = new ReportRowAdapter(this, R.layout.row_report);
		setListAdapter(dbAdapter);				
	}

	private void displaySpinningWheelWhileLoading() {
		setProgressBarIndeterminate(true);
		setProgressBarIndeterminateVisibility(true);
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
		
		setProgressBarIndeterminateVisibility(false);
	}

	@Override
	public void run(AccountManagerFuture<Bundle> arg0) {
		setupAccount();
	}
}
