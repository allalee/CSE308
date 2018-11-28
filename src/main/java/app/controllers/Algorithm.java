package app.controllers;

import app.algorithm.Move;
import app.enums.Metric;
import app.state.State;

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
        weights = new HashMap<>();
    }

    public void start(){
        if( running ){
            stop();
        }
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
        return 1;
    }

    public void setInitialObjFunctionValue(double value){
        functionValue = value;
    }

    abstract void run();

}
