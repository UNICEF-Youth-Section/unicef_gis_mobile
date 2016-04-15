package org.unicef.gis.auth;

import org.unicef.gis.infrastructure.UnicefGisApi;
import org.unicef.gis.ui.AuthenticatorActivity;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Authenticator extends AbstractAccountAuthenticator {
	
	public static final String AUTH_TOKEN_TYPE = "";
	public static String ACCOUNT_TYPE = "unicef-gis.org";

	private Context context;
	
	private UnicefGisApi api = null;

	public Authenticator(Context context) {
		super(context);
		this.context = context;
		this.api = new UnicefGisApi(context);
	}

	@Override
	public Bundle addAccount(AccountAuthenticatorResponse response,
			String accountType, String authTokenType,
			String[] requiredFeatures, Bundle options)
			throws NetworkErrorException {
		final Intent intent = new Intent(context, AuthenticatorActivity.class);
		intent.putExtra(AuthenticatorActivity.PARAM_ACCOUNT_TYPE, ACCOUNT_TYPE);
		intent.putExtra(AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, authTokenType);
		intent.putExtra(AuthenticatorActivity.PARAM_NEW_ACCOUNT, true);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		intent.putExtra(AuthenticatorActivity.PARAM_SHOULD_AUTHENTICATE, options.getBoolean(AuthenticatorActivity.PARAM_SHOULD_AUTHENTICATE, true));
		
		final Bundle bundle = new Bundle();
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);

		return bundle;
	}

	@Override
	public Bundle confirmCredentials(AccountAuthenticatorResponse response,
			Account account, Bundle options) throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle editProperties(AccountAuthenticatorResponse response,
			String accountType) {
		return null;
	}

	@Override
	public Bundle getAuthToken(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle options)
			throws NetworkErrorException {
		Log.e("Authenticator", "getAuthToken");
		
		if (!authTokenType.equals(AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE)) {
			Log.e("Authenticator", "Invalid auth token");
			final Bundle result = new Bundle();
			result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid authTokenType");
			return result;
		}
		
		final AccountManager am = AccountManager.get(context);
		
		String authToken = am.peekAuthToken(account, AUTH_TOKEN_TYPE);
		
		if (authToken != null) 
			Log.e("Authenticator", "Got auth token from AccountManager: " + authToken);
		
		if (authToken == null || authToken.isEmpty()){
			final String password = am.getPassword(account);
			
			if (password != null) {
				Log.e("Authenticator", "No token found, will try to authenticate with stored password");
				String token = api.authenticate(account.name, password);

				if (token != null && !token.isEmpty()) {
					final Bundle result = new Bundle();
					result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
					result.putString(AccountManager.KEY_ACCOUNT_TYPE, Authenticator.ACCOUNT_TYPE);
					result.putString(AccountManager.KEY_AUTHTOKEN, token);
					return result;
				}
			}
		}
		
		Log.e("Authenticator", "Password missing or incorrect, starting authenticator activity to renew user credentials.");
				
		// Password is missing or incorrect. Start the activity to add the
		// missing data.
		final Intent intent = new Intent(context, AuthenticatorActivity.class);
		intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
		intent.putExtra(AuthenticatorActivity.PARAM_ACCOUNT_TYPE, Authenticator.ACCOUNT_TYPE);
		intent.putExtra(AuthenticatorActivity.PARAM_AUTHTOKEN_TYPE, AUTH_TOKEN_TYPE);
		
		final Bundle bundle = new Bundle(); 
		bundle.putParcelable(AccountManager.KEY_INTENT, intent);

		return bundle;
	}

	@Override
	public String getAuthTokenLabel(String authTokenType) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle hasFeatures(AccountAuthenticatorResponse response,
			Account account, String[] features) throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Bundle updateCredentials(AccountAuthenticatorResponse response,
			Account account, String authTokenType, Bundle options)
			throws NetworkErrorException {
		// TODO Auto-generated method stub
		return null;
	}

}
