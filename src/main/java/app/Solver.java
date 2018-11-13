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
        return currentAlgorithm.calculateFunctionValue();
    }
}
