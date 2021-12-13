package com.example.covidvolunteeringsite;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
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
    private NotificationAppManager notificationAppManager;
    private float prevZoomLevel = 3.0F;
    private double minDistance = 500;
    private Integer currentUserID;
    private Boolean isCurrentUserAdmin;
    private String currentUserName, currentUserUsername;
    private String ROUTE_API;
    private String jsonStr = "";
    private Polyline polyline;
    private String[] from = new String[]{
            DatabaseHelper.SITE_ID,
            "get_site_name",
            DatabaseHelper.USER_NAME,
    };
    private int[] to = new int[]{
            R.id.list_site_num,
            R.id.list_site_name,
            R.id.list_site_leader_name
    };
    private String[] from1 = new String[]{
            DatabaseHelper.SITE_ID,
            DatabaseHelper.SITE_NAME,
    };
    private int[] to1 = new int[]{
            R.id.list_site_num,
            R.id.list_site_name
    };
    private LatLng currentPosition;
    private RadioGroup radioGroup;
    private String filterChoice = "site_name";
    private Cursor searchCursor = null;
    private Button cancelRoute;
    private LinearLayout loggedInContent, guestContent, superUserContent;
    private NotificationManager notificationManager;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
        notificationManager = getSystemService(NotificationManager.class);
        createNotificationChannels();
        loggedInContent = findViewById(R.id.logged_in_tab);
        guestContent = findViewById(R.id.guest_tab);

        FloatingActionButton floatingActionButton = findViewById(R.id.floating);
        floatingActionButton.setOnClickListener(v -> {
            if(currentUserID == null){
                requestLogin();
            } else {
                //Move to add new site activity
                Intent intent = new Intent(MapsActivity.this, AddSiteActivity.class);
                intent.putExtra("leader_name", currentUserName);
                intent.putExtra("leader_id", currentUserID);
                intent.putExtra("from_floating", true);
                startActivityForResult(intent, 100);
            }
        });
        TextView bottomRegisteredItem = findViewById(R.id.view_sites);
        bottomRegisteredItem.setOnClickListener(v -> {
            viewAllRegisteredSite();
        });

        TextView bottomUpdateItem = findViewById(R.id.update_site);
        bottomUpdateItem.setOnClickListener(v -> {
            viewLeadSites(true);
        });

        TextView bottomJoinItem = findViewById(R.id.join_site);
        bottomJoinItem.setOnClickListener(v -> {
            Toast.makeText(MapsActivity.this, "join", Toast.LENGTH_SHORT).show();
        });

        TextView bottomViewVolunteersItem = findViewById(R.id.view_volunteers);
        bottomViewVolunteersItem.setOnClickListener(v -> {
            viewLeadSites(false);
        });

        TextView bottomLogoutItem = findViewById(R.id.logout);
        bottomLogoutItem.setOnClickListener(v -> {
            if(currentUserID == null) {
                Toast.makeText(MapsActivity.this, "You have not signed in!", Toast.LENGTH_SHORT).show();
                return;
            }
            currentUserID = null;
            isCurrentUserAdmin = null;
            currentUserName = null;
            currentUserUsername = null;
            Toast.makeText(MapsActivity.this, "You have successfully logged out!", Toast.LENGTH_SHORT).show();
            showNearbySite();
            loggedInContent.setVisibility(View.GONE);
            guestContent.setVisibility(View.VISIBLE);
        });

        siteManager = new SiteManager(this);
        userManager = new UserManager(this);
        notificationAppManager = new NotificationAppManager(this);
        userManager.open();
        siteManager.open();
        notificationAppManager.open();

        cancelRoute = findViewById(R.id.cancel_route);

        radioGroup = findViewById(R.id.search_filter);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.name_choice) filterChoice = "site_name";
                else if (checkedId == R.id.leader_choice) filterChoice = "site_leader_name";
            }
        });

        SearchView searchView = findViewById(R.id.searchSite);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(filterChoice.equals("site_name")) searchCursor = siteManager.getSitesByName(newText);
                else if(filterChoice.equals("site_leader_name")) searchCursor = siteManager.getSitesByLeaderName(newText);
                SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(MapsActivity.this,
                        R.layout.site_listview_layout, searchCursor, from, to, 0);
                simpleCursorAdapter.notifyDataSetChanged();
                searchView.setSuggestionsAdapter(simpleCursorAdapter);
                return true;
            }
        });

        searchView.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                return false;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                searchCursor.moveToPosition(position);
                showSiteInfo(searchCursor.getInt(0));
                return true;
            }
        });
        Button gotoLogin = findViewById(R.id.mainToLoginIn);
        Button gotoSignUp = findViewById(R.id.mainToSignUp);
        gotoLogin.setOnClickListener(v -> {
            gotoLogin(false);
        });
        gotoSignUp.setOnClickListener(v -> {
            gotoLogin(true);
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        requestPermission();
        client = LocationServices.getFusedLocationProviderClient(MapsActivity.this);
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        client.getLastLocation().addOnSuccessListener(location -> {
            currentPosition = new LatLng(location.getLatitude(), location.getLongitude());
            Bitmap bitmap = Bitmap.createScaledBitmap(
                            BitmapFactory.decodeResource(getResources(), R.drawable.ic_current),
                            120, 120, false);
            mMap.addMarker(new MarkerOptions().position(currentPosition).title("YOU ARE HERE").zIndex(1.0f)
                    .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));
        }
        );

        mMap.setOnMapClickListener(latLng -> {
            if(polyline != null) Toast.makeText(MapsActivity.this, "You should cancel the route mode first", Toast.LENGTH_SHORT).show();
            if(currentUserID == null){
                requestLogin();
            } else {
                //Move to add new site activity
                Intent intent = new Intent(MapsActivity.this, AddSiteActivity.class);
                intent.putExtra("longitude", latLng.longitude);
                intent.putExtra("latitude", latLng.latitude);
                intent.putExtra("leader_name", currentUserName);
                intent.putExtra("leader_id", currentUserID);
                startActivityForResult(intent, 100);
            }
        });
        mMap.setOnMarkerClickListener(marker -> {
            if(polyline != null) Toast.makeText(MapsActivity.this, "You should cancel the route mode first", Toast.LENGTH_SHORT).show();
            //Show the info of the site
            if(markers.containsKey(marker)){
                Integer site_id = markers.get(marker);
                if(site_id != null) showSiteInfo(site_id);
            }
            return true;
        });
        mMap.setOnCameraIdleListener(this::showNearbySite);
    }
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel1 = new NotificationChannel("site_change_1",
                    "Site Change", NotificationManager.IMPORTANCE_HIGH);
            channel1.setDescription("This is Site Change Channel");
            notificationManager.createNotificationChannel(channel1);
        }
    }

    private void sendNotifications() {
        if(currentUserID == null) return;
        Cursor lCursor = notificationAppManager.getNotificationOfLeader(currentUserID);
        do{
            if(lCursor.getCount() > 0 ){
                Notification notification = new NotificationCompat.Builder(this,
                        "site_change_1")
                        .setSmallIcon(R.drawable.ic_lead_site)
                        .setContentTitle("New Volunteer!")
                        .setContentText(lCursor.getString(2))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .build();
                notificationManager.notify(lCursor.getInt(0), notification);
            }
        } while (lCursor.moveToNext());

        Cursor cursor = notificationAppManager.getNotificationOfVolunteer(currentUserID);
        do{
            if(cursor.getCount() > 0 ){
                Notification notification = new NotificationCompat.Builder(this,
                        "site_change_1")
                        .setSmallIcon(R.drawable.ic_checked)
                        .setContentTitle("Joined Site Modified")
                        .setContentText(cursor.getString(2))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                        .build();
                notificationManager.notify(cursor.getInt(0), notification);
            }
        } while (cursor.moveToNext());
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
        Cursor joinedCursor = null;
        ArrayList<Integer> joinedSiteId = new ArrayList<>();
        if(currentUserID != null){
            joinedCursor = siteManager.getJoinedSitesOfOneUser(currentUserID);
            do{
                if(joinedCursor.getCount() > 0) joinedSiteId.add(joinedCursor.getInt(0));
            } while(joinedCursor.moveToNext());
        }
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
                    Marker newMarker;
                    if(joinedSiteId.contains(cursor.getInt(0))){
                        Bitmap bitmap = Bitmap.createScaledBitmap(
                                BitmapFactory.decodeResource(getResources(), R.drawable.ic_checked),
                                120, 120, false);
                        newMarker = mMap.addMarker(new MarkerOptions().position(pos)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                    } else if (currentUserID != null && currentUserID == cursor.getInt(2)) {
                        Bitmap bitmap = Bitmap.createScaledBitmap(
                                BitmapFactory.decodeResource(getResources(), R.drawable.ic_lead_site),
                                120, 120, false);
                        newMarker = mMap.addMarker(new MarkerOptions().position(pos)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                    } else {
                        Bitmap bitmap = Bitmap.createScaledBitmap(
                                BitmapFactory.decodeResource(getResources(), R.drawable.ic_normal_site),
                                120, 120, false);
                        newMarker = mMap.addMarker(new MarkerOptions().position(pos)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                    }
                    markers.put(newMarker, cursor.getInt(0));
                }
            }
        } while(cursor.getCount() > 0 && cursor.moveToNext());
        prevZoomLevel = zoomLevel;
    }

    private void showSiteInfo(int site_id){
        Cursor cursor = siteManager.getOneSite(site_id);
        Cursor userCursor = userManager.getOneUser(cursor.getInt(2));
        Cursor volunteerCursor = siteManager.getAllVolunteerInOneSite(site_id);

        if(cursor.getCount() > 0){
            AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
            alertDialog.setTitle("Site Details");

            String content = "Site Number: " + cursor.getInt(0) +
                    "\nSite Name: " + cursor.getString(1) +
                    "\nSite Leader Name: " + userCursor.getString(0);

            String content1 = "Site Number: " + cursor.getInt(0) +
                    "\nSite Name: " + cursor.getString(1) +
                    "\nSite Latitude: " + cursor.getDouble(4) +
                    "\nSite Longitude: " + cursor.getDouble(3) +
                    "\nSite Leader Name: " + userCursor.getString(0) +
                    "\nNumber of volunteers: " + volunteerCursor.getCount() +
                    "\nNumber of tested people: " + cursor.getDouble(5);
            if(currentUserID != null && currentUserID == cursor.getInt(2)){
                alertDialog.setMessage(content1);
            } else if(currentUserID == null || !siteManager.checkExistVolunteer(site_id, currentUserUsername)){
                alertDialog.setMessage(content);
            } else {
                alertDialog.setMessage(content1);
            }

            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", (dialog, which) -> dialog.dismiss());

            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "REGISTER", (dialog, which) -> {
                if(currentUserID == null){
                    requestLogin();
                } else {
                    Intent intent = new Intent(MapsActivity.this, JoinSiteActivity.class);
                    intent.putExtra("user_id", currentUserID);
                    intent.putExtra("user_name", currentUserName);
                    intent.putExtra("user_username", currentUserUsername);
                    intent.putExtra("site_id", cursor.getInt(0));
                    intent.putExtra("site_name", cursor.getString(1));
                    intent.putExtra("site_leader", userCursor.getString(0));
                    intent.putExtra("site_leader_id", cursor.getInt(2));
                    startActivityForResult(intent, 200);
                    dialog.dismiss();
                }
            });
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ROUTE", (dialog, which) -> {
                if(currentPosition != null){
                    LatLng dest = new LatLng(cursor.getDouble(4), cursor.getDouble(3));
                    route(currentPosition, dest);
                    Toast.makeText(MapsActivity.this, "You would go to route!", Toast.LENGTH_SHORT).show();
                } else Toast.makeText(MapsActivity.this, "We need to get your location", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            });
            alertDialog.show();
        }
    }
    private void route(LatLng currentPosition, LatLng routeSite){
        ROUTE_API = "https://maps.googleapis.com/maps/api/directions/json?origin=" + currentPosition.latitude
                + "," + currentPosition.longitude + "&destination=" + routeSite.latitude + "," + routeSite.longitude
                + "&mode=driving&key=" + getString(R.string.google_maps_key);
        new GetRoute().execute();
    }


    private void requestLogin(){
        AlertDialog alertDialog = new AlertDialog.Builder(MapsActivity.this).create();
        alertDialog.setTitle("LOGIN REQUIRED");

        alertDialog.setMessage("You need to login first");
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "CANCEL", (dialog, which) -> dialog.dismiss());
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "LOGIN", (dialog, which) -> {
            gotoLogin(false);
            dialog.dismiss();
        });
        alertDialog.show();
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(MapsActivity.this, new String[]{
                        android.Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);
    }
    private void gotoLogin(boolean straightToSignUp){
        Intent intent = new Intent(MapsActivity.this, LoginActivity.class);
        if(straightToSignUp) intent.putExtra("straight_to_sign_up", true);
        else intent.putExtra("straight_to_sign_up", false);
        startActivityForResult(intent, 300);
    }
    public void showFilterChoices(View v){
        if(radioGroup.getVisibility() == View.GONE) radioGroup.setVisibility(View.VISIBLE);
        else radioGroup.setVisibility(View.GONE);
    }

    private void viewAllRegisteredSite(){
        if(currentUserID == null){
            requestLogin();
            return;
        }
        Cursor cursor = siteManager.getJoinedSitesOfOneUser(currentUserID);

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MapsActivity.this);
        builderSingle.setTitle("LIST OF YOUR JOINED SITES");

        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(MapsActivity.this,
                R.layout.site_listview_layout, cursor, from, to, 0 );
        simpleCursorAdapter.notifyDataSetChanged();

        builderSingle.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());

        builderSingle.setAdapter(simpleCursorAdapter, (dialog, which) -> showSiteInfo(cursor.getInt(0)));
        builderSingle.show();
    }

    private void viewLeadSites(boolean toUpdate){
        if(currentUserID == null){
            requestLogin();
            return;
        }
        Cursor cursor = siteManager.getLeadSites(currentUserID);

        AlertDialog.Builder builderSingle = new AlertDialog.Builder(MapsActivity.this);
        builderSingle.setTitle("LIST OF YOUR LEAD SITES");

        SimpleCursorAdapter simpleCursorAdapter = new SimpleCursorAdapter(MapsActivity.this,
                R.layout.site_listview_layout, cursor, from1, to1, 0 );
        simpleCursorAdapter.notifyDataSetChanged();


        builderSingle.setNegativeButton("CANCEL", (dialog, which) -> dialog.dismiss());

        builderSingle.setAdapter(simpleCursorAdapter, (dialog, which) -> {
            if(toUpdate){
                Intent intent = new Intent(MapsActivity.this, UpdateSiteActivity.class);
                intent.putExtra("site_id", cursor.getInt(0));
                intent.putExtra("site_name", cursor.getString(1));
                intent.putExtra("longitude", cursor.getDouble(3));
                intent.putExtra("latitude", cursor.getDouble(4));
                intent.putExtra("leader_name", currentUserName);
                intent.putExtra("num_of_tested", cursor.getInt(5));
                startActivityForResult(intent, 400);
            } else {
                Intent intent = new Intent(MapsActivity.this, VolunteerListActivity.class);
                intent.putExtra("site_id", cursor.getInt(0));
                intent.putExtra("site_name", cursor.getString(1));
                startActivityForResult(intent, 500);
            }
        });
        builderSingle.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(polyline != null) cancelRoute(cancelRoute);
        if(requestCode == 100){
            if(resultCode == 100){
                showNearbySite();
                Toast.makeText(MapsActivity.this, "New site is successfully added!", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == 200){
            if(resultCode == 200){
                showNearbySite();
                Toast.makeText(MapsActivity.this, "You have successfully joined!", Toast.LENGTH_SHORT).show();
            }
        }
        if(requestCode == 300){
            if(resultCode == 300){
                currentUserID = data.getExtras().getInt("user_id");
                currentUserName = data.getExtras().getString("user_name");
                currentUserUsername = data.getStringExtra("user_username");
                isCurrentUserAdmin = (data.getExtras().getInt("is_super") == 1);
                showNearbySite();
                guestContent.setVisibility(View.GONE);
                loggedInContent.setVisibility(View.VISIBLE);
                sendNotifications();
            }
        }
    }

    public void cancelRoute(View v){
        polyline.remove();
        polyline = null;
        cancelRoute.setVisibility(View.GONE);
    }

    private class GetRoute extends AsyncTask<Void, Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            jsonStr = HttpHandler.getRequest(ROUTE_API);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            if(polyline != null) cancelRoute(cancelRoute);
            ArrayList<LatLng> latLngs = new ArrayList<>();
            try{
                JSONObject jsonObject = new JSONObject(jsonStr);
                JSONObject routeObject = jsonObject.getJSONArray("routes").getJSONObject(0);
                JSONArray stepArray = routeObject.getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
                for (int i = 0; i < stepArray.length(); i++) {
                    JSONObject stepObject = stepArray.getJSONObject(i);
                    ArrayList<LatLng> polylineList = decodePoly(stepObject.getJSONObject("polyline").getString("points"));
                    latLngs.addAll(polylineList);
                }
                polyline = mMap.addPolyline(new PolylineOptions().addAll(latLngs));
                cancelRoute.setVisibility(View.VISIBLE);
            } catch (JSONException e){
                e.printStackTrace();
                Toast.makeText(MapsActivity.this, "ROUTE NOT FOUND!", Toast.LENGTH_SHORT).show();
            }

        }

        private ArrayList<LatLng> decodePoly(String encoded) {

            ArrayList<LatLng> poly = new ArrayList<>();
            int index = 0, len = encoded.length();
            int lat = 0, lng = 0;

            while (index < len) {
                int b, shift = 0, result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lat += dlat;

                shift = 0;
                result = 0;
                do {
                    b = encoded.charAt(index++) - 63;
                    result |= (b & 0x1f) << shift;
                    shift += 5;
                } while (b >= 0x20);
                int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
                lng += dlng;

                LatLng p = new LatLng((((double) lat / 1E5)),
                        (((double) lng / 1E5)));
                poly.add(p);
            }

            return poly;
        }
    }

    @Override
    protected void onDestroy() {
        userManager.close();
        siteManager.close();
        notificationAppManager.close();
        super.onDestroy();
    }
}