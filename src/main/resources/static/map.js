
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

var currentStateID = null; //Keeping track of which state the user clicks on
var currentStateName = null;

var statesData;
var districtData;
var precinctData;

var currentConstText;

var connector = makeConnector();
connector.onMessage(consoleLog)
connector.connect();
con.start_reading();

function consoleLog(message_body){
    var console = document.getElementById("console")
    console.appendChild(document.createElement("br"))
    console.append(message_body["console_log"])
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
//Only allow to search on state layer
function stateSearch() {
  if(mymap.hasLayer(districtJson) || mymap.hasLayer(precinctJson)) {
    return;
  }
  stateName = document.getElementById('statefield').value;
  stateNameUpper = stateName.toUpperCase();
  if(id = state_fps_hashmap[stateNameUpper]) {
    currentStateID= id;
    currentStateName = stateNameUpper.value.toLowerCase();
    currentStateName = currentStateName.charAt(0).toUpperCase(); //Make first letter uppercase
    targetState = findState(currentStateID);
    mymap.fitBounds(targetState.getBounds());
    //Retrieve districts data from server and set
    loadStateJson(currentStateName, currentStateID);
    stateJson.remove();
    addDistrictsLayer();
  }
}

function findState(stateId) {
  for(var i in stateJson._layers) {
    current = stateJson._layers[i]
    if(current.feature.properties['STATE']==stateId) {
      return current;
    }
  }
}
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

    loadPrecinctProperties(layer)
}
function resetHighlight(e) {
    stateJson.resetStyle(e.target);
}
function resetDistrictHighlight(e) {
    manager.reset_district_color(e.target)
}
function resetPrecinctHighlight(e) {
    manager.reset_precinct_color(e.target)
    info.update();
}

// ... our listeners
//stateJson = L.geoJson(statesData);
function zoomToFeature(e) {
    mymap.fitBounds(e.target.getBounds());
}

function setCurrentState(target) {
    currentStateID = target.feature.properties['STATE'];
    currentStateName = target.feature.properties['NAME'];
    sendState(currentStateID, currentStateName);
}

function loadDistricts(e) {
    mymap.fitBounds(e.target.getBounds());
    setCurrentState(e.target);
    //Retrieve districts data from server and set
    loadStateJson(currentStateName, currentStateID);
    stateJson.remove();

}
function addDistrictsLayer() {
  districtJson = L.geoJson(districtData, {
      style: function(){
        return {
            fillOpacity: 0.4,
            color: "grey"
        }
      },
      onEachFeature: onEachDistrictFeature
  }).addTo(mymap);
  layer_manager.build_district_maps(districtJson)
  layer_manager.color_districts()
}

function loadPrecincts(e) {
    mymap.fitBounds(e.target.getBounds());
    districtJson.remove();
    //Checks if the zoom in already loaded the precinct data
    if (!mymap.hasLayer(precinctJson)) {
      addPrecinctsLayer();
    }
}

