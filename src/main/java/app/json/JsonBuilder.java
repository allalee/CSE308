package app.json;

import app.state.District;
import app.election.ElectionData;
import app.state.Precinct;
import app.state.State;
import app.enums.Ethnicity;
import app.enums.Parties;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vividsolutions.jts.geom.Geometry;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


public class JsonBuilder {
    private Gson gson;

    public JsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setPrettyPrinting();
        gson = gsonBuilder.create();
    }

    public String buildStateJson(State state) throws JSONException {
        Collection<District> districts = state.getAllDistricts();
        Collection<Precinct> precincts = new ArrayList<>();
        for(District d : districts){
            precincts.addAll(d.getAllPrecincts());
        }
        String districtsJson = buildDistrictJson(districts);
        String precinctsJson = buildPrecinctJson(precincts);
        return gson.toJson(combinedJson(districtsJson, precinctsJson));
    }

    public String buildPrecinctDataJson(Precinct p){
        StringBuilder builder = new StringBuilder("{");
        HashMap<Ethnicity, Integer> demoMap = p.getDemographics();
        if(!demoMap.isEmpty()) {
            builder.append("\"demographics\": {");
            for (Ethnicity e : demoMap.keySet()) {
                String ethnicity = e.toString();
                int population = demoMap.get(e);
                builder.append("\"" + ethnicity + "\": \"" + population + "\",");
            }
            builder.setCharAt(builder.length() - 1, '}');
        }
        if(!demoMap.isEmpty()){
            builder.append(",");
        }
        builder.append("\"population\": \"" + p.getPopulation() + "\"");
        //PERHAPS MISSING VOTING DATA FOR THAT PRECINCT, RESULTING IN BAD JSON i.e: "population" : "948","voting_data" : }}
        ElectionData ed = p.getElectionData();
        if(!(ed.getVoterDistribution().isEmpty())){
            builder.append(",\"voting_data\": {");
            for(Parties parties : ed.getVoterDistribution().keySet()){
                String party = parties.toString();
                int votes = ed.getVoterDistribution().get(parties);
                builder.append("\"" + party + "\": \"" + votes + "\",");
            }
            builder.setCharAt(builder.length()-1, '}');
        }
        builder.append("}");
        return builder.toString();
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
        combinedJson = combinedJson.replace(" ", "");
        return combinedJson;
    }
}
