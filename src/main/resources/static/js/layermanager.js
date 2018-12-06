var layer_manager = (function(){
    var ATTR_PROPERTY_NAME = "properties"
    var ATTR_DISTRICT_ID_NAME = "DISTRICTID"
    var ATTR_PRECINCT_ID_NAME = "PRECINCTID"
    var DEFAULT_REGION_COLOR = "white"

    manager = {}
    manager.district_map = {}
    manager.precinct_map = {}
    manager.district_layer_color_map = {}

    //Builds a hashmap of district_ids and the layer associated with the id
    manager.build_district_maps = function(layer_group){
        var district_layers = layer_group._layers
        var temp_district_map = {}
        var temp_color_map = {}
        var rgb_values = ["red", "orange", "blue", "yellow", "green", "purple", "aqua", "pink", "silver"] //HARD CODED AS STRINGS FOR THE TIME BEING
        var default_region_color = "white"
        var rgb_index = 0
        for(var key in district_layers){
            var district_id = district_layers[key].feature[ATTR_PROPERTY_NAME][ATTR_DISTRICT_ID_NAME]
            temp_district_map[district_id] = district_layers[key]
            temp_color_map[district_id] = rgb_values[rgb_index]
            rgb_index++;
        }
        manager.district_map = temp_district_map
        manager.district_layer_color_map = temp_color_map
    }

    manager.build_precincts_map = function(layer_group){
        var precinct_layers = layer_group._layers
        var temp_precinct_map = {}
        for(var key in precinct_layers){
            var precinct_id = precinct_layers[key].feature[ATTR_PROPERTY_NAME][ATTR_PRECINCT_ID_NAME]
            temp_precinct_map[precinct_id] = precinct_layers[key]
        }
        manager.precinct_map = temp_precinct_map
    }

    manager.color_districts = function(){
        for(var id in manager.district_map){
            var color = manager.district_layer_color_map[id]
            manager.district_map[id].setStyle({fillColor: color})
        }
    }

    manager.color_precincts = function(){
        for(var id in manager.precinct_map){
            var layer = manager.precinct_map[id]
            var district_id = layer.feature[ATTR_PROPERTY_NAME][ATTR_DISTRICT_ID_NAME]
            var color = manager.district_layer_color_map[district_id]
            manager.precinct_map[id].setStyle({fillColor: color})
        }
    }

    manager.color_unassigned_precincts = function(){
        for(var id in manager.precinct_map){
            var layer = manager.precinct_map[id]
            layer.feature[ATTR_PROPERTY_NAME][ATTR_DISTRICT_ID_NAME] = 0
            manager.precinct_map[id].setStyle({fillColor : DEFAULT_REGION_COLOR})
        }
    }

    manager.color_default_regions = function(seeds){
        for(var key in seeds){
            var layer = manager.precinct_map[key]
            var district_id = seeds[key]
            var color = manager.district_layer_color_map[district_id]
            layer.feature[ATTR_PROPERTY_NAME][ATTR_DISTRICT_ID_NAME] = district_id
            manager.precinct_map[key].setStyle({fillColor: color})
        }
    }

    manager.reset_district_color = function(layer){
        var district_id = layer.feature[ATTR_PROPERTY_NAME][ATTR_DISTRICT_ID_NAME]
        var color = manager.district_layer_color_map[district_id]
        manager.district_map[district_id].setStyle({fillColor: color, fillOpacity: 0.4, color: "grey"})
    }

    manager.reset_precinct_color = function(layer){
        var district_id = layer.feature[ATTR_PROPERTY_NAME][ATTR_DISTRICT_ID_NAME]
        var precinct_id = layer.feature[ATTR_PROPERTY_NAME][ATTR_PRECINCT_ID_NAME]
        var color = manager.district_layer_color_map[district_id]
        manager.precinct_map[precinct_id].setStyle({fillColor: color, fillOpacity: 0.4, color: "grey"})
    }

    manager.color_precinct = function(id, color){
        manager.precinct_map[id].setStyle({fillColor: color})
    }

    manager.move_precinct = function(precinct_id, district_id){
        var precinct_layer = manager.precinct_map[precinct_id]
        precinct_layer.feature[ATTR_PROPERTY_NAME][ATTR_DISTRICT_ID_NAME] = district_id
        manager.color_precinct(precinct_id, manager.district_layer_color_map[district_id])
    }

    manager.get_precinct_id = function(layer){
        return layer.feature[ATTR_PROPERTY_NAME][ATTR_PRECINCT_ID_NAME]
    }

    manager.get_district_id = function(layer){
        return layer.feature[ATTR_PROPERTY_NAME][ATTR_DISTRICT_ID_NAME]
    }

    manager.get_district_id_by_precinct_layer = function(layer){
        return layer.feature[ATTR_PROPERTY_NAME][ATTR_DISTRICT_ID_NAME]
    }

    manager.set_new_precinct_district = function(precinct, district){
        layer = manager.precinct_map[precinct]
        layer.feature[ATTR_PROPERTY_NAME][ATTR_DISTRICT_ID_NAME] = district
    }

    return manager
})();