package preprocess.preprocessctmd;

import gerrymandering.HibernateManager;
import gerrymandering.model.District;
import gerrymandering.model.Population;
import gerrymandering.model.State;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import preprocess.dbclasses.Demographics;
import preprocess.dbclasses.Populations;
import preprocess.dbclasses.Precincts;
import preprocess.dbclasses.VotingData;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * Created by Andrew on 11/24/2018.
 */
public class Preprocessing {
    private static HibernateManager hb;
    public static void main(String args[]) throws Throwable {
        hb = HibernateManager.getInstance();
        String[] districtFileNames = new String[2];
        String[] precinctFileNames = new String[2];
        String[] votingDataFileNames = new String[2];
        String[] demographicDataFileNames = new String[1];
        String[] populationDataFileNames = new String[1];
        String[] townDataFileNames = new String[1];

        districtFileNames[0] = "D:/CSE308/src/main/resources/static/geojson/connecticut_districts.json";
        districtFileNames[1] = "D:/CSE308/src/main/resources/static/geojson/maryland_districts.json";
        precinctFileNames[0] = "D:/CSE308/src/main/resources/static/geojson/precinct_data/connecticut_precincts.json";
        precinctFileNames[1] = "D:/CSE308/src/main/resources/static/geojson/precinct_data/maryland_precincts_2012.json";
        votingDataFileNames[0] = "D:/CSE308/voting_data/connvoting.json";
        votingDataFileNames[1] = "D:/CSE308/voting_data/marylandvoting.json";
        demographicDataFileNames[0] = "D:/CSE308/voting_data/population/marylanddemographics.json";
        populationDataFileNames[0] = "D:/CSE308/voting_data/population/conn_town_pop.json";
        townDataFileNames[0] = "D:/CSE308/voting_data/population/conn_town_to_precinctid.json";

        ArrayList<File> districtFiles = PreprocessHelper2.loadFiles(districtFileNames);
        ArrayList<File> precinctFiles = PreprocessHelper2.loadFiles(precinctFileNames);
        ArrayList<File> votingDataFiles = PreprocessHelper2.loadFiles(votingDataFileNames);
        ArrayList<File> demographicDataFiles = PreprocessHelper2.loadFiles(demographicDataFileNames);
        ArrayList<File> populationDataFiles = PreprocessHelper2.loadFiles(populationDataFileNames);
        ArrayList<File> townDataFiles = PreprocessHelper2.loadFiles(townDataFileNames);
//STATES
        Set<State> states = PreprocessHelper2.generateStates();
//        persistStates(states);
        HashMap<String, Integer> stateHashMap = generateStateHashMap();

//DISTRICTS
        ArrayList<District> districts = PreprocessHelper2.generateDistricts(districtFiles, stateHashMap);
//        persistDistricts(districts);
        HashMap<District, Integer> connDistricts = generateDistrictHashMap(stateHashMap.get("Connecticut"));
        HashMap<District, Integer> marylandDistricts = generateDistrictHashMap(stateHashMap.get("Maryland"));

//PRECINCTS
        ArrayList<Precincts> connPrecincts = PreprocessHelper2.generateConnPrecincts(precinctFiles, connDistricts);
//        //persistPrecincts(connPrecincts);
        ArrayList<Precincts> marylandPrecincts = PreprocessHelper2.generateMarylandPrecincts(precinctFiles, marylandDistricts);
        //persistPrecincts(marylandPrecincts);
//VOTING DATA
        HashMap<Integer, Integer> connPrecinctMap = generatePrecinctHashMap(connPrecincts);
        ArrayList<VotingData> connVotingData = PreprocessHelper2.generateConnVotingData(votingDataFiles, connPrecinctMap);
//        persistVoting(connVotingData);
        HashMap<Integer, Integer> marylandPrecinctMap = generatePrecinctHashMap(marylandPrecincts);
        HashMap<String, String> marylandVotingMap = generateMarylandVoteHashMap(precinctFiles, marylandPrecincts);
        ArrayList<VotingData> marylandVotingData = PreprocessHelper2.generateMarylandVotingData(votingDataFiles, marylandPrecinctMap, marylandVotingMap);
//        persistVoting(marylandVotingData);
//DEMOGRAPHICS
        ArrayList<Demographics> marylandDemographicdata = PreprocessHelper2.generateMarylandDemographicData(demographicDataFiles, marylandPrecinctMap);
//        persistDemographics(marylandDemographicdata);



//POPULATION DATA
        ArrayList<Populations> marylandPopulationData = PreprocessHelper2.generateMarylandPopulationData(demographicDataFiles, marylandPrecinctMap);
//        persistPopulations(marylandPopulationData);
       HashMap<String, ArrayList<Integer>> connTownPrecinctMap = townToPrecinctsMap(townDataFiles);
        ArrayList<Populations> connPopulationData = PreprocessHelper2.generateConnPopulationData(populationDataFiles, connPrecinctMap, connTownPrecinctMap);
//        persistPopulations(connPopulationData);
    }


