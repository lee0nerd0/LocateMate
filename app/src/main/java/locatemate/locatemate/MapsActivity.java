package locatemate.locatemate;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.ads.MobileAds;
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
import java.security.Timestamp;
import java.sql.Time;
import java.util.HashMap;
import java.util.UUID;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    SharedRef sharedRef;
    int permissionCheck;
    static final int MY_LOCATION_REQUEST = 1;
    // android localhost is 10.0.2.2
    private static final String GET_DATA_URL = "http://10.0.2.2:3000/get_data";
    private static final String SAVE_DATA_URL = "http://localhost:3000/save_data";
    private Mate me = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        checkPermissions();

        sharedRef = new SharedRef(this);

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
        mMap.getUiSettings().setMapToolbarEnabled(false);

        JsonTask jsonTask = new JsonTask();
        jsonTask.execute(GET_DATA_URL);

        runListener();

    }

    public void checkPermissions(){
        permissionCheck = ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_LOCATION_REQUEST);
        } else {
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
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
                }
                return;
            }
        }
    }

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

    public void runListener(){
        MyLocationListener myLocationListener = new MyLocationListener(this);
        LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);

        // TODO: Setting DEFAULT MATE, NEED TO MOVE SOMEWHERE ELSE
        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();
        me = new Mate(Settings.Secure.ANDROID_ID, "#myname", "", "DefaultGroup", 0, myLocationListener.getLatlng(), ts);
        //

        try {

            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (location != null) {
                mMap.clear();
                LatLng you = new LatLng(location.getLatitude(), location.getLatitude());
                mMap.addMarker(new MarkerOptions().position(you).title("You are here"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(you));
            } else {
                //provider, minimum time to wait, distance for update, process to start
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30, 10, myLocationListener);

            }

        }
        catch (SecurityException e) {
            // User didn't provide location
        }

        MyThread thread = new MyThread();
        thread.start();
    }

    class MyThread extends Thread {
        @Override
        public void run() {
            while(true){
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (MyLocationListener.location != null) {
                            mMap.clear();
                            LatLng you = new LatLng(MyLocationListener.location.getLatitude(), MyLocationListener.location.getLatitude());
                            mMap.addMarker(new MarkerOptions().position(you).title("You are here"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(you));
                        }
                    }
                });
                try{
                    Thread.sleep(1000);
                }catch(InterruptedException e){
                    e.printStackTrace();
                }
            }
        }
    }

    private class JsonTask extends AsyncTask<String, JSONArray, JSONObject> {

        private ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MapsActivity.this);
            progressDialog.show(MapsActivity.this,"JSON Task", "Working...", false, true);
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {
                JSONArray json;

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
                    json = ConvertInputToJsonArray(in);

                    publishProgress(json);
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(JSONArray... values) {
            try {
                Mate mate;
                JSONArray json = values[0];
                Toast.makeText(MapsActivity.this, "onProgressUpdate", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            int success = 0;
            String message = "";

            if (progressDialog != null && progressDialog.isShowing()){
                progressDialog.dismiss();
            }
        }
    }

    public static JSONArray ConvertInputToJsonArray(InputStream in){
        BufferedReader breader = new BufferedReader(new InputStreamReader(in));
        String line;
        String result = "";

        try {
            while ((line = breader.readLine()) != null){
                result += line;
            }

            JSONArray json = new JSONArray(result);

            in.close();

            return json;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

}
