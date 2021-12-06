package com.example.covidvolunteeringsite;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private final HashMap<Marker, Integer> markers = new HashMap<>();
    private static final long UPDATE_INTERVAL = 10*1000 ;
    private static final long FASTEST_INTERVAL = 5000 ;
    protected FusedLocationProviderClient client;
    protected LocationRequest mLocationRequest;
    private GoogleMap mMap;
    private SiteManager siteManager;
    private UserManager userManager;
    private float prevZoomLevel = 3.0F;
    private double minDistance = 500;
    private int currentUserID;
    private boolean isCurrentUserAdmin;
    private String currentUserName, currentUserUsername;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        siteManager = new SiteManager(this);
        userManager = new UserManager(this);
        userManager.open();
        siteManager.open();
        Intent intent = getIntent();
        currentUserID = intent.getExtras().getInt("user_id");
        currentUserName = intent.getExtras().getString("user_name");
        currentUserUsername = intent.getStringExtra("user_username");
        isCurrentUserAdmin = (intent.getExtras().getInt("is_super") == 1);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        requestPermission();
        client = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
        mMap = googleMap;
        client.getLastLocation().addOnSuccessListener(location -> {
            LatLng curr = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(curr).title("YOU ARE HERE").zIndex(1.0f));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(curr));

        });
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.setOnMapClickListener(latLng -> {
            //Move to add new site activity
            Intent intent = new Intent(MapsActivity.this, AddSiteActivity.class);
            intent.putExtra("longitude", latLng.longitude);
            intent.putExtra("latitude", latLng.latitude);
            intent.putExtra("leader_name", currentUserName);
            intent.putExtra("leader_id", currentUserID);
            startActivityForResult(intent, 100);
        });
        mMap.setOnMarkerClickListener(marker -> {
            //Show the info of the site
            if(markers.containsKey(marker)){
                Integer site_id = markers.get(marker);
                if(site_id != null) showSiteInfo(site_id);
            }
            return true;
        });
        mMap.setOnCameraIdleListener(this::showNearbySite);
    }

    private void showNearbySite(){
        VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();
        CameraPosition cameraPosition = mMap.getCameraPosition();
        LatLng cameraTarget = cameraPosition.target;
        float zoomLevel = cameraPosition.zoom;
        for (Marker aMarker: markers.keySet()) {
            aMarker.remove();
        }
        markers.clear();

        Cursor cursor = siteManager.getSiteByDistance(visibleRegion.latLngBounds.southwest.latitude,
                visibleRegion.latLngBounds.northeast.latitude,
                visibleRegion.latLngBounds.southwest.longitude,
                visibleRegion.latLngBounds.northeast.longitude);
        Location currLocation = new Location("");
        currLocation.setLongitude(cameraTarget.longitude);
        currLocation.setLatitude(cameraTarget.latitude);
        if(zoomLevel < prevZoomLevel){
            minDistance += 150;
        } else if(zoomLevel > prevZoomLevel){
            minDistance -= 150;
        }
        do{
            if(cursor.getCount() > 0){
                LatLng pos = new LatLng(cursor.getDouble(4), cursor.getDouble(3));
                Location site = new Location("");
                site.setLongitude(pos.longitude);
                site.setLatitude(pos.latitude);
                if(currLocation.distanceTo(site) >= minDistance * 1000){
                    Marker newMarker = mMap.addMarker(new MarkerOptions().position(pos)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    markers.put(newMarker, cursor.getInt(0));
                }
            }
        } while(cursor.getCount() > 0 && cursor.moveToNext());
        prevZoomLevel = zoomLevel;
    }

    private void showSiteInfo(int site_id){
        Cursor cursor = siteManager.getOneSite(site_id);
        Cursor userCursor = userManager.getOneUser(cursor.getInt(2));

        if(cursor.getCount() > 0){
            AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
            alertDialog.setTitle("Site Details");

            String content = "Site Number: " + cursor.getInt(0) +
                    "\nSite Name: " + cursor.getString(1) +
                    "\nSite Latitude: " + cursor.getDouble(4) +
                    "\nSite Longitude: " + cursor.getDouble(3) +
                    "\nSite Leader Name: " + userCursor.getString(0) +
                    "\nNumber of tested people: " + cursor.getDouble(5);
            alertDialog.setMessage(content);

            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", (dialog, which) -> dialog.dismiss());

            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "JOIN", (dialog, which) -> {
                Intent intent = new Intent(MapsActivity.this, JoinSiteActivity.class);
                intent.putExtra("user_id", currentUserID);
                intent.putExtra("user_name", currentUserName);
                intent.putExtra("user_username", currentUserUsername);
                intent.putExtra("site_id", cursor.getInt(0));
                intent.putExtra("site_name", cursor.getString(1));
                intent.putExtra("site_leader", userCursor.getString(0));
                startActivityForResult(intent, 200);
                dialog.dismiss();
            });
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ROUTE", (dialog, which) -> {
                Toast.makeText(MapsActivity.this, "You would go to route!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
            alertDialog.show();
        }
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100){
            if(resultCode == 100){
                Toast.makeText(MapsActivity.this, "New site is successfully added!", Toast.LENGTH_SHORT).show();
                showNearbySite();
            }
        }
        if(requestCode == 200){
            if(resultCode == 200){
                Toast.makeText(MapsActivity.this, "You have successfully joined!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}