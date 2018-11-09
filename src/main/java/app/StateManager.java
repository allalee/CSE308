package app;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.geojson.GeoJsonReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class StateManager {
    int i = 0;
    int j = 0;
    private HashMap<String, State> stateMap;
    private State currentState;

    public StateManager(){
        stateMap = new HashMap<>();
        currentState = null;
    }

    public void createState(String stateName, Integer stateID) throws IOException, ParseException, com.vividsolutions.jts.io.ParseException {
        if (stateMap.get(stateName) != null){
            loadState(stateName);
        } else {
            File fileD = loadDistrictFile(stateName);
            FileReader districtInput = new FileReader(fileD);
            JSONParser parser = new JSONParser();
            JSONArray districtJSONArray= (JSONArray) parser.parse(districtInput);
            File fileP = loadPrecinctFile(stateName);
            FileReader precinctInput = new FileReader(fileP);
            JSONObject precinctJsonObject = (JSONObject) parser.parse(precinctInput);
            generateStateData(stateName, stateID, districtJSONArray, precinctJsonObject);
        }
    }

    /**If a state already exists, then get it from the HashMap
     * @param stateName Name of the state you wish to get
     */
    public void loadState(String stateName){
        currentState = stateMap.get(stateName);
    }

    /**Loads in the correct file associated with the name of the state
     * @param stateName The name of the state whose files you are trying to load
     * @return  The opened file
     */
    public File loadDistrictFile(String stateName){
        File file;
        String pathName = "";
        if (stateName.equals("Kansas")){
            pathName = "src/main/resources/static/geojson/kansas_districts.json";
        }
        file = new File(pathName);
        return file;
    }

    /**Load in the precinct json associated with the name of the state
     * @param stateName The name of the state whose files you are trying to load
     * @return The opened file
     */
    public File loadPrecinctFile(String stateName){
        String pathName = "";
        if (stateName.equals("Kansas")){
            pathName = "src/main/resources/static/geojson/precinct_data/kansas_state_voting_precincts_2012.json";
        }
        File file = new File(pathName);
        return file;
    }

    private void generateStateData(String stateName, Integer stateID, JSONArray districtJson, JSONObject precinctJsonObject) throws com.vividsolutions.jts.io.ParseException {
        State state = new State(stateName, stateID);
        this.stateMap.put(stateName, state);
        HashMap<Integer, District> districtMap = state.getDistricts();
        generateDistricts(districtJson, districtMap, state);
        generatePrecincts(precinctJsonObject, state);
        currentState = state;
    }

    private void generateDistricts(JSONArray json, HashMap<Integer, District> map, State state) throws com.vividsolutions.jts.io.ParseException {
        for(Object district: json){
            JSONObject properties = (JSONObject) ((JSONObject)district).get("properties");
            Integer geoID = Integer.parseInt(properties.get("GEOID").toString());
            GeoJsonReader geoReader = new GeoJsonReader();
            String districtGeometry = ((JSONObject)district).get("geometry").toString();
            Geometry geo = geoReader.read(districtGeometry);
            District d = new District(geoID, state, geo);
            map.put(geoID, d);
        }
    }

    private void generatePrecincts(JSONObject json, State state) throws com.vividsolutions.jts.io.ParseException {
        JSONArray precinctList = (JSONArray) json.get("features");
        for(Object precinct : precinctList){
            JSONObject properties = (JSONObject) ((JSONObject)precinct).get("properties");
            Integer precinctID = Integer.parseInt(properties.get("ID").toString());
            GeoJsonReader geoReader = new GeoJsonReader();
            String precinctGeometry = ((JSONObject)precinct).get("geometry").toString();
            Geometry geo = geoReader.read(precinctGeometry);
            Precinct p = new Precinct(precinctID, geo);
            i+=1;
            findDistrict(state, p);
        }
        System.out.println(i);
        System.out.println(j);
    }

    /**Finds the district that the precinct is in and all of the neighbors of the district
     * @param state The state
     * @param precinct The precinct
     */
    private void findDistrict(State state, Precinct precinct){
        HashMap<Integer, District> map = state.getDistricts();
        for (District d : map.values()){
            Geometry dGeo = d.getGeometry();
            Geometry pGeo = precinct.getGeometry();
            if(dGeo.covers(pGeo) || dGeo.intersects(pGeo)){
                d.addPrecinct(precinct);
                precinct.setDistrict(d);
                j+=1;
            }
        }
    }
}
