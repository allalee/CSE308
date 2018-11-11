package app;

import com.vividsolutions.jts.geom.Geometry;

import java.util.HashMap;
import java.util.Set;

public class District{
    private int ID;
    private State state;
    private HashMap<Integer, Precinct> precinctMap = new HashMap<>();
    protected Geometry geometry;


    public District(int ID, State state){
        this.ID = ID;
        this.state = state;
    }

    public Precinct getPrecinct(int id){
        return precinctMap.get(id);
    }
    public void addPrecinct(Precinct precinct){
        precinctMap.put(precinct.getID(), precinct);
    }

    public Geometry getGeometry() {
        return geometry;
    }
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
}
