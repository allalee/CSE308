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
import java.util.ArrayList;

public class PreprocessHelper {
    public static ArrayList<File> loadFiles(String[] fileNames){
        ArrayList<File> files = new ArrayList<>();
        for (String s : fileNames){
            File f = new File(s);
            files.add(f);
        }
        return files;
    }

    public static ArrayList<State> generateStates() throws IOException {
        ArrayList<State> stateArrayList = new ArrayList<>();
        File stateTxt = new File("src/main/java/preprocess/CreateState");
        BufferedReader br = new BufferedReader(new FileReader(stateTxt));
        String line;
        while((line = br.readLine()) != null){
            String[] splitData = line.split(",");
            State state = new State(splitData[1], splitData[2], splitData[3]);
            stateArrayList.add(state);
        }
        return stateArrayList;
    }

    public static ArrayList<District> generateDistricts(ArrayList<File> files) throws IOException, com.vividsolutions.jts.io.ParseException, ParseException {
        ArrayList<District> districtArrayList = new ArrayList<>();
        JSONParser parser = new JSONParser();
        for(File f : files){
            FileReader reader = new FileReader(f);
            JSONArray districtJSONArray= (JSONArray) parser.parse(reader);
            for(Object district: districtJSONArray){
                JSONObject properties = (JSONObject) ((JSONObject)district).get("properties");
                Integer geoID = Integer.parseInt(properties.get("GEOID").toString());
                GeoJsonReader geoReader = new GeoJsonReader();
                String districtGeometry = ((JSONObject)district).get("geometry").toString();
                Geometry geo = geoReader.read(districtGeometry);
                District d = new District();
            }
        }
        return districtArrayList;
    }
}
