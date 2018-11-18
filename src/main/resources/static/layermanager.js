
// remember to give it a Leafet's LayerGroup object for it to work
var layer_manager = (function(){
    var ATTR_PROPERTY_NAME = "properties"
    var ATTR_DISTRICT_ID_NAME = "GEOID"
    var ATTR_PRECINCT_ID_NAME = "ID"

    m = {}
    m.district_layer_map = {}
    m.precinct_layer_map = {}

    m.manage_district = function(layer_group){
        layers = layer_group._layers
        for(var key in layers){
            var id = layers[key].feature[ATTR_PROPERTY_NAME][ATTR_DISTRICT_ID_NAME]
            m.district_layer_map[id] = layers[key]
        }
    }

    m.manage_precinct = function(layer_group){
        layers = layer_group._layers
        for(var key in layers){
            var id = layers[key].feature[ATTR_PROPERTY_NAME][ATTR_PRECINCT_ID_NAME]
            m.precinct_layer_map[id] = layers[key]
        }
    }

    m.color_district = function(district_id, color){
        m.district_layer_map[district_id].setStyle({fillColor:color})
    }

    m.color_precinct = function(precinct_id, color){
        m.precinct_layer_map[precinct_id].setStyle({fillColor:color})
    }

    m.clear = function(){ m.district_layer_map = {}; m.precinct_layer_map = {} }

    return m

})();

var dynamic_color_changer = (function(){
    cc = {};
    cc.color_map = {};
    cc.used_colors = [];
    cc.color_list = ["red", "black", "yellow", "pink", "purple", "blue", "green", "aqua", "silver"];
    cc.color_pointer = 0;
    cc.color = function(move_object){
        var to_color = undefined
        if ( cc.color_map[move_object.dest] ){
            to_color = cc.color_map[move_object.dest]
        }
        else{
            to_color = cc.next_color();
            cc.color_map[move_object.dest] = to_color;
        }
        layer_manager.color_precinct(move_object.precinct, to_color);

    }
    cc.next_color = function(){
        if ( cc.color_pointer > cc.color_list.length - 1 )
            cc.color_pointer = 0;
        return cc.color_list[cc.color_pointer++];
        /*
        var new_color = 'rgb('+Math.floor(Math.random()*255)+','+Math.floor(Math.random()*255)+','+Math.floor(Math.random()*255)+')'
        while( cc.used_colors.indexOf(new_color) != -1 ){
            new_color = 'rgb('+Math.floor(Math.random()*255)+','+Math.floor(Math.random()*255)+','+Math.floor(Math.random()*255)+')'
        }
        cc.used_colors.push(new_color);
        console.log("New Color Created")
        return new_color;
        */
    }
    return cc
})();