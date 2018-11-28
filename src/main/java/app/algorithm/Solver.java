package app.algorithm;

import app.controllers.Algorithm;
import app.election.ElectionData;
import app.enums.Metric;
import app.enums.Parties;
import app.state.District;
import app.state.Precinct;
import app.state.State;

import java.util.ArrayList;
import java.util.HashMap;

public class Solver {
    private ArrayList<Algorithm> algorithmList = new ArrayList<>();
    private Algorithm currentAlgorithm;
    private State state;
    private double objectiveFunctionValue;

    public Solver(){
        objectiveFunctionValue = 0;
        state = null;
    }

    public void addAlgorithm(Algorithm algo){
        algorithmList.add(algo);
        currentAlgorithm = algo;
        System.out.println(algo);
    }

    public void setState(State state){
        this.state = state;
    }

    public void run(){
        currentAlgorithm.start();
    }

    public void setFunctionWeights(double partisanFairness, double compactness, double populationEquality){
        currentAlgorithm.setMetricWeights(partisanFairness, compactness, populationEquality);
    }

    public void initAlgorithm(){
        double populationEqualityValue = 1 - computeAveragePercentError();
        calculateVoteTotal();
        double partisanFairnessValue = 1 - computeAveragePartisanFairness();
        objectiveFunctionValue = currentAlgorithm.getWeights().get(Metric.POPULATION_EQUALITY)*populationEqualityValue;
        currentAlgorithm.setInitialObjFunctionValue(objectiveFunctionValue);
    }

    private double computeAveragePercentError(){
        int idealPopulation = state.getIdealPopulation();
        double percentError = 0;
        for(app.state.District district : state.getAllDistricts()){
            percentError += Math.abs((double)(district.getPopulation() - idealPopulation)/idealPopulation);
        }
        return percentError/state.getDistrictMap().size();
    }

    private double computeAveragePartisanFairness(){
        return 0;
    }

    private void calculateVoteTotal(){
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

    public double calculateFunctionValue(){
        return 1;
    }
}
