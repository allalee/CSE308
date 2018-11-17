package preprocess;

import gerrymandering.HibernateManager;
import gerrymandering.model.District;
import gerrymandering.model.State;

import java.io.File;
import java.util.*;

public class Preprocessing {
    private static HibernateManager hb;
    public static void main(String args[]) throws Throwable {
        hb = HibernateManager.getInstance();
        String[] districtFileNames = new String[1];
        String[] precinctFileNames = new String[1];
        String[] votingDataFileNames = new String[1];
        districtFileNames[0] = "CSE308/src/main/resources/static/geojson/kansas_districts.json";
        precinctFileNames[0] = "CSE308/src/main/resources/static/geojson/precinct_data/kansas_state_voting_precincts_2012.json";
        votingDataFileNames[0] = "CSE308/voting_data/kansas_2012_president_election.json";

        Set<File> districtFiles = PreprocessHelper.loadFiles(districtFileNames);
        Set<File> precinctFiles = PreprocessHelper.loadFiles(precinctFileNames);
        Set<File> votingDataFiles = PreprocessHelper.loadFiles(votingDataFileNames);

        Set<State> states = PreprocessHelper.generateStates();
//        persistStates(states);
        HashMap<String, Integer> stateHashMap = generateStateHashMap();
        Set<District> districts = PreprocessHelper.generateDistricts(districtFiles, stateHashMap);
//        persistDistricts(districts);
        HashMap<District, Integer> kansasDistricts = generateDistrictHashMap(stateHashMap.get("Kansas"));
        Set<Precincts> precincts = PreprocessHelper.generatePrecincts(precinctFiles, kansasDistricts);
//        persistPrecincts(precincts);
        Set<Populations> populations = PreprocessHelper.generatePopulations(precinctFiles);
//        persistPopulation(populations);
        HashMap<String, Integer> precinctVTD = new HashMap<>();
        Set<Demographics> demographics = PreprocessHelper.generateDemographics(precinctFiles, precinctVTD);
//        persistDemographics(demographics);
        Set<VotingData> votingData = PreprocessHelper.generateVotingData(votingDataFiles, precinctVTD);
        persistVotingData(votingData);
    }

    private static void persistStates(Set<State> states) throws Throwable {
        for(State s : states) {
            hb.persistToDB(s);
        }
    }

    private static void persistDistricts(Set<District> district) throws Throwable {
        for(District d: district){
            hb.persistToDB(d);
        }
    }

    private static void persistPrecincts(Set<Precincts> precincts) throws Throwable {
        for (Precincts p : precincts){
            hb.persistToDB(p);
        }
    }

    private static void persistPopulation(Set<Populations> population) throws Throwable {
        for(Populations p : population){
            hb.persistToDB(p);
        }
    }

    private static void persistDemographics(Set<Demographics> demographics) throws Throwable {
        for(Demographics d : demographics){
            hb.persistToDB(d);
        }
    }

    private static void persistVotingData(Set<VotingData> votingData) throws Throwable {
        for(VotingData v : votingData){
            hb.persistToDB(v);
        }
    }

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

    private static HashMap<District, Integer> generateDistrictHashMap(int stateID) throws Throwable {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("stateId", stateID);
        List<Object> list = hb.getRecordsBasedOnCriteria(District.class, criteria);
        Iterator<Object> itr = list.iterator();
        HashMap<District, Integer> hm = new HashMap<>();
        while(itr.hasNext()){
            District d = (District) itr.next();
            hm.put(d, d.getDistrictId());
        }
        return hm;
    }
}
