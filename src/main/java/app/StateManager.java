package app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import gerrymandering.HibernateManager;
import org.json.simple.parser.ParseException;

public class StateManager {
    private HashMap<String, app.State> stateMap;
    private app.State currentState;
    private HibernateManager hb;

    public StateManager() throws Exception {
        stateMap = new HashMap<>();
        hb = HibernateManager.getInstance();
        currentState = null;
    }

    public void createState(String stateName, Integer stateID) throws Throwable {
        if (stateMap.get(stateName) != null){
            loadState(stateName);
        } else {
            app.State state = getState(stateName);
            getDistricts(state);
            stateMap.put(stateName, state);
            currentState = state;
        }
    }


    /**If a state already exists, then get it from the HashMap
     * @param stateName Name of the state you wish to get
     */
    private void loadState(String stateName){
        currentState = stateMap.get(stateName);
    }

    private State getState(String stateName) throws Throwable {
        List<Object> l;
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("name", stateName);
        l = hb.getRecordsBasedOnCriteria(gerrymandering.model.State.class, criteria);
        gerrymandering.model.State s = (gerrymandering.model.State) l.iterator().next();
        app.State state = new State(s.getName(), s.getStateId());
        return state;
    }

    private void getDistricts(app.State state) throws Throwable {
        int stateID = state.getID();

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
