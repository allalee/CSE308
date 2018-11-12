package app;

import com.vividsolutions.jts.geom.Geometry;

import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

public class District{
    private int ID;
    private State state;
    private HashMap<Integer, Precinct> precinctMap;
    protected Geometry geometry;


    public District(int ID, State state){
        this.ID = ID;
        this.state = state;
        precinctMap = new HashMap<>();
    }

    public Precinct getPrecinct(int id){
        return precinctMap.get(id);
    }
    public void addPrecinct(Precinct precinct){
        precinctMap.put(precinct.getID(), precinct);
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
        District clonedDistrict = new District(ID, newOwnerState);
        clonedDistrict.geometry = geometry;
        clonedDistrict.precinctMap = new HashMap<>();   // empty precincts. to be set in state
        return clonedDistrict;
    }
}
