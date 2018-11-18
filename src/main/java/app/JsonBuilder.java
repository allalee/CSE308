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
        gsonBuilder.excludeFieldsWithoutExposeAnnotation().setPrettyPrinting();
        gson = gsonBuilder.create();
    }

    public void buildStateJson(State state) {
        String districtsJson = buildDistrictJson(state.getAllDistricts());
        //String precinctsJson = buildPrecinctJson(state.getAllPrecincts());
        System.out.println(districtsJson);

    }
    private String buildDistrictJson(Collection<District> districts) {
        String districtsJson = "";
        for(District district: districts) {
            Geometry districtGeometry = district.getGeometry();
            String districtCoordinate = districtGeometry.toString();
            String geoType = districtGeometry.getGeometryType();
            districtsJson+=gson.toJson(district);
        }
        return districtsJson;

    }

    private String buildPrecinctJson(ArrayList<Precinct> precincts) {
        String precinctsJson = "";
        for(Precinct precinct: precincts) {
            precinctsJson+=gson.toJson(precinct);
        }
        return precinctsJson;
    }

    private String combinedJson(String district, String precinct){
        String combinedJson = "{\"district\" : [district], \"precinct\": [precinct] }";
        combinedJson.replace("[district]", district);
        combinedJson.replace("[precinct]", precinct);
        return null;
    }
}
