package preprocess;

import gerrymandering.HibernateManager;
import gerrymandering.model.District;
import gerrymandering.model.State;

import java.io.File;
import java.util.ArrayList;

public class Preprocessing {
    public static void main(String args[]) throws Throwable {
        HibernateManager hb = HibernateManager.getInstance();
        String[] districtFileNames = new String[1];
        String[] precinctFileNames = new String[1];
        districtFileNames[0] = "src/main/resources/static/geojson/kansas_districts.json";
        precinctFileNames[0] = "src/main/resources/static/geojson/precinct_data/kansas_state_voting_precincts_2012.json";

        ArrayList<File> districtFiles = PreprocessHelper.loadFiles(districtFileNames);
        ArrayList<File> precinctFiles = PreprocessHelper.loadFiles(precinctFileNames);

        ArrayList<State> states = PreprocessHelper.generateStates();
        ArrayList<District> districts = PreprocessHelper.generateDistricts(districtFiles);
        for(State s : states){
            hb.persistToDB(s);
        }
//        State state = new State("Dinkleberg", "DB", "SampleText");
//        state.setShortName("NY");
//        state.setName("New York");
//        state.setStateId(1);
//        state.setConstitutionText("SampleText");
//        boolean result = hb.persistToDB(state);
//        System.out.println(result);
    }
}
