makeRegionTracker = function(layerManager, forPrecinct){
    rt = {}
    rt.selection = undefined
    rt.selection_id = undefined
    rt.style = undefined
    rt.color = "black"
    rt.selection_history = {}   //id -> original style
    //rt.selection_save = {}
    rt.forPrecicnt = forPrecicnt

    rt.clickFunction = function(e){
        var layer = e.target;
        if (rt.selection == layer){ //toggle select
            rt.remove(layer)
            return
        }

        rt.prev_style = layer.options.style().
        rt.selection = layer;
        rt.selection_id = rt.get_layer_id()
        layerManager.color_precinct(rt.selection_id, rt.color)

        rt.selection_history[selection_id] = layer
    }

    rt.get_layer_id = function(layer){
        if (!rt.forPrecinct) return layerManager.get_district_id(layer)
        return layerManager.get_precinct_id(layer)
    }

    rt.resetSelection = function(){
        rt.selection.setStyle(rt.prev_style)
    }

    rt.remove = function(layer){
        var layer_id = rt.get_layer_id(layer)

        // revert to original style
        if (forPrecint){

            layerManager.reset_precinct_color( rt.selection_history[layer_id] )
        }
        else{
            layerManager.reset_district_color( rt.selection_history[layer_id] )
        }

        rt.selection_history[layer_id] = undefined
    }
                                        //////
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
}