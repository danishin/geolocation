package com.danishin.geolocation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import com.google.android.gms.location.*;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
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
  private static final String NO_LOCATION = "no_location";
  
  private CordovaInterface cdv;
  
  private LocationCallback locationCallback;
  
  private void debug(String msg) { Log.d("Geolocation", msg); }
  
  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    
    this.cdv = cordova;
    
    debug("Initialized");
  }
  
  @Override
  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) {
    if (action.equals("getLocation")) {
      debug("getLocation");
      
      cdv.getThreadPool().submit(new Runnable() {
        @Override
        public void run() {
          getLocation(callbackContext);
        }
      });
      
      return true;
    }
    
    return false;
  }
  
  private void getLocation(final CallbackContext cb) {
    boolean gpsIsOn = ((LocationManager) cdv.getActivity().getSystemService(Context.LOCATION_SERVICE)).isProviderEnabled(LocationManager.GPS_PROVIDER);
    boolean hasPermission = ActivityCompat.checkSelfPermission(cdv.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(cdv.getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    
    if (!gpsIsOn) {
      cb.error(GPS_OFF);
    } else if (!hasPermission) {
      cb.error(NO_PERMISSION);
    }  else {
      final LocationRequest locationRequest = new LocationRequest();
      
      locationRequest.setInterval(LOCATION_INTERVAL);
      locationRequest.setFastestInterval(LOCATION_FASTEST_INTERVAL);
      locationRequest.setPriority(LOCATION_PRIORITY);
      
      final FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(cdv.getActivity());
      
      locationCallback = new LocationCallback() {
        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
          super.onLocationAvailability(locationAvailability);
          
          debug("onLocationAvailability: " + locationAvailability);
        }
        
        @Override
        public void onLocationResult(LocationResult locationResult) {
          super.onLocationResult(locationResult);
          
          debug("onLocationResult: " + locationResult);
          
          if (locationResult != null && locationResult.getLastLocation() != null) {
            try {
              Location lastLocation = locationResult.getLastLocation();
              
              debug("Got Location: " + lastLocation);
              
              cb.success(getCoordinateJson(lastLocation, cdv));
            } catch (JSONException e) {
              e.printStackTrace();
              
              cb.error("Unknown Error: " + e.getMessage());
            }
          } else {
            debug("LocationResult is Null: " + locationResult);
          }
        }
      };
      
      debug("requestLocationUpdates");
      
      Looper.prepare();
      
      client.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())
        .addOnSuccessListener(new OnSuccessListener<Void>() {
          @Override
          public void onSuccess(Void aVoid) {
            debug("requestLocationUpdates.onSuccess");
            
            // TODO: remove this
//            client.removeLocationUpdates(locationCallback);

//            debug("removeLocationUpdates");
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            e.printStackTrace();
            debug("requestLocationUpdates.onFailure: " + e);
            cb.error("Unknown Error: " + e.getMessage());
          }
        });
    }
  }
}