package app;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yixiu Liu on 11/11/2018.
 */
public class Solver {
    private ArrayList<Algorithm> algorithmList = new ArrayList<>();
    private Algorithm currentAlgorithm;
    private State state;


    public void addAlgoirhtm(Algorithm algo){
        algorithmList.add(algo);
        currentAlgorithm = algo;
    }

    public void setState(State state){
        this.state = state;
    }

    public void run(){
        currentAlgorithm.setState(state);
        currentAlgorithm.start();
    }

    public double calculateFunctionValue(){
        double compactnessSum = 0.0;
        int numDistricts = state.getAllDistricts().size();
        for (District d : state.getAllDistricts()) {
            compactnessSum+=d.computeMetricValue(Metric.COMPACTNESS);
        }
        double normalizedCompactness = compactnessSum/numDistricts;
        return 1;
    }
}