function addPrecinctsLayer() {
  precinctJson = L.geoJson(precinctData, {
        style: function(){
            return {
                fillOpacity: 0.4,
                color: "grey"
            }
        },
      onEachFeature: onEachPrecinctFeature
  }).addTo(mymap);
  layer_manager.build_precincts_map(precinctJson)
  layer_manager.color_precincts()
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
var MODE = {
    NORMAL: 0,
    MANUAL_SELECT: 1
}
var mode = MODE.NORMAL
function precinctClickEvent(e){
    switch(mode){
        case MODE.NORMAL:
            console.log("normal")
        break;
        case MODE.MANUAL_SELECT:
            console.log("manual")
        break;
        default: console.log("invalid mode: "+ mode)
    }
}

function resetMap(){
	if(mymap.hasLayer(districtJson)) {
    districtJson.remove();
  } else if(mymap.hasLayer(precinctJson)) {
    precinctJson.remove();
  }
    currentStateID = null;
    currentConstText = null;
  addStateLayer();
  mymap.setView([37.0902, -95.7129], 4);
}


addStateLayer();


mymap.on("zoomend", function() {
    if(currentStateID) {
      if(mymap.getZoom() > 9 && mymap.hasLayer(districtJson)) {
        districtJson.remove();
        addPrecinctsLayer();
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
info.update = function (props, demo, repub) {
    this._div.innerHTML = '<h4>Precinct Information</h4>' +  (props ?
        '<b>Demographics </b><br>'
        +'Asian/Pacific Islander: ' + props['demographics']['ASIAN'] + '<br>'
        + 'Caucasian: ' + props['demographics']['CAUCASIAN'] + '<br>'
        + 'Hispanic: ' + props['demographics']['HISPANIC'] + '<br>'
        + 'African-American: ' + props['demographics']['AFRICAN_AMERICAN'] + '<br>'
        + 'Native American: ' + props['demographics']['NATIVE_AMERICAN'] + '<br>'
        + 'Other: ' + props['demographics']['OTHER'] + '<br>'
        + '<br><b>Election</b><br>'
        + 'Democrat: ' + demo + '<br>'
        + 'Republican: ' + repub + '<br>'
        + '<br><b>Population</b><br>'
        + props['population']
        : 'Hover over a precinct');
};

//create a button onto leaflet that will show the constitution in alert
var constInfo = L.control({position: 'bottomright'});
constInfo.onAdd = function (mymap) {
    this._button = L.DomUtil.create('button', 'constInfo');
    this._button.style.height = "50px";
    this._button.style.width = "200px";
    this._button.innerHTML = "State Constitution";
    this._button.onclick = function(){
            alert((currentConstText != null ? currentConstText : "Select a State"));
    };
    return this._button;
};



function loadStateJson(state, currentState){
    var request = new XMLHttpRequest();
    var url = "http://localhost:8080/getState?stateName=" + state + "&stateID=" + currentState
    request.open("GET", url, true)
    request.onreadystatechange = function(){
        if(request.status == 200){
            var loadedJson = request.response
            var obj = JSON.parse(loadedJson);
            obj = JSON.parse(obj);
            districtData = obj.district;
            precinctData = obj.precinct;
            addDistrictsLayer();
        }
    }
    request.send(null);
}

function loadPrecinctProperties(layer){
      var district_id = layer.feature["properties"]["DISTRICTID"]
      var precinct_id = layer.feature["properties"]["PRECINCTID"]
      var url = "http://localhost:8080/loadPrecinctData?districtID=" + district_id + "&precinctID=" + precinct_id
      var request = new XMLHttpRequest()
      request.open("GET", url, true)
      request.onreadystatechange = function(){
        if(request.status == 200){
            var loadedJson = request.response
            var obj = JSON.parse(loadedJson)
            if(obj['voting_data']){
                var democratic = obj['voting_data']['DEMOCRATIC']
                var republican = obj['voting_data']['REPUBLICAN']
            } else {
                var democratic = "N/A"
                var republican = "N/A"
            }
            info.update(obj, democratic, republican)
            if (!L.Browser.ie && !L.Browser.opera && !L.Browser.edge) {
               layer.bringToFront();
            }
        }
      }
      request.send(null)
}

function sendState(currentStateID, currentStateName){
    var url = "http://localhost:8080/stateConst?stateID=" + currentStateID + "&stateName=" + currentStateName;
    var request = new XMLHttpRequest();
    request.open("GET", url, true);
    request.onreadystatechange = function(){
       if(request.status == 200){
            currentConstText = request.response;
       }
   }
   request.send(null);
}

document.getElementById("start").onclick = startAlgorithm

function startAlgorithm(){
    var algorithm_type = $('input[name="algorithm"]:checked').val()
    if(currentStateID == null){
        document.getElementById("console").appendChild(document.createElement("br"))
        document.getElementById("console").append("No state selected for algorithm to run")
    } else {
        var console = document.getElementById("console")
        console.appendChild(document.createElement("br"))
        console.append("Retrieving slider data for the server...")
        var populationEquality = document.getElementById("population_equality").value
        var partisanFairness = document.getElementById("partisan_fairness").value
        var compactness = document.getElementById("compactness").value
        console.appendChild(document.createElement("br"))
        console.append("Forwarding slider data to the server...")
        var url = "http://localhost:8080/startAlgorithm?popEqual=" + populationEquality + "&partFairness=" + partisanFairness + "&compactness=" + compactness
        var request = new XMLHttpRequest()
        request.open("GET", url, true)
        request.send(null)
    }
}

info.addTo(mymap);
constInfo.addTo(mymap);

var stateOptionTemplate = "<div class=\"custom-control custom-radio\"> <input type=\"radio\" id=\"[id]\" name=\"state_option\" class=\"custom-control-input\" value=\"[name]\"> <label class=\"custom-control-label\" for=\"[id]\">[name]</label> </div>"
function createStateOption(id, name){
    var div = stateOptionTemplate.split('[id]').join(id)
    div = div.split('[name]').join(name)
    return div
}

function populateStateSelect(){
    selectorDiv = document.getElementById('state_selector_options')
    for(var i in state_fps_hashmap){
        optionDiv = createStateOption(state_fps_hashmap[i], i)
        selectorDiv.innerHTML += optionDiv
    }
}

function toggleStateSearch(){
    var dropdown = document.getElementById("dropdownStateSearch")
    var text = document.getElementById("textStateSearch")

    dropdown.disabled = !dropdown.disabled
    text.disabled = !text.disabled
}

function dropdownStateSearch(){
    if(mymap.hasLayer(districtJson) || mymap.hasLayer(precinctJson)) {
        return;
    }

    var selected_radio = document.getElementById('state_selector_options').querySelector('input[type=radio]:checked')
    var id = selected_radio.id
    var name = selected_radio.value

    currentStateName = name.toLowerCase();
    currentStateName = currentStateName.charAt(0).toUpperCase(); //Make first letter uppercase
    targetState = findState(id);
    mymap.fitBounds(targetState.getBounds());
    //Retrieve districts data from server and set
    loadStateJson(currentStateName, id);
    stateJson.remove();
    addDistrictsLayer();

}
populateStateSelect();

// when manual mode toggled
//  enable pane, init empty, move/lock disabled
//  precinctlayer's onClick = populateDistrcitSelect, <
//                              move/lock enable,
//                              reset mm.selected_district, <
//                              mm.selected_precinct = e
//                              mm.selected_precinct
//  precinctlayer's oneneter = same
//  precinctlayer's onexit = same + if e == mm.selected_precinct { setStyle("{fillColor: "+mm.selected_color+"}") }


ManualMover = function(layerManager){

    var mm = {}
    mm.selected_precinct;
    mm.selected_color = "black"

    mm.onClick = function(e){
        var layer = e.target;

        // clear and set new selection
        if (mm.selected_precinct){
            layerManager.reset_precinct_color(layer)
        }
        layerManager.color_precinct(mm.selected_color)
        mm.selected_precinct = layer;

        // repopulate the district options
        removeDistrictOption()
        for(var i in layerManager.district_layer_color_map){
            insertDistrictOption(i, layerManager.district_layer_color_map[i])
        }

    }

    mm.exit = function(){
        if (mm.selected_precinct){
            layerManager.reset_precinct_color(layer)
        }
    }

    mm.getSelectedID = function(){
        return layerManager.get_precinct_id(mm.selected_precinct)
    }

    mm.resetSelection = function(){
        mm.selected_precinct = undefined
        layerManager.reset_precinct_color(mm.selected_precinct)
    }

    return mm
}

// move: onclick => ajax('trymove' radio.value+ ,   mm.getSelectedID() ), print out funct value
// lock: onclick => ajax('actually move it' radio.value+ ,   mm.getSelectedID() ) // move on client+server, clear selection
//                  layer_manager.move_precinct(mm.getSelectedID(), radio.value)
//                  mm.resetSelection()



var districtOptionTemplate = "<div class=\"custom-control custom-radio\"> <input type=\"radio\" id=\"district[id]\" name=\"district_option\" class=\"custom-control-input\" value=\"[id]\"> <label id=\"districtlabel[id]\" class=\"custom-control-label\" for=\"district[id]\">&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</label> </div>"
function insertDistrictOption(id, color){
    var div = districtOptionTemplate.split('[id]').join(id)
        var selectorDiv = document.getElementById('district_selector_options')
        selectorDiv.innerHTML += div
        var label = document.getElementById('districtlabel'+id)
        label.setAttribute("style", "background-color:"+color)
    return div
}

function removeDistrictOption(){
    var selectorDiv = document.getElementById('district_selector_options')
    selectorDiv.innerHTML += ""
}

function enableManualSelect(){

}
