var exec = require('cordova/exec');

var geolocation = {
  getCurrentCoordinate: function(win, fail) {
    exec(win, fail, "Geolocation", "getLocation", []);
  }
};

module.exports = geolocation;