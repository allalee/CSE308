package preprocess;

import gerrymandering.HibernateManager;
import gerrymandering.model.District;
import gerrymandering.model.State;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class Preprocessing {
    private static HibernateManager hb;
    public static void main(String args[]) throws Throwable {
        hb = HibernateManager.getInstance();
        String[] districtFileNames = new String[1];
        String[] precinctFileNames = new String[1];
        districtFileNames[0] = "CSE308/src/main/resources/static/geojson/kansas_districts.json";
        precinctFileNames[0] = "src/main/resources/static/geojson/precinct_data/kansas_state_voting_precincts_2012.json";

        Set<File> districtFiles = PreprocessHelper.loadFiles(districtFileNames);
        Set<File> precinctFiles = PreprocessHelper.loadFiles(precinctFileNames);

        Set<State> states = PreprocessHelper.generateStates();
        persistStates(states);
        HashMap<Integer, String> stateHashMap = generateStateHashMap();
        Set<District> districts = PreprocessHelper.generateDistricts(districtFiles, stateHashMap);




//        State state = new State("Dinkleberg", "DB", "SampleText");
//        state.setShortName("NY");
//        state.setName("New York");
//        state.setStateId(1);
//        state.setConstitutionText("SampleText");
//        boolean result = hb.persistToDB(state);
//        System.out.println(result);
    }

    private static void persistStates(Set<State> states) throws Throwable {
        for(State s : states) {
            hb.persistToDB(s);
        }
    }

    private static HashMap<Integer, String> generateStateHashMap() throws Throwable {
        List<Object> list = hb.getAllRecords(State.class);
        Iterator<Object> itr = list.iterator();
        HashMap<Integer, String> hm = new HashMap<>();
        while(itr.hasNext()){
            State s = (State) itr.next();
            hm.put(s.getStateId(), s.getName());
        }
        return hm;

    }
}
