package app;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yixiu Liu on 11/11/2018.
 */
public class Solver {
    private List<Algorithm> algorithmList = new ArrayList<>();
    private Algorithm currentAlgorithm;

    public void addAlgoirhtm(Algorithm algo){
        algorithmList.add(algo);
    }

    public void run(){
        currentAlgorithm.run();
    }
}
