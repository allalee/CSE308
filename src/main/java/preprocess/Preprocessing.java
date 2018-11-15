package preprocess;

import gerrymandering.HibernateManager;
import gerrymandering.model.District;
import gerrymandering.model.Precinct;
import gerrymandering.model.State;

import java.io.File;
import java.util.*;

public class Preprocessing {
    private static HibernateManager hb;
    public static void main(String args[]) throws Throwable {
        hb = HibernateManager.getInstance();
        String[] districtFileNames = new String[1];
        String[] precinctFileNames = new String[1];
        districtFileNames[0] = "CSE308/src/main/resources/static/geojson/kansas_districts.json";
        precinctFileNames[0] = "CSE308/src/main/resources/static/geojson/precinct_data/kansas_state_voting_precincts_2012.json";

        Set<File> districtFiles = PreprocessHelper.loadFiles(districtFileNames);
        Set<File> precinctFiles = PreprocessHelper.loadFiles(precinctFileNames);

        Set<State> states = PreprocessHelper.generateStates();
        persistStates(states);
        HashMap<String, Integer> stateHashMap = generateStateHashMap();
        Set<District> districts = PreprocessHelper.generateDistricts(districtFiles, stateHashMap);
        persistDistricts(districts);
        HashMap<District, Integer> kansasDistricts = generateDistrictHashMap(stateHashMap.get("Kansas"));
        Set<Precinct> precincts = PreprocessHelper.generatePrecincts(precinctFiles, kansasDistricts);
        persistPrecincts(precincts);
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

    private static void persistPrecincts(Set<Precinct> precincts) throws Throwable {
        for (Precinct p : precincts){
            hb.persistToDB(p);
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
