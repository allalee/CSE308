package app;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class State {
    private String name;
    private int ID;
    private HashMap<Integer, District> districtMap;
    private int totalPopulation;

    public State(String name, int ID){
        this.name = name;
        this.ID = ID;
        districtMap = new HashMap<>();
        totalPopulation = 0;
    }

    public HashMap<Integer, District> getDistrictMap(){
        return districtMap;
    }
    public void addDistrict(District district){ districtMap.put(district.getID(), district); }
    public Collection<District> getAllDistricts(){
        return districtMap.values();
    }

    public ArrayList<Precinct> getAllPrecincts(){
        ArrayList<Precinct> allPrecincts = new ArrayList<>();
        for(District district: districtMap.values()){
            allPrecincts.addAll(district.getAllPrecincts());
        }
        return allPrecincts;
    }

    public String getName(){
        return name;
    }
    public int getID(){
        return ID;
    }

    public State clone(){
        State clonedState = new State(getName(), getID());

        // clone districts
        District tempDistrict = new District(-1, null);
        for(District district: this.getAllDistricts()){
            clonedState.addDistrict(district.clone(clonedState));
            tempDistrict = district; // get the last district
        }

        // clone precincts & link with district
        Precinct startPrecinct = tempDistrict.getAllPrecincts().iterator().next();
        startPrecinct.chainClone(clonedState.getDistrictMap());

        return clonedState;
    }
}
