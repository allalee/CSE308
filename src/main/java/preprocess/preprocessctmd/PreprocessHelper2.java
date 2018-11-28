package preprocess.preprocessctmd;

import app.enums.ElectionType;
import app.enums.Parties;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.geojson.GeoJsonReader;
import gerrymandering.model.District;
import gerrymandering.model.State;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import preprocess.dbclasses.Precincts;
import preprocess.dbclasses.VotingData;
import utils.PartyName;

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

    public static ArrayList<VotingData> generateConnVotingData(ArrayList<File> files, HashMap<Integer, Integer> connPrecinctMap) throws Throwable {
        ArrayList<VotingData> votingDataList = new ArrayList<>();
        JSONParser parser = new JSONParser();
        //build voting data for connecticut
        FileReader reader = new FileReader(files.get(0));
        JSONArray connVTDJSON = (JSONArray) parser.parse(reader);
        buildConnVTD(votingDataList,connVTDJSON, connPrecinctMap);
        return votingDataList;
    }

    public static ArrayList<VotingData> generateMarylandVotingData(ArrayList<File> files, HashMap<Integer, Integer> marylandPrecinctMap, HashMap<String, String> marylandVotingMap) throws Throwable {
        ArrayList<VotingData> votingDataList = new ArrayList<>();
        JSONParser parser = new JSONParser();
        //build voting data for maryland
        FileReader reader = new FileReader(files.get(1));
        JSONArray marylandVTDJSON = (JSONArray) parser.parse(reader);
        buildMarylandVTD(votingDataList,marylandVTDJSON, marylandPrecinctMap, marylandVotingMap);
        return votingDataList;
    }

    private static void buildMarylandVTD(List<VotingData> vtdList, JSONArray json, HashMap<Integer, Integer> marylandPrecinctMap, HashMap<String,String> marylandVotingMap) throws Throwable{
        ArrayList<String> keysNotExist = new ArrayList<>();
        for(Object vd : json){
            JSONObject vdJObject = ((JSONObject)vd);
            String pID = vdJObject.get("PRECINCT").toString();
            if(pID == null || pID.equals("UNABLE TO DETERMINE")){
                continue;
            }else{
                String party = vdJObject.get("PARTY").toString();
                String representative = "";
                if(party.equalsIgnoreCase("DEMOCRAT") || party.equalsIgnoreCase(Parties.REPUBLICAN.toString()) || party.equalsIgnoreCase(Parties.LIBERTARIAN.toString()) || party.equalsIgnoreCase(Parties.GREEN.toString())) {
                    String county = vdJObject.get("COUNTY").toString();
                    Integer voteCnt = Integer.parseInt(vdJObject.get("TOTAL_VOTERS").toString());

                    //get the proper districtid associated with the name10 precinctids.
                    String name10 = county + " " + pID.substring(1);
                    if(marylandVotingMap.containsKey(name10)) {
                        String precinctId = marylandVotingMap.get(name10);
                        precinctId = precinctId.replace("-", ""); //remove chars
                        Integer precinctIdint = Integer.parseInt(precinctId);
                        Integer districtId = marylandPrecinctMap.get(precinctIdint);

                        if (party.equals("DEMOCRAT")) {
                            party = Parties.DEMOCRATIC.toString();
                            representative = "Obama, Barack";
                        }
                        if (party.equalsIgnoreCase(Parties.REPUBLICAN.toString())) {
                            representative = "Romney, Mitt";
                        }
                        if (party.equalsIgnoreCase(Parties.LIBERTARIAN.toString())) {
                            representative = "Johnson, Gary";
                        }
                        if (party.equalsIgnoreCase(Parties.GREEN.toString())) {
                            representative = "Stein, Jill";
                        }
                        VotingData data = new VotingData(county, voteCnt, precinctIdint, representative, Parties.valueOf(party), ElectionType.PRESIDENTIAL, 2012, districtId);
                        vtdList.add(data);
                    }
                    else{
                        keysNotExist.add(name10);
                    }
                }
            }
        }
    }

    private static void buildConnVTD(List<VotingData> vtdList, JSONArray json, HashMap<Integer, Integer> connPrecinctMap) throws Throwable {
        for(Object vd : json){
            JSONObject vdJOBject = ((JSONObject)vd);
            String pID = vdJOBject.get("GeoID10").toString();
            if(pID == null){
                continue;
            } else{
                String county = vdJOBject.get("Town").toString();
                Integer voteCntRep = Integer.parseInt(vdJOBject.get("John McCain (PRES)").toString());
                Integer voteCntDem = Integer.parseInt(vdJOBject.get("Barack Obama (PRES)").toString());
                Integer voteCntGreen = Integer.parseInt(vdJOBject.get("Cynthia McKinney (PRES)").toString());
                Integer voteCntInd = Integer.parseInt(vdJOBject.get("Ralph Nader (PRES)").toString());
                String representativeRep = "McCain, John";
                String representativeDem = "Obama, Barack";
                String representativeGreen = "McKinney, Cynthia";
                String representativeInd = "Nader, Ralph";
                String partyRep = "REPUBLICAN";
                String partyDem = "DEMOCRATIC";
                String partyGreen = "GREEN";
                String partyInd = "INDEPENDENT";

                //GETTING THE PROPER pID TO PERSIST, AND THE CORRECT DistrictID

                if(pID.equals("ZZZ")){
                    pID = pID.replace("ZZZ", "999"); //remove chars
                }
                else {
                    pID = pID.substring(3); //take off 090 for connecticut precincts because too large for int.
                                            //090 are the first 3 numbers of EVERY geoid10.
                    pID = pID.replace("-", ""); //remove chars
                }
                Integer precinctID = Integer.parseInt(pID);
                Integer districtID = connPrecinctMap.get(precinctID);

                //DISTRICTID FIELD WILL BE ADDED TO THE VOTING DATA MODEL.
                //DISTRICTID FROM 46 TO 50 INCLUSIVE IS PART OC CONNECTICUT.
                if (districtID != null) {
                    VotingData dataRep = new VotingData(county, voteCntRep, precinctID, representativeRep, Parties.valueOf(partyRep), ElectionType.PRESIDENTIAL, 2008, districtID);
                    VotingData dataDem = new VotingData(county, voteCntDem, precinctID, representativeDem, Parties.valueOf(partyDem), ElectionType.PRESIDENTIAL, 2008, districtID);
                    VotingData dataGreen = new VotingData(county, voteCntGreen, precinctID, representativeGreen, Parties.valueOf(partyGreen), ElectionType.PRESIDENTIAL, 2008, districtID);
                    VotingData dataInd = new VotingData(county, voteCntInd, precinctID, representativeInd, Parties.valueOf(partyInd), ElectionType.PRESIDENTIAL, 2008, districtID);
                    vtdList.add(dataRep);
                    vtdList.add(dataDem);
                    vtdList.add(dataGreen);
                    vtdList.add(dataInd);
                }
            }
        }
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
            if(geo10ID.startsWith("24")){
                geo10ID = geo10ID.substring(2);
            }
            else if(geo10ID.startsWith("090")){
                geo10ID = geo10ID.substring(3);
            }

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
