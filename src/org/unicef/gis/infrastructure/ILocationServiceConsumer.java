package org.unicef.gis.infrastructure;

import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;

public interface ILocationServiceConsumer extends ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
}
