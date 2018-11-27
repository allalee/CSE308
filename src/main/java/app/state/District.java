package app.state;

import app.enums.Metric;
import app.enums.Parties;
import com.vividsolutions.jts.geom.Geometry;

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

    public void recalculateBoundaryPrecincts(){
        for(Precinct p: precinctMap.values()){
            for(Precinct neighbor: p.getNeighbors()){
                if(neighbor.getDistrict().getID() != this.getID()){
                    this.borderPrecincts.add(p);
                }
            }
        }
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
        this.currentGeometry= this.currentGeometry.union(geometry);
    }
    //Difference the district geometry with the specified precinct geometry

    public void subtractFromCurrentGeometry(Geometry geometry) {
        this.currentGeometry = this.currentGeometry.difference(geometry);
    }
    public void addPopulation(int population) {
        this.population+=population;
    }
    public void removePopulation(int population) {
        this.population-=population;
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
    public void removeVotes(Parties p,int votes) {
        switch(p){
            case DEMOCRATIC:
                democraticVotes-=votes;
                break;
            case REPUBLICAN:
                republicanVotes-=votes;
                break;
        }
    }

    public int getID(){ return ID; }

    public District clone(State state){
        District clonedDistrict = new District(this.ID, state, this.geometry);
        clonedDistrict.setPopulation(this.population);
        for(Precinct precinct: this.getAllPrecincts()){
            Precinct clonedPrecinct = precinct.clone(this);
            clonedDistrict.getPrecinctMap().put(clonedPrecinct.getID(), clonedPrecinct);
        }
        return clonedDistrict;
    }
    public int getTotalVotes() {
        return democraticVotes+republicanVotes;
    }
    public double calculateVotesToWin() {
        return getTotalVotes()/2;
    }
    //Getting the losing party wasted votes
    public int getLosingWastedVotes() {
        double votesNeedToWin = calculateVotesToWin();
        if (democraticVotes>votesNeedToWin) {
            return republicanVotes;
        } else {
            return democraticVotes;
        }
    }
    //Getting the winning party wasted votes
    public int getWinningWastedVotes() {
        int losingVotes = getLosingWastedVotes();
        double votesNeedToWin =  calculateVotesToWin();
        if (democraticVotes==losingVotes) {
            return (int) (republicanVotes - votesNeedToWin);
        } else {
            return (int) (democraticVotes - votesNeedToWin);
        }
    }
    public double computeMetricValue(Metric m) {
        switch(m) {
            case POPULATION_EQUALITY:
                double idealPopulation = this.state.getIdealPopulation();
                return calculatePopulationRatio(this.population, idealPopulation);
            case COMPACTNESS:
                double area = this.currentGeometry.getArea();
                double perimeter = this.currentGeometry.getLength();
                return computePolsby(area, perimeter);
            case PARTISAN_FAIRNESS:
                int winningWastedVotes = getWinningWastedVotes();
                int losingWastedVotes = getLosingWastedVotes();
                int totalVotes = getTotalVotes();
                return (winningWastedVotes-losingWastedVotes)/totalVotes;

        }
        return 0.0;
    }

    private double computePolsby(double area, double perimeter) {
        return (4*Math.PI*area)/Math.pow(perimeter, 2);
    }
    private double calculatePopulationRatio(int population, double idealPopulation) {
        return population/idealPopulation;
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
}
