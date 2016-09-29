package locatemate.locatemate;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
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
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    SharedRef sharedRef;
    int permissionCheck;
    static final int MY_LOCATION_REQUEST = 1;
    private static final String GET_DATA_URL = "http://localhost:3000/get_data";
    private static final String SAVE_DATA_URL = "http://localhost:3000/save_data";

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

    private class JsonTask extends AsyncTask<String, String, JSONObject> {

        private ProgressDialog progressDialog;

        private static final String TAG_SUCCESS = "success";
        private static final String TAG_MESSAGE = "message";

        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(MapsActivity.this);
            progressDialog.setMessage("Attempting login...");
            progressDialog.setIndeterminate(false);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected JSONObject doInBackground(String... args) {

            try {
                String json;

                URL url = new URL(args[0]);

                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                urlConnection.setDoOutput(true);
                urlConnection.setDoInput(true);
                urlConnection.setRequestMethod("POST");
                urlConnection.setConnectTimeout(7000);

                try {
                    InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                    OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());

                    json = ConvertInputToString(in);

                    publishProgress(json);
                } finally {
                    urlConnection.disconnect();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

//            try {
//                HashMap<String, String> params = new HashMap<>();
//                params.put("id", args[0]);
//                params.put("userName", args[1]);
//                params.put("password", args[2]);
//                params.put("groupName", args[3]);
//                params.put("iconid", args[4]);
//                params.put("lat", args[5]);
//                params.put("lon", args[6]);
//                params.put("timestamp", args[7]);
//
//                Log.d("request", "starting");
//
//                JSONObject json = jsonParser.makeHttpRequest(LOGIN_URL, "POST", params);
//
//                if (json != null){
//                    Log.d("JSON result", json.toString());
//
//                    return json;
//                }
//            }catch (Exception e){
//                e.printStackTrace();
//            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            try {
                // Parse the JSON??!!?!??
                Mate mate;
                JSONArray json = new JSONArray(values[0]);
//                for (int i = 0 ; i < json.length() ; i++) {
//                    mate = (Mate)json.get(i);
//
//                }
                mate = (Mate)json.get(0);

                Toast.makeText(getApplicationContext(), "GroupName: " + mate.getGroupName(), Toast.LENGTH_LONG).show();
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

            if (json != null){
                Toast.makeText(MapsActivity.this, json.toString(), Toast.LENGTH_LONG).show();
                try {
                    success = json.getInt(TAG_SUCCESS);
                    message = json.getString(TAG_MESSAGE);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }

            if (success == 1){
                Log.d("Success!", message);
            }else {
                Log.d("Failure", message);
            }
        }
    }

    public static String ConvertInputToString(InputStream in){
        BufferedReader breader = new BufferedReader(new InputStreamReader(in));
        String line;
        String result = "";

        try {
            while ((line = breader.readLine()) != null){
                result += line;
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

}
