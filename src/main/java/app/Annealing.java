package app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@Scope(value = "prototype")
public class Annealing extends Algorithm{
    @Autowired
    SocketHandler handler;

    @Override
    void run() {
        Collection<Precinct> allPrecincts = state.getAllPrecincts();
        Collection<District> allDistricts = state.getAllDistricts();

        // stagnation is when the objective value barely changes after X iterations.
        int stagnant_iterations = 0;
        int max_stagnant = 10;
        double startFunctionValue = functionValue;
        double endFunctionValue = functionValue;

        // only stop if the score stagnanted for X amt of loops
        // does not backtrack, do we want to?
        while(running && stagnant_iterations < max_stagnant){

            // save the value for check for stagnation
            startFunctionValue = functionValue;

            for(District dis: allDistricts){    // loop thru districts and anneal neighbor districts
                //get other district precinct AND move to this district
                //get boundary
                //get boundary's neighbors that has diff district
                Collection<Precinct> boundaries = dis.getBoundaries();
                Set<Precinct> annealTarget = new HashSet<>();

                // get all foreign precincts touching the border
                for(Precinct pre: boundaries){
                    for(Precinct neighor: pre.getNeighbors()){
                        if( neighor.getDistrict() != pre.getDistrict() ){
                            annealTarget.add(neighor);
                        }
                    }
                }

                // anneal the foreign precincts
                for(Precinct foreign: annealTarget){
                    Move move = new Move(foreign.getDistrict(), dis, foreign);
                    move.execute();
                    double newFunctionValue = calculateFunctionValue();

                    // if good, save the data
                    if ( isBetter(newFunctionValue, functionValue) ){
                        functionValue = newFunctionValue;
                        addToMoveStack(move);
                        updateClient(move);
                    }
                    else {  // if bad, undo the move
                        move.undo();
                    }
                }
            }

            // ending function value
            endFunctionValue = functionValue;

            // check stagnation
            if ( isStagnant(startFunctionValue, endFunctionValue) ){
                stagnant_iterations++;
            }
            // reset counter if there were good enough improvements to the moves
            else{
                stagnant_iterations = 0;
            }
        }

        running = false;

        System.out.println("Algo finished");
    }

    private boolean isStagnant(double oldValue, double newValue){
        double threshold = 0.5;
        return Math.abs(oldValue - newValue) < threshold;
    }

    private void updateClient(Move move){
        System.out.println("SEND");
        handler.send(move.toString());
    }

}
