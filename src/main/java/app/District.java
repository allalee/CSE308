package app;

import com.vividsolutions.jts.geom.Geometry;

import java.util.HashMap;


public class District {
    private int ID;
    private State state;
    private HashMap<Integer, Precinct> precincts = new HashMap<>();
    private Geometry geometry;

    public District(int ID, State state, Geometry geometry){
        this.ID = ID;
        this.state = state;
        this.geometry = geometry;
    }

    public Geometry getGeometry(){
        return this.geometry;
    }

    public void addPrecinct(int ID, Precinct p){
        precincts.put(ID, p);
    }
}
