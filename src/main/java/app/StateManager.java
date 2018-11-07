package app;

import java.io.File;
import java.util.HashMap;

public class StateManager {
    private HashMap<String, State> stateMap;
    private State currentState;

    public StateManager(){
        stateMap = new HashMap<>();
        currentState = null;
    }

    public void createState(String stateName){
        if (stateMap.get(stateName) != null){
            loadState(stateName);
        } else {
            File file = loadFile(stateName);
        }
    }

    public void loadState(String stateName){
        currentState = stateMap.get(stateName);
    }

    public File loadFile(String stateName){
        File file;
        String pathName = "";
        if (stateName == "Kansas"){
            pathName = "../../resources/static/geojson/kansas_districts.json";
            System.out.println("Kansas Json loaded");
        }
        file = new File(pathName);
        System.out.println("File returned to createState method.");
        return file;
    }
}
