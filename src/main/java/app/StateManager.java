package app;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.geojson.GeoJsonReader;
import gerrymandering.HibernateManager;
import org.json.simple.parser.ParseException;

public class StateManager {
    private HashMap<String, State> stateMap;
    private State currentState;
    private HibernateManager hb;
    private List<Object> l;

    public StateManager() throws Exception {
        stateMap = new HashMap<>();
        hb = HibernateManager.getInstance();
        currentState = null;
    }

    public void createState(String stateName, Integer stateID) throws IOException, ParseException, com.vividsolutions.jts.io.ParseException {
        if (stateMap.get(stateName) != null){
            loadState(stateName);
        } else {
            State state = createState(stateName);
        }
    }

    /**If a state already exists, then get it from the HashMap
     * @param stateName Name of the state you wish to get
     */
    private void loadState(String stateName){
        currentState = stateMap.get(stateName);
    }

    private State createState(String stateName) throws Throwable {
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("name", stateName);
        l = hb.getRecordsBasedOnCriteria(gerrymandering.model.State.class, criteria);
    }

    public State cloneState(String name){
        return stateMap.get(name).clone();
    }

    public void addState(State state){
        stateMap.put(state.getName(), state);
    }

    public Set<Precinct> getNeighborPrecincts(int precinctID){
        System.out.println("ID Requested "+precinctID);
        Map<Integer, District> allDistrictMap = currentState.getDistrictMap();
        Precinct targetPrecinct = new Precinct(-1, null); // dummy

        for(District district: allDistrictMap.values()){
            Precinct precinct = district.getPrecinct(precinctID);
            if( precinct!=null ){
                targetPrecinct = precinct;
                break;
            }
        }

        if (targetPrecinct.getID() == -1) {
            System.out.println("PRECINCT NOT FOUDN IN ALL DISTRICTS");
        }
        return targetPrecinct.getNeighbors();
    }
}
