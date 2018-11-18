package app;

import com.google.gson.annotations.Expose;
import com.vividsolutions.jts.geom.Geometry;

import java.util.*;

public class Precinct{
    @Expose
    private int ID;
    private District district;
    @Expose
    private int districtID;
    private Set<Precinct> neighbors;
    @Expose
    private Geometry geometry;
    private double area;
    private int population;
    private ElectionData electionData;

    public Precinct(int ID, Geometry geometry){
        this.ID = ID;
        this.geometry = geometry;
        neighbors = new HashSet<>();
    }

    public int getID(){ return ID; }
    public District getDistrict(){
        return district;
    }
    public void setDistrict(District district){
        this.district = district;
        this.districtID = district.getID();
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
    public ElectionData getElectionData() {
        return electionData;
    }

    public Precinct chainClone(HashMap<Integer, District> districtMap){
        return chainClone(new HashMap<>(), districtMap);
    }

    private Precinct chainClone(HashMap<Integer, Precinct> clonedSoFar, HashMap<Integer, District> districtMap){
        Precinct clonedPrecinct = new Precinct(ID, this.geometry);
        clonedPrecinct.geometry = geometry;
        District clonedDistrict = districtMap.get(district.getID());// get the new cloned district
        clonedPrecinct.district = clonedDistrict;
        clonedDistrict.addPrecinct(clonedPrecinct.getID(), clonedPrecinct);

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
    public double getArea() {
        return area;
    }

    public int getPopulation(){
        return population;
    }

}
