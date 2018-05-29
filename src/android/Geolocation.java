package com.danishin.geolocation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Geolocation extends CordovaPlugin {
  private static int LOCATION_INTERVAL = 1000;
  private static int LOCATION_FASTEST_INTERVAL = 100;
  private static int LOCATION_PRIORITY = LocationRequest.PRIORITY_HIGH_ACCURACY;
  
  /* Success Response */
  private static JSONObject getCoordinateJson(Location location, CordovaInterface cordova) throws JSONException {
    JSONObject json = new JSONObject();
    
    json.put("latitude", location.getLatitude());
    json.put("longitude", location.getLongitude());
    json.put("accuracy", location.getAccuracy());
    
    boolean isMocked = Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
      ? location.isFromMockProvider()
      : !Settings.Secure.getString(cordova.getActivity().getApplicationContext().getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
    json.put("isMocked", isMocked);
    
    return json;
  }
  
  /* Error Responses */
  private static final String GPS_OFF = "gps_off";
  private static final String NO_PERMISSION = "no_permission";
  
  private GoogleApiClient googleApiClient;
  
  private void debug(String msg) { Log.d("Geolocation", msg); }
  
  @Override
  protected void pluginInitialize() {
    super.pluginInitialize();
    
    this.googleApiClient = new GoogleApiClient.Builder(cordova.getActivity())
      .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
          debug("onConnectionFailed: " + connectionResult);
        }
      })
      .addApi(LocationServices.API)
      .build();
    
    googleApiClient.connect();
    
    debug("Initialized");
  }
  
  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
    if (action.equals("getLocation")) {
      debug("getLocation");
      
      googleApiClient.registerConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
          debug("onConnected: " + bundle);
          
          getLocation(callbackContext);
        }
        
        @Override
        public void onConnectionSuspended(int i) {
          debug("onConnectionSuspended: " + i);
        }
      });
      
      return true;
    }
    
    return false;
  }
  
  private void getLocation(final CallbackContext cb) {
    boolean gpsIsOn = ((LocationManager) cordova.getActivity().getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
    boolean hasPermission = ActivityCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(cordova.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    
    if (!gpsIsOn) {
      cb.error(GPS_OFF);
    } else if (!hasPermission) {
      cb.error(NO_PERMISSION);
    }  else {
      LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
          debug("onLocationChanged: " + location);
          
          try {
            JSONObject json = getCoordinateJson(location, cordova);
            
            cb.success(json);
            
          } catch (JSONException e) {
            e.printStackTrace();
            debug("Unknown Error: " + e);
            cb.error("Unknown Error: " + e);
          }
        }
      };
      
      LocationRequest locationRequest = LocationRequest.create();
      
      locationRequest.setInterval(LOCATION_INTERVAL);
      locationRequest.setFastestInterval(LOCATION_FASTEST_INTERVAL);
      locationRequest.setPriority(LOCATION_PRIORITY);
      locationRequest.setNumUpdates(1);
      
      LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, locationListener);
    }
  }
}