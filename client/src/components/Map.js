// src/components/Map.js

import React, { useEffect } from 'react';
import 'leaflet/dist/leaflet.css';
import 'mapbox-gl/dist/mapbox-gl.css';
import { initMap, refreshData, displayFacilities } from '../mapFunctions';

function Map() {
  useEffect(() => {
    initMap();
    displayFacilities();
    refreshData();
    setInterval(refreshData, 5000);
  }, []);

  return (
    <div>
      <div id="mapid" style={{height: "500px"}}></div>
      <div id="layer-toggle" style={{position: "absolute", top: "10px", right: "10px", zIndex: "1000", padding: "10px", background: "white"}}>
        <div>
          <input type="checkbox" id="fire-toggle" defaultChecked />
          <label htmlFor="fire-toggle">Show Fires</label>
        </div>
        <div>
          <input type="checkbox" id="facility-toggle" defaultChecked />
          <label htmlFor="facility-toggle">Show Facilities</label>
        </div>
        <div>
          <input type="checkbox" id="vehicle-toggle" defaultChecked />
          <label htmlFor="vehicle-toggle">Show Vehicles</label>
        </div>
      </div>

      <div id="fire-info"></div>
    </div>
  );
}

export default Map;