package preprocess;

import gerrymandering.model.District;
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

    public static Set<District> generateDistricts(Set<File> files, HashMap<Integer, String> stateHashMap) throws IOException, com.vividsolutions.jts.io.ParseException, ParseException {
        Set<District> districtSet = new HashSet<>();
        JSONParser parser = new JSONParser();
        Iterator<File> fileIterator = files.iterator();
        for(Integer id : stateHashMap.keySet()){
            if(stateHashMap.get(id).equals("Kansas")){
                FileReader reader = new FileReader(fileIterator.next());
                JSONArray kansasJSONArray = (JSONArray) parser.parse(reader);
                buildDistricts(districtSet, id, kansasJSONArray);
            }
        }
//        for(File f : files){
//            FileReader reader = new FileReader(f);
//            JSONArray districtJSONArray= (JSONArray) parser.parse(reader);
//            for(Object district: districtJSONArray){
//                JSONObject properties = (JSONObject) ((JSONObject)district).get("properties");
//                Integer geoID = Integer.parseInt(properties.get("GEOID").toString());
//                GeoJsonReader geoReader = new GeoJsonReader();
//                String districtGeometry = ((JSONObject)district).get("geometry").toString();
//                Geometry geo = geoReader.read(districtGeometry);
//                District d = new District();
//            }
//        }
        return districtSet;
    }

    private static void buildDistricts(Set<District> districtSet, int stateID, JSONArray districtJSONArray){
        for(Object district : districtJSONArray){
            JSONObject properties = (JSONObject)((JSONObject)district).get("properties");

        }
    }
}
