package org.unicef.gis.auth;

import org.unicef.gis.R;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class AuthenticatorActivity extends AccountAuthenticatorActivity {
	private static final String TAG = AuthenticatorActivity.class.getSimpleName();

	public static final String PARAM_CONFIRM_CREDENTIALS = "confirmCredentials";
    public static final String PARAM_AUTHTOKEN_TYPE = "authtokenType";
	
	public final static String EMAIL = "org.unicef.gis.AuthenticatorActivity.EMAIL";
	public final static String PASSWORD = "org.unicef.gis.AuthenticatorActivity.PASSWORD";

	private AccountManager accountManager;

	private String email;
	private String password;
	private boolean requestNewAccount;

	private EditText emailEdit;
	private EditText passwordEdit;
	private TextView feedbackView;
	private ProgressDialog progressDialog;

	private UserLoginTask authTask;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate(" + savedInstanceState + ")");
		super.onCreate(savedInstanceState);
		
        accountManager = AccountManager.get(this);
        
        final Intent intent = getIntent();
        
        email = intent.getStringExtra(EMAIL);
        requestNewAccount = email == null;
       
        setContentView(R.layout.activity_login);
        
        emailEdit = getEmailEdit(); 
        passwordEdit = getPasswordEdit();
        feedbackView = getFeedbackView();
        
        if (!TextUtils.isEmpty(email)) 
        	emailEdit.setText(email);
        
        feedbackView.setText(getFeedback());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	/**
     * Handles onClick event on the Submit button. Sends username/password to
     * the server for authentication. The button is configured to call
     * handleLogin() in the layout XML.
     *
     * @param view The Submit button for which this method is invoked
     */
    public void handleLogin(View view) {
        if (requestNewAccount) {
            email = emailEdit.getText().toString();
        }
        
        password = passwordEdit.getText().toString();
        
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            feedbackView.setText(getFeedback());
        } else {
            // Show a progress dialog, and kick off a background task to perform
            // the user login attempt.
            showProgress();
            authTask = new UserLoginTask(email, password, this);
            authTask.execute();
        }
    }
	
	@SuppressWarnings("deprecation")
	private void showProgress() {
		showDialog(0);
	}
	
    /**
     * Hides the progress UI for a lengthy operation.
     */
    private void hideProgress() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

	/*
     * {@inheritDoc}
     */
    @Override
    protected Dialog onCreateDialog(int id, Bundle args) {
        final ProgressDialog dialog = new ProgressDialog(this);
        dialog.setMessage(getText(R.string.authenticating));
        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        
        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                Log.i(TAG, "user cancelling authentication");
                if (authTask != null) {
                    authTask.cancel(true);
                }
            }
        });
        
        progressDialog = dialog;
        return dialog;
    }
	
	private EditText getEmailEdit() {
		return (EditText) findViewById(R.id.edit_email);
	}
	
	private EditText getPasswordEdit() {
		return (EditText) findViewById(R.id.edit_password);
	}
	
	private TextView getFeedbackView() {
		return (TextView) findViewById(R.id.message);
	}
	
	private CharSequence getFeedback() {
	    if (TextUtils.isEmpty(email)) {
	        // If no email, then we ask the user to log in using an
	        // appropriate service.
	        return getText(R.string.enter_email);
	    }
	    if (TextUtils.isEmpty(password)) {
	        // We have an account but no password
	        return getText(R.string.enter_password);
	    }
	    return null;   
	}

	/**
     * Called when the authentication process completes (see attemptLogin()).
     *
     * @param authToken the authentication token returned by the server, or NULL if
     *            authentication failed.
     */
    public void onAuthenticationResult(Bundle authToken) {
        boolean success = authToken != null;
        Log.i(TAG, "onAuthenticationResult(" + success + ")");

        // Our task is complete, so clear it out
        authTask = null;

        // Hide the progress dialog
        hideProgress();

        if (success) {
            finishLogin(authToken);
        } else {
            Log.e(TAG, "onAuthenticationResult: failed to authenticate");
            feedbackView.setText(getText(R.string.please_enter_valid_email_and_password));
        }
    }

    public void onAuthenticationCancel() {
        Log.i(TAG, "onAuthenticationCancel()");

        // Our task is complete, so clear it out
        authTask = null;

        // Hide the progress dialog
        hideProgress();
    }
    
    /**
     * Called when response is received from the server for authentication
     * request. See onAuthenticationResult(). Sets the
     * AccountAuthenticatorResult which is sent back to the caller. We store the
     * authToken that's returned from the server as the 'password' for this
     * account - so we're never storing the user's actual password locally.
     *
     * @param result the confirmCredentials result.
     */
    private void finishLogin(Bundle authToken) {
        Log.i(TAG, "finishLogin()");
        final Account account = new Account(email, AuthenticatorService.ACCOUNT_TYPE);
        if (requestNewAccount) {
            accountManager.addAccountExplicitly(account, password, null);
        } else {
            accountManager.setPassword(account, password);
        }
        
        final Intent intent = new Intent();
        intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, email);
        intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, AuthenticatorService.ACCOUNT_TYPE);
        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }
}
