package app.controllers;

import app.algorithm.Move;
import app.enums.Property;
import app.json.GeoJsonReader;
import app.json.JsonBuilder;
import app.json.PropertiesManager;
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
    double threshold = 1; //Decaying threshold?

    @Override
    void run() {
        Collection<Precinct> allPrecincts = state.getAllPrecincts();
        Collection<District> allDistricts = state.getAllDistricts();
        Collection<Precinct> unassignedPrecincts = new ArrayList<>();
        for(Precinct p : allPrecincts){
            p.setDistrict(null);
            unassignedPrecincts.add(p);
        }
        double thresholdChanger = unassignedPrecincts.size() * 0.2;
        int incrementer = 0;
        int stagnant_iterations = 0;
        int max_stagnant = 100;
        int roundRobinCounter = 0;
        double initFuncValue = functionValue;
        //Call to the client to update all of the precincts white to denote that they are not part of a district
        handler.send("{\"default" + "\": \"" + 0 + "\"}");
        ArrayList<District> regions = generateRegions(allDistricts, unassignedPrecincts);
        //Color the starting precincts in the map
        updateClientForRegions(regions);
        Random random = new Random();
        //Start the while loop with the condition until all precincts are placed into a district
        handler.send("{\"console_log\": \"Starting algorithm...\"}");
        while(running && unassignedPrecincts.size() != 0 && max_stagnant != stagnant_iterations){
            double startFunctionValue = calculateFunctionValue();
            District currentDistrictToGrow;

            if(this.variant.equals("RR")){
                if(roundRobinCounter == regions.size()){
                    roundRobinCounter = 0;
                }
                currentDistrictToGrow = (District)regions.toArray()[roundRobinCounter];
                while(currentDistrictToGrow.getBorderPrecincts().size() == 0){
                    roundRobinCounter++;
                    if(roundRobinCounter == regions.size())
                        roundRobinCounter = 0;
                    currentDistrictToGrow = (District)regions.toArray()[roundRobinCounter];
                }
                roundRobinCounter++;
            } else {
                int i = random.nextInt(allDistricts.size());
                currentDistrictToGrow = (District) regions.toArray()[i];
                while (currentDistrictToGrow.getBorderPrecincts().size() == 0) {
                    i = random.nextInt(regions.size());
                    currentDistrictToGrow = (District) regions.toArray()[i];
                }
            }

            Precinct precinctToAdd = getPrecinctToAdd(currentDistrictToGrow, random, unassignedPrecincts);
            unassignedPrecincts.remove(precinctToAdd);
            Move move = new Move(currentDistrictToGrow, precinctToAdd);
            move.execute(true);
            calculateFunctionValue();
            if(checkThreshold(startFunctionValue, functionValue)){
                currentDistrictToGrow.calculateBoundaryPrecincts(true);
                updateClient(move);
                stagnant_iterations = 0;
            } else {
                move.undo(true);
                currentDistrictToGrow.calculateBoundaryPrecincts(true);
                functionValue = startFunctionValue;
            }
            if (isStagnant(startFunctionValue, functionValue)) {
                stagnant_iterations++;
                System.out.println(stagnant_iterations+" "+functionValue);
            } else {
                stagnant_iterations +=1;
            }
            incrementer++;
            if(thresholdChanger < incrementer){
                incrementer = 0;
                threshold *= threshold * 0.8;
            }
            stagnant_iterations += 1;
            System.out.println(unassignedPrecincts.size());
        }
        running = false;
        System.out.println("Algo done");
        handler.send("{\"console_log\": \"Initial Function Value = " + initFuncValue + "\"}");
        handler.send("{\"console_log\": \"Final Function Value = " + functionValue + "\"}");
    }

    private boolean isStagnant(double oldValue, double newValue) {
        double threshold = 0.001;
        return (Math.abs(oldValue - newValue) < threshold);
    }

    private boolean checkThreshold(double oldValue, double newValue){
        if(newValue > oldValue){
            return true;
        } else {
            return (Math.abs(oldValue-newValue) < threshold);
        }
    }

    private ArrayList<District> generateRegions(Collection<District> allDistricts, Collection<Precinct> unassignedPrecincts){
        ArrayList<District> regions = new ArrayList<>();
        ArrayList<Precinct> temp = new ArrayList<>(unassignedPrecincts);
        for(District d : allDistricts){
            Precinct seedPrecinct = getManualPrecinctSeed(d); // check for manual seed
            if(seedPrecinct == null){
                Random rnd = new Random();
                int i = rnd.nextInt(d.getAllPrecincts().size());
                seedPrecinct = (Precinct)d.getAllPrecincts().toArray()[i];
            }
            temp.remove(seedPrecinct);
            District newDistrict = new District(d.getID(), d.getState(), seedPrecinct.getGeometry());
            newDistrict.addPrecinct(seedPrecinct.getID(), seedPrecinct);
            seedPrecinct.setDistrict(newDistrict);
            newDistrict.calculateBoundaryPrecincts(true);
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
    private Precinct getPrecinctToAdd(District currentDistrictToGrow, Random random, Collection<Precinct> unassigned){
        Collection<Precinct> borderPrecincts = currentDistrictToGrow.getBorderPrecincts();
        int next = random.nextInt(borderPrecincts.size());
        Precinct p = (Precinct)borderPrecincts.toArray()[next];
//        while(!unassigned.contains(p)){ //STUCK IN THIS WHILE LOOP
//            next = random.nextInt(borderPrecincts.size());
//            p = (Precinct)borderPrecincts.toArray()[next];
//            System.out.println("ongoing");
//        }
        return p;
    }

    private void checkDoneGrowing(District district, ArrayList<District> regions){
        boolean notDone = false;
        Collection<Precinct> borderCollection = district.getBorderPrecincts();
        for(Precinct p : borderCollection){
            if(p.getDistrict() == null)
                notDone = true;
        }
        if(!notDone){
            regions.remove(district);
        }
    }

    private void updateClient(Move move){
        handler.send(move.toString(true));
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
}
