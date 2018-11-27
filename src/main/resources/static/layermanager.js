var layer_manager = (function(){
    var ATTR_PROPERTY_NAME = "properties"
    var ATTR_DISTRICT_ID_NAME = "DISTRICTID"
    var ATTR_PRECINCT_ID_NAME = "PRECINCTID"

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
        var precinct_layer = manager.precinct_map[id]
        precinct_layer.feature[ATTR_PROPERTY_NAME][ATTR_DISTRICT_ID_NAME] = district_id
        manager.color_precinct(precinct_id, manager.district_layer_color_map[district_id])
    }

    manager.get_precinct_id = function(layer){
        return layer.feature[ATTR_PROPERTY_NAME][ATTR_PRECINCT_ID_NAME]
    }

    manager.get_district_id_by_precinct_layer = function(layer){
        return layer.feature[ATTR_PROPERTY_NAME][ATTR_DISTRICT_ID_NAME]
    }

    return manager
})();


//// remember to give it a Leafet's LayerGroup object for it to work
//var layer_manager = (function(){
//    var ATTR_PROPERTY_NAME = "properties"
//    var ATTR_DISTRICT_ID_NAME = "GEOID"
//    var ATTR_PRECINCT_ID_NAME = "ID"
//
//    m = {}
//    m.district_layer_map = {}
//    m.precinct_layer_map = {}
//    m.precinct_district_map = {}
//
//    m.manage_district = function(layer_group){
//        layers = layer_group._layers
//        for(var key in layers){
//            var id = layers[key].feature[ATTR_PROPERTY_NAME][ATTR_DISTRICT_ID_NAME]
//            m.district_layer_map[id] = layers[key]
//        }
//    }
//ayer_color_map
//    m.manage_precinct = function(layer_group){
//        layers = layer_group._layers
//        for(var key in layers){
//            var id = layers[key].feature[ATTR_PROPERTY_NAME][ATTR_PRECINCT_ID_NAME]
//            m.precinct_layer_map[id] = layers[key]
//        }
//        // get the precinct's owner district property. set it in a map
//    }
//
//    m.color_district = function(district_id, color){
//        m.district_layer_map[district_id].setStyle({fillColor:color})
//    }
//
//    m.color_precinct = function(precinct_id, color){
//        m.precinct_layer_map[precinct_id].setStyle({fillColor:color})
//    }
//
//    m.clear = function(){ m.district_layer_map = {}; m.precinct_layer_map = {} }
//
//    m.map_precinct_color = function(district_id){
//
//    }
//
//    return m
//
//})();
//
//var dynamic_color_changer = (function(){
//    cc = {};
//    cc.color_map = {};
//    cc.used_colors = [];
//    cc.color_list = ["red", "black", "yellow", "pink", "purple", "blue", "green", "aqua", "silver"];
//    cc.color_pointer = 0;
//    cc.color = function(move_object){
//        var to_color = undefined
//        if ( cc.color_map[move_object.dest] ){
//            to_color = cc.color_map[move_object.dest]
//        }
//        else{
//            to_color = cc.next_color();
//            cc.color_map[move_object.dest] = to_color;
//        }
//        layer_manager.color_precinct(move_object.precinct, to_color);
//        return to_color;
//    }
//    cc.next_color = function(){
//        if ( cc.color_pointer > cc.color_list.length - 1 )
//            cc.color_pointer = 0;
//        return cc.color_list[cc.color_pointer++];
//        /*
//        var new_color = 'rgb('+Math.floor(Math.random()*255)+','+Math.floor(Math.random()*255)+','+Math.floor(Math.random()*255)+')'
//        while( cc.used_colors.indexOf(new_color) != -1 ){
//            new_color = 'rgb('+Math.floor(Math.random()*255)+','+Math.floor(Math.random()*255)+','+Math.floor(Math.random()*255)+')'
//        }
//        cc.used_colors.push(new_color);
//        console.log("New Color Created")
//        return new_color;
//        */
//    }
//    cc.reset = function(){ cc.color_map = {} }
//    return cc
//})();