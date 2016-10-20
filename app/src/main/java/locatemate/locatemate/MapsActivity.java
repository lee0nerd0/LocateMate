package locatemate.locatemate;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    SharedRef sharedRef;
    // Permission things
    int permissionCheck;
    static final int MY_LOCATION_REQUEST = 1;
    // android localhost is 10.0.2.2
    private static final String GET_DATA_URL = "http://10.0.2.2:3000/get_data";
    private static final String SAVE_DATA_URL = "http://10.0.2.2:3000/save_data";
    // Mate things
    private Mate me;
    private JSONArray jsonMates;
    // Location things
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        checkPermissions();

        buildGoogleApiClient();

        sharedRef = new SharedRef(this);

    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(googleApiClient.isConnected()){
            googleApiClient.disconnect();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY).setInterval(100);
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        } catch (SecurityException e){
            e.printStackTrace();
        }

        if (lastLocation != null) {
            updateUI();
        }

    }

    private void updateUI() {
        MarkerOptions o = new MarkerOptions();
        LatLng pos;
        pos = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        o.position(pos).title("ME");
        mMap.addMarker(o);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        buildGoogleApiClient();
    }

    @Override
    public void onLocationChanged(Location location) {
        updateUI();
    }

    synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
    }

    public void checkPermissions(){
        permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, MY_LOCATION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_LOCATION_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    // Do things I'm supposed to do
                    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.map);
                    mapFragment.getMapAsync(this);
                } else {
                    // Permission denied
                    Toast.makeText(this, "Location Permission Denied", Toast.LENGTH_SHORT).show();
                    checkPermissions();
                }
                return;
            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setPadding(20, 20, 20, 20);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        JsonTask jsonTask = new JsonTask();
        jsonTask.execute(GET_DATA_URL);

    }

//    private void drawMates(JSONArray jsonMates) {
//        LatLng pos;
//        MarkerOptions o = new MarkerOptions();
//        JSONObject  mate;
//        int counter = 0;
//        try {
//            for (int i = 0; i < jsonMates.length(); i++) {
//                mate = jsonMates.getJSONObject(i);
//                pos = new LatLng(Double.valueOf(mate.getString("lat")),Double.valueOf(mate.getString("lon")));
//                o.position(pos).title(mate.getString("userName"));
//                mMap.addMarker(o);
//                counter++;
//                Log.d("drawMates: ", "Drawing Mate #" + String.valueOf(counter));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//    }

    private class JsonTask extends AsyncTask<String, String, JSONArray> {

        @Override
        protected JSONArray doInBackground(String... args) {

            try {
                JSONObject json;

                URL url = new URL(args[0]);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setConnectTimeout(7000);

                try {
                    OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                    out.write(me.toJSON().getBytes("UTF-8"));
                    out.close();

                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    json = ConvertInputToJson(in);

                    jsonMates = json.getJSONArray("data");

                    return jsonMates;

                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(JSONArray json) {
            super.onPostExecute(json);
            LatLng pos;
            MarkerOptions o = new MarkerOptions();
            JSONObject  mate;
            int counter = 0;
            try {
                for (int i = 0; i < json.length(); i++) {
                    mate = json.getJSONObject(i);
                    pos = new LatLng(Double.valueOf(mate.getString("lat")),Double.valueOf(mate.getString("lon")));
                    o.position(pos).title(mate.getString("userName"));
                    mMap.addMarker(o);
                    counter++;
                    Log.d("drawMates: ", "Drawing Mate #" + String.valueOf(counter));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static JSONObject ConvertInputToJson(InputStream in){
        BufferedReader breader = new BufferedReader(new InputStreamReader(in));
        String line;
        String result ="";

        try {
            while ((line = breader.readLine()) != null) {
                result += line;
            }

            JSONObject json = new JSONObject(result);

            in.close();

            return json;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

//    public static ArrayList<Mate> ConvertToMates(JSONArray jArray){
//        ArrayList<Mate> mates = new ArrayList<>(50);
//        JSONObject jMate;
//        Mate mate;
//        try {
//            for (int i = 0; i < jArray.length(); i++) {
//                jMate = jArray.getJSONObject(i);
//                mate = new Mate(jMate.getString("id"),
//                        jMate.getString("userName"),
//                        jMate.getString("password"),
//                        jMate.getString("groupName"),
//                        jMate.getInt("icon"),
//                        jMate.getString("lat"),
//                        jMate.getString("lon"),
//                        jMate.getString("timestamp"));
//                mates.add(i, mate);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return mates;
//    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.preferences:
            {
                Intent intent = new Intent();
                intent.setClassName(this, "locatemate.locatemate.MyPreferenceActivity");
                startActivity(intent);
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }
}