package app;

import java.util.*;

import com.vividsolutions.jts.geom.Geometry;
import gerrymandering.HibernateManager;


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
            getPrecincts(state.getDistrictMap());
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
        List<Object> l;
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("stateId", stateID);
        l = hb.getRecordsBasedOnCriteria(gerrymandering.model.District.class, criteria);
        GeoJsonReader reader = new GeoJsonReader();
        for(Object o : l){
            gerrymandering.model.District d = (gerrymandering.model.District) o;
            app.District district = new app.District(d.getDistrictId(), state, reader.read(d.getBoundary()));
            state.addDistrict(district);
        }
    }

    private void getPrecincts(HashMap<Integer, District> map) throws Throwable {
        List<Object> l;
        Map<String, Object> criteria;
        com.vividsolutions.jts.io.geojson.GeoJsonReader reader = new com.vividsolutions.jts.io.geojson.GeoJsonReader();
        for(Integer key : map.keySet()){
            app.District d = map.get(key);
            criteria = new HashMap<>();
            criteria.put("districtId", d.getID());
            l = hb.getRecordsBasedOnCriteria(preprocess.Precincts.class, criteria);
            for(Object o : l){
                preprocess.Precincts p = (preprocess.Precincts) o;
                app.Precinct precinct = new app.Precinct(p.getPrecinctId(), reader.read(p.getBoundaryJSON()));
                precinct.setDistrict(d);
                d.addPrecinct(precinct.getID(), precinct);
            }
        }
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
