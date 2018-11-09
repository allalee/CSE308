package app;

import com.vividsolutions.jts.geom.Geometry;

import java.util.ArrayList;


public class District {
    private int ID;
    private State state;
    private ArrayList<Precinct> precincts = new ArrayList<>();
    private Geometry geometry;

    public District(int ID, State state, Geometry geometry){
        this.ID = ID;
        this.state = state;
        this.geometry = geometry;
    }

    public Geometry getGeometry(){
        return this.geometry;
    }

    public void addPrecinct(Precinct p){
        precincts.add(p);
    }
}
