
var mymap = L.map('mapid').setView([37.0902, -95.7129], 4);
var mapAccessToken = 'pk.eyJ1Ijoib3ZlcnRoZWNsb3VkcyIsImEiOiJjam1hdWwxc2I1aGhrM3FwNGZ1cXd1c2c5In0.ixJrpwux_Hmz8kuRU-da-w';
L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1Ijoib3ZlcnRoZWNsb3VkcyIsImEiOiJjam1hdWwxc2I1aGhrM3FwNGZ1cXd1c2c5In0.ixJrpwux_Hmz8kuRU-da-w', {
    attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
    maxZoom: 18,
    id: 'mapbox.streets'
}).addTo(mymap);

var stateJson; //State handler added to map
var districtJson; //District handler added to the map
var precinctJson;

var currentStateID; //Keeping track of which state the user clicks on

var statesData;
var districtData=kansasDist;
var precinctData=kansasPrec;

function highlightFeature(e) {
    var layer = e.target;

    layer.setStyle({
        weight: 3,
        color: '#666',
        dashArray: '',
        fillOpacity: 0.7
    });

    if (!L.Browser.ie && !L.Browser.opera && !L.Browser.edge) {
        layer.bringToFront();
    }
}

function highlightPrecinctFeature(e) {
    var layer = e.target;

    layer.setStyle({
        weight: 5,
        color: '#666',
        dashArray: '',
        fillOpacity: 0.7
    });

    info.update(layer.feature.properties);

    if (!L.Browser.ie && !L.Browser.opera && !L.Browser.edge) {
        layer.bringToFront();
    }
}
function resetHighlight(e) {
    stateJson.resetStyle(e.target);
}
function resetDistrictHighlight(e) {
    districtJson.resetStyle(e.target);
}
function resetPrecinctHighlight(e) {
    precinctJson.resetStyle(e.target);
    info.update();
}

// ... our listeners
//stateJson = L.geoJson(statesData);
function zoomToFeature(e) {
    mymap.fitBounds(e.target.getBounds());
}

function setCurrentState(target) {
    currentStateID = target.feature.properties['STATE'];
}

function loadDistricts(e) {
    mymap.fitBounds(e.target.getBounds());
    setCurrentState(e.target);
    //Retrieve districts data from server and set
    stateJson.remove();
    addDistrictsLayer();

}
function addDistrictsLayer() {
  districtJson = L.geoJson(districtData, {
      onEachFeature: onEachDistrictFeature
  }).addTo(mymap);
}

function loadPrecincts(e) {
    mymap.fitBounds(e.target.getBounds());
    districtJson.remove();
    //Checks if the zoom in already loaded the precinct data
    if (!mymap.hasLayer(precinctJson)) {
      addPrecintsLayer();
    }
}

function addPrecintsLayer() {
  precinctJson = L.geoJson(precinctData, {
      onEachFeature: onEachPrecinctFeature
  }).addTo(mymap);
}

function addStateLayer () {
  stateJson = L.geoJson(statesData, {
      onEachFeature: onEachStateFeature
  }).addTo(mymap);
}

function onEachStateFeature(feature, layer) {
    layer.on({
        mouseover: highlightFeature,
        mouseout: resetHighlight,
        click: loadDistricts
    });
}
function onEachDistrictFeature(feature, layer) {
    layer.on({
        mouseover: highlightFeature,
        mouseout: resetDistrictHighlight,
        click: loadPrecincts
    });
}
function onEachPrecinctFeature(feature, layer) {
    layer.on({
        mouseover: highlightPrecinctFeature,
        mouseout: resetPrecinctHighlight,
        click: zoomToFeature
    });
}
function resetMap(){
	if(mymap.hasLayer(districtJson)) {
    districtJson.remove();
  } else if(mymap.hasLayer(precinctJson)) {
    precinctJson.remove();
  }
	currentStateID = null;
  addStateLayer();
  mymap.setView([37.0902, -95.7129], 4);
}


addStateLayer();


mymap.on("zoomend", function() {
    if(currentStateID) {
      if(mymap.getZoom() > 9 && mymap.hasLayer(districtJson)) {
        districtJson.remove();
        addPrecintsLayer();
      }
    }
})
//ADDING POPULATION AND VOTING DATA
var info = L.control();
info.onAdd = function (mymap) {
    this._div = L.DomUtil.create('div', 'info'); // create a div with a class "info"
    this.update();
    return this._div;
};
// method that we will use to update the control based on feature properties passed
info.update = function (props) {
    this._div.innerHTML = '<h4>Precinct Information</h4>' +  (props ?
        '<b>Area:' + props['AREA'] + '</b>'
        : 'Hover over a precinct');
};

info.addTo(mymap);
