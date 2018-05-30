var exec = require('cordova/exec');

var Geolocation = {
  getCurrentPosition: function(options, win, fail) {
    var enableHighAccuracy = options.enableHighAccuracy !== undefined ? options.enableHighAccuracy : true;

    exec(win, fail, "Geolocation", "getLocation", [enableHighAccuracy]);
  }
};

module.exports = Geolocation;