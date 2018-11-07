package com.example.paragjai.origin_destination_directions_api;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.model.TravelMode;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {

    private final double originLat=12.965570;
    private final double originLong=77.606250; /*Shanthi Park Apartments, Jayanagar 9th Block, Bangalore*/
    private final double destinationLat= 12.9243385;
    private final double destinationLong= 77.6803683; /*Adarsh Palm Retreat, Intel, Bellandur, Bangalore*/
    private final String API_KEY = "AIzaSyDU7DT4NsobTmantOe37IeGTkAyolKWfik";
    private static final int MY_PERMISSION_REQUEST_LOCATION_PERMISSION = 1;
    final String perm[] = {Manifest.permission.INTERNET};
    private GoogleMap mMap;
    LocationRequest mLocationRequest; /* Add  implementation 'com.google.android.gms:play-services-location:16.0.0' to build.gradle (app) */
    Marker originMarker;
    Marker destinationMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    /* 1. Hard code :
            Origin and destination Latitude, Longitude

       2. Use Google's Direction API :
                implementation 'com.google.maps:google-maps-services:0.1.20'
            to build.gradle (Module:app)
            Ask for Internet Permission - just adding it to the manifest works. No asking for runtime permission required.
       3. Parse the result :
            i) Get time between the two.
            ii) Get the distance between the two.
     */



        @Override
    public void onMapReady(GoogleMap googleMap) {
            mMap = googleMap;
            mapSettings();
            usingDirectionsApi();
    }

    /* Part of Directions API code : Begins */
    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3)
                .setApiKey(API_KEY)
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(5, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }

    public void usingDirectionsApi()
    {
        Log.d("MainActivity:" , "Inside usingDirectionsApi");
        LatLng origin = new LatLng(originLat, originLong);
        LatLng destination = new LatLng(destinationLat, destinationLong);

        /* Directions API Begins */
        DateTime now = new DateTime();
        try {
            GeoApiContext gpc = getGeoContext();
            com.google.maps.model.LatLng originForDirectionsApi = new com.google.maps.model.LatLng(originLat, originLong);
            com.google.maps.model.LatLng destinationForDirectionsApi = new com.google.maps.model.LatLng(destinationLat, destinationLong);
            DirectionsResult result = DirectionsApi.newRequest(gpc)
                    .mode(TravelMode.DRIVING).origin(originForDirectionsApi)
                    .destination(destinationForDirectionsApi).departureTime(now)
                    .await();

            Log.d("MainActivity:", result.routes[0].legs[0].startAddress);
            Log.d("MainActivity:", "At " + now);
            Log.d("MainActivity:", "Time to travel between the two :"+ result.routes[0].legs[0].duration);
            Log.d("MainActivity:", " Distance :" + result.routes[0].legs[0].distance);
            Log.d("MainActivity:", "Time to travel between the two :"+ result.routes[0].legs[0].duration.humanReadable);
            Log.d("MainActivity:", " Distance :" + result.routes[0].legs[0].distance.humanReadable);
            puttingMarkerOnMap(result.routes[0].legs[0].duration.humanReadable);
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        /* Directions API Ends */
    }
    /* Part of Directions API code : Ends */

    public void puttingMarkerOnMap(String durationAwayFromOrigin)
    {

        Log.d("MainActivity:" , "Inside puttingMarkerOnMap");
        LatLng origin = new LatLng(originLat, originLong);
        LatLng destination = new LatLng(destinationLat, destinationLong);

        /* BEGINS : Adding origin and destination marker to Map */
        originMarker = mMap.addMarker(new MarkerOptions().position(origin).title("You are here"));

        BitmapDescriptor destinationIcon = BitmapDescriptorFactory.fromResource(R.drawable.ic_directions_car_black_24dp);

        destinationMarker = mMap.addMarker(new MarkerOptions().position(destination).title(durationAwayFromOrigin)
                .snippet("SRR South C")
                .icon(destinationIcon));
        destinationMarker.showInfoWindow();
        /* ENDS : Adding origin and destination marker to Map */


        /* BEGINS : Adding all the markers to a list */
        List<Marker> markerList =   new ArrayList<>();
        markerList.add(originMarker);
        markerList.add(destinationMarker);
        /* ENDS : Adding all the markers to a list */

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for(Marker m : markerList)
        {
            Log.d("Marker: ", m.getTitle());
            builder.include(m.getPosition());
            //LatLng latlng = new LatLng(m.getPosition().latitude, m.getPosition().longitude);
            //mMap.addMarker(new MarkerOptions().position(latlng));
            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latlng, 2));
            //mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 12));
        }
        LatLngBounds bounds = builder.build();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding));


    }


    public void mapSettings()
    {
        Log.d("MainActivity:" , "Inside mapSettings");
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setTrafficEnabled(false);
        mMap.setIndoorEnabled(false);
        mMap.setBuildingsEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);
    }

}
