package app;

import java.util.*;

import com.vividsolutions.jts.geom.Geometry;
import gerrymandering.HibernateManager;
import gerrymandering.model.Population;
import preprocess.Populations;


public class StateManager {
    private HashMap<String, app.State> stateMap;
    private app.State currentState;
    private HibernateManager hb;

    public StateManager() throws Exception {
        stateMap = new HashMap<>();
        hb = HibernateManager.getInstance();
        currentState = null;
    }

    public String createState(String stateName, Integer stateID) throws Throwable {
        if (stateMap.get(stateName) != null){
            loadState(stateName);
        } else {
            app.State state = getState(stateName);
            getDistricts(state);
            getPrecincts(state.getDistrictMap());
            getPrecinctNeighbors(state);
            getPopulation(state);
            stateMap.put(stateName, state);
            currentState = state;
        }
        JsonBuilder jsonBuilder = new JsonBuilder();
        return jsonBuilder.buildStateJson(currentState);
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

    private void getPopulation(app.State state) throws Throwable {
        List<Object> l;
        Map<String, Object> criteria = new HashMap<>();
        HashMap<Integer, District> dMap = state.getDistrictMap();
        for(District d : dMap.values()){
            criteria.put("districtId", d.getID());
            l = hb.getRecordsBasedOnCriteria(Populations.class, criteria);
            for(Object o : l){
                Populations pop = (Populations)o;
                d.getPrecinct(pop.getPrecinctId()).setPopulation(pop.getPopulation());
            }
        }

    }

    public String loadPrecinctData(Integer districtID, Integer precinctID) throws Throwable {
        app.Precinct precinct = currentState.getDistrictMap().get(districtID).getPrecinct(precinctID);
        getDemographics(precinct);
        getElectionData(precinct);
        JsonBuilder builder = new JsonBuilder();
        return builder.buildPrecinctDataJson(precinct);
    }
    private void getDemographics(app.Precinct precinct) throws Throwable{
        if(precinct.getDemographics().isEmpty()){
            Map<String, Object> criteria = new HashMap<>();
            List<Object> l;
            criteria.put("precinctID", precinct.getID());
            l = hb.getRecordsBasedOnCriteria(preprocess.Demographics.class, criteria);
            preprocess.Demographics d = (preprocess.Demographics)l.get(0);
            HashMap<String, Integer> dMap = d.getDemographicMap();
            precinct.addDemographic(Ethnicity.ASIAN, dMap.get("Asian"));
            precinct.addDemographic(Ethnicity.AFRICAN_AMERICAN, dMap.get("African-American"));
            precinct.addDemographic(Ethnicity.CAUCASIAN, dMap.get("Caucasian"));
            precinct.addDemographic(Ethnicity.HISPANIC, dMap.get("Hispanic"));
            precinct.addDemographic(Ethnicity.NATIVE_AMERICAN, dMap.get("Native-American"));
            precinct.addDemographic(Ethnicity.OTHER, dMap.get("Other"));
        }
    }

    private void getElectionData(app.Precinct precinct) throws Throwable{
        if(precinct.getElectionData().getVoterDistribution().isEmpty()){
            int totalVotes = 0;
            ElectionData ed = precinct.getElectionData();
            Map<String, Object> criteria = new HashMap<>();
            List<Object> l;
            criteria.put("precinct_id", precinct.getID());
            l = hb.getRecordsBasedOnCriteria(preprocess.VotingData.class, criteria);
            Iterator itr = l.iterator();
            while(itr.hasNext()){
                preprocess.VotingData p = (preprocess.VotingData)itr.next();
                HashMap<Parties, Integer> vd = ed.getVoterDistribution();
                if(vd.containsKey(p.getParty())){
                    totalVotes += p.getVoteCount();
                    int count = vd.get(p.getParty()) + p.getVoteCount();
                    vd.replace(p.getParty(), count);
                } else {
                    totalVotes += p.getVoteCount();
                    vd.put(p.getParty(), p.getVoteCount());
                }
                Representative rep = new Representative(p.getRepresentative());
                if(ed.getReps().contains(rep)){
                    ed.addRepresentative(rep);
                }
                ed.setYear(p.getYear());
                ed.setElectionType(p.getElectionType());
            }
            ed.setTotalVotes(totalVotes);
        }
    }

    private void getPrecinctNeighbors(app.State state){
        List<Precinct> precinctList = new ArrayList<>();
        for(app.District d : state.getDistrictMap().values()){
            precinctList.addAll(d.getAllPrecincts());
        }
        JTSConverter converter = new JTSConverter();
        converter.buildNeighbor(precinctList);
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
