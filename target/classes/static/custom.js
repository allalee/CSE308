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



state_fps_hashmap = 
{
    'ALASKA' : 2,	
    'MISSISSIPPI' : 28,
    'ALABAMA': 1,		
    'MONTANA': 30,
    'ARKANSAS': 5,
    'NORTH CAROLINA': 37,
    'AMERICAN SAMOA': 60,		
    'NORTH DAKOTA' : 38,
    'ARIZONA NE': 4,		
    'NEBRASKA': 31, 
    'CALIFORNIA':6,			
	'NEW HAMPSHIRE': 33,
	'COLORADO':	8,	'NEW JERSEY': 34,
	'CONNECTICUT': 9,	'NEW MEXICO': 35,
	'DISTRICT OF COLUMBIA':	11,	'NEVADA': 32,
	'DELAWARE':	10,	'NEW YORK': 36,
	'FLORIDA':	12,	'OHIO': 39,
	'GEORGIA':	13,	'OKLAHOMA': 40,
	'GUAM':	66,	'OREGON': 41,
	'HAWAII':15, 'PENNSYLVANIA':42,
	'IOWA':	19,	'PUERTO RICO': 72,
	'IDAHO':16,	'RHODE ISLAND': 44,
	'ILLINOIS':17, 'SOUTH CAROLINA': 45,
	'INDIANA':18, 'SOUTH DAKOTA': 46,
	'KANSAS':20, 'TENNESSEE': 47,
	'KENTUCKY':21,	'TEXAS': 48,
	'LOUISIANA':22,	'UTAH': 49,
	'MASSACHUSETTS':25,	'VIRGINIA': 51,
	'MARYLAND':24, 'VIRGIN ISLANDS': 78,
	'MAINE':23,	'VERMONT': 50,
	'MICHIGAN':	26,	'WASHINGTON': 53,
	'MINNESOTA':27,	'WISCONSIN': 55,
	'MISSOURI':	29,	'WEST VIRGINIA': 54,
 	'WYOMING': 56	
};

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


function onClickZoom(e){
	//ON CLICK, LOAD THE DISTRICTS AS THE CURRENT LAYER.
	//REMOVE THE STATES LAYER AND LOAD THE CORRECT DISTRICT LAYER BASED ON THE currentState.
	mymap.fitBounds(e.target.getBounds());
	stateGeoJson.remove();
	if(!mymap.hasLayer(distGeoJson)){
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
		if(currentState == ""){
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

