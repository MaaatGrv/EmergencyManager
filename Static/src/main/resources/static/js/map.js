var mymap = L.map('mapid').setView([45.75647670543468, 4.866953950030793], 13);

L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
}).addTo(mymap);

async function displayFires() {
    const response = await fetch('http://vps.cpe-sn.fr:8081/fires');
    const fires = await response.json();
    if (fires.length === 0) {
        console.log("No fires to display");
        return;
    }
    for (const fire of fires) {
        let fireIcon = L.icon({
            iconUrl: 'static/img/fire.png',
            iconSize: [38, 95],
            iconAnchor: [22, 94],
            popupAnchor: [-3, -76]
        });
        let marker = L.marker([fire.lat, fire.lon], {icon: fireIcon}).addTo(mymap);
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
    }
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
    }
}

async function displayVehicles() {
    const response = await fetch('http://vps.cpe-sn.fr:8081/vehicles');
    const vehicles = await response.json();
    for (const vehicle of vehicles) {
        let vehicleIcon = L.divIcon({
            className: 'vehicle-icon-'+ (vehicle.facilityRefID).toString(), 
            html: `
                <i class="fa-solid fa-location-pin"></i>
                <i class="fa-solid fa-truck-moving fa"></i>
                `,
            iconSize: [25, 25],
        });
        let marker = L.marker([vehicle.lat, vehicle.lon], {icon: vehicleIcon}).addTo(mymap);
        let vehicleInfoHtml = `
            <div id="vehicle-${vehicle.id}" class="vehicle-info">
                <p>Type: ${vehicle.type}</p>
                <p>Fuel: ${vehicle.fuel}</p>
                <p>Crew Member: ${vehicle.crewMember}</p>
                <p>Liquid Quantity: ${vehicle.liquidQuantity}</p>
                <p>Longitude: ${vehicle.lon}</p>
                <p>Latitude: ${vehicle.lat}</p>
            </div>
        `;
        marker.bindPopup(vehicleInfoHtml);
        marker.on('click', function (e) {
            marker.getPopup().openPopup();
        });
    }
}

$(document).ready(function () {
    displayFires().then(() => console.log("fires displayed"));
    displayFacilities().then(() => console.log("facilities displayed"));
    displayVehicles().then(() => console.log("vehicles displayed"));
});
