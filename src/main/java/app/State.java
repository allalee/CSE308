package app;

import java.util.HashMap;

public class State {
    private String name;
    private int ID;
    private HashMap<Integer, District> districts;
    private int totalPopulation;

    public State(String name, int ID){
        this.name = name;
        this.ID = ID;
        districts = new HashMap<>();
        totalPopulation = 0;
    }

    public HashMap<Integer, District> getDistricts(){
        return districts;
    }
}
