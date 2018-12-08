package app.state;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

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
    public void setDistrictMap(HashMap<Integer, District> districtMap) { this.districtMap = districtMap;}
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
        State clonedState = new State(this.name, this.ID);
        clonedState.setPopulation(this.totalPopulation);
        for(District district: this.districtMap.values()){
            District clonedDistrict = district.clone(this);
            clonedDistrict.setState(clonedState);
            clonedState.getDistrictMap().put(clonedDistrict.getID(), clonedDistrict);
        }
        return clonedState;
    }

    public void addPopulation(int population){
        this.totalPopulation += population;
    }
    public int getIdealPopulation() {
        return this.totalPopulation/this.districtMap.size();
    }

    public void setPopulation(int population){
        this.totalPopulation = population;
    }
    public District getDistrict(int key){
        return this.districtMap.get(key);
    }

    public Precinct getPrecinct(int key){
        for(District d: districtMap.values()){
            Precinct p = d.getPrecinct(key);
            if(p!=null){
                return p;
            }
        }
        return null;
    }
}
