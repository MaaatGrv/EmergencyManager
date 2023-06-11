const VehicleType = {
    CAR: {fuelCapacity: 50, liquidCapacity: 10},
    FIRE_ENGINE: {fuelCapacity: 60, liquidCapacity: 50},
    PUMPER_TRUCK: {fuelCapacity: 500, liquidCapacity: 1000},
    WATER_TENDERS: {fuelCapacity: 500, liquidCapacity: 1000},
    TURNTABLE_LADDER_TRUCK: {fuelCapacity: 500, liquidCapacity: 1000},
    TRUCK: {fuelCapacity: 500, liquidCapacity: 2000}
};

async function displayVehicles() {
    const response = await fetch('http://vps.cpe-sn.fr:8081/vehicles');
    const vehicles = await response.json();
    const mainElement = document.querySelector('main');
    mainElement.innerHTML = '';

    let vehiclesInfoHtml = `
        <div class="vehicles-icon">
        </div>
    `;

    let vehiclesInfo = document.createElement('div');
    vehiclesInfo.className = 'vehicles-info glass';
    vehiclesInfo.innerHTML = vehiclesInfoHtml;

    mainElement.appendChild(vehiclesInfo);

    for (const vehicle of vehicles) {
        if (vehicle.facilityRefID === 35 || vehicle.facilityRefID === 3918) {
            const vehicleType = VehicleType[vehicle.type];
            const fuelPercentage = (vehicle.fuel / vehicleType.fuelCapacity) * 100;
            const liquidPercentage = (vehicle.liquidQuantity / vehicleType.liquidCapacity) * 100;

            let vehicleInfoHtml = `
            <div id="vehicle-${vehicle.id}" class="vehicle-info glass">
                <p>Id: ${vehicle.id}</p>
                <p>Type: ${vehicle.type}</p>
                <p>Liquid Type: ${vehicle.liquidType}</p>
                <div class="crew-members">
                    <p>Crew members: </p>
                    ${Array(vehicle.crewMember).fill('<i class="fa-solid fa-person"></i>').join('')}
                </div>
                <div class="progress-bar-container">
                    <div class="progress-label">
                        <p>Fuel: ${fuelPercentage}%</p>
                    </div>
                    <div class="progress-bar-fuel" style="width:${fuelPercentage}%"></div>
                </div>
                <div class="progress-bar-container">
                    <div class="progress-label">
                        <p>Liquid: ${liquidPercentage}%</p>
                    </div>
                    <div class="progress-bar-liquid" style="width:${liquidPercentage}%"></div>
                </div>
                <button class="delete-button" data-vehicle-id="${vehicle.id}"><i class="fa-solid fa-trash fa-xl"></i></button>
            </div>
            `;

            let vehicleInfoElement = document.createElement('div');
            vehicleInfoElement.innerHTML = vehicleInfoHtml;
            vehiclesInfo.appendChild(vehicleInfoElement);
        }
    }
}

$(document).ready(function() {
    displayVehicles();
});

function deleteVehicle(vehicleId) {
    const url = `http://localhost:8083/emergency/vehicle/${vehicleId}`;

    fetch(url, {
        method: 'DELETE'
    })
    .then(function(response) {
        if (response.ok) {
            console.log("Vehicle deleted successfully.");
            // Mettre à jour l'affichage des véhicules après la suppression réussie
            displayVehicles();
        } else {
            console.log("Error deleting vehicle.");
        }
    })
    .catch(function(error) {
        console.log("Error:", error);
    });
}

$(document).on('click', '.delete-button', function(event) {
    const vehicleId = event.currentTarget.dataset.vehicleId;
    console.log(`Delete vehicle ${vehicleId}`);
    deleteVehicle(vehicleId);
});
