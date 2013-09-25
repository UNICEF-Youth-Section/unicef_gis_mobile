package org.unicef.gis.ui;

import org.unicef.gis.R;
import org.unicef.gis.auth.Authenticator;
import org.unicef.gis.infrastructure.UnicefGisApi;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {

	public static final String PARAM_AUTHTOKEN_TYPE = "authenticator_authtoken_type";
	public static final String PARAM_ACCOUNT_TYPE = "authenticator_account_type";
	public static final String PARAM_USER_PASS = "authenticator_user_pass";
	public static final String PARAM_NEW_ACCOUNT = "authenticator_new_account";
	public static final String PARAM_SHOULD_AUTHENTICATE = "authenticator_should_authenticate";
	
	private UnicefGisApi api = null;
	private AccountManager accountManager = null;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_login);
		
		api = new UnicefGisApi(this);
		accountManager = AccountManager.get(this);
	}
	
	public void submit(View view) {
	    final String email = ((TextView) findViewById(R.id.edit_email)).getText().toString();
	    final String password = ((TextView) findViewById(R.id.edit_password)).getText().toString();
	    
	    final boolean shouldAuthenticate = getIntent().getExtras().getBoolean(AuthenticatorActivity.PARAM_SHOULD_AUTHENTICATE, true);
	    
    	AsyncTask<Void, Void, Intent> authentication = new AsyncTask<Void, Void, Intent>() {
	        @Override
	        protected Intent doInBackground(Void... params) {
	        	String authtoken = "";
	        	
	        	if (shouldAuthenticate)
	        		authtoken = api.authenticate(email, password);
	        	
	            final Intent res = new Intent();
	            res.putExtra(AccountManager.KEY_ACCOUNT_NAME, email);
	            res.putExtra(AccountManager.KEY_ACCOUNT_TYPE, Authenticator.ACCOUNT_TYPE);
	            res.putExtra(AccountManager.KEY_AUTHTOKEN, authtoken);
	            res.putExtra(PARAM_USER_PASS, password);
	            return res;
	        }
	        @Override
	        protected void onPostExecute(Intent intent) {
	            finishLogin(intent, email, password);
	        }
	    };
	    
	    authentication.execute();	    
	}

	private void finishLogin(Intent intent, String email, String password) {
		final Account account = new Account(email, Authenticator.ACCOUNT_TYPE);
		
		String authToken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
		
		if (getIntent().getBooleanExtra(PARAM_NEW_ACCOUNT, false)) {			
			accountManager.addAccountExplicitly(account, password, null);
			accountManager.setAuthToken(account, Authenticator.AUTH_TOKEN_TYPE, authToken);
			finishWithAuthOk(intent);
		} else {
			if (authToken == null || authToken.isEmpty()) {
				showInvalidCredentialsFeedback();
			} else {
				accountManager.setAuthToken(account, Authenticator.AUTH_TOKEN_TYPE, authToken);
				accountManager.setPassword(account, password);
				finishWithAuthOk(intent);
			}
		}						
	}

	private void finishWithAuthOk(Intent intent) {
		setAccountAuthenticatorResult(intent.getExtras());
		setResult(RESULT_OK, intent);
		finish();
	}

	private void showInvalidCredentialsFeedback() {
		TextView feedback = (TextView) findViewById(R.id.message);
		feedback.setText(R.string.please_enter_valid_email_and_password);
	}
}
