package preprocess;

import com.vividsolutions.jts.geom.Point;
import gerrymandering.HibernateManager;
import gerrymandering.model.District;
import gerrymandering.model.Population;
import gerrymandering.model.State;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
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

    public static Set<Precincts> generatePrecincts(Set<File> files, HashMap<District, Integer> districtMap) throws Exception {
        Set<Precincts> precinctSet = new HashSet<>();
        JSONParser parser = new JSONParser();
        Iterator<File> fileIterator = files.iterator();
        FileReader reader = new FileReader(fileIterator.next());
        JSONObject kansasPrecinctJSON = (JSONObject) parser.parse(reader);
        buildPrecincts(precinctSet, kansasPrecinctJSON, districtMap);
        return precinctSet;
    }

    public static Set<Populations> generatePopulations(Set<File> files) throws Throwable {
        Set<Populations> populationSet = new HashSet<>();
        JSONParser parser = new JSONParser();
        Iterator<File> fileIterator = files.iterator();
        FileReader reader = new FileReader(fileIterator.next());
        JSONObject kansasPrecinctJSON = (JSONObject) parser.parse(reader);
        buildPopulations(populationSet, kansasPrecinctJSON);
        return populationSet;
    }

    public static Set<Demographics> generateDemographics(Set<File> files) throws Throwable {
        Set<Demographics> demographicsSet = new HashSet<>();
        JSONParser parser = new JSONParser();
        Iterator<File> fileIterator = files.iterator();
        FileReader reader = new FileReader(fileIterator.next());
        JSONObject kansasJSON = (JSONObject) parser.parse(reader);
        buildDemographics(demographicsSet, kansasJSON);
        return demographicsSet;
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

    private static void buildPrecincts(Set<Precincts> precinctSet, JSONObject precinctJSON, HashMap<District, Integer> districtMap) throws Exception {
        JSONArray precinctJSONArray = (JSONArray) precinctJSON.get("features");
        for(Object precinct : precinctJSONArray){
            JSONObject properties = (JSONObject) ((JSONObject)precinct).get("properties");
            Integer precinctID = Integer.parseInt(properties.get("ID").toString());
            GeoJsonReader geoReader = new GeoJsonReader();
            String precinctGeometry = ((JSONObject)precinct).get("geometry").toString();
            Geometry geometry = geoReader.read(precinctGeometry);
            Point centerPoint = geometry.getCentroid();
            String centerString = "{x:" + centerPoint.getX() + ",y:" + centerPoint.getY() + "}";
            Precincts p = new Precincts(precinctID, findDistrictID(geometry, districtMap), centerString, precinctGeometry);
            precinctSet.add(p);
        }
    }

    private static void buildPopulations(Set<Populations> populationSet, JSONObject json) throws Throwable {
        JSONArray precinctJSONArray = (JSONArray) json.get("features");
        HibernateManager hb = HibernateManager.getInstance();
        for(Object precinct : precinctJSONArray){
            JSONObject properties = (JSONObject)((JSONObject)precinct).get("properties");
            Integer precinctID = Integer.parseInt(properties.get("ID").toString());
            Map<String, Object> criteria = new HashMap<>();
            criteria.put("precinctId", precinctID);
            List<Object> list = hb.getRecordsBasedOnCriteria(Precincts.class, criteria);
            Precincts p = (Precincts) list.get(0);
            int pID = p.getPrecinctId();
            int dID = p.getDistrictId();
            Integer population = Integer.parseInt(properties.get("POPULATION").toString());
            Populations pop = new Populations(population, pID, dID);
            populationSet.add(pop);
        }
    }

    private static void buildDemographics(Set<Demographics> demographicsSet, JSONObject json) throws Throwable {
        JSONArray precinctJSONArray = (JSONArray) json.get("features");
        HibernateManager hb = HibernateManager.getInstance();
        for(Object precinct : precinctJSONArray){
            JSONObject properties = (JSONObject)((JSONObject)precinct).get("properties");
            Integer precinctID = Integer.parseInt(properties.get("ID").toString());
            int asian = Integer.parseInt(properties.get("ASIAN").toString());
            int caucasian = Integer.parseInt(properties.get("WHITE").toString());
            int hispanic = Integer.parseInt(properties.get("HISPANIC_O").toString());
            int african_american = Integer.parseInt(properties.get("BLACK").toString());
            int native_american = Integer.parseInt(properties.get("AMINDIAN").toString());
            int other = Integer.parseInt(properties.get("OTHER").toString());
            Demographics d = new Demographics(precinctID, asian, caucasian, hispanic, african_american, native_american, other);
            demographicsSet.add(d);
        }
    }

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
