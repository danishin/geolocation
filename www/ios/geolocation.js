var exec = require('cordova/exec');

var Geolocation = {
  getCurrentPosition: function(options, win, fail) {
    var enableHighAccuracy = options.enableHighAccuracy !== undefined ? options.enableHighAccuracy : true;
    var maximumAge = 0; // NOTE: we didn't change objc implementation so we just tweaked js file here instead by providing default argument.

    exec(win, fail, "Geolocation", "getLocation", [enableHighAccuracy, maximumAge]);
  }
};

module.exports = Geolocation;