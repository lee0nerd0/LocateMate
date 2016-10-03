package locatemate.locatemate;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by lee0nerd0 on 9/27/2016.
 */

public class MyLocationListener implements LocationListener {
    public static Location location;

    Context context;

    public MyLocationListener(Context context){
        this.context = context;
    }

    public LatLng getLatlng() {
        if (location != null) {
            return new LatLng(location.getLatitude(), location.getLongitude());
        }
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
        Toast.makeText(context, "GPS is changed", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(String s) {
        Toast.makeText(context, "GPS is enabled", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderDisabled(String s) {
        Toast.makeText(context, "GPS is disabled", Toast.LENGTH_LONG).show();
    }
}
