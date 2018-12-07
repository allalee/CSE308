package app.state;

import app.enums.Metric;
import app.enums.Parties;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.simplify.VWSimplifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class District{
    private int ID;
    private State state;
    private HashMap<Integer, Precinct> precinctMap;
    private Set<Precinct> borderPrecincts;
    protected Geometry geometry; //Set once for finding precinct in district
    private Geometry currentGeometry; //For calculating area and perimeter
    private int population; //Population
    private int democraticVotes;
    private int republicanVotes;


    public District(int ID, State state, Geometry geometry){
        this.ID = ID;
        this.state = state;
        this.geometry = geometry;
        this.currentGeometry = (Geometry) geometry.clone();
        precinctMap = new HashMap<>();
        borderPrecincts = new HashSet<>();
    }

    public Set<Precinct> getBoundaries(){
        return borderPrecincts;
    }

    public void calculateBoundaryPrecincts(){
        this.borderPrecincts.clear();
        for(Precinct p: precinctMap.values()){
            for(Precinct neighbor: p.getNeighbors()){
                if(neighbor.getDistrict().getID() != this.getID()){
                    this.borderPrecincts.add(neighbor);
                }
            }
        }
    }

    public void calculateBoundaryPrecincts(boolean regionGrowing){
        this.borderPrecincts.clear();
        for(Precinct p: precinctMap.values()){
            for(Precinct neighbor: p.getNeighbors()){
                if(neighbor.getDistrict() == null){
                    this.borderPrecincts.add(neighbor);
                }
            }
        }
    }

    public boolean isCutoff(){
        int numBorders = borderPrecincts.size();
        Precinct beginPrecinct = borderPrecincts.iterator().next();
        Set<Precinct> iteratedPrecincts = new HashSet<>();
        int numReached = numBordersReachable(beginPrecinct, iteratedPrecincts);
        return numReached != numBorders;
    }

    private int numBordersReachable(Precinct current, Set<Precinct> iteratedPrecincts){
        int reachedBorders = 1;
        iteratedPrecincts.add(current);
        for(Precinct neighbor: current.getNeighbors()){
            if(!iteratedPrecincts.contains(neighbor) && borderPrecincts.contains(neighbor))
                reachedBorders += numBordersReachable(neighbor, iteratedPrecincts);
        }
        return reachedBorders;
    }

    public Precinct getPrecinct(int id){
        return precinctMap.get(id);
    }

    public void addPrecinct(int ID, Precinct precinct){
        precinctMap.put(ID, precinct);
    }

    public void removePrecinct(Precinct precinct){
        removePrecinct(precinct.getID());
    }

    public void removePrecinct(int ID){
        precinctMap.remove(ID);
    }

    public Collection<Precinct> getAllPrecincts(){
        return precinctMap.values();
    }

    public Geometry getGeometry() {
        return geometry;
    }

    //Temporary
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
        this.currentGeometry = (Geometry) geometry.clone();
    }

    //Merges the district geometry with the specified precinct geometry
    public void addToCurrentGeometry(Geometry geometry) {
//        this.currentGeometry = VWSimplifier.simplify(currentGeometry, 0.001);
//        geometry = VWSimplifier.simplify(geometry, 0.001);
        this.currentGeometry = this.currentGeometry.union(geometry);
    }
    //Difference the district geometry with the specified precinct geometry

    public void subtractFromCurrentGeometry(Geometry geometry) {
//        this.currentGeometry = VWSimplifier.simplify(currentGeometry, 0.001);
//        geometry = VWSimplifier.simplify(geometry, 0.001);
        this.currentGeometry = this.currentGeometry.difference(geometry);
    }
    public void addPopulation(int population) {
        this.population+=population;
    }

    public void addVotes(Parties p, int votes) {
        switch(p){
            case DEMOCRATIC:
                democraticVotes+=votes;
                break;
            case REPUBLICAN:
                republicanVotes+=votes;
                break;
        }
    }

    public int getID(){ return ID; }

    public District clone(State state){
        District clonedDistrict = new District(this.ID, state, this.geometry);
        clonedDistrict.setPopulation(this.population);
        for(Precinct precinct: this.getAllPrecincts()){
            Precinct clonedPrecinct = precinct.clone(this);
            clonedPrecinct.setDistrict(clonedDistrict);
            clonedDistrict.getPrecinctMap().put(clonedPrecinct.getID(), clonedPrecinct);
        }
        return clonedDistrict;
    }
    public int getTotalVotes() {
        return democraticVotes+republicanVotes;
    }

    public int calculateVotesToWin() {
        return getTotalVotes()/2;
    }

    public HashMap<Parties, Integer> retrieveWastedVotes(){
        HashMap<Parties, Integer> map = new HashMap<>();
        int targetVotes = calculateVotesToWin();
        if(democraticVotes >= targetVotes){
            map.put(Parties.DEMOCRATIC, democraticVotes - targetVotes);
            map.put(Parties.REPUBLICAN, republicanVotes);
        } else {
            map.put(Parties.REPUBLICAN, republicanVotes - targetVotes);
            map.put(Parties.DEMOCRATIC, democraticVotes);
        }
        return map;
    }

    public double computePolsby() {
        return (4*Math.PI*this.geometry.getArea())/Math.pow(this.geometry.getLength(), 2);
    }

    public void setPopulation(int population){
        this.population = population;
    }
    public int getPopulation(){
        return this.population;
    }

    public HashMap<Integer, Precinct> getPrecinctMap(){
        return this.precinctMap;
    }

    public int getDemocraticVotes(){
        return this.democraticVotes;
    }

    public int getRepublicanVotes(){
        return this.republicanVotes;
    }

    public Set<Precinct> getBorderPrecincts(){
        return this.borderPrecincts;
    }

    public void setState(State state){
        this.state = state;
    }

    public State getState(){
        return this.state;
    }
}
