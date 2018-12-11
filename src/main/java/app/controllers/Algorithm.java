package app.controllers;

import app.algorithm.Move;
import app.election.ElectionData;
import app.enums.Metric;
import app.enums.Parties;
import app.enums.Property;
import app.json.PropertiesManager;
import app.state.District;
import app.state.Precinct;
import app.state.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope(value = "prototype")
public abstract class Algorithm{
    @Autowired
    SocketHandler handler;

    protected State state;
    protected volatile boolean running;
    protected volatile boolean paused;
    private ArrayDeque<Move> listOfMoves;
    private Thread algoThread;
    protected double functionValue;
    private HashMap<Metric, Double> weights;
    protected long systemStartTime;
    protected final long MAX_RUN_TIME;
    protected long remainingRunTime;
    private final int ONE = 1;
    private final int ZERO = 0;
    protected final String endMessage = "{\"enable_reset\": 1, \"console_log\" : \"Algo ended\"}";
    protected String variant = "";
    protected Set<Precinct> precinctSeeds;
    protected Set<District> districtSeeds;
    protected Set<District> districtExcluded;


    public Algorithm(){
        running = false;
        paused = false;
        listOfMoves = new ArrayDeque<>();
        weights = new HashMap<>();
        MAX_RUN_TIME = Integer.parseInt(PropertiesManager.get(Property.MAX_RUNTIME));
        precinctSeeds = new HashSet<>();
        districtSeeds = new HashSet<>();
        districtExcluded = new HashSet<>();
    }

    public void resetPrecinctSeeds(Set<Precinct> seeds){
        precinctSeeds.clear();
        precinctSeeds.addAll(seeds);
    }

    public void resetDistrictExcluded(Set<District> excludes){
        districtExcluded.clear();
        districtExcluded.addAll(excludes);
    }

    public void resetDistrictSeeds(Set<District> seeds){
        districtSeeds.clear();
        districtSeeds.addAll(seeds);
    }

    protected Precinct getManualPrecinctSeed(District district){
        for (Precinct seed : precinctSeeds) { // check for manually set seed
            Precinct precinct = district.getPrecinct(seed.getID());
            if (precinct != null) {
                return precinct;
            }
        }
        return null;
    }

    public Set<District> removeExcludedDistricts(Collection<District> districts){
        Set<District> notExcluded = new HashSet<>();
        for(District d: districts){
            if(!districtExcluded.contains(d)){
                notExcluded.add(d);
            }
        }
        return notExcluded;
    }

    public Set<Precinct> removeExcludedPrecincts(Collection<Precinct> precincts){
        Set<Precinct> notExcludedPrecincts = new HashSet<>();
        for(Precinct p: precincts){
            if(!districtExcluded.contains(p.getDistrict())){
                notExcludedPrecincts.add(p);
            }
        }
        return notExcludedPrecincts;
    }

    public void start(){
        if( running ){
            stop();
        }
        //systemStartTime = System.currentTimeMillis();
        remainingRunTime = MAX_RUN_TIME;
        algoThread = new Thread(()->{
            running = true;
            run();
            running = false;
            handler.send(endMessage);
        });
        algoThread.start();
    }

    public void stop(){
        running = false;
        paused = false;
        algoThread.interrupt();
    }

    public void pause(boolean pause){
        if(running) {
            paused = pause;
            System.out.println("Pause click");
        }
    }

    public void addToMoveStack(Move move){
        listOfMoves.push(move);
    }

    public void setState(State state){
        this.state = state;
    }

    public boolean isRunning(){
        return running;
    }

    public void setMetricWeights(double partisanFairness, double compactness, double populationEquality){
        weights.put(Metric.PARTISAN_FAIRNESS, partisanFairness);
        weights.put(Metric.COMPACTNESS, compactness);
        weights.put(Metric.POPULATION_EQUALITY, populationEquality);
    }

    public HashMap<Metric, Double> getWeights(){
        return weights;
    }

    public boolean isBetter(double newValue, double oldValue){
        return true;
    }

    public double calculateFunctionValue(){
        double populationEqualityValue = ONE - computeAveragePercentError();
        calculateVoteTotal();
        double partisanFairnessValue = ONE - computePartisanFairness();
        double compactnessValue = computeCompactness();
        return weights.get(Metric.POPULATION_EQUALITY) * populationEqualityValue +
                weights.get(Metric.PARTISAN_FAIRNESS) * partisanFairnessValue + weights.get(Metric.COMPACTNESS) *
                compactnessValue;
    }

    public void setInitialObjFunctionValue(double value){
        functionValue = value;
    }

    public double computeAveragePercentError(){
        int idealPopulation = state.getIdealPopulation();
        double percentError = ZERO;
        for(app.state.District district : state.getAllDistricts()){
            percentError += Math.abs((double)(district.getPopulation() - idealPopulation)/idealPopulation);
        }
        return percentError/state.getDistrictMap().size();
    }

    public double computeCompactness(){
        double totalCompactness = ZERO;
        for(District district : state.getAllDistricts()){
            totalCompactness += district.computePolsby();
        }
        return totalCompactness/state.getDistrictMap().size();
    }

    public double computePartisanFairness(){
        int totalVotes = ZERO;
        int democraticWastedVotes = ZERO;
        int republicanWastedVotes = ZERO;
        for(District district : state.getAllDistricts()){
            totalVotes += district.getTotalVotes();
            HashMap<Parties, Integer> map = district.retrieveWastedVotes();
            democraticWastedVotes += map.get(Parties.DEMOCRATIC);
            republicanWastedVotes += map.get(Parties.REPUBLICAN);
        }
        return Math.abs(((double)(democraticWastedVotes - republicanWastedVotes)/totalVotes));
    }

    public void calculateVoteTotal(){
        for(District district : state.getAllDistricts()){
            for(Precinct precinct : district.getAllPrecincts()){
                ElectionData ed = precinct.getElectionData();
                HashMap<Parties, Integer> voterDistribution = ed.getVoterDistribution();
                if(voterDistribution.containsKey(Parties.DEMOCRATIC)){
                    district.addVotes(Parties.DEMOCRATIC, voterDistribution.get(Parties.DEMOCRATIC));
                }
                if(voterDistribution.containsKey(Parties.REPUBLICAN)){
                    district.addVotes(Parties.REPUBLICAN, voterDistribution.get(Parties.REPUBLICAN));
                }
            }
        }
    }

    public double computeFunctionDistrict(District d){
        double idealPopulation = state.getIdealPopulation();
        double percentError = Math.abs((double)(d.getPopulation() - idealPopulation)/idealPopulation);
        double popEqualityValue = ONE - percentError;
        int totalVotes = d.getTotalVotes();
        HashMap<Parties, Integer> map = d.retrieveWastedVotes();
        int democraticWastedVotes = map.get(Parties.DEMOCRATIC);
        int republicanWastedVotes = map.get(Parties.REPUBLICAN);
        double partisanFairness = Math.abs(((double)(democraticWastedVotes-republicanWastedVotes)/totalVotes));
        double compactness = d.computePolsby();
        return weights.get(Metric.POPULATION_EQUALITY) * popEqualityValue +
                weights.get(Metric.PARTISAN_FAIRNESS) * partisanFairness+ weights.get(Metric.COMPACTNESS) *
                compactness;
    }

    public void setVariant(String variant){this.variant = variant;}

    abstract void run();

}
