const layerToggle = document.getElementById('layer-toggle');
const toggleButton = document.getElementById('toggle-button');

toggleButton.addEventListener('click', function() {
    layerToggle.classList.toggle('collapsed');
});