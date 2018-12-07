package app.controllers;

import app.algorithm.Move;
import app.enums.Property;
import app.json.PropertiesManager;
import app.state.District;
import app.state.Precinct;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.TopologyException;
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
        Collection<District> allDistricts = state.getAllDistricts();
        int stagnant_iterations = 0;
        int max_stagnant = Integer.parseInt(PropertiesManager.get(Property.STAGNANT_ITERATION));
        double initFuncValue = functionValue;
        //Calculate boundary precincts which are precincts in the district that border another district
        for (District district : allDistricts) {
            district.calculateBoundaryPrecincts();
        }
        handler.send("{\"console_log\": \"Starting algorithm...\"}");
        while (running && stagnant_iterations < max_stagnant && remainingRunTime > 0) {
            if (paused) {
                System.out.println("Algorithm Paused...");
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                }
                continue;
            }
            long startTime = System.currentTimeMillis();
            double startFunctionValue = calculateFunctionValue();
            District districtToModify = getRandomDistrict(allDistricts);
            Precinct neighboringPrecinctToAdd = getNeighborToAnneal(districtToModify.getBorderPrecincts());
            Move currentMove = new Move(neighboringPrecinctToAdd.getDistrict(), districtToModify, neighboringPrecinctToAdd);
            currentMove.execute();
            calculateFunctionValue();
            if (checkThreshold(startFunctionValue, functionValue)) {
                updateClient(currentMove);
            } else {
                currentMove.undo();
                functionValue = startFunctionValue;
            }
            if (isStagnant(startFunctionValue, functionValue)) {
                stagnant_iterations++;
                System.out.println(stagnant_iterations+" "+functionValue);
            } else {
                stagnant_iterations = 0;
            }
            long deltaTime = System.currentTimeMillis() - startTime;
            remainingRunTime -= deltaTime;
        }
        running = false;
        handler.send("{\"console_log\": \"Initial Function Value = " + initFuncValue + "\"}");
        handler.send("{\"console_log\": \"Final Function Value = " + functionValue + "\"}");
    }

    private boolean isStagnant(double oldValue, double newValue){
        double threshold = 0.001;
        return (Math.abs(oldValue - newValue) < threshold);
    }

    private boolean checkThreshold(double oldValue, double newValue){
        double threshold = Double.parseDouble(PropertiesManager.get(Property.ANNEALINGTHRESHOLD));
        if(newValue > oldValue){
            return true;
        } else {
            return (Math.abs(oldValue-newValue) < threshold);
        }
    }

    private void updateClient(Move move){
        handler.send(move.toString());
    }

    private District getRandomDistrict(Collection<District> dCollection){
        Random random = new Random();
        int index = random.nextInt(dCollection.size());
        return (District)dCollection.toArray()[index];
    }

    private Precinct getNeighborToAnneal(Collection<Precinct> borderingPrecincts){
        Random random = new Random();
        int index = random.nextInt(borderingPrecincts.size());
        return (Precinct)borderingPrecincts.toArray()[index];
    }

    //Get a random boundary precinct from another district
    private Precinct getPrecinctToMove(Collection<District> dCollection){
        Random random = new Random();
        int index = random.nextInt(dCollection.size());
        District selectedDistrict = (District)dCollection.toArray()[index]; //Retrieve a random district to have their precinct moved.
        int precinctIndex = random.nextInt(selectedDistrict.getBorderPrecincts().size()); //Select a random bordering precinct
        return (Precinct)selectedDistrict.getBorderPrecincts().toArray()[precinctIndex]; //Return the bordering precinct to be moved to another adjacent district
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

    private void reconfigureBoundaries(Precinct precinct, District oldDistrict){
        //Remove the precinct from the boundary set of old district
        oldDistrict.getBorderPrecincts().remove(precinct);
        //Add the precinct to the boundary set of new district
        precinct.getDistrict().getBorderPrecincts().add(precinct);
        //Check the neighbors of the moved precinct to see if they are a boundary
        Set<Precinct> pNeighbors = precinct.getNeighbors();
        for(Precinct neighbor : pNeighbors){
            for(Precinct neighborsNeighbor : neighbor.getNeighbors()){
                if(neighbor.getDistrict().getID() != neighborsNeighbor.getDistrict().getID()){ //If the neighbor is now a boundary precinct, add it to the set in district
                    neighbor.getDistrict().getBorderPrecincts().add(neighbor);
                    break;
                } else {
                    neighbor.getDistrict().getBorderPrecincts().remove(neighbor); //If the neighbor is not a boundary precinct, remove it from the set in the district
                }
            }
        }
    }

}