    private static void persistStates(Set<State> states) throws Throwable {
        for(State s : states) {
            hb.persistToDB(s);
        }
    }

    private static void persistDistricts(ArrayList<District> district) throws Throwable {
        for(District d: district){
            hb.persistToDB(d);
        }
    }

    private static void persistPrecincts(ArrayList<Precincts> precincts) throws Throwable {
        for (Precincts p : precincts){
            hb.persistToDB(p);
        }
    }

    private static void persistVoting(ArrayList<VotingData> votingData) throws Throwable {
        for (VotingData v : votingData){
            hb.persistToDB(v);
        }
    }

    private static void persistDemographics(ArrayList<Demographics> demographicsData) throws Throwable {
        for (Demographics d : demographicsData){
            hb.persistToDB(d);
        }
    }

    private static void persistPopulations(ArrayList<Populations> populationsData) throws Throwable {
        for (Populations p : populationsData){
            hb.persistToDB(p);
        }
    }

    //generate hashmap for states (statename maps to db autogen id), so we can have the id the state is associated with for the districts to map
    private static HashMap<String, Integer> generateStateHashMap() throws Throwable {
        List<Object> list = hb.getAllRecords(State.class);
        Iterator<Object> itr = list.iterator();
        HashMap<String, Integer> hm = new HashMap<>();
        while(itr.hasNext()){
            State s = (State) itr.next();
            hm.put(s.getName(), s.getStateId());
        }
        return hm;
    }

    //takes in a stateID autogenerated by the DB.
    //generate hashmap of district object to district id
    private static HashMap<District, Integer> generateDistrictHashMap(int stateID) throws Throwable {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("stateId", stateID);
        List<Object> list = hb.getRecordsBasedOnCriteria(District.class, criteria); //get districts based on DB auto stateID
        Iterator<Object> itr = list.iterator();
        HashMap<District, Integer> hm = new HashMap<>();
        while(itr.hasNext()){
            District d = (District) itr.next();
            hm.put(d, d.getDistrictId());
        }
        return hm;
    }

    //generate hashmap of precinct_id to district_id
    private static HashMap<Integer, Integer> generatePrecinctHashMap(ArrayList<Precincts> PrecinctsMap) throws Throwable {
        HashMap<Integer, Integer> hm = new HashMap<>();
        for(Precincts p : PrecinctsMap){
            hm.put(p.getPrecinctId(), p.getDistrictId());
        }
        return hm;
    }

    private static HashMap<String, String> generateMarylandVoteHashMap(ArrayList<File> files, ArrayList<Precincts> marylandPrecincts) throws IOException, ParseException {
        HashMap<String, String> countyToID = new HashMap<>();
        JSONParser parser = new JSONParser();
        FileReader reader = new FileReader(files.get(1));
        JSONObject marylandPrecinctJSON = (JSONObject) parser.parse(reader);
        JSONArray precinctJSONArray = (JSONArray) marylandPrecinctJSON.get("features");
        for(Object precinct : precinctJSONArray) {
            JSONObject properties = (JSONObject) ((JSONObject) precinct).get("properties");
            String name10 = properties.get("NAME10").toString();
            name10 = name10.replace("Precinct ", "");
            String precinctId = properties.get("GEOID10").toString();
            precinctId = precinctId.substring(2);
            if(precinctId.contains("ZZZZZZ")){
                precinctId = precinctId.replace("ZZZZZZ", "009999"); //remove chars
            }
            countyToID.put(name10, precinctId);
        }
        return countyToID; //count 1850. there are 10 where NAME10s are "Voting Districts not defined", resulting in 9 values missing
    }

    private static HashMap<String, ArrayList<Integer>> townToPrecinctsMap(ArrayList<File> connTownPrecinctFile) throws IOException, ParseException {
        HashMap<String, ArrayList<Integer>> townToPrecincts = new HashMap<>();
        JSONParser parser = new JSONParser();
        FileReader reader = new FileReader(connTownPrecinctFile.get(0));
        JSONArray connTownPrecinctJSON = (JSONArray) parser.parse(reader);
        for(Object connTown : connTownPrecinctJSON) {
            JSONObject connTownObj = (JSONObject) connTown;
            String pID = connTownObj.get("GeoID10").toString();
            if (pID != null) {
                String townName = connTownObj.get("Town").toString();
                if (pID.equals("ZZZ")) {
                    pID = pID.replace("ZZZ", "999"); //remove chars
                } else {
                    pID = pID.substring(3); //take off 090 for connecticut precincts because too large for int.
                    //090 are the first 3 numbers of EVERY geoid10.
                    pID = pID.replace("-", ""); //remove chars
                }
                Integer precinctId = Integer.parseInt(pID);
                if (townToPrecincts.containsKey(townName)) {
                    townToPrecincts.get(connTownObj.get("Town").toString()).add(precinctId);
                } else {
                    ArrayList<Integer> precinctIds = new ArrayList<>();
                    precinctIds.add(precinctId);
                    townToPrecincts.put(townName, precinctIds);
                }
            }
        }
        return townToPrecincts;
    }
}
