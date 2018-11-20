package app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.Collection;


public class JsonBuilder {
    private Gson gson;

    public JsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gson = gsonBuilder.create();
    }

    public String buildStateJson(State state) {
        Collection<District> districts = state.getAllDistricts();
        Collection<Precinct> precincts = new ArrayList<>();
        for(District d : districts){
            precincts.addAll(d.getAllPrecincts());
        }
        String districtsJson = buildDistrictJson(districts);
        String precinctsJson = buildPrecinctJson(precincts);
        return gson.toJson(combinedJson(districtsJson, precinctsJson));
    }
    private String buildDistrictJson(Collection<District> districts) {
        StringBuilder builder = new StringBuilder("[");
        for(District district: districts) {
            Geometry districtGeometry = district.getGeometry();
            jsonBuilderHelper(builder, districtGeometry);
            builder.append(" \"properties\": {\"DISTRICTID\": \"" + district.getID() + "\"}},");
        }
        builder.setCharAt(builder.length()-1, ']');
        return builder.toString();
    }

    private String buildPrecinctJson(Collection<Precinct> precincts) {
        StringBuilder builder = new StringBuilder("[");
        for(Precinct precinct: precincts) {
            Geometry precinctGeometry = precinct.getGeometry();
            jsonBuilderHelper(builder, precinctGeometry);
            builder.append(" \"properties\": {\"DISTRICTID\": \"" + precinct.getDistrict().getID() + "\", \"PRECINCTID\": \"" + precinct.getID() + "\"}},\n");
        }
        builder.setCharAt(builder.length()-2, ']');
        return builder.toString();
    }

    private void jsonBuilderHelper(StringBuilder builder, Geometry geo){
        builder.append("{\"type\":\"Feature\", \"geometry\": {\"type\":");
        String coordinates = geo.toString();
        String geoType = geo.getGeometryType();
        coordinates = coordinates.replace("POLYGON ", "[");
        coordinates = coordinates.replace(", ", "],[");
        coordinates = coordinates.replace("(", "[");
        coordinates = coordinates.replace(")", "]");
        coordinates = coordinates.replace(" ", ",");
        if(coordinates.contains("MULTI")){
            coordinates = coordinates.replace("MULTI", "");
            builder.append("\"" + geoType + "\", \"coordinates\":" + coordinates + "]},");
        } else {
            builder.append("\"" + geoType + "\", \"coordinates\":" + coordinates + "]},");
        }
    }

    private String combinedJson(String district, String precinct){
        String combinedJson = "{\"district\" : [district], \"precinct\": [precinct] }";
        combinedJson = combinedJson.replace("[district]", district);
        combinedJson = combinedJson.replace("[precinct]", precinct);
        return combinedJson;
    }
}
