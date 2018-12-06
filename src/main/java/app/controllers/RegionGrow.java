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
        //Call to the client to update all of the precincts white to denote that they are not part of a district
        handler.send("{\"default" + "\": \"" + 0 + "\"}");
        ArrayList<District> regions = generateRegions(allDistricts, unassignedPrecincts);
        //Color the starting precincts in the map
        updateClientForRegions(regions);
        Random random = new Random();
        //Start the while loop with the condition until all precincts are placed into a district
        while(running && unassignedPrecincts.size() != 0){
            double startFunctionValue = calculateFunctionValue();
            int i = random.nextInt(allDistricts.size());
            District currentDistrictToGrow = (District)regions.toArray()[i];
            while(currentDistrictToGrow.getBorderPrecincts().size() == 0){
                i = random.nextInt(regions.size());
                currentDistrictToGrow = (District)regions.toArray()[i];
            }
            Precinct precinctToAdd = getPrecinctToAdd(currentDistrictToGrow, random, unassignedPrecincts);
            unassignedPrecincts.remove(precinctToAdd);
            Move move = new Move(currentDistrictToGrow, precinctToAdd);
            move.execute(true);
            currentDistrictToGrow.calculateBoundaryPrecincts(true);
            updateClient(move);
            //Add precinct the region and check if the move is good
            //Check if the move is good
            //Reconfigure the bordering precincts of the region
        }
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
        while(!unassigned.contains(p)){ //STUCK IN THIS WHILE LOOP
            next = random.nextInt(borderPrecincts.size());
            p = (Precinct)borderPrecincts.toArray()[next];
        }
        return p;
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

    enum IterationType{
        Random
    }


}
