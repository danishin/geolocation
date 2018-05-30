var exec = require('cordova/exec');

var Geolocation = {
  // NOTE: we didn't change objc implementation so we just tweaked js file here instead by providing patches.
  getCurrentPosition: function(options, successCallback, failCallback) {
    var enableHighAccuracy = options.enableHighAccuracy !== undefined ? options.enableHighAccuracy : false; // default to low accuracy if not given
    var maximumAge = 0;

    var win = function(res) {
      var geoposition = {
        latitude: res.latitude,
        longitude: res.longitude,
        accuracy: res.accuracy,
        isMocked: false // location cannot be faked in ios
      };

      successCallback(geoposition);
    };

    var fail = function(errorCode) {
      var error = (function() {
        switch (errorCode) {
          case 1: return "permission_denied";
          case 2: return "position_unavailable";
          case 3: return "timeout"; // this is actually never returned from ios..
        }
      })();

      failCallback(error);
    };

    exec(win, fail, "Geolocation", "getLocation", [enableHighAccuracy, maximumAge]);
  }
};

module.exports = Geolocation;