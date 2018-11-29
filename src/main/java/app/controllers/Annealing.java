package app.controllers;

import app.algorithm.Move;
import app.state.District;
import app.state.Precinct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Scope(value = "prototype")
public class Annealing extends Algorithm {
    @Autowired
    SocketHandler handler;
    @Override
    void run() {
        Collection<Precinct> allPrecincts = state.getAllPrecincts();
        Collection<District> allDistricts = state.getAllDistricts();
        int stagnant_iterations = 0;
        int max_stagnant = 10;
        long programEndTime = systemStartTime + 300000;
        for(District district : allDistricts){
            district.calculateBoundaryPrecincts();
        }
        Precinct previouslyMovedPrecinct = null;
        Precinct precinctToMove;
        handler.send("{\"console_log\": \"Starting algorithm...\"}");
        while(running && stagnant_iterations < max_stagnant && System.currentTimeMillis() < programEndTime){
            double startFunctionValue = functionValue;
            do  {
                precinctToMove = getPrecinctToMove(allDistricts);
            } while (previouslyMovedPrecinct == precinctToMove);
            District destDistrict = selectDestinationDistrict(precinctToMove);
            Move currentMove = new Move(precinctToMove.getDistrict(), destDistrict, precinctToMove);

            //Check to see if the move is good or not
            //If good then execute the move and update the state's data and send json back to client for updates, if bad then check threshold and revert change
            //Reconfigure the bordering precincts of a district based on the precinct that was moved
            //Recalculate objective function
            //Check if stagnant

//            for(District dis: allDistricts){    // loop thru districts and anneal neighbor districts
//                //get other district precinct AND move to this district
//                //get boundary
//                //get boundary's neighbors that has diff district
//                Collection<Precinct> boundaries = dis.getBoundaries();
//                Set<Precinct> annealTarget = new HashSet<>();
//                // get all foreign precincts touching the border
//                for(Precinct pre: boundaries){
//                    for(Precinct neighor: pre.getNeighbors()){
//                        if( neighor.getDistrict() != pre.getDistrict() ){
//                            annealTarget.add(neighor);
//                        }
//                    }
//                }
//                // anneal the foreign precincts
//                for(Precinct foreign: annealTarget){
//                    Move move = new Move(foreign.getDistrict(), dis, foreign);
//                    move.execute();
//                    double newFunctionValue = calculateFunctionValue();
//                    // if good, save the data
//                    if ( isBetter(newFunctionValue, functionValue) ){
//                        functionValue = newFunctionValue;
//                        addToMoveStack(move);
//                        updateClient(move);
//                    }
//                    else {  // if bad, undo the move
//                        move.undo();
//                    }
//                }
//            }
//
            double endFunctionValue = functionValue;
            if(isStagnant(startFunctionValue,endFunctionValue)){
                stagnant_iterations++;
            } else{
                stagnant_iterations = 0;
            }
        }
        running = false;
        System.out.println("Algo finished");
    }

    private boolean isStagnant(double oldValue, double newValue){
        double threshold = 0.01;
        return (Math.abs(oldValue - newValue) < threshold);
    }

    private void updateClient(Move move){
        System.out.println("SEND");
        handler.send(move.toString());
    }

    private Precinct getPrecinctToMove(Collection<District> dCollection){
        Random random = new Random();
        int index = random.nextInt(dCollection.size());
        District selectedDistrict = (District)dCollection.toArray()[index];
        System.out.println(selectedDistrict.getBorderPrecincts().size()); //There are no bordering percincts. Error
        int precinctIndex = random.nextInt(selectedDistrict.getBorderPrecincts().size());
        return (Precinct)selectedDistrict.getBorderPrecincts().toArray()[precinctIndex];
    }

    private District selectDestinationDistrict(Precinct precinct){
        int precinctDistrictID = precinct.getDistrict().getID();
        ArrayList<Precinct> possiblePrecincts = new ArrayList<>();
        Set<Precinct> neighbors = precinct.getNeighbors();
        for(Precinct p: neighbors){
            if(p.getDistrict().getID() != precinctDistrictID){
                possiblePrecincts.add(p);
            }
        }
        Random random = new Random();
        int index = random.nextInt(possiblePrecincts.size());
        return possiblePrecincts.get(index).getDistrict();
    }

}
