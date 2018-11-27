package preprocess.preprocessctmd;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.geojson.GeoJsonReader;
import gerrymandering.model.District;
import gerrymandering.model.State;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import preprocess.dbclasses.Precincts;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Andrew on 11/24/2018.
 */
public class PreprocessHelper2 {

    public static ArrayList<File> loadFiles(String[] fileNames){
        ArrayList<File> files = new ArrayList<>();
        for (String s : fileNames){
            File f = new File(s);
            files.add(f);
        }
        return files;
    }

    public static Set<State> generateStates() throws IOException {
        Set<State> stateSet = new HashSet<>();
        File ctTxt = new File("D:\\CSE308\\src\\main\\java\\preprocess\\preprocessctmd\\CTState");
        BufferedReader br = new BufferedReader(new FileReader(ctTxt));
        String line;
        while((line = br.readLine()) != null){
            String[] splitData = line.split("\\|");
            State state = new State(splitData[1], splitData[2], splitData[3]);
            stateSet.add(state);
        }
        File mdTxt = new File("D:\\CSE308\\src\\main\\java\\preprocess\\preprocessctmd\\MDState");
        BufferedReader br2 = new BufferedReader(new FileReader(mdTxt));
        while((line = br2.readLine()) != null){
            String[] splitData = line.split("\\|");
            State state = new State(splitData[1], splitData[2], splitData[3]);
            stateSet.add(state);
        }
        return stateSet;
    }

    public static ArrayList<District> generateDistricts(ArrayList<File> files, HashMap<String, Integer> stateHashMap) throws Exception {
        ArrayList<District> districtList = new ArrayList<>();
        JSONParser parser = new JSONParser();
        for(String stateName : stateHashMap.keySet()){
            if(stateName.equals("Connecticut")){
                FileReader reader = new FileReader(files.get(0));
                JSONArray connJSONArray = (JSONArray) parser.parse(reader);
                buildDistricts(districtList, stateHashMap.get(stateName), connJSONArray);
            }
            if(stateName.equals("Maryland")){
                FileReader reader = new FileReader(files.get(1));
                JSONArray marylandJSONArray = (JSONArray) parser.parse(reader);
                buildDistricts(districtList, stateHashMap.get(stateName), marylandJSONArray);
            }
        }
        return districtList;
    }

    public static ArrayList<Precincts> generateConnPrecincts(ArrayList<File> files, HashMap<District, Integer> districtMap) throws Exception {
        ArrayList<Precincts> precinctList = new ArrayList<>();
        JSONParser parser = new JSONParser();
        //build precincts for connecticut
        FileReader reader = new FileReader(files.get(0));
        JSONObject connPrecinctJSON = (JSONObject) parser.parse(reader);
        buildPrecincts(precinctList, connPrecinctJSON, districtMap);
        return precinctList;
    }

    public static ArrayList<Precincts> generateMarylandPrecincts(ArrayList<File> files, HashMap<District, Integer> districtMap) throws Exception {
        ArrayList<Precincts> precinctList = new ArrayList<>();
        JSONParser parser = new JSONParser();
        //build precincts for maryland
        FileReader reader = new FileReader(files.get(1));
        JSONObject marylandPrecinctJSON = (JSONObject) parser.parse(reader);
        buildPrecincts(precinctList, marylandPrecinctJSON, districtMap);
        return precinctList;
    }


    private static void buildDistricts(ArrayList<District> districtList, int stateID, JSONArray districtJSONArray) throws Exception {
        for(Object district : districtJSONArray){
            JSONObject properties = (JSONObject)((JSONObject)district).get("properties");
            String geoID = properties.get("GEOID").toString();
            String districtGeometry = ((JSONObject)district).get("geometry").toString();
            District d  = new District(stateID, geoID, districtGeometry);
            districtList.add(d);
        }
    }

    private static void buildPrecincts(List<Precincts> precinctList, JSONObject precinctJSON, HashMap<District, Integer> districtMap) throws Exception {
        JSONArray precinctJSONArray = (JSONArray) precinctJSON.get("features");
        for(Object precinct : precinctJSONArray){
            JSONObject properties = (JSONObject) ((JSONObject)precinct).get("properties");
            String geo10ID = properties.get("GEOID10").toString();
            geo10ID = geo10ID.substring(3); //take off 090 for connecticut precincts because too large for int.
                                            //take off 240 for maryland precincts as well.
                                            //both 090 and 240 are the first 3 numbers of EVERY geoid10.
            if(geo10ID.contains("ZZZZZZ")){
                geo10ID = geo10ID.replace("ZZZZZZ", "009999"); //remove chars
            }
            else {
                geo10ID = geo10ID.replace("-", ""); //remove chars
            }
            Integer precinctID = Integer.parseInt(geo10ID);
            GeoJsonReader geoReader = new GeoJsonReader();
            String precinctGeometry = ((JSONObject)precinct).get("geometry").toString();
            Geometry geometry = geoReader.read(precinctGeometry);
            Point centerPoint = geometry.getCentroid();
            String centerString = "{x:" + centerPoint.getX() + ",y:" + centerPoint.getY() + "}";
            Precincts p = new Precincts(precinctID, findDistrictID(geometry, districtMap), centerString, precinctGeometry);
            precinctList.add(p);
        }
    }

    //link precincts to respective districts
    private static int findDistrictID(Geometry precinctGeometry, HashMap<District, Integer> dMap) throws com.vividsolutions.jts.io.ParseException {
        District highestIntersectingDistrict = null;
        double area = 0;
        GeoJsonReader reader = new GeoJsonReader();
        for (District d : dMap.keySet()){
            Geometry dGeo = reader.read(d.getBoundary());
            if(dGeo.contains(precinctGeometry)){
                return d.getDistrictId();
            }
            if(highestIntersectingDistrict == null || dGeo.intersection(precinctGeometry).getArea() > area){
                highestIntersectingDistrict = d;
                area = dGeo.intersection(precinctGeometry).getArea();
            }
        }
        return highestIntersectingDistrict.getDistrictId();
    }

}
