package app.controllers;

import app.algorithm.Move;
import app.json.GeoJsonReader;
import app.json.JsonBuilder;
import app.state.District;
import app.state.Precinct;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.io.ParseException;
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

        Collection<Precinct> unassignedPrecincts = new ArrayList<>();
        for(Precinct p : allPrecincts){
            p.setDistrict(null);
            unassignedPrecincts.add(p);
        }
        //Call to the client to update all of the precincts white to denote that they are not part of a district
        handler.send("{\"default" + "\": \"" + 0 + "\"}");
        ArrayList<District> regions = generateRegions(allDistricts, unassignedPrecincts);
        //Color the starting precincts in the map
        updateClientForRegions(regions);
        //Start the while loop with the condition until all precincts are done growing
        Random random = new Random();
        while(running && allPrecincts.size() != 0){
            int i = random.nextInt(allDistricts.size()); //randomly select a district for algorithm to use
            District currentDistrictToGrow = (District)regions.toArray()[i];
            currentDistrictToGrow.calculateBoundaryPrecincts();
            System.out.println(currentDistrictToGrow.getBorderPrecincts().size());
        }


//        District dummyDistrict = new District(-1, null, null);
//        for(Precinct pre: allPrecincts){
//            pre.setDistrict(dummyDistrict);
//        }
//
//        //Initial set up
//        ArrayDeque<Precinct> precinctsToGrow = generateSeeds(allDistricts, IterationType.Random);
//        Collection<Precinct> leftOvers = allPrecincts;
//        leftOvers.removeAll(precinctsToGrow);
//
//        while(!precinctsToGrow.isEmpty() && running){
//            Precinct currentPrecinct = nextPrecinct(precinctsToGrow);
//
//            // remove already claimed neighbors
//            ArrayDeque<Precinct> availableNeighbors = new ArrayDeque<>();
//            for(Precinct pre: currentPrecinct.getNeighbors()){
//                if( pre.getDistrict() == dummyDistrict )
//                    availableNeighbors.add(pre);
//            }
//
//            //try claiming one or all (depend on algo) of the neighbors
//            for(Precinct neighbor: availableNeighbors){
//                Move move = new Move(neighbor.getDistrict(), currentPrecinct.getDistrict(), neighbor);
//                move.execute();
//                double newFunctionValue = calculateFunctionValue();
//                if ( isBetter(newFunctionValue, functionValue) ){
//                    functionValue = newFunctionValue;
//                    addToMoveStack(move);
//                    precinctsToGrow.addLast(neighbor);
//                    leftOvers.remove(neighbor);
//                    updateClient(move);
//                }
//                else {
//                    move.undo();
//                }
//            }
//        }

        running = false;
        System.out.println("Algo done");
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

    private ArrayList<District> generateRegions(Collection<District> allDistricts, Collection<Precinct> unassignedPrecincts){
        ArrayList<District> regions = new ArrayList<>();
        ArrayList<Precinct> temp = new ArrayList<>(unassignedPrecincts);
        for(District d : allDistricts){
            Random rnd = new Random();
            int i = rnd.nextInt(d.getAllPrecincts().size());
            Precinct seedPrecinct = (Precinct)d.getAllPrecincts().toArray()[i];
            temp.remove(seedPrecinct);
            District newDistrict = new District(d.getID(), d.getState(), seedPrecinct.getGeometry());
            newDistrict.addPrecinct(seedPrecinct.getID(), seedPrecinct);
            seedPrecinct.setDistrict(newDistrict);
            regions.add(newDistrict);
        }
        unassignedPrecincts.clear();
        unassignedPrecincts.addAll(temp);
        HashMap<Integer, District> newMap = new HashMap<>();
        for(District region: regions){
            newMap.put(region.getID(), region);
        }
        state.setDistrictMap(newMap);
        return regions;
    }

    private void findBorderPrecincts(){

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

    private void updateClientForRegions(ArrayList<District> regions){
        StringBuilder builder = new StringBuilder("{\"seeds\": {");
        for(District region: regions){
            Precinct p = (Precinct)region.getAllPrecincts().toArray()[0];
            builder.append("\"" + p.getID() + "\": \"" + region.getID() + "\",");
        }
        builder.setCharAt(builder.length() -1, '}');
        builder.append('}');
        handler.send(builder.toString());
    }

    enum IterationType{
        Random
    }


}
