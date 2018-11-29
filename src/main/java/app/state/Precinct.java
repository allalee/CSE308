package app.state;

import app.election.ElectionData;
import app.enums.Ethnicity;
import com.vividsolutions.jts.geom.Geometry;

import java.util.*;

public class Precinct{
    private int ID;
    private District district;
    private Set<Precinct> neighbors;
    private Geometry geometry;
    private double area;
    private int population;
    private ElectionData electionData;
    private HashMap<Ethnicity, Integer> demographics;

    public Precinct(int ID, Geometry geometry){
        this.ID = ID;
        this.geometry = geometry;
        this.neighbors = new HashSet<>();
        this.demographics = new HashMap<>();
        this.electionData = new ElectionData();
        this.area = geometry.getArea();
    }
  
    public void addNeighbor(Precinct other) {
        neighbors.add(other);
        other.neighbors.add(this);
    }

    public void addDemographic(Ethnicity ethn, Integer pop){
        demographics.put(ethn, pop);
    }

    public Precinct clone(District dist){
        Precinct clonedPrecinct = new Precinct(this.ID, this.geometry);
        clonedPrecinct.setPopulation(this.population);
        return clonedPrecinct;
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

    public void setPopulation(int pop){
        this.population = pop;
    }

    public int getPopulation(){
        return population;
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

    public HashMap<Ethnicity, Integer> getDemographics(){ return demographics;}

    public ElectionData getElectionData() {
        return electionData;
    }

}
