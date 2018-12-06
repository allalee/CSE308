
makeManualMover = function(layerManager, selector_option_div){

    var mm = {}
    mm.selected_precinct;
    mm.selector_option_div = selector_option_div
    mm.selected_color = "black"
    mm.districtOptionTemplate = "<div class=\"custom-control custom-radio\"> <input type=\"radio\" id=\"district[id]\" name=\"district_option\" class=\"custom-control-input\" value=\"[id]\"> <label id=\"districtlabel[id]\" class=\"custom-control-label\" for=\"district[id]\">&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp&nbsp</label> </div>"

    mm.clickFunction = function(e){
        var layer = e.target;
        if (mm.selected_precinct == layer)
            return

        // clear and set new selection
        if (mm.selected_precinct){
            mm.resetSelection()
        }
        mm.selected_precinct = layer;
        layerManager.color_precinct(mm.getSelectedID(), mm.selected_color)

        // repopulate the district options
        mm.removeDistrictOption()
        for(var i in layerManager.district_layer_color_map){
            mm.insertDistrictOption(i, layerManager.district_layer_color_map[i])
        }

    }

    mm.mouseoutFunction = function(e){
        var layer = e.target;
        if (mm.selected_precinct == layer)  // reset to custom color for selected
            layerManager.color_precinct(mm.getSelectedID(), mm.selected_color)
        else
            manager.reset_precinct_color(layer)
        info.update();
    }

    mm.mouseoverFunction = function(e){
        var layer = e.target;

        if (mm.selected_precinct != layer){
            layer.setStyle({
                weight: 5,
                color: '#666',
                dashArray: '',
                fillOpacity: 0.7
            });
        }
        loadPrecinctProperties(layer)
    }

    mm.exit = function(){
        if (mm.selected_precinct){
            mm.resetSelection()
        }
        mm.removeDistrictOption()
    }

    mm.getSelectedID = function(){
        return layerManager.get_precinct_id(mm.selected_precinct)
    }

    // reset when a move lock is done
    mm.resetSelection = function(){
        layerManager.reset_precinct_color(mm.selected_precinct)
        mm.selected_precinct = undefined
    }

    mm.sendManualMove = function (isLock, dest_id, message_log_div, writer){

        // check if a precinct is selected
        if(!mm.selected_precinct){
            writer.write(message_log_div, "No precinct selected", "red", 5000)
            return
        }

        // make move
        var url = "manualMove"
        var isLock = isLock;
        var src_id = layerManager.get_district_id_by_precinct_layer(mm.selected_precinct)
        var precinct_id = mm.getSelectedID()

        var request = new XMLHttpRequest();
        var url = "http://localhost:8080/" + url + "?src=" + src_id + "&dest=" + dest_id +"&precinct=" + precinct_id + "&lock=" + isLock
        request.open("GET", url, true)
        request.onreadystatechange = function(){
            if(request.readyState == 4 && request.status == 200){
                 json = JSON.parse(request.response);
                json.forEach(function(e){
                    layerManager.color_precinct(e, "black")
                })
                return
                value = json.value;
                console.log("The move is worth: "+value)
                if(!json.valid){    // invalid move
                    console.log(json.message)
                    writer.write(message_log_div, json.message, "red", 5000)
                }
                else{               // valid
                    console.log(json.value)
                    var message_div = document.getElementById(mm.LOG_DIV_ID)
                    writer.write(message_log_div, json.message, "green", 5000)
                    //if LOCK
                    if(isLock){
                        layerManager.move_precinct(precinct_id, dest_id)
                        mm.resetSelection()
                    }
                }
            }
        }
        request.send(null);
    }

    mm.insertDistrictOption = function (id, color){
        var div = mm.districtOptionTemplate.split('[id]').join(id)
        var selectorDiv = mm.selector_option_div
        selectorDiv.innerHTML += div
        var label = document.getElementById('districtlabel'+id)
        label.setAttribute("style", "background-color:"+color)
        return div
    }
    mm.removeDistrictOption = function(){
        var selectorDiv = mm.selector_option_div
        selectorDiv.innerHTML = ""
    }

    return mm
}


makeFadeOutWriter = function(){
    fow = {}
    fow.timer
    fow.write = function(element, message, color, millisecond){
        if(fow.timer){
            clearTimeout(fow.timer)
        }
        element.innerHTML = message
        element.style.color = color
        fow.timer = setTimeout(function(){
            element.innerHTML = "&nbsp"
            fow.timer = undefined
        }, millisecond)
    }
    return fow
}