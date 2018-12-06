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
        for(District district : allDistricts){
            district.calculateBoundaryPrecincts();
        }
        Precinct previouslyMovedPrecinct = null;
        Precinct precinctToMove;
        handler.send("{\"console_log\": \"Starting algorithm...\"}");
        while(running && stagnant_iterations < max_stagnant && remainingRunTime > 0){
            if (paused) {
                System.out.println("Algorithm Paused...");
                try { Thread.sleep(500); } catch (InterruptedException e) {}
                continue;
            }

            long startTime = System.currentTimeMillis();
            double startFunctionValue = functionValue;

            // find src, dest
            do  {
                precinctToMove = getPrecinctToMove(allDistricts);
            } while (previouslyMovedPrecinct == precinctToMove);
            District destDistrict = selectDestinationDistrict(precinctToMove);
            District srcDistrict = precinctToMove.getDistrict();

            // check if geometry will be split before making the irrevertible move
            srcDistrict.removePrecinct(precinctToMove);
            srcDistrict.getBorderPrecincts().remove(precinctToMove);
            destDistrict.addPrecinct(precinctToMove.getID(), precinctToMove);
            destDistrict.getBorderPrecincts().add(precinctToMove);

            // combine all illegalmoves
            boolean illegalMoveForSure = destDistrict.isCutoff();
            illegalMoveForSure |= srcDistrict.isCutoff();
            //illegalMoveForSure |= touchedEnough;

            if(illegalMoveForSure){
                // this is a very bad move, do not move. Clean up everything used in calculation
                destDistrict.removePrecinct(precinctToMove);
                destDistrict.getBorderPrecincts().remove(precinctToMove);
                srcDistrict.addPrecinct(precinctToMove.getID(), precinctToMove);
                srcDistrict.getBorderPrecincts().add(precinctToMove);
                System.out.println("Bad move on: " + precinctToMove.getID());
            }
            else{
                Move currentMove = new Move(srcDistrict, destDistrict, precinctToMove);
                currentMove.execute();
                functionValue = calculateFunctionValue();

                if (checkThreshold(startFunctionValue, functionValue)) {
                    updateClient(currentMove);
                } else {
                    currentMove.undo();
                }

                if (isStagnant(startFunctionValue, functionValue)) {
                    stagnant_iterations++;
                    System.out.println(stagnant_iterations+" "+functionValue);
                } else {
                    stagnant_iterations = 0;
                }
                startFunctionValue = functionValue;

                previouslyMovedPrecinct = precinctToMove;
                srcDistrict.calculateBoundaryPrecincts();
                destDistrict.calculateBoundaryPrecincts();
            }


            long deltaTime = System.currentTimeMillis() - startTime;
            remainingRunTime -= deltaTime;
        }
        running = false;
        System.out.println("Algo finished");
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

    private Set<Precinct> used = new HashSet<>();
    private Precinct getPrecinctToMove(Collection<District> dCollection){
        int totalPrecincts = 0;
        for(District d: dCollection){
            totalPrecincts += d.getBorderPrecincts().size();
        }
        if (used.size() >= totalPrecincts ) used.clear();
        for(District d: dCollection){
            for(Precinct p : d.getBorderPrecincts()){
                if(!used.contains(p)){
                    used.add(p);
                    return p;
                }
            }
        }


        Random random = new Random();
        int index = random.nextInt(dCollection.size());
        District selectedDistrict = (District)dCollection.toArray()[index];
        int precinctIndex = random.nextInt(selectedDistrict.getBorderPrecincts().size());
        return (Precinct)selectedDistrict.getBorderPrecincts().toArray()[precinctIndex];

//        Precinct thisSideBorder = (Precinct)selectedDistrict.getBorderPrecincts().toArray()[precinctIndex];
//        Precinct otherSide = null;
//        for(Precinct neighbor : thisSideBorder.getNeighbors()){
//            if(neighbor.getDistrict().getID() != thisSideBorder.getDistrict().getID()){
//                otherSide = neighbor;
//            }
//        }
//        if(otherSide==null) System.out.println("How can this bee");
//        return otherSide;
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
