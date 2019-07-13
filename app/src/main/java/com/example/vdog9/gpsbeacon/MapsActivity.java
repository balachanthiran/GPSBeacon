package com.example.vdog9.gpsbeacon;

import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.device.BeaconDevice;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.EddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleEddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    LatLng myLocation;

    private ProximityManager proximityManager;
    private Map<String, LatLng> beaconPositions = new HashMap<>();
    private boolean goodBeaconSignal;
    private Marker userMarker;
    private IBeaconDevice bestBeaconDevice;


    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        beaconPositions.put("jjrM",  new LatLng(55.367478, 10.427811));
        beaconPositions.put("Inhb", new LatLng(55.367271, 10.427873));

        KontaktSDK.initialize("agRCMwTLnKgZinRxgtypvHLWPqWZVBcw");
        proximityManager = ProximityManagerFactory.create(this);
        proximityManager.setIBeaconListener(createIBeaconListener());
        startScanning();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {

            @SuppressLint("MissingPermission")
            @Override
            public void onLocationChanged(Location location) {
                //GET MY LAST KNOWN LOCATION
                double latitude = location.getLatitude();
                double longtitude = location.getLongitude();
                myLocation = new LatLng(latitude, longtitude);

                if(userMarker == null) {
                    userMarker = mMap.addMarker(new MarkerOptions().position(myLocation).title("Current Position"));
                }
                if(location.getAccuracy() > 10 && goodBeaconSignal){
                        userMarker.setPosition(beaconPositions.get(bestBeaconDevice.getUniqueId()));
                        userMarker.setSnippet(bestBeaconDevice.getUniqueId() + "" + bestBeaconDevice.getRssi());
                        userMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                } else {
                        userMarker.setPosition(myLocation);
                        userMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        if(userMarker.getSnippet() != null){
                            userMarker.setSnippet(null);
                        }
=======
                mMap.addMarker(new MarkerOptions().position(myLocation).title("GPS position"));
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}

            @Override
            public void onProviderEnabled(String s) {}

            @Override
            public void onProviderDisabled(String s) {}
        };

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }

    }

    private void startScanning() {
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.startScanning();
            }
        });

    }

    private IBeaconListener createIBeaconListener() {
        return new SimpleIBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {
            }

            @Override
            public void onIBeaconsUpdated(List<IBeaconDevice> ibeacons, IBeaconRegion region) {
                super.onIBeaconsUpdated(ibeacons, region);
                for(IBeaconDevice bd : ibeacons){
                    if(bestBeaconDevice == null){
                        bestBeaconDevice = bd;
                    }
                    else if(bestBeaconDevice.getRssi() > bd.getRssi()){
                        bestBeaconDevice = bd;
                    }

                }
                goodBeaconSignal = false;

                if(bestBeaconDevice.getRssi() < -70) { //IF USER POSITION IS FOUND WITHIN BEACON SIGNAL
                    goodBeaconSignal = true;
                }
            }

            @Override
            public void onIBeaconLost(IBeaconDevice ibeacon, IBeaconRegion region) {
                super.onIBeaconLost(ibeacon, region);
                goodBeaconSignal = false;
            }
        };
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setMyLocationEnabled(true);
    }
}
