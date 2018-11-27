package preprocess.preprocessky;

import app.enums.ElectionType;
import app.enums.Parties;
import com.vividsolutions.jts.geom.Point;
import gerrymandering.HibernateManager;
import gerrymandering.model.District;
import gerrymandering.model.State;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.geojson.GeoJsonReader;
import preprocess.dbclasses.Populations;
import preprocess.dbclasses.Precincts;
import preprocess.dbclasses.VotingData;
import preprocess.dbclasses.Demographics;

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

    public static Set<Precincts> generatePrecincts(Set<File> files, HashMap<District, Integer> districtMap, HashMap<Integer, Integer> pToDID) throws Exception {
        Set<Precincts> precinctSet = new HashSet<>();
        JSONParser parser = new JSONParser();
        Iterator<File> fileIterator = files.iterator();
        FileReader reader = new FileReader(fileIterator.next());
        JSONObject kansasPrecinctJSON = (JSONObject) parser.parse(reader);
        buildPrecincts(precinctSet, kansasPrecinctJSON, districtMap, pToDID);
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

    public static Set<Demographics> generateDemographics(Set<File> files, HashMap<String, Integer> map) throws Throwable {
        Set<Demographics> demographicsSet = new HashSet<>();
        JSONParser parser = new JSONParser();
        Iterator<File> fileIterator = files.iterator();
        FileReader reader = new FileReader(fileIterator.next());
        JSONObject kansasJSON = (JSONObject) parser.parse(reader);
        buildDemographics(demographicsSet, kansasJSON, map, "VTD_S", "VTDNAME");
        return demographicsSet;
    }

    public static Set<VotingData> generateVotingData(Set<File> files, HashMap<String, Integer> vtdMap, HashMap<Integer, Integer> pToDID) throws Throwable {
        Set<VotingData> votingDataSet = new HashSet<>();
        JSONParser parser = new JSONParser();
        Iterator<File> fileIterator = files.iterator();
        FileReader reader = new FileReader(fileIterator.next());
        JSONArray kansasVTDJSON = (JSONArray) parser.parse(reader);
        buildVTD(votingDataSet, kansasVTDJSON, vtdMap, pToDID);
        return votingDataSet;
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

    private static void buildPrecincts(Set<Precincts> precinctSet, JSONObject precinctJSON, HashMap<District, Integer> districtMap, HashMap<Integer, Integer> pToDID) throws Exception {
        JSONArray precinctJSONArray = (JSONArray) precinctJSON.get("features");
        for(Object precinct : precinctJSONArray){
            JSONObject properties = (JSONObject) ((JSONObject)precinct).get("properties");
            Integer precinctID = Integer.parseInt(properties.get("ID").toString());
            GeoJsonReader geoReader = new GeoJsonReader();
            String precinctGeometry = ((JSONObject)precinct).get("geometry").toString();
            Geometry geometry = geoReader.read(precinctGeometry);
            Point centerPoint = geometry.getCentroid();
            String centerString = "{x:" + centerPoint.getX() + ",y:" + centerPoint.getY() + "}";
            int districtID = findDistrictID(geometry, districtMap);
            Precincts p = new Precincts(precinctID, districtID, centerString, precinctGeometry);
            pToDID.put(precinctID, districtID);
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

    private static void buildDemographics(Set<Demographics> demographicsSet, JSONObject json, HashMap<String, Integer> map, String key1, String key2) throws Throwable {
        JSONArray precinctJSONArray = (JSONArray) json.get("features");
        for(Object precinct : precinctJSONArray){
            JSONObject properties = (JSONObject)((JSONObject)precinct).get("properties");
            Integer precinctID = Integer.parseInt(properties.get("ID").toString());
            int asian = Integer.parseInt(properties.get("ASIAN").toString());
            int caucasian = Integer.parseInt(properties.get("WHITE").toString());
            int hispanic = Integer.parseInt(properties.get("HISPANIC_O").toString());
            int african_american = Integer.parseInt(properties.get("BLACK").toString());
            int native_american = Integer.parseInt(properties.get("AMINDIAN").toString());
            int other = Integer.parseInt(properties.get("OTHER").toString());
            other += Integer.parseInt(properties.get("HAWAIIAN").toString());
            other += Integer.parseInt(properties.get("F2_RACES").toString());
            String VTD_S = properties.get(key1).toString().replaceAll("\\s", "");
            String VTDNAME = properties.get(key2).toString().replaceAll("\\s", "");
            String key = VTD_S + " " + VTDNAME;
            map.put(key, precinctID);
            Demographics d = new Demographics(precinctID, asian, caucasian, hispanic, african_american, native_american, other);
            demographicsSet.add(d);
        }
    }

    private static void buildVTD(Set<VotingData> vtdSet, JSONArray json, HashMap<String, Integer> vtdMap, HashMap<Integer, Integer> pToDID) throws Throwable {
        for(Object vd : json){
            JSONObject vdJOBject = ((JSONObject)vd);
            String VTD_S = vdJOBject.get("VTD").toString().replaceAll("\\s","");
            String VTDNAME = vdJOBject.get("PRECINCT").toString().replaceAll("\\s","");
            String key = VTD_S + " " + VTDNAME;
            Integer pID = vtdMap.get(key);
            String party = vdJOBject.get("PARTY").toString();
            if(pID == null){
                continue;
            } else if(party.equals("Democratic") || party.equals("Republican") || party.equals("Libertarian") || party.equals("Green") || party.equals("Other")){
                String county = vdJOBject.get("COUNTY").toString();
                Integer voteCount = Integer.parseInt(vdJOBject.get("VOTES").toString());
                String representative = vdJOBject.get("CANDIDATE").toString();
                String repParty = vdJOBject.get("PARTY").toString().toUpperCase();
                VotingData data = new VotingData(county, voteCount, pID, representative, Parties.valueOf(repParty), ElectionType.PRESIDENTIAL, 2012, pToDID.get(pID));
                vtdSet.add(data);
            }
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
