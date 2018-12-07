
var mymap = L.map('mapid').setView([37.0902, -95.7129], 4);
var mapAccessToken = 'pk.eyJ1Ijoib3ZlcnRoZWNsb3VkcyIsImEiOiJjam1hdWwxc2I1aGhrM3FwNGZ1cXd1c2c5In0.ixJrpwux_Hmz8kuRU-da-w';
L.tileLayer('https://api.tiles.mapbox.com/v4/{id}/{z}/{x}/{y}.png?access_token=pk.eyJ1Ijoib3ZlcnRoZWNsb3VkcyIsImEiOiJjam1hdWwxc2I1aGhrM3FwNGZ1cXd1c2c5In0.ixJrpwux_Hmz8kuRU-da-w', {
    attribution: 'Map data &copy; <a href="https://www.openstreetmap.org/">OpenStreetMap</a> contributors, <a href="https://creativecommons.org/licenses/by-sa/2.0/">CC-BY-SA</a>, Imagery © <a href="https://www.mapbox.com/">Mapbox</a>',
    maxZoom: 18,
    id: 'mapbox.light'
}).addTo(mymap);

var stateJson; //State handler added to map
var districtJson; //District handler added to the map
var precinctJson;
var originalPrecinctJson = null;
var loadedMapJson = null;


var currentStateID = null; //Keeping track of which state the user clicks on
var currentStateName = null;

var statesData;
var districtData;
var precinctData = null;
var originalPrecinctData; //Retreived from server to display original when selected
var loadedMapData;

var currentConstText;

var currentLayer = 0; //State: 0 ; District: 1 ; Precinct: 2

var connector = makeConnector();
connector.onMessage(consoleLog)
connector.connect();
con.start_reading();
function consoleLog(message_body){
    var console = document.getElementById("console")
    if(message_body["console_log"]){
        console.appendChild(document.createElement("br"))
        console.append(message_body["console_log"])
        console.scrollTop = console.scrollHeight
    }
    if(message_body["dest"] && message_body["precinct"]){
            layer_manager.set_new_precinct_district(message_body["precinct"], message_body["dest"])
            layer_manager.color_changed_precinct(message_body["precinct"], message_body["dest"])
    }
    if(message_body["enable_reset"]){
        connector.clear_message()
        document.getElementById("reset").disabled = false;
        updateButtons(ButtonState.STOPPED)
        enableManualMoveOption(true)
    }
    if(message_body["default"]){
        layer_manager.color_unassigned_precincts()
    }
    if(message_body["seeds"]){
        layer_manager.color_default_regions(message_body["seeds"])
    }
}

function consoleWrite(text){
    var console = document.getElementById("console")
    console.appendChild(document.createElement("br"))
    console.append(text)
    console.scrollTop = console.scrollHeight
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

function stateSearch() {
  if(currentLayer!=0) { //If not state layer, return
    return;
  }
  /*
  if(mymap.hasLayer(districtJson) || mymap.hasLayer(precinctJson)) {
    return;
  }*/
  stateName = document.getElementById('statefield').value;
  stateNameUpper = stateName.toUpperCase();
  if(id = state_fps_hashmap[stateNameUpper]) {
    currentStateID= id;
    currentStateName = stateNameUpper.toLowerCase();
    currentStateName = currentStateName.charAt(0).toUpperCase() + currentStateName.slice(1);
    targetState = findState(currentStateID);
    mymap.fitBounds(targetState.getBounds());
    stateJson.remove();
    loadStateJson(currentStateName, currentStateID);

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
  update_district_list()
  currentLayer = 1;
}

function loadPrecincts(e) {
    mymap.fitBounds(e.target.getBounds());
    districtJson.remove();
    //Checks if the zoom in already loaded the precinct data
    if (!mymap.hasLayer(precinctJson)) {
      addPrecinctsLayer();
    }

    // enable manual redistrict
    enableManualMoveOption(true)
    updateButtons(ButtonState.RUNNABLE)
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
  currentLayer = 2;
}

function addStateLayer () {
  stateJson = L.geoJson(statesData, {
      onEachFeature: onEachStateFeature
  }).addTo(mymap);
  currentLayer = 0;
}

function addOriginalPrecinctsLayer() {
  originalPrecinctJson = L.geoJson(originalPrecinctData, {
      style: function() {
        return {
          fillOpacity: 0.4,
          color: "grey"
        }
      },
      onEachFeature : onEachPrecinctFeature
  }).addTo(mymap);
  layer_manager.build_precincts_map(originalPrecinctJson)
  layer_manager.color_precincts()
  currentLayer = 2;
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
        mouseover: precinctOverEvent,
        mouseout: precinctOutEvent,
        click: precinctClickEvent
    });
}

