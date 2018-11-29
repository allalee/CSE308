var sideNavState = 0;
function toggleNav(){
  if (sideNavState===0) { //Open nav
    document.getElementById("sidenav").style.width = "300px";
    document.getElementById("main").style.marginLeft = "300px";
    //document.getElementById("controls").style.visibility = "visible";
    sideNavState = 1;
  } else { //Close nav
    document.getElementById("sidenav").style.width = "0";
    document.getElementById("main").style.marginLeft= "0";
    //document.getElementById("controls").style.visibility = "hidden";
    //document.getElementById("home").setAttribute("class", "collapse");
    //document.getElementById("events").setAttribute("class", "collapse");
    sideNavState = 0;
  }
}
var slider = document.getElementById("customRange3");
function getValueEx() {
  var val = document.getElementById("customRange3").value;
  console.log(val);
}

/*
var stateGeoJson;
var distGeoJson;
var precinctGeoJson;
var stateSelected = false;

var mymap = L.map('mapid').setView([37.0902, -95.7129], 4);
var info = L.control();
var currentState = "";

info.onAdd = function(mymap){
	this._div = L.DomUtil.create('div', 'info'); // create a div with a class "info"
    this.update();
    return this._div;
};

info.update = function(properties){
	if(!(properties == null)){
		currentState = properties["STATE"];
	}
};

info.addTo(mymap);

function mouseIn(e){
	var currentLayer = e.target;

	currentLayer.setStyle({
		weight: 5,
		color: '#39ff11'
	});

	currentLayer.bringToFront();

	//IF IT IS A STATE JSON DATA LAYER CURRENTLY LOADED, WE UPDATE THE CURRENT STATE THAT IS SELECTED
	//TO LOAD THE APPROPRIATE DISTRICT IN SAID STATE.

	//DO NOT DO THIS WHEN THE CURRENT LAYER IS DISTRICTS.
	if(mymap.hasLayer(stateGeoJson)){
		info.update(currentLayer.feature.properties);
	}
}

function mouseOut(e){
	if(mymap.hasLayer(stateGeoJson)){
		stateGeoJson.resetStyle(e.target);
	}
	if(mymap.hasLayer(distGeoJson)){
		distGeoJson.resetStyle(e.target);
	}
	if(mymap.hasLayer(precinctGeoJson)){
		precinctGeoJson.resetStyle(e.target);
	}

	info.update();
}

function resetMap(){
	mymap.setView([37.0902, -95.7129], 4);
	distGeoJson.remove();
	stateGeoJson.addTo(mymap);
	stateSelected = false;
}

function onEachFeature(feature, currentLayer){
		currentLayer.on({
			mouseover: mouseIn,
			mouseout: mouseOut,
			click: onClickZoom
		});
}


function onClickZoom(e){ //State to district
	//ON CLICK, LOAD THE DISTRICTS AS THE CURRENT LAYER.
	//REMOVE THE STATES LAYER AND LOAD THE CORRECT DISTRICT LAYER BASED ON THE currentState.
	mymap.fitBounds(e.target.getBounds());
	stateGeoJson.remove();
	if(!mymap.hasLayer(distGeoJson)){ //Check if a dist is loaded
		if(currentState == "24"){
			distGeoJson = L.geoJson(marylandDist, {style: styleDist, onEachFeature: onEachFeature});
			distGeoJson.addTo(mymap);
			stateSelected = true;
		}
		if(currentState == "09"){
			distGeoJson = L.geoJson(connDist, {style: styleDist, onEachFeature: onEachFeature});
			distGeoJson.addTo(mymap);
			stateSelected = true;
		}
		if(currentState == "20"){
			distGeoJson = L.geoJson(kansasDist, {style: styleDist, onEachFeature: onEachFeature});
			distGeoJson.addTo(mymap);
			stateSelected = true;
			loadState("Kansas", currentState);
		}
		if(currentState == ""){ //Prevent clicking on NULL
			distGeoJson.addTo(mymap);
		}
	}
}

function loadState(state, currentState){
    var request = new XMLHttpRequest();
    var url = "http://localhost:8080/getState?stateName=" + state + "&stateID=" + currentState
    request.open("GET", url, true)
    request.send(null);
    request.onreadystatechange = function(e){
        console.log(request.response)
    }
}


var mapAccessToken = 'pk.eyJ1Ijoib3ZlcnRoZWNsb3VkcyIsImEiOiJjam1hdWwxc2I1aGhrM3FwNGZ1cXd1c2c5In0.ixJrpwux_Hmz8kuRU-da-w';
L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1Ijoib3ZlcnRoZWNsb3VkcyIsImEiOiJjam1hdWwxc2I1aGhrM3FwNGZ1cXd1c2c5In0.ixJrpwux_Hmz8kuRU-da-w', {
    attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
    maxZoom: 18,
    id: 'mapbox.streets'
}).addTo(mymap);

var style = {
    "color": "#ff9635",
    "weight": 5,
    "opacity": 0.8
};

var styleDist = {
	"color": "#8f3ef2",
    "weight": 2,
    "opacity": 0.8
}

var stylePrecincts = {
	"color": "#0256ff",
	"weight": 2,
	"opacity": 0.8
}

stateGeoJson = L.geoJson(statesData, {style: style, onEachFeature: onEachFeature}).addTo(mymap);
distGeoJson = L.geoJson(congressionalDist, {style: styleDist, onEachFeature: onEachFeature});

mymap.on("zoomend", function(){
	if(stateSelected){
		if(mymap.getZoom() > 9){
			if(!mymap.hasLayer(precinctGeoJson)){
				distGeoJson.remove();
				if(currentState == "24"){
					precinctGeoJson = L.geoJson(marylandPrec, {style: stylePrecincts, onEachFeature: onEachFeature});
				}
				if(currentState == "09"){
					precinctGeoJson = L.geoJson(connPrec, {style: stylePrecincts, onEachFeature: onEachFeature});
				}
				if(currentState == "20"){
					precinctGeoJson = L.geoJson(kansasPrec, {style: stylePrecincts, onEachFeature: onEachFeature});
				    //precinctGeoJson.eachLayer(function(layer) {
                    //  layer.on('click', function(){
                    //        color_neighbors(layer.feature.properties['ID'])
                    //  });
                    //});
				}

				precinctGeoJson.addTo(mymap);
			}
		}
		if(mymap.getZoom() <= 9){
			if(mymap.hasLayer(precinctGeoJson)){
				precinctGeoJson.remove();
				distGeoJson.addTo(mymap);
			}
		}
	}
});

// change precinct color
function color_district( precinct_id, color ){
    precinctGeoJson.setStyle(function(feature){
        if ( feature.properties["ID"] == precinct_id ){
            return {
                fillColor: color
            };
        }
    });
}

// remove all below when DB is done
function color_neighbors( precinct_id ){
    $.ajax({
        type : "GET",
        url: "/getNeighbor?id="+precinct_id,
        contentType : "application/json",
        success: function(res){
            console.log(res);
            $.parseJSON(res).forEach(function(e){
                color_district(e, "red")
            });
        },
        failure: function(e){console.log("get neighbor failed");}
    })
}

function serverLoadKansas(){
    console.log("please wait while we set up Kansas")
    $.ajax({
        type : "GET",
        url: "/loadKansas",
        contentType : "application/json",
        success: function(e){console.log(e)},
        failure: function(e){console.log("kansas failed");}
    })
}
serverLoadKansas();

$('#start').on("click", function(e){
    $.ajax({
        type : "GET",
        url: "/startAlgorithm?state_name=Kansas_2",
        contentType : "application/json",
        success: function(e){console.log(e)},
        failure: function(e){console.log("Algo failed to start");}
    })
});
//ADDING POPULATION AND VOTING DATA
var info_box = L.control();
info_box.onAdd = function (mymap) {
    this._div = L.DomUtil.create('div', 'info_box'); // create a div with a class "info"
    this.update();
    return this._div;
};
// method that we will use to update the control based on feature properties passed
info_box.update = function (props) {
    this._div.innerHTML = '<h4>Precinct Information</h4>' +  (props ?
        '<b>' + props['AREA'] + '</b><br />' + props['AREA'] + ' people / mi<sup>2</sup>'
        : 'Hover over a precinct');
};

info_box.addTo(mymap);

function highlightFeature(e) {
    info.update(layer.feature.properties);
}

function resetHighlight(e) {
    info.update();
}
*/
