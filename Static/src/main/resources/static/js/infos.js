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
    const mainElement = document.querySelector('main'); // Selection du main element
    mainElement.innerHTML = ''; // Effacer l'élément main

    let vehiclesInfoHtml = `
        <div class="vehicles-icon">
            <i class="fa-solid fa-truck-moving fa"></i>
        </div>
    `;

    let vehiclesInfo = document.createElement('div');
    vehiclesInfo.className = 'vehicles-info glass';
    vehiclesInfo.innerHTML = vehiclesInfoHtml;
    
    mainElement.appendChild(vehiclesInfo); // Ajoutez tout l'élément à main, pas seulement le premier enfant

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
                </div>
            `;
            
            // Création de l'élément HTML pour la section principale
            let vehicleInfoElement = document.createElement('div');
            vehicleInfoElement.innerHTML = vehicleInfoHtml;
            vehiclesInfo.appendChild(vehicleInfoElement); // Ajoutez tout l'élément à vehicles-info, pas seulement le premier enfant
        }
    }

    $(document).on('click', '.vehicles-info', function() {
        const vehiclesIcon = $('.vehicles-icon', this);
        const vehicleInfos = $('.vehicle-info', this);

        vehicleInfos.slideToggle('slow', function() {
            // Cette fonction est appelée une fois l'animation terminée
            if (vehicleInfos.is(":visible")) {
                vehiclesIcon.hide();
            } else {
                vehiclesIcon.show();
            }
        });
    });
}

$(document).ready(function () {
    displayVehicles();
});
