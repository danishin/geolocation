var exec = require('cordova/exec');

var Geolocation = {
  getCurrentPosition: function(win, fail) {
    exec(win, fail, "Geolocation", "getLocation", []);
  }
};

module.exports = Geolocation;