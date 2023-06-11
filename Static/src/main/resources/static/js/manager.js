// Lorsque le formulaire est soumis
document.getElementById("vehicle-form").addEventListener("submit", function(event) {
    event.preventDefault(); // Empêche le rechargement de la page

    // Récupérer les valeurs du formulaire
    var crewMember = parseInt(document.getElementById("crew-member").value);
    var facilityRefID = parseInt(document.getElementById("facility-ref-id").value);
    var liquidType = document.getElementById("liquid-type").value;
    var vehicleType = document.getElementById("vehicle-type").value;

    // Créer l'objet vehicleDto
    var vehicleDto = {
        crewMember: crewMember,
        facilityRefID: facilityRefID,
        fuel: 0,
        id: 0,
        lat: 0,
        liquidQuantity: 0,
        liquidType: liquidType,
        lon: 0,
        type: vehicleType
    };

    // Récupérer les coordonnées latitude et longitude de l'installation sélectionnée
    fetch("http://localhost:8083/emergency/facilities/team")
        .then(function(response) {
            return response.json();
        })
        .then(function(data) {
            if (data.length > 0) {
                var facility = data.find(function(item) {
                    return item.id === facilityRefID;
                });

                if (facility) {
                    vehicleDto.lat = facility.lat;
                    vehicleDto.lon = facility.lon;

                    // Envoyer la requête POST pour créer le véhicule
                    createVehicle(vehicleDto);
                } else {
                    console.log("Facility not found.");
                }
            } else {
                console.log("No facilities found.");
            }
        })
        .catch(function(error) {
            console.log("Error:", error);
        });

});

// Fonction pour envoyer la requête POST de création du véhicule
function createVehicle(vehicleDto) {
    var url = "http://localhost:8083/emergency/vehicle"

    console.log("Creating vehicle...");
    console.log(vehicleDto);

    fetch(url, {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(vehicleDto)
    })
    .then(function(response) {
        if (response.ok) {
            console.log("Vehicle created successfully.");
        } else {
            console.log("Error creating vehicle.");
        }
    })
    .catch(function(error) {
        console.log("Error: maybe to much vehicles", error);
    });
}
