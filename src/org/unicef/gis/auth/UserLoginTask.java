package org.unicef.gis.auth;

import org.unicef.gis.infrastructure.RoutesResolver;
import org.unicef.gis.infrastructure.UnicefGisApi;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class UserLoginTask extends AsyncTask<Void, Void, Bundle> {
	private static final String TAG = UserLoginTask.class.getSimpleName();
	
	private String password;
	private String email;
	private AuthenticatorActivity context;

	public UserLoginTask(String email, String password, AuthenticatorActivity requestor) {
		this.email = email;
		this.password = password;
		this.context = requestor;
	}

    @Override
    protected Bundle doInBackground(Void... params) {
        try {
        	UnicefGisApi api = new UnicefGisApi(new RoutesResolver(context));        	
        	return api.authenticate(email, password);
        } catch (Exception ex) {
            Log.e(TAG, "UserLoginTask.doInBackground: failed to authenticate");
            Log.i(TAG, ex.toString());
            return null;
        }
    }

    @Override
    protected void onPostExecute(final Bundle authToken) {
        // On a successful authentication, call back into the Activity to
        // communicate the authToken (or null for an error).
        context.onAuthenticationResult(authToken);
    }

    @Override
    protected void onCancelled() {
        // If the action was canceled (by the user clicking the cancel
        // button in the progress dialog), then call back into the
        // activity to let it know.
        context.onAuthenticationCancel();
    }
}