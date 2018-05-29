package com.danishin.geolocation;

import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.json.JSONArray;

public class Geolocation extends CordovaPlugin /*implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener*/ {
  private void debug(String msg) { Log.d("Geolocation", msg); }
  
  @Override
  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);
    
    this.cordova = cordova;
    
    debug("Initialized");
  }
  
  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
    if (action.equals("getLocation")) {
      this.getLocation(callbackContext);
      return true;
    }
    
    return false;
  }
  
  private void getLocation(CallbackContext callbackContext) {
  
  }
}