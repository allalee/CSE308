package app;

import com.vividsolutions.jts.geom.Geometry;

import java.util.*;

public class Precinct{
    private int ID;
    private District district;
    private Set<Precinct> neighbors;
    private Geometry geometry;

    public Precinct(int ID){
        this.ID = ID;
        neighbors = new HashSet<>();
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


    public void addNeighbor(Precinct other) {
        neighbors.add(other);
        other.neighbors.add(this);
    }

    public Precinct chainClone(HashMap<Integer, District> districtMap){
        return chainClone(new HashMap<>(), districtMap);
    }

    private Precinct chainClone(HashMap<Integer, Precinct> clonedSoFar, HashMap<Integer, District> districtMap){
        Precinct clonedPrecinct = new Precinct(ID);
        clonedPrecinct.geometry = geometry;
        District clonedDistrict = districtMap.get(district.getID());// get the new cloned district
        clonedPrecinct.district = clonedDistrict;
        clonedDistrict.addPrecinct(clonedPrecinct);

        clonedSoFar.put(this.hashCode(), clonedPrecinct);

        for(Precinct other: neighbors){
            Precinct clonedNeighbor;
            if( clonedSoFar.containsKey(other.hashCode()) ){
                clonedNeighbor = clonedSoFar.get(other.hashCode());
            }
            else{
                clonedNeighbor = other.chainClone(clonedSoFar, districtMap);
            }
            clonedPrecinct.neighbors.add(clonedNeighbor);
        }
        return clonedPrecinct;
    }

}
