package app.controllers;

import app.algorithm.Move;
import app.algorithm.Solver;
import app.json.JTSConverter;
import app.state.District;
import app.state.Precinct;
import app.state.State;
import app.user.Maps;
import com.vividsolutions.jts.JTSVersion;
import com.vividsolutions.jts.geom.Geometry;
import gerrymandering.HibernateManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.util.*;


/**
 * Place all http ajax requests here
 *
 */

@RestController
public class RequestHandler {
        private StateManager sm = new StateManager();
        private Solver solver = new Solver();

        @Autowired
        SocketHandler socketHandler;

    public RequestHandler() throws Exception {
    }
        @RequestMapping(value = "/getState", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
        public @ResponseBody
        String getState(@RequestParam ("stateName") String state, @RequestParam("stateID") Integer stateID) throws Throwable {
            return sm.createState(state, stateID);
        }

        @RequestMapping(value = "/saveMap", method = RequestMethod.GET)
        @ResponseStatus(value = HttpStatus.OK)
        public
        void saveMap(HttpServletRequest req, @RequestParam ("name") String name) throws Throwable {
            Cookie userCookie = getCookie(req, "user");
            String email = "";
            if(userCookie != null) {
                email = userCookie.getValue();
            }
            if(email!="" && name!="") {
                sm.saveMap(email, name);
            }
            //sm.saveMap(email, name);
        }

        @RequestMapping(value = "/deleteMap", method = RequestMethod.GET)
        @ResponseStatus(value = HttpStatus.OK)
        public
        void deleteMap(HttpServletRequest req, @RequestParam ("name") String name) throws Throwable {
            Cookie userCookie = getCookie(req, "user");
            String email = "";
            if(userCookie != null) {
                email = userCookie.getValue();
            }
            sm.deleteMap(email, name);
        }

        @RequestMapping(value = "/loadPrecinctData", method = RequestMethod.GET)
        public @ResponseBody
        String loadPrecinctData(@RequestParam ("districtID") Integer districtID, @RequestParam("precinctID") Integer precinctID) throws Throwable {
            return sm.loadPrecinctData(districtID, precinctID);
        }

        @RequestMapping(value = "/getOriginal", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
        public @ResponseBody
        String getOriginal() {
            return sm.getOriginalPrecinctsMap();
        }

        @RequestMapping(value = "/loadMap", method = RequestMethod.GET, produces = "application/json;charset=utf-8")
        public @ResponseBody
        String loadMap(HttpServletRequest req, @RequestParam ("name") String mapName) throws Throwable{
            Cookie userCookie = getCookie(req, "user");
            String email = "";
            if(userCookie != null) {
                email = userCookie.getValue();
            }
            return sm.loadMap(email, mapName);
        }

        @RequestMapping(value = "/stateConst", method = RequestMethod.GET)
        public @ResponseBody
        String getStateConst(@RequestParam ("stateName") String state, @RequestParam("stateID") int stateID) throws Throwable {
            return sm.getStateConstitution(state);
        }

        @RequestMapping(value = "/getDebugInfo", method = RequestMethod.GET)
        public @ResponseBody
        String getDebugInfo(@RequestParam ("src") Integer district, @RequestParam("precinct") Integer precinct ) throws Throwable {
            if(sm.getClonedState()==null){
                sm.cloneState(sm.getCurrentState().getName());
                JTSConverter.buildNeighbor(sm.getClonedState().getAllPrecincts());
            }
            State cloneState = sm.getClonedState();
            District d = cloneState.getDistrict(district);
            Precinct p = null;
            for(District dis : cloneState.getAllDistricts()){
                p = dis.getPrecinct(precinct);
                if(p!=null)
                    break;
            }

            // all border precincts
            cloneState.getDistrict(district).calculateBoundaryPrecincts();

            Set<Precinct> borders = new HashSet<>();
            boolean isCutoff = cloneState.getDistrict(district).getCutOff(borders);
            String borderjson = "\"border\":[";
            for(Precinct pre : borders) {
                borderjson += pre.getID() +",";
            }
            borderjson = borderjson.substring(0, borderjson.length()-1);
            borderjson += "]";

            // is district cut off
            String isCut = "\"cut\":"+isCutoff;

            // is precinct a border
            String isBorder = "\"border\":" + borders.contains(p);

            // overlap info between district and precinct
            Geometry intersection = d.getGeometry().intersection(p.getGeometry());
            double area = intersection.getArea();
            double perimeter = intersection.getLength();
            System.out.println("PERI" + perimeter);
            System.out.println("AREA" + area);
            String overlapjson =
                    "\"overlap\":" +
                            "{\"area\":"+area+
                            ",\"perimeter\":"+perimeter+"}";

            // count initial island precincts
            System.out.println("Islands: "+d.gatherInitIslandPrecincts().size());



            String finalJson = "{";
            finalJson += borderjson;
            finalJson += ",";
            finalJson += isCut;
            finalJson += ",";
            finalJson += isBorder;
            finalJson += ",";
            finalJson += overlapjson;
            finalJson += "}";
            return finalJson;
        }

        @RequestMapping(value = "/manualMove", method = RequestMethod.GET)
        public @ResponseBody
        String tempMove(@RequestParam ("src") Integer src, @RequestParam("dest") Integer dest, @RequestParam("precinct") Integer precinct, @RequestParam("lock") Boolean lock, @RequestParam ("popEqual") Double popEqualityMetric, @RequestParam("partFairness") Double partFairnessMetric, @RequestParam("compactness") Double compactnessMetric) throws Throwable {
            System.out.println("inputs are: " + src+" "+ dest+" "+ precinct);
            if(sm.getClonedState() == null) {
                sm.cloneState(sm.getCurrentState().getName());
                sm.loadElectionData();
                JTSConverter.buildNeighbor(sm.getClonedState().getAllPrecincts());
            }
            State currentState = sm.getClonedState();
            if(solver.getCurrentAlgorithm() == null)
                solver.addAlgorithm(beanFactory.getBean(Annealing.class));
            solver.setState(sm.getClonedState());
            solver.setFunctionWeights(partFairnessMetric/100, compactnessMetric/100, popEqualityMetric/100);
            solver.initAlgorithm();

            Precinct p = null;
            for(District d : currentState.getAllDistricts()){
                p = d.getPrecinct(precinct);
                if(p!=null)
                    break;
            }

            // error checks
            if(p == null){
               return "{ \"value\" : \"-1\", " +
                       "\"valid\" : false, " +
                       "\"message\" : \"invalid precinct\" }";
            }
            currentState.getDistrict(src).gatherInitIslandPrecincts();
            currentState.getDistrict(dest).gatherInitIslandPrecincts();

            currentState.getDistrict(src).calculateBoundaryPrecincts();
            currentState.getDistrict(dest).calculateBoundaryPrecincts();
//
//            boolean isBorder = currentState.getDistrict(src).getBorderPrecincts().contains(p);
//            System.out.println("is border: "+  isBorder);
            // move
            Move move = new Move(currentState.getDistrict(src), currentState.getDistrict(dest), p);
            move.execute();

            currentState.getDistrict(src).calculateBoundaryPrecincts();
            currentState.getDistrict(dest).calculateBoundaryPrecincts();

            double functionValue = solver.calculateFunctionValue();
            System.out.println("Value is: "+functionValue);

            boolean cutOff = currentState.getDistrict(src).isCutoff();
            cutOff |= currentState.getDistrict(dest).isCutoff();
            System.out.println("is cut off: "+cutOff);

            // undo if it is not a locking move
            if(!lock || cutOff) {
                move.undo();
//                cutOff = currentState.getDistrict(src).isCutoff();
//                System.out.println("is cut off: "+cutOff);
                currentState.getDistrict(src).calculateBoundaryPrecincts();
                currentState.getDistrict(dest).calculateBoundaryPrecincts();
            }

            if(cutOff){
                return  "{\"value\" : \"-1\", " +
                        "\"valid\" : false, " +
                        "\"message\" : \"Cannot create a disjoin district\" }";
            }

//            Set<Precinct> borders = new HashSet<>();
//            currentState.getDistrict(src).getCutOff(borders);
//            String json = "[";
//            for(Precinct pre : borders) {
//                json += pre.getID() +",";
//
//            }
//            json = json.substring(0, json.length()-1);
//            json += "]";
//
//            return json;

            return  "{ \"value\" : \""+functionValue+"\", " +
                    "\"valid\" : true, " +
                    "\"message\" : \"move value is: "+functionValue+"\" }";
                    //"\"message\" : \"cut off: "+cutOff+"\" }";
        }


        @Autowired BeanFactory beanFactory;
        @Autowired SocketHandler handler;

        @RequestMapping(value = "/startAlgorithm", method = RequestMethod.POST)
        public @ResponseBody
        String startAlgo(@RequestParam("algorithmType") String algorithmType, @RequestParam ("popEqual") Double popEqualityMetric, @RequestParam("partFairness") Double partFairnessMetric, @RequestParam("compactness") Double compactnessMetric, @RequestBody String requestBody ) throws Throwable {
            handler.send("{\"console_log\":\"Server received connection...\"}");
            if(sm.getClonedState() == null) {
                sm.cloneState(sm.getCurrentState().getName());
                sm.loadElectionData();
                JTSConverter.buildNeighbor(sm.getClonedState().getAllPrecincts());
            }
            //sm.cloneState(sm.getCurrentState().getName());
            handler.send("{\"console_log\":\"Building precinct neighbors...\"}");
            HashMap<Integer, District> districtMap = sm.getClonedState().getDistrictMap();
            //JTSConverter.buildNeighbor(sm.getClonedState().getAllPrecincts());
            handler.send("{\"console_log\":\"Retrieving election data...\"}");
            //sm.loadElectionData();
            handler.send("{\"console_log\":\"Setting up algorithm...\"}");
            switch(algorithmType){
                case "Simulated Annealing":
                    solver.addAlgorithm(beanFactory.getBean(Annealing.class));
                    break;
                case "Region Growing":
                    solver.addAlgorithm(beanFactory.getBean(RegionGrow.class));
                    break;
                case "Region Growing Variant":
                    solver.addAlgorithm(beanFactory.getBean(RegionGrow.class));
                    solver.setVariant("RR");
                    break;
                case "Simulated Annealing Variant":
                    solver.addAlgorithm(beanFactory.getBean(Annealing.class));
                    solver.setVariant("DL");
                    break;
            }
            solver.setState(sm.getClonedState());
            solver.setFunctionWeights(partFairnessMetric/100, compactnessMetric/100, popEqualityMetric/100);
            solver.initAlgorithm();
            State state = sm.getClonedState();

            Set<Precinct> precinctSeeds = new HashSet<>();
            Set<District> districtSeeds = new HashSet<>();
            Set<District> districtsExcluded = new HashSet<>();

            JSONParser parser = new JSONParser();
            JSONObject body = (JSONObject)parser.parse(requestBody);
            JSONArray precinctSeedList = (JSONArray)body.get("precinct_seeds");
            JSONArray districtSeedList = (JSONArray)body.get("district_seeds");
            JSONArray districtExcludeList = (JSONArray)body.get("excludedDistricts");
            for(Object precinctIDString : precinctSeedList){
                Integer id = Integer.parseInt((String)precinctIDString);
                precinctSeeds.add(state.getPrecinct(id));
            }
            for(Object districtIDString : districtSeedList){
                Integer id = Integer.parseInt((String)districtIDString);
                districtSeeds.add(state.getDistrict(id));
            }
            for(Object districtIDString : districtExcludeList){
                Integer id = Integer.parseInt((String)districtIDString);
                districtsExcluded.add(state.getDistrict(id));
            }
            solver.getCurrentAlgorithm().resetPrecinctSeeds(precinctSeeds);
            solver.getCurrentAlgorithm().resetDistrictSeeds(districtSeeds);
            solver.getCurrentAlgorithm().resetDistrictExcluded(districtsExcluded);

            solver.run();
            return "Algo started";
        }

        @RequestMapping(value = "/stopAlgorithm", method = RequestMethod.GET)
        public @ResponseBody
        String stopAlgorithm(){
            if(solver!=null)
                solver.stop();

            return "";
        }

        @RequestMapping(value = "/pauseAlgorithm", method = RequestMethod.GET)
        public @ResponseBody
        String pauseAlgorithm(){
            if(solver!=null)
                solver.pause(true);

            return "";
        }

        @RequestMapping(value = "/unpauseAlgorithm", method = RequestMethod.GET)
        public @ResponseBody
        String unpauseAlgorithm(){
            if(solver!=null)
                solver.pause(false);

            return "";
        }


    @RequestMapping(value = "/loadSavedMaps", method = RequestMethod.GET)
    public @ResponseBody
    String loadSavedMaps(HttpServletRequest req,@RequestParam("currentStateName") String stateName) throws Throwable {
        HibernateManager hm = HibernateManager.getInstance();
        Cookie userCookie = getCookie(req, "user");
        String email = "";
        if(userCookie != null) {
            email = userCookie.getValue();
        }


        Map<String, Object> criteriaState = new HashMap<>();
        criteriaState.put("name", stateName);
        List<Object> states = hm.getRecordsBasedOnCriteria(gerrymandering.model.State.class, criteriaState);
        gerrymandering.model.State state = (gerrymandering.model.State) states.get(0);
        int state_id = state.getStateId();
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("email", email);
        criteria.put("state_id", state_id);


        List<Object> savedmapList = hm.getRecordsBasedOnCriteria(Maps.class, criteria);
        ArrayList<String> userMapNames = new ArrayList<>();
        int index = 0;
        while (index < savedmapList.size()) {
            Maps thisMap = (Maps) savedmapList.get(index);
            if(!userMapNames.contains(thisMap.getName())) {
                userMapNames.add(thisMap.getName());
            }
            index++;
        }
        JSONObject savedMapsJSON = new JSONObject();
        savedMapsJSON.put("names", userMapNames);
        String savedMapsJSONString = savedMapsJSON.toString();
        return savedMapsJSONString;
    }

    public Cookie getCookie(HttpServletRequest req, String cookieName){
        Cookie[] cookies = req.getCookies();
        if(cookies != null) {
            for(Cookie cookie : cookies){
                if(cookie.getName().equals(cookieName)){
                    return cookie;
                }
            }
        }
        return null;
    }

}
