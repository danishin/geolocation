declare interface GeoPosition {
  latitude: number;
  longitude: number;
  accuracy: number;
  isMocked: boolean;
}

declare namespace cordova {
  namespace plugins {
    interface geolocation {
      getCurrentPosition(win: (geoposition: GeoPosition) => void, fail: (error: string) => void): void;
    }
  }
}