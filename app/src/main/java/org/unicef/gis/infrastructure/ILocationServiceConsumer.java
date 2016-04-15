package org.unicef.gis.infrastructure;

import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;

public interface ILocationServiceConsumer extends ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
}
