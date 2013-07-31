package org.unicef.gis.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorService extends Service {
    public static final String ACCOUNT_TYPE = "org.unicef.gis";
    public static final String AUTHTOKEN_TYPE = "org.unicef.gis.sync";
    public static final String USERDATA_LOCAST_API_URL = "locast_url";
    
	private Authenticator authenticator;
    
    @Override
    public void onCreate() {
        // Create a new authenticator object
        authenticator = new Authenticator(this);
    }
    
    /*
     * When the system binds to this Service to make the RPC call
     * return the authenticator's IBinder.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
