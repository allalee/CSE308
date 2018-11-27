package preprocess.preprocessctmd;

import gerrymandering.HibernateManager;
import gerrymandering.model.District;
import gerrymandering.model.State;
import preprocess.dbclasses.Precincts;
import preprocess.dbclasses.VotingData;

import java.io.File;
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
        districtFileNames[0] = "D:/CSE308/src/main/resources/static/geojson/connecticut_districts.json";
        districtFileNames[1] = "D:/CSE308/src/main/resources/static/geojson/maryland_districts.json";
        precinctFileNames[0] = "D:/CSE308/src/main/resources/static/geojson/precinct_data/connecticut_precincts.json";
        precinctFileNames[1] = "D:/CSE308/src/main/resources/static/geojson/precinct_data/maryland_precincts_2012.json";
        votingDataFileNames[0] = "D:/CSE308/voting_data/connvoting.json";
        votingDataFileNames[1] = "D:/CSE308/voting_data/marylandvoting.json";

        ArrayList<File> districtFiles = PreprocessHelper2.loadFiles(districtFileNames);
        ArrayList<File> precinctFiles = PreprocessHelper2.loadFiles(precinctFileNames);
        ArrayList<File> votingDataFiles = PreprocessHelper2.loadFiles(votingDataFileNames);

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
        HashMap<Integer, Integer> marylandPrecinctMap = generatePrecinctHashMap(marylandPrecincts);
        ArrayList<VotingData> marylandVotingData = PreprocessHelper2.generateMarylandVotingData(votingDataFiles, marylandPrecinctMap);
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

    private static void persistPrecincts(Set<Precincts> precincts) throws Throwable {
        for (Precincts p : precincts){
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

}
