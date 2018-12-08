makeRegionTracker = function(layerManager, forPrecinct){
    rt = {}
    rt.color = "black"
    rt.selection_history = {}   //id -> original style
    //rt.selection_save = {}
    //layer.options.style() // get style
    rt.forPrecinct = forPrecinct

    rt.clickFunction = function(e){
        var layer = e.target;
        var layer_id = rt.get_layer_id(layer)
        if (rt.selection_history[layer_id]){ //already selected
            rt.remove(layer)
        }
        else{
            rt.add(layer)
        }
    }

    rt.get_layer_id = function(layer){
        if (!rt.forPrecinct) return layerManager.get_district_id(layer)
        return layerManager.get_precinct_id(layer)
    }

    rt.remove = function(layer){
        var layer_id = rt.get_layer_id(layer)
        rt.reset_color(layer)
        delete rt.selection_history[layer_id]
    }

    rt.add = function(layer){
        var layer_id = rt.get_layer_id(layer)
        rt.color_region(layer)
        rt.selection_history[layer_id] = layer
    }


    rt.color_region = function(layer){
        var layer_id = rt.get_layer_id(layer)
        if (rt.forPrecinct){
            layerManager.color_precinct( layer_id, rt.color )
        }
        else{
            layerManager.color_district( layer_id, rt.color )
        }
    }
    rt.reset_color = function(layer){
        if (rt.forPrecinct){
            layerManager.reset_precinct_color( layer )
        }
        else{
            layerManager.reset_district_color( layer )
        }
    }
                                        //////
    rt.mouseoutFunction = function(e){
        var layer = e.target;
        var layer_id = rt.get_layer_id(layer)

        if (rt.selection_history[layer_id]) { // if is selected
            rt.color_region(layer)
        }
        else {
            rt.reset_color(layer)
        }
        info.update();
    }

    rt.mouseoverFunction = function(e){
        var layer = e.target;
        var layer_id = rt.get_layer_id(layer)

        if(rt.selection_history[layer_id]){
            // nothing will change to mouseover selected precinct
        }
        else{
            layer.setStyle({
                weight: 5,
                color: '#666',
                dashArray: '',
                fillOpacity: 0.7
            });
        }
        loadPrecinctProperties(layer)
    }

    rt.open = function(){
        for(var layer_id in rt.selection_history){
            var layer = rt.selection_history[layer_id]
            rt.color_region(layer)
        }
    }

    rt.close = function(){
        for(var layer_id in rt.selection_history){
            var layer = rt.selection_history[layer_id]
            rt.reset_color(layer)
        }
    }

    rt.clear = function(){
        for(var layer_id in rt.selection_history){
            var layer = rt.selection_history[layer_id]
            rt.reset_color(layer)
        }
        rt.selection_history = {}
    }

    return rt
}