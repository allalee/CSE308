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
    private double objectiveFunctionValue;

    public Solver(){
        objectiveFunctionValue = 0;
    }

    public void addAlgorithm(Algorithm algo){
        algorithmList.add(algo);
        currentAlgorithm = algo;
    }

    public void setState(State state){
        currentAlgorithm.setState(state);
    }

    public void run(){
        currentAlgorithm.start();
    }

    public void setFunctionWeights(double partisanFairness, double compactness, double populationEquality){
        currentAlgorithm.setMetricWeights(partisanFairness, compactness, populationEquality);
    }

    public void initAlgorithm(){
        double populationEqualityValue = 1 - currentAlgorithm.computeAveragePercentError();
        currentAlgorithm.calculateVoteTotal();
        double partisanFairnessValue = 1 - currentAlgorithm.computePartisanFairness();
        double compactnessValue = currentAlgorithm.computeCompactness();
        objectiveFunctionValue = currentAlgorithm.getWeights().get(Metric.POPULATION_EQUALITY) * populationEqualityValue +
                currentAlgorithm.getWeights().get(Metric.PARTISAN_FAIRNESS) * partisanFairnessValue + currentAlgorithm.getWeights().get(Metric.COMPACTNESS) *
                compactnessValue;
        System.out.println(objectiveFunctionValue);
        currentAlgorithm.setInitialObjFunctionValue(objectiveFunctionValue);
    }

    public void pause(){
        currentAlgorithm.pause();
    }

    public void unpause(){
        currentAlgorithm.unpause();
    }

    public void stop(){
        currentAlgorithm.stop();
    }
}