function resetMap(){
    // stop, clear, start reading again
    connector.stop_reading()
    connector.clear_message()
    connector.start_reading()

    // update ui
    updateButtons(ButtonState.RUNNABLE)
    enableManualMoveOption(false)
    reset_district_exclusion()

	if(mymap.hasLayer(districtJson)) {
    districtJson.remove();
  } else if(mymap.hasLayer(precinctJson)) {
    precinctJson.remove();
  } else if(mymap.hasLayer(originalPrecinctJson)) {
    originalPrecinctJson.remove();
  } else if(mymap.hasLayer(loadedMapJson)) {
    loadedMapJson.remove();
  }
    currentStateID = null;
    currentConstText = null;
    originalPrecinctData = null;
    originalPrecinctJson = null;
  if(mymap.hasLayer(stateJson)) {
    return;
  } else {
    addStateLayer();
    currentLayer = 0;
    mymap.setView([37.0902, -95.7129], 4);
  }
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
info.update = function (props, asian, caucasian, hispanic, african, native, other, demo, repub) {
    this._div.innerHTML = '<h4>Precinct Information</h4>' +  (props ?
        '<b>Demographics </b><br>'
        +'Asian/Pacific Islander: ' + asian + '<br>'
        + 'Caucasian: ' + caucasian + '<br>'
        + 'Hispanic (of Any Race): ' + hispanic + '<br>'
        + 'African-American: ' + african + '<br>'
        + 'Native American: ' + native + '<br>'
        + 'Other: ' + other + '<br>'
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
        if(request.readyState == 4 && request.status == 200){
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
function displayOriginalMap() {
    if(currentLayer!=2 || mymap.hasLayer(originalPrecinctJson)) {
      return;
    }
    /*
    if(mymap.hasLayer(stateJson) || mymap.hasLayer(districtJson) || mymap.hasLayer(originalPrecinctJson)) {
      return;
    }
    */
    if(originalPrecinctJson) {
      precinctJson.remove();
      addOriginalPrecinctsLayer();
      return;
    }
    var request = new XMLHttpRequest();
    var url = "http://localhost:8080/getOriginal"
    request.open("GET", url, true)
    request.onreadystatechange = function() {
      if (request.readyState == 4 && request.status == 200) {
        var loadedJson = request.response
        var obj = JSON.parse(loadedJson);
        originalPrecinctData = obj;
        precinctJson.remove();
        addOriginalPrecinctsLayer();
      }
    }
    request.send(null)
}
function displayGeneratedMap() {
  if(currentLayer!=2 || mymap.hasLayer(precinctJson)) {
    return;
  }
  /*
  if(mymap.hasLayer(stateJson) || mymap.hasLayer(districtJson) || mymap.hasLayer(precinctJson)) {
    return;
  }*/
  originalPrecinctJson.remove();
  addPrecinctsLayer();
}

function loadPrecinctProperties(layer){
      var district_id = layer.feature["properties"]["DISTRICTID"]
      var precinct_id = layer.feature["properties"]["PRECINCTID"]
      var url = "http://localhost:8080/loadPrecinctData?districtID=" + district_id + "&precinctID=" + precinct_id
      var request = new XMLHttpRequest()
      request.open("GET", url, true)
      request.onreadystatechange = function(){
        if(request.readyState == 4 && request.status == 200){
            var loadedJson = request.response
            var obj = JSON.parse(loadedJson)
            if(obj['voting_data']){
                var democratic = obj['voting_data']['DEMOCRATIC']
                var republican = obj['voting_data']['REPUBLICAN']
            } else {
                var democratic = "N/A"
                var republican = "N/A"
            }
            if(obj['demographics']){
                var asian = obj['demographics']['ASIAN']
                var caucasian = obj['demographics']['CAUCASIAN']
                var hispanic = obj['demographics']['HISPANIC']
                var african = obj['demographics']['AFRICAN_AMERICAN']
                var native = obj['demographics']['NATIVE_AMERICAN']
                var other = obj['demographics']['OTHER']
            }
            else{
                var asian = "N/A"
                var caucasian = "N/A"
                var hispanic = "N/A"
                var african = "N/A"
                var native = "N/A"
                var other = "N/A"
            }
            info.update(obj, asian, caucasian, hispanic, african, native, other, democratic, republican)
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
       if(request.readyState == 4 && request.status == 200){
            currentConstText = request.response;
       }
   }
   request.send(null);
}



function savePreferences(){
    var prefDiv = document.getElementById("prefDiv");
    var name = document.getElementById("prefName").value
    var populationEquality = document.getElementById("population_equality").value
    var partisanFairness = document.getElementById("partisan_fairness").value
    var compactness = document.getElementById("compactness").value
    var url = "http://localhost:8080/savePreferences?prefName=" + name + "&popEqual=" + populationEquality + "&partFairness=" + partisanFairness + "&compactness=" + compactness
    var request = new XMLHttpRequest()
    request.open("GET", url, true)
    request.onreadystatechange = function () {
        if(request.readyState == 4 && request.status == 200){
            consoleWrite("Preferences Saved"); //Update interface
            var newcontent = document.createElement('a');
            newcontent.innerHTML = name;
            newcontent.setAttribute("class", "dropdown-item");
            newcontent.setAttribute("onclick", "select_preference(this);");
            prefDiv.appendChild(newcontent);
        }
    }
    document.getElementById("prefName").value = "";
    request.send(null)
}

function select_preference(e){
    selected = e.innerText;
    target = document.getElementById("dropdownPreferences");
    target.innerText = selected;
}

function loadPreferences(){
    var name = document.getElementById("dropdownPreferences").innerText;
    var url = "http://localhost:8080/loadPreferences?name=" + name
    var request = new XMLHttpRequest()
    request.open("GET", url, true)
    request.onreadystatechange = function(){
        if(request.readyState == 4 && request.status == 200){
            var populationEquality = document.getElementById("population_equality")
            var partisanFairness = document.getElementById("partisan_fairness")
            var compactness = document.getElementById("compactness")
            var pop_output = document.getElementById("pop_value");
            var part_output = document.getElementById("part_value");
            var comp_output = document.getElementById("comp_value");
            var loadedJson = request.response
            var pref = JSON.parse(loadedJson)
            var popEqualityVal = pref.popequality
            var partisanVal = pref.partisan
            var compactnessVal = pref.compactness
            populationEquality.value = popEqualityVal
            partisanFairness.value = partisanVal
            compactness.value = compactnessVal
            pop_output.innerHTML = popEqualityVal
            part_output.innerHTML = partisanVal
            comp_output.innerHTML = compactnessVal
        }
    }
    request.send(null)
}

document.getElementById("start").onclick = startAlgorithm
document.getElementById("pause").onclick = togglePauseAlgorithm
document.getElementById("stop").onclick = stopAlgorithm

ButtonState = {
    RUNNABLE : 0,
    RUNNING : 1,
    PAUSED : 2,
    STOPPED : 3
}

updateButtons(ButtonState.STOPPED)
function turn(btn, on){
    btn.hidden = !on
}
function updateButtons(state){
    var start = document.getElementById("start")
    var pause = document.getElementById("pause")
    var stop = document.getElementById("stop")

    switch(state){
        case ButtonState.RUNNABLE:
            turn(start, true)
            turn(pause, false)
            turn(stop, false)
            paused = false
            break
        case ButtonState.RUNNING:
            turn(start, false)
            turn(pause, true)
            turn(stop, true)
            pause.innerHTML = "pause"
            paused = false
            break
        case ButtonState.PAUSED:
            turn(start, false)
            turn(pause, true)
            turn(stop, true)
            pause.innerHTML = "play_arrow"
            paused = true
            break
        case ButtonState.STOPPED:
            turn(start, false)
            turn(pause, false)
            turn(stop, false)
            paused = false
            break
        default: console.log("Invalid Button State: " + state)
    }
}

function startAlgorithm(){
    var algorithm_type = $('input[name="algorithm"]:checked').val()
    if(currentStateID == null){
        document.getElementById("console").appendChild(document.createElement("br"))
        document.getElementById("console").append("No state selected for algorithm to run")
    } else {
        enableManualMoveOption(false); //MAKE SURE TO ENABLE THIS button
        document.getElementById("reset").disabled = true;
        var console = document.getElementById("console")
        console.appendChild(document.createElement("br"))
        console.append("Retrieving slider data for the server...")
        var populationEquality = document.getElementById("population_equality").value
        var partisanFairness = document.getElementById("partisan_fairness").value
        var compactness = document.getElementById("compactness").value
        console.appendChild(document.createElement("br"))
        console.append("Forwarding slider data to the server...")
        var url = "http://localhost:8080/startAlgorithm?algorithmType=" + algorithm_type + "&popEqual=" + populationEquality + "&partFairness=" + partisanFairness + "&compactness=" + compactness
        var request = new XMLHttpRequest()
        request.open("GET", url, true)

        request.send(null)
        updateButtons(ButtonState.RUNNING)
    }
}

paused = false;
function togglePauseAlgorithm(){

    if(!paused){    // if not paused, pause it
        connector.stop_reading()    // stop updating
        var url = 'http://localhost:8080/pauseAlgorithm'    // send to pause
        updateButtons(ButtonState.PAUSED)
    }
    else{           // if paused, start it
        connector.start_reading()    // start updating
        var url = 'http://localhost:8080/unpauseAlgorithm'    // send to unpause
        updateButtons(ButtonState.RUNNING)
    }

    var request = new XMLHttpRequest()
    request.open("GET", url, true)
    request.send(null)
}

function stopAlgorithm(){
    // terminate updating, clear messages
    connector.stop_reading()
    connector.clear_message()
    connector.start_reading()

    // send
    var url = 'http://localhost:8080/stopAlgorithm'
    var request = new XMLHttpRequest()
    request.open("GET", url, true)
    request.send(null)

    // update the ui on client side
    document.getElementById("reset").disabled = false;
    updateButtons(ButtonState.STOPPED)
    consoleWrite("Algorithm stopped by client")
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
    if(currentLayer!=0) {
      return;
    }
    /*
    if(mymap.hasLayer(districtJson) || mymap.hasLayer(precinctJson)) {
        return;
    }*/

    var selected_radio = document.getElementById('state_selector_options').querySelector('input[type=radio]:checked')
    var name = selected_radio.value
    var id = state_fps_hashmap[name];
    currentStateID = id;
    currentStateName = name.toLowerCase();
    currentStateName = currentStateName.charAt(0).toUpperCase() + currentStateName.slice(1);
    targetState = findState(id);
    mymap.fitBounds(targetState.getBounds());
    //Retrieve districts data from server and set
    stateJson.remove();
    loadStateJson(currentStateName, id);


}
populateStateSelect();




/*
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
    selectorDiv.innerHTML = ""
}*/

var fadeWriter = makeFadeOutWriter()
var manualMoveWriter = { write: function(PLACEHOLDER, message){ consoleWrite(message) } }
var district_selector_div = document.getElementById('district_selector_options')
var mover = makeManualMover(layer_manager, district_selector_div)
var manualMoveToggle = document.getElementById("district_selector_toggle")
var tempMoveBtn = document.getElementById("move_temporary")
var lockMoveBtn = document.getElementById("move_lock")

tempMoveBtn.onclick = function(e){
    var destID = document.getElementById('district_selector_options').querySelector('input[type=radio]:checked').value
    var messageDiv = document.getElementById('district_selector_message')
    mover.sendManualMove(false, destID, messageDiv, manualMoveWriter)
}
lockMoveBtn.onclick = function(e){
    var destID = document.getElementById('district_selector_options').querySelector('input[type=radio]:checked').value
    var messageDiv = document.getElementById('district_selector_message')
    mover.sendManualMove(true, destID, messageDiv, manualMoveWriter)
}
manualMoveToggle.onclick = function(e){
    if(mode == MODE.NORMAL){    // switch to manual if normal
        tempMoveBtn.disabled = false
        lockMoveBtn.disabled = false
        mode = MODE.MANUAL_SELECT
    }
    else{
        tempMoveBtn.disabled = true
        lockMoveBtn.disabled = true
        mover.exit()
        mode = MODE.NORMAL
    }
}

var MODE = {
    NORMAL: 0,
    MANUAL_SELECT: 1
}
var mode = MODE.NORMAL
function precinctOverEvent(e){
     switch(mode){
         case MODE.NORMAL:
             highlightPrecinctFeature(e)
         break;
         case MODE.MANUAL_SELECT:
             mover.mouseoverFunction(e)
         break;
         default: console.log("invalid mode: "+ mode)
     }
 }
function precinctOutEvent(e){
     switch(mode){
         case MODE.NORMAL:
             resetPrecinctHighlight(e)
         break;
         case MODE.MANUAL_SELECT:
             mover.mouseoutFunction(e)
         break;
         default: console.log("invalid mode: "+ mode)
     }
 }
function precinctClickEvent(e){
    switch(mode){
        case MODE.NORMAL:
            zoomToFeature(e)
        break;
        case MODE.MANUAL_SELECT:
             mover.clickFunction(e)
        break;
        default: console.log("invalid mode: "+ mode)
    }
}


// 1. when precinct is loaded
function enableManualMoveOption(enable){
    manualMoveToggle.disabled = !enable

    if(manualMoveToggle.disabled){
        tempMoveBtn.disabled = true
        lockMoveBtn.disabled = true
        mover.exit()
        mode = MODE.NORMAL
    }
}
enableManualMoveOption(false)

function save_map() {
  console.log("Save Map");
  if (!mymap.hasLayer(precinctJson)) { //Enforces the user to save only algorithm generated map
    return;
  }
  var mapDiv = document.getElementById("mapMenu");
  mapInput = document.getElementById("mapfield");
  mapValue = mapInput.value;
  mapData = JSON.stringify(precinctData);
  var request = new XMLHttpRequest();
  var url = "http://localhost:8080/saveMap?name=" + mapValue
  request.open("GET", url, true);
  request.onreadystatechange = function() {
    if (request.readyState == 4 && request.status == 200) {
      consoleWrite("Map Saved"); //Update interface
      var newmap = document.createElement('a');
      newmap.innerHTML = mapValue;
      newmap.setAttribute("class", "dropdown-item");
      newmap.setAttribute("onclick", "select_map(this);");
      mapDiv.appendChild(newmap);
    }
  }
  request.send(null);
  mapInput.value = "";

}

function addLoadedPrecinctsLayer() {
  loadedMapJson = L.geoJson(loadedMapData, {
      style: function() {
        return {
          fillOpacity: 0.4,
          color: "grey"
        }
      },
      onEachFeature : onEachPrecinctFeature
  }).addTo(mymap);
  layer_manager.build_precincts_map(loadedMapJson)
  layer_manager.color_precincts()
  currentLayer = 2;
}

function load_map() {
  console.log("Load Map");
  if(currentLayer!=2) { //Has to be on any precinct Layer to load map
    return;
  }
  /*
  if (!mymap.hasLayer(precinctJson) || !mymap.hasLayer(originalPrecinctJson)) { //Remember to check or originalPrecicntJson too
    return;
  }*/
  mapObj = document.getElementById("dropdownMapButton");
  mapName= mapObj.innerText;
  var request = new XMLHttpRequest();
  var url = "http://localhost:8080/loadMap?name=" + mapName
  request.open("GET", url, true);
  request.onreadystatechange = function() {
    if (request.readyState ==4 && request.status == 200) {
      consoleWrite("Map Loaded");
      var loadedJson = request.response
      var obj = JSON.parse(loadedJson);
      loadedMapData = obj;
      if(mymap.hasLayer(precinctJson)) {
        precinctJson.remove();
      }
      if(mymap.hasLayer(originalPrecinctJson)) {
        originalPrecinctJson.remove();
      }
      if(mymap.hasLayer(loadedMapJson)) {
        loadedMapJson.remove();
      }
      addLoadedPrecinctsLayer();
    }
  }
  request.send(null);
}

function delete_map() {
  console.log("Delete Map");
  if(currentLayer!=2) { //User has to be on any precinct layers to delete map
    return;
  }
  /*
  if (!mymap.hasLayer(precinctJson)) {
    return;
  }*/
  mapObj = document.getElementById("dropdownMapButton");
  mapName= mapObj.innerText;
  var request = new XMLHttpRequest();
  var url = "http://localhost:8080/deleteMap?name=" + mapName
  request.open("GET", url, true);
  request.onreadystatechange = function() {
    if (request.readyState ==4 && request.status == 200) {
      consoleWrite("Map Deleted");
      children_list = document.getElementById("mapMenu").children;
      for (let i = 0; i < children_list.length; i++) {
        if(children_list[i].innerText==mapName) {
          children_list[i].remove();
        }
      }
    }
  }
  request.send(null);
  mapObj.innerText = "Select"
}

function update_district_list() {
  var district_list = layer_manager.district_map;
  var color_mapping = layer_manager.district_layer_color_map;
  var myMenu = document.getElementById("exclusionMenu");
  for (id in district_list) {
    color = color_mapping[id];
    myDiv = create_district_div();
    myInput = create_district_input(id);
    myLabel = create_district_label(id, color);
    myDiv.appendChild(myInput);
    myDiv.appendChild(myLabel);
    myMenu.appendChild(myDiv);
  }
}

function create_district_div() {
  var newDiv = document.createElement("div");
  newDiv.setAttribute("class", "form-check");
  return newDiv;
}

function create_district_input(district_id) {
  var newInput = document.createElement("input");
  newInput.setAttribute("class", "form-check-input");
  newInput.setAttribute("type", "checkbox");
  newInput.setAttribute("value", district_id);
  newInput.setAttribute("id", "district"+district_id);
  return newInput;
}

function create_district_label(district_id, color) {
  var newLabel = document.createElement("label");
  newLabel.setAttribute("class", "form-check-label");
  newLabel.setAttribute("for", "district"+district_id);
  newLabel.setAttribute("style", "background-color:"+color);
  newLabel.innerHTML = "&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp"
  return newLabel;
}

function reset_district_exclusion() {
  var myNode = document.getElementById("exclusionMenu");
  while (myNode.firstChild) {
      myNode.removeChild(myNode.firstChild);
  }
}

function loadStateSavedMaps(currentStateID){
    var url = "http://localhost:8080/loadSavedMaps?currentStateID=" + currentStateID;
    var request = new XMLHttpRequest();


}
