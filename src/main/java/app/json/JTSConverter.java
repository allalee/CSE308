package app.json;

import app.state.District;
import app.state.Precinct;
import app.state.State;
import com.vividsolutions.jts.geom.Geometry;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Yixiu Liu on 11/10/2018.
 */
public class JTSConverter {

    public void buildGeometry(Precinct precinct, JSONObject coordinates) throws com.vividsolutions.jts.io.ParseException {
        GeoJsonReader reader = new GeoJsonReader();
        String coordinatesJSONString = coordinates.toJSONString();

        Geometry geometry = reader.read(coordinatesJSONString);
        precinct.setGeometry(geometry);
    }

    public void buildGeometry(District district, JSONObject coordinates) throws com.vividsolutions.jts.io.ParseException {
        GeoJsonReader reader = new GeoJsonReader();
        String coordinatesJSONString = coordinates.toJSONString();

        Geometry geometry = reader.read(coordinatesJSONString);
        district.setGeometry(geometry);
    }

    public static void buildNeighbor(Collection<Precinct> precinctCollection){
        for(Precinct p : precinctCollection) {
            for(Precinct p2 : precinctCollection) {
                if (p != p2) {
                    boolean isNeighbor = p.getGeometry().touches(p2.getGeometry());
                    if (isNeighbor) {
                        p.addNeighbor(p2);
                    }
                }
            }
        }
    }

    // Remove all below when use DB

    public void loadAndSetUpKansas(State state) throws ParseException, com.vividsolutions.jts.io.ParseException, IOException {
        List<District> districts = districtLoadAndBuild(state);
        List<Precinct> precincts = precinctLoadAndBuild();
        buildNeighbor(precincts);
        buildLink(districts,precincts);
    }

    public void buildLink(List<District> districts, List<Precinct> precincts){
        for(Precinct p: precincts){
            double maxArea = 0;
            for(District d: districts){
                double area = p.getGeometry().intersection(d.getGeometry()).getArea();
                if (area > maxArea) {
                    p.setDistrict(d);
                    maxArea = area;
                }
            }
            p.getDistrict().addPrecinct(p.getID(), p);

            if ( p.getID() == 1439424){
                System.out.println("INT compare");
                for(District d : districts){
                    if (d.getPrecinct(1439424)!=null){
                        System.out.println("FOUND in INT compare");
                    }
                    if (d.getPrecinct(new Integer(1439424))!=null){
                        System.out.println("FOUND in INTEGER OBJECT compare");
                    }
                }
            }

        }
    }


    private List<District> districtLoadAndBuild(State state) throws IOException, ParseException, com.vividsolutions.jts.io.ParseException {

        // open file
        String filename = "src/main/resources/static/geojson/kansas_districts.json";
        FileReader input = new FileReader(new File(filename));

        // parse file as JSON
        JSONParser jsonParser = new JSONParser();
        //JSONObject geoJSONObject = (JSONObject) jsonParser.parse(input);
        JSONArray districtJSONList = (JSONArray) jsonParser.parse(input);

        // loop
        GeoJsonReader reader = new GeoJsonReader();
        ArrayList<District> districtList = new ArrayList<>();
        for( Object districtJSON: districtJSONList ) {

            // read in geometry property of each precinct. geometry{ type:Polygon, coordinates:[][][] }
            JSONObject coordinatesJSON = (JSONObject) ((JSONObject) districtJSON).get("geometry");
            JSONObject properties = (JSONObject) ((JSONObject) districtJSON).get("properties");

            int id = Integer.parseInt(properties.get("GEOID").toString());
            String coordinatesJSONString = coordinatesJSON.toJSONString();

            // create
            Geometry dGeometry = reader.read(coordinatesJSONString);
            District d = new District(id, state, dGeometry);
            state.getDistrictMap().put(id, d);
            d.setGeometry(dGeometry);

            // add to list
            districtList.add(d);
        }
        input.close();
        System.out.println("DISTRICTS" + districtList.size());
        return districtList;
    }

    private List<Precinct> precinctLoadAndBuild() throws IOException, ParseException, com.vividsolutions.jts.io.ParseException {

        // open file
        //String filename = "src\\kansas_state_voting_precincts_2012.json";
        String filename = "src/main/resources/static/geojson/precinct_data/kansas_state_voting_precincts_2012.json";
        FileReader input = new FileReader(new File(filename));

        // parse file as JSON
        JSONParser jsonParser = new JSONParser();
        JSONObject geoJSONObject = (JSONObject) jsonParser.parse(input);
        JSONArray precinctJSONList = (JSONArray) geoJSONObject.get("features");

        // loop
        GeoJsonReader reader = new GeoJsonReader();
        ArrayList<Precinct> precinctList = new ArrayList<>();
        for( Object precinctJSON: precinctJSONList ) {

            // read in geometry property of each precinct. geometry{ type:Polygon, coordinates:[][][] }
            JSONObject coordinatesJSON = (JSONObject) ((JSONObject) precinctJSON).get("geometry");
            JSONObject properties = (JSONObject) ((JSONObject) precinctJSON).get("properties");

            int id = Integer.parseInt(properties.get("ID").toString());
            String coordinatesJSONString = coordinatesJSON.toJSONString();

            // create
            Geometry precinctGeometry = reader.read(coordinatesJSONString);
            Precinct precinct = new Precinct(id, precinctGeometry);
            precinct.setGeometry(precinctGeometry);

            // add to list
            precinctList.add(precinct);
        }

        input.close();
        System.out.println("PRECINCTS" + precinctList.size());
        return precinctList;

    }

}
