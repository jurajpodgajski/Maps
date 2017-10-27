package juraj.podgajski.com.mape;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final int REQ_CODE_LOCATION = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private GoogleMap mMap;
    private static final LatLng LAT_LNG_ZG = new LatLng(45.817, 16);
    private boolean locationPermissionGranted;

    private Button bPicture;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        bPicture = (Button) findViewById(R.id.bPicture);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        requestUsersLocationPermission();
        connectGoogleApiClient();
    }

    private void connectGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        googleApiClient.disconnect();
        super.onDestroy();
    }

    private void requestUsersLocationPermission() {

        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQ_CODE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case REQ_CODE_LOCATION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                }
            }
            Toast.makeText(MapsActivity.this, "Location Permision Granted", Toast.LENGTH_SHORT).show();
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

        if (locationPermissionGranted) {

            // Add a marker in Sydney and move the camera
            LatLng sydney = new LatLng(-34, 151);
            mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        } else {
            LatLng latLngJuraja = getLatLngFromAddress("Siget 14e, Zagreb");
            configureMap();
            addMarker(latLngJuraja);
            animateCamera();
            setupMapListener();
        }
    }

    private LatLng getLatLngFromAddress(String address) {
        LatLng latLng = LAT_LNG_ZG;
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> adrese = geocoder.getFromLocationName(address, 1);
            if (adrese.size() > 0) {
                double lat = adrese.get(0).getLatitude();
                double lng = adrese.get(0).getLongitude();
                latLng = new LatLng(lat, lng);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return latLng;
    }

    private void setupMapListener() {

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                addMarker(latLng);
                String address = getAddressFromLatLng(latLng);
                Toast.makeText(MapsActivity.this, address, Toast.LENGTH_SHORT).show();
            }
        });

        bPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && requestCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap image = (Bitmap) extras.get("data");
            LatLng currentLatLng = getDeviceLocation();

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14f));
            mMap.addMarker(new MarkerOptions().position(currentLatLng).icon(BitmapDescriptorFactory.fromBitmap(image)));
        }
    }

    private LatLng getDeviceLocation() {
        if (ContextCompat.checkSelfPermission(MapsActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]
                    {Manifest.permission.ACCESS_FINE_LOCATION}, REQ_CODE_LOCATION);
        }
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());

        return currentLatLng;
    }

    private String getAddressFromLatLng(LatLng latLng) {

        String address = "";

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> listaAdresa = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            address = listaAdresa.get(0).getAddressLine(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return address;
    }

    private void animateCamera() {

        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LAT_LNG_ZG, 15f));

    }

    private void addMarker(LatLng latLng) {

        mMap.addMarker(new MarkerOptions().position(latLng).title("Zagreb"));

    }

    private void configureMap() {

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setTrafficEnabled(true);

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
