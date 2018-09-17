var exec = require('cordova/exec');

var Geolocation = {
  observeLocationStarted: false,
  fetchLocationListeners: [],
  lastFetchedLocation: null,

  getCurrentPosition: function(successCallback, failCallback) {
    if (!Geolocation.observeLocationStarted) {
      // observe location not started yet
      Geolocation.observeLocationStarted = true;

      var initialCoordinateSent = false;

      var win = function(res) {
        Geolocation.lastFetchedLocation = res;

        // if this is first coordinate fetched,
        if (!initialCoordinateSent) {
          initialCoordinateSent = true;

          successCallback(res);

          Geolocation.fetchLocationListeners.forEach(function(successAndFailListener) {
            successAndFailListener[0](res);
          });

          Geolocation.fetchLocationListeners = [];
        }
      };

      var fail = function(res) {
        // if observing location fails
        Geolocation.observeLocationStarted = false;

        failCallback(res);

        Geolocation.fetchLocationListeners.forEach(function(successAndFailListener) {
          successAndFailListener[1](res);
        });

        Geolocation.fetchLocationListeners = [];
      };

      exec(win, fail, "Geolocation", "observeLocation");
    } else {
      // observe location already started
      if (Geolocation.lastFetchedLocation !== null) {
        successCallback(Geolocation.lastFetchedLocation);
      } else {
        Geolocation.fetchLocationListeners.push([successCallback, failCallback]);
      }
    }
  }
};

module.exports = Geolocation;