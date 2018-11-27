package app;

import java.util.ArrayDeque;
import java.util.HashMap;

public abstract class Algorithm {
    protected State state;
    protected volatile boolean running;
    private ArrayDeque<Move> listOfMoves;
    private Thread algoThread;
    protected double functionValue;

    private HashMap<Metric, Double> weights;


    public Algorithm(){
        running = false;
        listOfMoves = new ArrayDeque<>();
        functionValue = 0;
        weights = new HashMap<>();
    }

    public void start(){
        if( running ){
            stop();
        }
        algoThread = new Thread(()->{
            functionValue = 0;
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

    public double calculateFunctionValue(){
        double compactnessSum = 0.0;
        int numDistricts = state.getAllDistricts().size();
        for (District d : state.getAllDistricts()) {
            compactnessSum+=d.computeMetricValue(Metric.COMPACTNESS);
        }
        double normalizedCompactness = compactnessSum/numDistricts;
        return 1.0;
    }

    protected boolean isBetter(double newValue, double oldValue){
        return newValue >= oldValue;
    }

    public void setMetricWeights(double partisanFairness, double compactness, double populationEquality){
        weights.put(Metric.PARTISAN_FAIRNESS, partisanFairness);
        weights.put(Metric.COMPACTNESS, compactness);
        weights.put(Metric.POPULATION_EQUALITY, populationEquality);
    }

    public HashMap<Metric, Double> getWeights(){
        return weights;
    }

    abstract void run();
}
