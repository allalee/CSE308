package app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

import java.util.ArrayList;
import java.util.Collection;


public class JsonBuilder {
    private GsonBuilder gsonBuilder;
    private Gson gson;

    public JsonBuilder() {
        gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gson = gsonBuilder.create();
    }

    public void buildStateJson(State state) {
        Collection<District> districts = state.getAllDistricts();
        Collection<Precinct> precincts = new ArrayList<>();
        for(District d : districts){
            precincts.addAll(d.getAllPrecincts());
        }
        String districtsJson = buildDistrictJson(districts);
        String precinctsJson = buildPrecinctJson(precincts);
        System.out.println(districtsJson);
//        gson.toJson(districtsJson); This line does work with the district json

    }
    private String buildDistrictJson(Collection<District> districts) {
        StringBuilder builder = new StringBuilder("[");
        for(District district: districts) {
            builder.append("{\"type\":\"Feature\", \"geometry\": {\"type\":");
            Geometry districtGeometry = district.getGeometry();
            String districtCoordinate = districtGeometry.toString();
            String geoType = districtGeometry.getGeometryType();
            districtCoordinate = districtCoordinate.replace("POLYGON ", "[");
            districtCoordinate = districtCoordinate.replace(", ", "],[");
            districtCoordinate = districtCoordinate.replace("(", "[");
            districtCoordinate = districtCoordinate.replace(")", "]");
            builder.append("\"" + geoType + "\", \"coordinates\":" + districtCoordinate + "]},");
            builder.append(" \"properties\": {\"DISTRICTID\": \"" + district.getID() + "\"}},");
        }
        builder.setCharAt(builder.length()-1, ']');
        return builder.toString();
    }

    private String buildPrecinctJson(Collection<Precinct> precincts) {
        String precinctsJson = "";
//        for(Precinct precinct: precincts) {
//            precinctsJson+=gson.toJson(precinct);
//        }
        return precinctsJson;
    }

    private String combinedJson(String district, String precinct){
        String combinedJson = "{\"district\" : [district], \"precinct\": [precinct] }";
        combinedJson.replace("[district]", district);
        combinedJson.replace("[precinct]", precinct);
        return null;
    }
}
