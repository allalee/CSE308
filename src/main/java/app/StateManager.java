package app;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class StateManager {
    private HashMap<String, State> stateMap;
    private State currentState;

    public StateManager() {
        stateMap = new HashMap<>();
        currentState = null;
    }

    public void createState(String stateName, Integer stateID) throws IOException, ParseException {
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

    private void generateStateData(String stateName, Integer stateID, JSONArray districtJson, JSONObject precinctJsonObject){
        State state = new State(stateName, stateID);
        this.stateMap.put(stateName, state);
        HashMap<Integer, District> districtMap = state.getDistrictMap();
        generateDistricts(districtJson, districtMap, state);
        generatePrecincts(precinctJsonObject);
        currentState = state;
    }

    private void generateDistricts(JSONArray json, HashMap<Integer, District> map, State state){
        for(Object district: json){
            JSONObject properties = (JSONObject) ((JSONObject)district).get("properties");
            Integer geoID = Integer.parseInt(properties.get("GEOID").toString());
            District d = new District(geoID, state);
            map.put(geoID, d);
        }
    }

    private void generatePrecincts(JSONObject json){
        JSONArray precinctList = (JSONArray) json.get("features");
        for(Object precinct : precinctList){
            JSONObject properties = (JSONObject) ((JSONObject)precinct).get("properties");
            Integer precinctID = Integer.parseInt(properties.get("ID").toString());
            Precinct p = new Precinct(precinctID);
            System.out.println(precinctID);
        }
    }

    public boolean setState(String name){
        currentState = stateMap.get(name);
        return currentState != null;
    }

    public void addState(String name, State state){
        stateMap.put(name, state);
    }

    public Set<Precinct> getNeighborPrecincts(int precinctID){
        System.out.println("ID Requested "+precinctID);
        Map<Integer, District> allDistrictMap = currentState.getDistrictMap();
        Precinct targetPrecinct = new Precinct(-1); // dummy

        for(District district: allDistrictMap.values()){
            Precinct precinct = district.getPrecinct(precinctID);
            if( precinct!=null ){
                targetPrecinct = precinct;
                break;
            }
        }

        if (targetPrecinct.getID() == -1) {
            System.out.println("PRECINCT NOT FOUDN IN ALL DISTRICTS");
        }
        return targetPrecinct.getNeighbors();
    }
}
