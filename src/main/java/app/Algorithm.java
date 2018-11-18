package app;

import java.util.ArrayDeque;

/**
 * Created by Yixiu Liu on 11/11/2018.
 */
public abstract class Algorithm {
    protected State state;
    protected volatile boolean running;
    private ArrayDeque<Move> listOfMoves;
    private Thread algoThread;
    protected double functionValue;

    public Algorithm(){
        running = false;
        listOfMoves = new ArrayDeque<>();
        functionValue = 0;
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

    public double calculateFunctionValue(){
        return 1;
    }

    protected boolean isBetter(double newValue, double oldValue){
        return newValue >= oldValue;
    }

    abstract void run();
}
