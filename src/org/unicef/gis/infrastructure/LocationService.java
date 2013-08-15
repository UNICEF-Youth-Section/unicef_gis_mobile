package org.unicef.gis.infrastructure;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

public class LocationService implements LocationListener {
	// Milliseconds per second
    private static final int MILLISECONDS_PER_SECOND = 1000;
    // Update frequency in seconds
    public static final int UPDATE_INTERVAL_IN_SECONDS = 5;
    // Update frequency in milliseconds
    private static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS = 1;
    // A fast frequency ceiling in milliseconds
    private static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    
    private LocationClient locationClient;
    private LocationRequest locationRequest;
    
    private final ILocationServiceConsumer consumer;
    
    public LocationService(Context context, ILocationServiceConsumer consumer) {    	
    	this.consumer = consumer;
    	
    	locationRequest = LocationRequest.create();
		locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		locationRequest.setInterval(UPDATE_INTERVAL);
		locationRequest.setFastestInterval(FASTEST_INTERVAL);

		locationClient = new LocationClient(context, consumer, consumer);
    }

	public void start() {
		locationClient.connect();
	}

	public void stop() {
		if (locationClient.isConnected())
			locationClient.removeLocationUpdates(this);
		
		locationClient.disconnect();
	}

	@Override
	public void onLocationChanged(Location location) {
		consumer.onLocationChanged(location);
	}

	public void playServicesConnected() {
		locationClient.requestLocationUpdates(locationRequest, this);
	}

	public void playServicesDisconnected() {
		locationClient.connect();
	}
}
