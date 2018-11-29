package app.algorithm;

import app.state.District;
import app.state.Precinct;
import app.controllers.SocketHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by Yixiu Liu on 11/11/2018.
 */
@Service
@Scope(value = "prototype")
public class RegionGrow extends Algorithm {
    @Autowired
    SocketHandler handler;

    @Override
    void run() {

        Collection<Precinct> allPrecincts = state.getAllPrecincts();
        Collection<District> allDistricts = state.getAllDistricts();
        District dummyDistrict = new District(-1, null, null);
        for(Precinct pre: allPrecincts){
            pre.setDistrict(dummyDistrict);
        }

        // main algo loop
        ArrayDeque<Precinct> precinctsToGrow = generateSeeds(allDistricts, IterationType.Random);
        Collection<Precinct> leftOvers = allPrecincts;
        leftOvers.removeAll(precinctsToGrow);

        while(!precinctsToGrow.isEmpty() && running){
            super.sleepIfSuspended();

            // pop head
            Precinct currentPrecinct = nextPrecinct(precinctsToGrow);

            // remove already claimed neighbors
            ArrayDeque<Precinct> availableNeighbors = new ArrayDeque<>();
            for(Precinct pre: currentPrecinct.getNeighbors()){
                if( pre.getDistrict() == dummyDistrict )
                    availableNeighbors.add(pre);
            }

            //try claiming one or all (depend on algo) of the neighbors
            for(Precinct neighbor: availableNeighbors){
                Move move = new Move(neighbor.getDistrict(), currentPrecinct.getDistrict(), neighbor);
                move.execute();
                double newFunctionValue = calculateFunctionValue();
                if ( isBetter(newFunctionValue, functionValue) ){
                    functionValue = newFunctionValue;
                    addToMoveStack(move);
                    precinctsToGrow.addLast(neighbor);
                    leftOvers.remove(neighbor);
                    updateClient(move);
                }
                else {
                    move.undo();
                }
            }
        }

        running = false;
        System.out.println("Algo done");
    }

    private ArrayDeque<Precinct> generateSeeds(Collection<District> districtList, IterationType type){
        switch(type){
            case Random: return generateSeedRandom(districtList);
            default: return null;
        }
    }

    /**
     * Districts will pick their precincts, overriding the precinct's previous district
     * @param districtList
     * @return
     */
    private ArrayDeque<Precinct> generateSeedRandom(Collection<District> districtList) {
        ArrayDeque<Precinct> seedList = new ArrayDeque<>();
        for(District dis: districtList){
            Collection<Precinct> precinctsInDistrict = dis.getAllPrecincts();
            int randomIndex = new Random().nextInt( precinctsInDistrict.size() );
            for(Precinct p: precinctsInDistrict){
                if ( randomIndex<= 0 ){
                    seedList.addLast(p);
                    p.setDistrict(dis);
                    break;
                }
                randomIndex--;
            }
        }
        return seedList;
    }

    private Precinct nextPrecinct(ArrayDeque<Precinct> availablePrecincts){
        return availablePrecincts.pollFirst();
    }


    private void updateClient(Move move){
        // make JSON
        System.out.println("Sent");
        String json = "{";
        json += "\"src\":\""+move.getSrcDistrict();
        json += "\",\"dest\":\""+move.getDestDistrict();
        json += "\",\"precinct\":\""+move.getPrecinctID();
        json += "\"}";
        handler.send(json);
    }

    enum IterationType{
        Random
    }


}
