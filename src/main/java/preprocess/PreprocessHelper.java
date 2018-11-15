package preprocess;

import com.vividsolutions.jts.geom.Point;
import gerrymandering.model.District;
import gerrymandering.model.Precinct;
import gerrymandering.model.State;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.geojson.GeoJsonReader;

import java.io.*;
import java.util.*;

public class PreprocessHelper {
    public static Set<File> loadFiles(String[] fileNames){
        Set<File> files = new HashSet<>();
        for (String s : fileNames){
            File f = new File(s);
            files.add(f);
        }
        return files;
    }

    public static Set<State> generateStates() throws IOException {
        Set<State> stateSet = new HashSet<>();
        File stateTxt = new File("CSE308/src/main/java/preprocess/CreateState");
        BufferedReader br = new BufferedReader(new FileReader(stateTxt));
        String line;
        while((line = br.readLine()) != null){
            String[] splitData = line.split(",");
            State state = new State(splitData[1], splitData[2], splitData[3]);
            stateSet.add(state);
        }
        return stateSet;
    }

    public static Set<District> generateDistricts(Set<File> files, HashMap<String, Integer> stateHashMap) throws Exception {
        Set<District> districtSet = new HashSet<>();
        JSONParser parser = new JSONParser();
        Iterator<File> fileIterator = files.iterator();
        for(String stateName : stateHashMap.keySet()){
            if(stateName.equals("Kansas")){
                FileReader reader = new FileReader(fileIterator.next());
                JSONArray kansasJSONArray = (JSONArray) parser.parse(reader);
                buildDistricts(districtSet, stateHashMap.get(stateName), kansasJSONArray);
            }
        }
        return districtSet;
    }

    public static Set<Precinct> generatePrecincts(Set<File> files, HashMap<Integer, District> districtMap) throws IOException, ParseException {
        Set<Precinct> precinctSet = new HashSet<>();
        JSONParser parser = new JSONParser();
        Iterator<File> fileIterator = files.iterator();
        FileReader reader = new FileReader(fileIterator.next());
        JSONArray kansasPrecinctJSON = (JSONArray) parser.parse(reader);
        buildPrecincts(precinctSet, kansasPrecinctJSON, districtMap);
        return precinctSet;
    }

    private static void buildDistricts(Set<District> districtSet, int stateID, JSONArray districtJSONArray) throws Exception {
        for(Object district : districtJSONArray){
            JSONObject properties = (JSONObject)((JSONObject)district).get("properties");
            String geoID = properties.get("GEOID").toString();
            String districtGeometry = ((JSONObject)district).get("geometry").toString();
            District d  = new District(stateID, geoID, districtGeometry);
            districtSet.add(d);
        }
    }

    private static void buildPrecincts(Set<Precinct> precinctSet, JSONArray precinctJSONArray, HashMap<Integer, District> districtMap) throws Exception {
        for(Object precinct : precinctJSONArray){
//            JSONObject properties = (JSONObject)((JSONObject)precinct).get("properties");
            GeoJsonReader geoReader = new GeoJsonReader();
            String precinctGeometry = ((JSONObject)precinct).get("geometry").toString();
            Geometry geometry = geoReader.read(precinctGeometry);
            Point centerPoint = geometry.getCentroid();
            Precinct p = new Precinct(findDistrict(geometry), centerPoint.getBoundary().toString(), precinctGeometry);
            precinctSet.add(p);
        }
    }

    private static int findDistrict(Geometry precinctGeometry){

    }
//    private void generatePrecincts(JSONObject json, State state) throws com.vividsolutions.jts.io.ParseException {
//        JSONArray precinctList = (JSONArray) json.get("features");
//        for(Object precinct : precinctList){
//            JSONObject properties = (JSONObject) ((JSONObject)precinct).get("properties");
//            Integer precinctID = Integer.parseInt(properties.get("ID").toString());
//            GeoJsonReader geoReader = new GeoJsonReader();
//            String precinctGeometry = ((JSONObject)precinct).get("geometry").toString();
//            Geometry geo = geoReader.read(precinctGeometry);
//            Precinct p = new Precinct(precinctID, geo);
//            findDistrict(state, p, precinctID);
//        }
//    }
}
