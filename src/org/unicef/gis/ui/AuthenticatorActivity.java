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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {

	public static final String PARAM_AUTHTOKEN_TYPE = "authenticator_authtoken_type";
	public static final String PARAM_ACCOUNT_TYPE = "authenticator_account_type";
	public static final String PARAM_USER_PASS = "authenticator_user_pass";
	public static final String PARAM_NEW_ACCOUNT = "authenticator_new_account";
	public static final String PARAM_SHOULD_AUTHENTICATE = "authenticator_should_authenticate";
	
	private UnicefGisApi api = null;
	private AccountManager accountManager = null;
	
	private EditText editEmail = null;
	private EditText editPassword = null;
	private Button login = null;
	
	@Override
	protected void onCreate(Bundle icicle) {
		super.onCreate(icicle);
		setContentView(R.layout.activity_login);
		
		editEmail = (EditText) findViewById(R.id.edit_email);
		editPassword = (EditText) findViewById(R.id.edit_password);
		login = (Button) findViewById(R.id.login_button);
		
		api = new UnicefGisApi(this);
		accountManager = AccountManager.get(this);
	}
	
	
	public void submit(View view) {
		disableSubmitButton();
		
	    final String email = editEmail.getText().toString();
	    final String password = editPassword.getText().toString();
	    
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

	private void disableSubmitButton() {		
		login.setText(R.string.saving_account_details);
		login.setEnabled(false);
	}
	
	private void reenableSubmitButton() {
		login.setText(R.string.button_login);
		login.setEnabled(true);
	}


	private void finishLogin(Intent intent, String email, String password) {
		String authToken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
		
		Account targetAccount = updateAccounts(email, password, authToken);
		
		if (shouldHaveAuthenticated() && authFailed(authToken)){
			showInvalidCredentialsFeedback();					
		} else {
			accountManager.setAuthToken(targetAccount, Authenticator.AUTH_TOKEN_TYPE, authToken);
			finishWithAuthOk(intent);	
		}
				
		reenableSubmitButton();
	}


	private boolean authFailed(String authToken) {
		return authToken == null || authToken.isEmpty();
	}


	private boolean shouldHaveAuthenticated() {
		return !getIntent().getBooleanExtra(PARAM_NEW_ACCOUNT, false);
	}

	private Account updateAccounts(String email, String password, String authToken) {
		Account targetAccount = null;
		Account[] accounts = accountManager.getAccountsByType(Authenticator.ACCOUNT_TYPE);
		for (int i = 0; i < accounts.length; i++) {
			if (accounts[i].name == email)
				targetAccount = accounts[i];
			else
				accountManager.removeAccount(accounts[i], null, null);
		}
										
		if (targetAccount == null){
			targetAccount = new Account(email, Authenticator.ACCOUNT_TYPE);
			accountManager.addAccountExplicitly(targetAccount, password, null);
		}
		
		return targetAccount;
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
