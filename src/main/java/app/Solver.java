package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
}
