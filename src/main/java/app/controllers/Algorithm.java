package app.controllers;

import app.algorithm.Move;
import app.election.ElectionData;
import app.enums.Metric;
import app.enums.Parties;
import app.state.District;
import app.state.Precinct;
import app.state.State;

import java.util.ArrayDeque;
import java.util.HashMap;

public abstract class Algorithm{
    protected State state;
    protected volatile boolean running;
    private ArrayDeque<Move> listOfMoves;
    private Thread algoThread;
    protected double functionValue;
    private HashMap<Metric, Double> weights;
    protected long systemStartTime;
    private final int ONE = 1;
    private final int ZERO = 0;


    public Algorithm(){
        running = false;
        listOfMoves = new ArrayDeque<>();
        weights = new HashMap<>();
    }

    public void start(){
        if( running ){
            stop();
        }
        systemStartTime = System.currentTimeMillis();
        algoThread = new Thread(()->{
            running = true;
            run();
        });
        algoThread.start();
    }

    public void stop(){
        running = false;
        algoThread.interrupt();
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

    abstract void run();

}
