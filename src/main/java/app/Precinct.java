package app;

import com.vividsolutions.jts.geom.Geometry;

import java.util.HashSet;
import java.util.Set;

public class Precinct{
    private int ID;
    private District district;
    private Set<Precinct> neighbors = new HashSet<>();
    protected Geometry geometry;

    public Precinct(int ID){
        this.ID = ID;
    }
    public int getID(){ return ID; }
    public District getDistrict(){
        return district;
    }
    public void setDistrict(District district){
        this.district = district;
    }
    public Geometry getGeometry() {
        return geometry;
    }
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
    public Set<Precinct> getNeighbors(){
        return neighbors;
    }

    public void addNeighbor(Precinct other){
        neighbors.add(other);
        other.neighbors.add(this);
    }


}
