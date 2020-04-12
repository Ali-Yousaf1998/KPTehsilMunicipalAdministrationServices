package com.uzair.kptehsilmunicipaladministrationservices.AppModules.UserComplaint.ComplaintsMain;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.uzair.kptehsilmunicipaladministrationservices.Models.MapPresenterImplementer;
import com.uzair.kptehsilmunicipaladministrationservices.Presenters.MapPresenter;
import com.uzair.kptehsilmunicipaladministrationservices.R;
import com.uzair.kptehsilmunicipaladministrationservices.Views.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.defaultMarker;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback , MapView
{

    private GoogleMap mMap;
    private double currentLat, currentLong;
    private FusedLocationProviderClient mLocationService;
    private SearchView searchView;
    private Circle circle;
    private MapPresenterImplementer mapPresenterImplementer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        initViews();
        mapPresenterImplementer.initMap();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                mMap.clear();

                String searchLocation = searchView.getQuery().toString().trim();

                mapPresenterImplementer.searchLocation(searchLocation.toUpperCase());

                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {

                return false;
            }
        });

    }

    private void initViews() {

        mLocationService = new FusedLocationProviderClient(this);

        searchView = findViewById(R.id.searchView);

        mapPresenterImplementer = new MapPresenterImplementer(this , getApplicationContext());

    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    // check location permissions
    @Override
    public boolean onCheckPermission()
    {
        return ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    // request location permission
    @Override
    public void onRequestPermission() {

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
            }
        }
    }

    // check google services on device
    @Override
    public boolean onCheckService()
    {
        GoogleApiAvailability googleApi = GoogleApiAvailability.getInstance();
        int result = googleApi.isGooglePlayServicesAvailable(this);

        if (result == ConnectionResult.SUCCESS) {
            return true;
        } else if (googleApi.isUserResolvableError(result)) {
            Dialog dialog = googleApi.getErrorDialog(this, result, 12, null);
            dialog.show();
        } else {
            Toast.makeText(this, "Play services is require by this application ", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    // check enable gps or not
    @Override
    public boolean onGPSEnabled()
    {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        boolean locationProvider = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (locationProvider) {
            return true;
        } else {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this)
                    .setTitle("GPS Enable")
                    .setMessage("GPS is required for this app to work .Pleas enable GPS")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, 2);
                            MapActivity.this.finish();

                        }
                    });
            alertDialog.setCancelable(false);
            alertDialog.show();

        }


        return false;
    }

    // get the user current location
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onGetCurrentLocation() {
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                return;
            }


        mLocationService.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onComplete(@NonNull Task<Location> task) {

                if (task.getResult() != null) {
                    Location location = task.getResult();
                    currentLat = location.getLatitude();
                    currentLong = location.getLongitude();

                    getLngLat(location.getLongitude(), location.getLatitude(), "Your Current Location");

                }
                else
                {
                    Toast.makeText(MapActivity.this, "Please try again", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // inflate map in activity
    @Override
    public void onInflateMap() {

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(this);
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void getLatLng(double lat, double lng) {

        getLngLat(lat , lat , "Your Search Location");
    }

    // check where the permission is granted or not after request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Ready To Map", Toast.LENGTH_SHORT).show();
        }
    }

    // move to the current location
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void getLngLat(double lng, double lat, String locationName) {

        LatLng latLng = new LatLng(lat, lng);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 19);

        mMap.animateCamera(cameraUpdate);

        setMarker(latLng, locationName);
    }

    // set marker on the current location
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setMarker(LatLng latLng, String locationName) {
        MarkerOptions markerOptions = new MarkerOptions()
                .title(locationName)
                .position(latLng);
        markerOptions.icon(defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        mMap.addMarker(markerOptions);

        circle = mMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(2)
                .strokeWidth(2)
                .strokeColor(Color.GREEN)
                .fillColor(getColor(R.color.transparent)));
    }


    // button click add location to complaint
    public void sendLocationToComplaintData(View view)
    {
        Toast.makeText(this, "Complaint Location is Added", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(MapActivity.this, AddComplaints.class);
        intent.putExtra("lat", currentLat);
        intent.putExtra("lng", currentLong);
        setResult(RESULT_OK, intent);
        finish();

    }

}
