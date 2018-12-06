package app.controllers;

import java.util.*;

import app.election.ElectionData;
import app.election.Representative;
import app.enums.Ethnicity;
import app.enums.Parties;
import app.enums.Property;
import app.json.GeoJsonReader;
import app.json.JTSConverter;
import app.json.JsonBuilder;
import app.json.PropertiesManager;
import app.state.District;
import app.state.Precinct;
import app.state.State;
import gerrymandering.HibernateManager;
import preprocess.dbclasses.Populations;
import preprocess.dbclasses.VotingData;
import preprocess.dbclasses.Demographics;
import preprocess.dbclasses.Precincts;


public class StateManager {
    private HashMap<String, State> stateMap;
    private State currentState;
    private State clonedState;
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
            State state = getState(stateName);
            getDistricts(state);
            getPrecincts(state.getDistrictMap());
            getPrecinctNeighbors(state);
            getPopulation(state);
            for(District d : state.getDistrictMap().values()){
                state.addPopulation(d.getPopulation());
            }
            stateMap.put(stateName, state);
            currentState = state;
        }
        JsonBuilder jsonBuilder = new JsonBuilder();
        return jsonBuilder.buildStateJson(currentState);
    }
    public String getOriginalPrecinctsMap() {
        JsonBuilder jsonBuilder = new JsonBuilder();
        return jsonBuilder.buildPrecinctJson(currentState.getAllPrecincts());
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
        criteria.put(PropertiesManager.get(Property.GETSTATE_NAME), stateName);
        l = hb.getRecordsBasedOnCriteria(gerrymandering.model.State.class, criteria);
        gerrymandering.model.State s = (gerrymandering.model.State) l.iterator().next();
        State state = new State(s.getName(), s.getStateId());
        return state;
    }

    private void getDistricts(State state) throws Throwable {
        int stateID = state.getID();
        List<Object> l;
        Map<String, Object> criteria = new HashMap<>();
        criteria.put(PropertiesManager.get(Property.GETDISTRICT_STATEID), stateID);
        l = hb.getRecordsBasedOnCriteria(gerrymandering.model.District.class, criteria);
        GeoJsonReader reader = new GeoJsonReader();
        for(Object o : l){
            gerrymandering.model.District d = (gerrymandering.model.District) o;
            District district = new District(d.getDistrictId(), state, reader.read(d.getBoundary()));
            state.addDistrict(district);
        }
    }

    private void getPrecincts(HashMap<Integer, District> map) throws Throwable {
        List<Object> l;
        Map<String, Object> criteria;
        com.vividsolutions.jts.io.geojson.GeoJsonReader reader = new com.vividsolutions.jts.io.geojson.GeoJsonReader();
        for(Integer key : map.keySet()){
            District d = map.get(key);
            criteria = new HashMap<>();
            criteria.put(PropertiesManager.get(Property.GETPRECINCT_DISTRICTID), d.getID());
            l = hb.getRecordsBasedOnCriteria(Precincts.class, criteria);
            for(Object o : l){
                Precincts p = (Precincts) o;
                Precinct precinct = new Precinct(p.getPrecinctId(), reader.read(p.getBoundaryJSON()));
                precinct.setDistrict(d);
                d.addPrecinct(precinct.getID(), precinct);
            }
        }
    }

    private void getPopulation(State state) throws Throwable {
        List<Object> l;
        Map<String, Object> criteria = new HashMap<>();
        HashMap<Integer, District> dMap = state.getDistrictMap();
        for(District d : dMap.values()){
            criteria.put("districtId", d.getID());
            l = hb.getRecordsBasedOnCriteria(Populations.class, criteria);
            for(Object o : l){
                Populations pop = (Populations)o;
                d.getPrecinct(pop.getPrecinctId()).setPopulation(pop.getPopulation());
                d.addPopulation(pop.getPopulation());
            }
        }

    }

    public String loadPrecinctData(Integer districtID, Integer precinctID) throws Throwable {
        Precinct precinct = currentState.getDistrictMap().get(districtID).getPrecinct(precinctID);
        if(precinct != null){
            getDemographics(precinct);
            getElectionData(precinct);
            JsonBuilder builder = new JsonBuilder();
            return builder.buildPrecinctDataJson(precinct);
        }
        return "{}";
    }

    //METHOD IS PURELY FOR CLONED STATES ONLY AS THIS IS FOR THE ALGORITHM TO RUN
    public void loadElectionData() throws Throwable {
        for(Integer districtID : clonedState.getDistrictMap().keySet()){
            Map<String, Object> criteria = new HashMap<>();
            List<Object> l;
            criteria.put(PropertiesManager.get(Property.LOADELECTIONDATA_DISTRICTID), districtID);
            l = hb.getRecordsBasedOnCriteria(VotingData.class, criteria);
            for(Object o : l){
                VotingData vd = (VotingData)o;
                Precinct precinct = clonedState.getDistrictMap().get(districtID).getPrecinct(vd.getPrecinctID());
                HashMap<Parties, Integer> precinctElectionData = precinct.getElectionData().getVoterDistribution();
                if(precinctElectionData.containsKey(vd.getParty())){
                    precinct.getElectionData().addTotalVotes(vd.getVoteCount());
                    int count = precinctElectionData.get(vd.getParty()) + vd.getVoteCount();
                    precinctElectionData.replace(vd.getParty(), count);
                } else {
                    precinct.getElectionData().addTotalVotes(vd.getVoteCount());
                    precinctElectionData.put(vd.getParty(), vd.getVoteCount());
                }
                Representative rep = new Representative(vd.getRepresentative());
                if(!(precinct.getElectionData().getReps().contains(rep))){
                    precinct.getElectionData().addRepresentative(rep);
                }
                precinct.getElectionData().setYear(vd.getYear());
                precinct.getElectionData().setElectionType(vd.getElectionType());
            }
        }
    }

    private void getDemographics(Precinct precinct) throws Throwable{
        if(precinct.getDemographics().isEmpty()){
            Map<String, Object> criteria = new HashMap<>();
            List<Object> l;
            criteria.put(PropertiesManager.get(Property.GETDEMOGRAPHICS_PRECINCTID), precinct.getID());
            criteria.put(PropertiesManager.get(Property.GETPRECINCT_DISTRICTID), precinct.getDistrict().getID());
            l = hb.getRecordsBasedOnCriteria(Demographics.class, criteria);
            if(l.size() != 0) {
                Demographics d = (Demographics) l.get(0);
                HashMap<String, Integer> dMap = d.getDemographicMap();
                precinct.addDemographic(Ethnicity.ASIAN, dMap.get("Asian"));
                precinct.addDemographic(Ethnicity.AFRICAN_AMERICAN, dMap.get("African-American"));
                precinct.addDemographic(Ethnicity.CAUCASIAN, dMap.get("Caucasian"));
                precinct.addDemographic(Ethnicity.HISPANIC, dMap.get("Hispanic"));
                precinct.addDemographic(Ethnicity.NATIVE_AMERICAN, dMap.get("Native-American"));
                precinct.addDemographic(Ethnicity.OTHER, dMap.get("Other"));
            }
        }
    }

    private void getElectionData(Precinct precinct) throws Throwable{
        if(precinct.getElectionData().getVoterDistribution().isEmpty()){
            int totalVotes = 0;
            ElectionData ed = precinct.getElectionData();
            Map<String, Object> criteria = new HashMap<>();
            List<Object> l;
            criteria.put(PropertiesManager.get(Property.GETELECTIONDATA_PRECINCTID), precinct.getID());
            l = hb.getRecordsBasedOnCriteria(VotingData.class, criteria);
            Iterator itr = l.iterator();
            while(itr.hasNext()){
                VotingData p = (VotingData)itr.next();
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
                if(!(ed.getReps().contains(rep))){
                    ed.addRepresentative(rep);
                }
                ed.setYear(p.getYear());
                ed.setElectionType(p.getElectionType());
            }
            ed.setTotalVotes(totalVotes);
        }
    }

    private void getPrecinctNeighbors(State state){
        List<Precinct> precinctList = new ArrayList<>();
        for(District d : state.getDistrictMap().values()){
            precinctList.addAll(d.getAllPrecincts());
        }
        JTSConverter converter = new JTSConverter();
        converter.buildNeighbor(precinctList);
    }

    public void cloneState(String name){
        this.clonedState =  stateMap.get(name).clone();
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

    public String getStateConstitution(String stateName) throws Throwable {
        Map<String, Object> criteria = new HashMap<>();
        List<Object> l;
        criteria.put(PropertiesManager.get(Property.GETSTATECONSTITUTION_NAME), stateName);
        l = hb.getRecordsBasedOnCriteria(gerrymandering.model.State.class, criteria);
        gerrymandering.model.State state = (gerrymandering.model.State) l.get(0);
        return state.getConstitutionText();
    }

    public State getCurrentState(){
        return this.currentState;
    }

    public State getClonedState(){
        return this.clonedState;
    }
}
