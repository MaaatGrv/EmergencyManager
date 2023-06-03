var mymap = L.map('mapid').setView([45.75647670543468, 4.866953950030793], 13);
var fireMarkers = L.layerGroup().addTo(mymap);
var facilityMarkers = L.layerGroup().addTo(mymap);
var vehicleMarkers = L.layerGroup().addTo(mymap);

L.tileLayer('https://api.mapbox.com/styles/v1/{id}/tiles/{z}/{x}/{y}?access_token={accessToken}', {
    attribution: 'Map data &copy; <a href="https://www.mapbox.com/">Mapbox</a>',
    maxZoom: 18,
    id: 'mapbox/light-v11', // Remplacez par le style Mapbox de votre choix
    tileSize: 512,
    zoomOffset: -1,
    accessToken: 'pk.eyJ1IjoibWFhYXRncnYiLCJhIjoiY2xpMXQwZzFhMDUwcDNzcWpuaG92ZGRtayJ9.blQzt4ZlhFsr4HLThIj6ow'
}).addTo(mymap);

var currentFireMarkers = new Map();
var currentVehicleMarkers = new Map();

async function displayFires() {
    const response = await fetch('http://vps.cpe-sn.fr:8081/fires');
    const fires = await response.json();

    const newFireMarkers = new Map();
    for (const fire of fires) {
        // if the fire marker is already present, use it, else create a new marker
        let marker = currentFireMarkers.get(fire.id);
        if (marker) {
            marker.setLatLng(new L.LatLng(fire.lat, fire.lon));
        } else {
            let fireIcon = L.divIcon({
                className: 'fire-icon-' + (fire.id).toString(), 
                html: `<span class="fa-stack fa-lg">
                            <i class="fa-solid fa-fire fa-2xl" style="color: #ff4000;"></i>
                    </span>`,
                iconSize: [25, 25],
            });
            marker = L.marker([fire.lat, fire.lon], {icon: fireIcon}).addTo(mymap);
            let fireInfoHtml = `
                <div id="fire-${fire.id}" class="fire-info">
                    <p>Type: ${fire.type}</p>
                    <p>Intensity: ${fire.intensity}</p>
                    <p>Range: ${fire.range}</p>
                    <p>Longitude: ${fire.lon}</p>
                    <p>Latitude: ${fire.lat}</p>
                </div>
            `;
            marker.bindPopup(fireInfoHtml);
            marker.on('click', function (e) {
                marker.getPopup().openPopup();
            });
            newFireMarkers.set(fire.id, marker);
        }
    }
    currentFireMarkers = newFireMarkers;
}

async function displayFacilities() {
    const response = await fetch('http://vps.cpe-sn.fr:8081/facility');
    const facilities = await response.json();
    for (const facility of facilities) {
        let facilityIcon = L.divIcon({
            className: 'facility-icon-' + (facility.id).toString(), 
            html: `<span class="fa-stack fa-lg">
                      <i class="fas fa-circle fa-stack-2x"></i>
                      <i class="fas fa-industry fa-stack-1x fa-inverse"></i>
                   </span>`,
            iconSize: [25, 25],
        });
        let marker = L.marker([facility.lat, facility.lon], {icon: facilityIcon}).addTo(mymap);
        let facilityInfoHtml = `
            <div id="facility-${facility.id}" class="facility-info">
                <p>Name: ${facility.name}</p>
                <p>Max Vehicle Space: ${facility.maxVehicleSpace}</p>
                <p>People Capacity: ${facility.peopleCapacity}</p>
                <p>Longitude: ${facility.lon}</p>
                <p>Latitude: ${facility.lat}</p>
            </div>
        `;
        
        marker.bindPopup(facilityInfoHtml);
        marker.on('click', function (e) {
            marker.getPopup().openPopup();
        });
        fireMarkers.addLayer(marker); // add marker to layer group
    }
}

async function displayVehicles() {
    const response = await fetch('http://vps.cpe-sn.fr:8081/vehicles');
    const vehicles = await response.json();

    const newVehicleMarkers = new Map();
    for (const vehicle of vehicles) {
        // if the vehicle marker is already present, use it, else create a new marker
        let marker = currentVehicleMarkers.get(vehicle.id);
        if (marker) {
            marker.setLatLng(new L.LatLng(vehicle.lat, vehicle.lon));
        } else {
            let vehicleIcon = L.divIcon({
                className: 'vehicle-icon-'+ (vehicle.facilityRefID).toString(), 
                html: `
                    <i class="fa-solid fa-location-pin"></i>
                    <i class="fa-solid fa-truck-moving fa"></i>
                `,
                iconSize: [25, 25],
            });
            marker = L.marker([vehicle.lat, vehicle.lon], {icon: vehicleIcon}).addTo(mymap);
            let vehicleInfoHtml = `
                <div id="vehicle-${vehicle.id}" class="vehicle-info">
                    <p>Id: ${vehicle.id}</p>
                    <p>Type: ${vehicle.type}</p>
                    <p>Fuel: ${vehicle.fuel}</p>
                    <p>Crew Member: ${vehicle.crewMember}</p>
                    <p>Liquid Quantity: ${vehicle.liquidQuantity}</p>
                    <p>Liquid Type: ${vehicle.liquidType}</p>
                </div>
            `;
            marker.bindPopup(vehicleInfoHtml);
            marker.on('click', function (e) {
                marker.getPopup().openPopup();
            });
        }
        newVehicleMarkers.set(vehicle.id, marker);
    }
    // remove markers for vehicles that are no longer present
    for (const [id, marker] of currentVehicleMarkers) {
        if (!newVehicleMarkers.has(id)) {
            mymap.removeLayer(marker);
        }
    }
    currentVehicleMarkers = newVehicleMarkers;
}

function refreshData() {
    if ($("#fire-toggle").prop('checked')) {
        displayFires();
    }
    if ($("#vehicle-toggle").prop('checked')) {
        displayVehicles();
    }
}

$(document).ready(function () {
    displayFacilities();
    refreshData();
    setInterval(refreshData, 5000);

    // Add event listeners for checkboxes
    $("#fire-toggle").change(function() {
        if(this.checked) {
            fireMarkers.addTo(mymap);
        } else {
            mymap.removeLayer(fireMarkers);
        }
    });

    $("#facility-toggle").change(function() {
        if(this.checked) {
            facilityMarkers.addTo(mymap);
        } else {
            mymap.removeLayer(facilityMarkers);
        }
    });

    $("#vehicle-toggle").change(function() {
        if(this.checked) {
            vehicleMarkers.addTo(mymap);
        } else {
            mymap.removeLayer(vehicleMarkers);
        }
    });
});