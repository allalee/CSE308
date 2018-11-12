package app;

import com.vividsolutions.jts.geom.Geometry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.HashMap;

public class District{
    private int ID;
    private State state;
    private HashMap<Integer, Precinct> precinctMap;
    protected Geometry geometry;


    public District(int ID, State state, Geometry geometry){
        this.ID = ID;
        this.state = state;
        this.geometry = geometry;
        precinctMap = new HashMap<>();
    }

    public Precinct getPrecinct(int id){
        return precinctMap.get(id);
    }
    public void addPrecinct(int ID, Precinct precinct){
        precinctMap.put(ID, precinct);
    }

    public Collection<Precinct> getAllPrecincts(){
        return precinctMap.values();
    }

    public Geometry getGeometry() {
        return geometry;
    }
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
    public int getID(){ return ID; }

    public District clone(State newOwnerState){
        District clonedDistrict = new District(ID, newOwnerState, this.geometry);
        clonedDistrict.precinctMap = new HashMap<>();   // empty precincts. to be set in state
        return clonedDistrict;
    }
}
