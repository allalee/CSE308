package app.algorithm;

import app.state.State;

import java.util.ArrayList;

public class Solver {
    private ArrayList<Algorithm> algorithmList = new ArrayList<>();
    private Algorithm currentAlgorithm;
    private State state;


    public void addAlgorithm(Algorithm algo){
        algorithmList.add(algo);
        currentAlgorithm = algo;
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

    public double calculateFunctionValue() {
        return 1;
        //return currentAlgorithm.calculateFunctionValue();
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
