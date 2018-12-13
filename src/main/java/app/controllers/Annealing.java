package app.controllers;

import app.algorithm.Move;
import app.enums.Metric;
import app.enums.Property;
import app.json.PropertiesManager;
import app.state.District;
import app.state.Precinct;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.TopologyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.text.DecimalFormat;
import java.util.*;

@Service
@Scope(value = "prototype")
public class Annealing extends Algorithm {
    @Autowired
    SocketHandler handler;
    @Override
    void run() {
        Collection<District> allDistricts = state.getAllDistricts();
        allDistricts = removeExcludedDistricts(allDistricts);
        if(allDistricts.size() <= 0)    // if all districts are excluded, end algo
            return;

        int stagnant_iterations = 0;
        int max_stagnant = Integer.parseInt(PropertiesManager.get(Property.STAGNANT_ITERATION));
        double initFuncValue = 1*functionValue;
        double startFunctionValue = initFuncValue;
        resetBest();
        updateBest(startFunctionValue);
        //Calculate boundary precincts which are precincts in the district that border another district
        for (District district : allDistricts) {
            district.calculateBoundaryPrecincts();
            district.gatherInitIslandPrecincts();
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
            //double startFunctionValue = calculateFunctionValue();
            District districtToModify;
            if(this.variant.equals("DL")){
                districtToModify = findLowestFunctionDistrict(allDistricts);
            } else {
                districtToModify = getRandomDistrict(allDistricts);
            }
            districtToModify.calculateBoundaryPrecincts();
            //Precinct neighboringPrecinctToAdd = getNeighborToAnneal(districtToModify.getBorderPrecincts());
            Set<Precinct> availableBorders = removeExcludedPrecincts(districtToModify.getBorderPrecincts());
            if(availableBorders.size() <= 0){   // if all precincts are from exlucded district, next iter
                stagnant_iterations++;
                continue;
            }
            Precinct neighboringPrecinctToAdd = getNeighborToAnneal(availableBorders);
            District targetDistrict = neighboringPrecinctToAdd.getDistrict();
            targetDistrict.calculateBoundaryPrecincts();

            Move currentMove = null;
            boolean invalidMove = true;
            try {
                currentMove = new Move(neighboringPrecinctToAdd.getDistrict(), districtToModify, neighboringPrecinctToAdd);
                currentMove.execute();
                functionValue = calculateFunctionValue();
                invalidMove = districtToModify.isCutoff() || targetDistrict.isCutoff();
            }catch (com.vividsolutions.jts.geom.TopologyException | IllegalArgumentException e){
                System.out.println("Merge problem");
            }

            System.out.println(districtToModify.getID() + " "+targetDistrict.getID());
            System.out.println("IS valid: "+!invalidMove);

            if (!invalidMove && checkThreshold(startFunctionValue, functionValue)) {
                updateClient(currentMove);
                System.out.println("Send");
            } else {
                if(currentMove!=null)
                    currentMove.undo();
                //functionValue = startFunctionValue;
            }
            if (isStagnant(startFunctionValue, functionValue)) {
                stagnant_iterations++;
                System.out.println(stagnant_iterations+" "+functionValue);
            } else {
                stagnant_iterations = 0;
            }
            System.out.println("this iter - start: "+startFunctionValue+" end: "+functionValue);

            startFunctionValue = functionValue;
            updateBest(functionValue);

            long deltaTime = System.currentTimeMillis() - startTime;
            remainingRunTime -= deltaTime;
        }
        long totalRunTime = MAX_RUN_TIME-remainingRunTime;
        summary(initFuncValue,functionValue,totalRunTime);
        DecimalFormat df = new DecimalFormat("#.###");
        String init = df.format(Algorithm.normalize(initFuncValue));
        String fin = df.format(Algorithm.normalize(functionValue));
        handler.send("{\"console_log\": \"Initial Function Value = " + init + "\"}");
        handler.send("{\"console_log\": \"Final Function Value = " + fin + "\"}");
        int demoDistrictsToRepub = 0;
        for(District d: allDistricts){
            if(d.getDemocraticVotes() > d.getRepublicanVotes()){
                demoDistrictsToRepub += 1;
            }
        }
        for(District d: state.getAllDistricts()){
            if(!allDistricts.contains(d)){
                if(d.getDemocraticVotes() > d.getRepublicanVotes()){
                    demoDistrictsToRepub += 1;
                }
            }
        }
        if(state.getAllDistricts().size() - demoDistrictsToRepub < demoDistrictsToRepub){
            int repubDistrict = state.getAllDistricts().size() - demoDistrictsToRepub;
            handler.send("{\"console_log\": \"Democratic wins: " + demoDistrictsToRepub + " districts to " + repubDistrict + "\"}");
        } else {
            int repubDistrict = state.getAllDistricts().size() - demoDistrictsToRepub;
            handler.send("{\"console_log\": \"Republican wins: " + repubDistrict + " districts to " + demoDistrictsToRepub + "\"}");
        }
    }

//    private boolean isStagnant(double oldValue, double newValue){
//        double threshold = 0.0001;
//        return (Math.abs(oldValue - newValue) < threshold);
//    }
//
    private boolean checkThreshold(double oldValue, double newValue){
        double threshold = Double.parseDouble(PropertiesManager.get(Property.ANNEALINGTHRESHOLD));
        if(newValue > oldValue){
            return true;
        } else {
            return (Math.abs(oldValue-newValue) < threshold);
        }
    }


    private double bestValue = 0;
    private void resetBest(){
        bestValue = 0;
    }
    private void updateBest(double newValue){
        bestValue = newValue > bestValue ? newValue : bestValue;
    }
    private boolean isStagnant(double oldValue, double newValue){
        return newValue <= bestValue;
    }
//    private boolean checkThreshold(double oldValue, double newValue){
//        double threshold = Double.parseDouble(PropertiesManager.get(Property.ANNEALINGTHRESHOLD));
//        if(newValue > bestValue){
//            bestValue = newValue;
//            return true;
//        } else {
//            return Math.abs(bestValue - newValue) < threshold;
//        }
//    }

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

    private District findLowestFunctionDistrict(Collection<District> allDistricts){
        District worstDistrict = (District) allDistricts.toArray()[0];
        double worstScore = this.computeFunctionDistrict(worstDistrict);
        for(District d: allDistricts){
            double score = this.computeFunctionDistrict(d);
            if(score < worstScore){
                worstDistrict = d;
                worstScore = score;
            }
        }
        return worstDistrict;
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
    public void summary(double initValue, double finalValue, long runTime) {
        String algoName = "Simulated Annealing";
        String algoVariant = this.variant;
        if(algoVariant.equals("DL")) {
            algoVariant = "Lowest District Score";
        } else {
            algoVariant = "Random";
        }
        double partisanWeight = getWeights().get(Metric.PARTISAN_FAIRNESS);
        double compactWeight = getWeights().get(Metric.COMPACTNESS);
        double popWeight = getWeights().get(Metric.POPULATION_EQUALITY);
        System.out.println("Algorithm: " + algoName);
        System.out.println("State: " + state.getName());
        System.out.println("Variant: " + algoVariant);
        System.out.println("Partisan Fairness Weight: " + partisanWeight);
        System.out.println("Compactness Weight: " + compactWeight);
        System.out.println("Population Equality Weight: " + popWeight);
        System.out.println("Total Run Time: " + runTime +"ms");
        System.out.println("Initial Value: " + initValue);
        System.out.println("Final Value: " + finalValue);
    }

}
