package app.controllers;

import app.algorithm.Move;
import app.algorithm.Solver;
import app.json.JTSConverter;
import app.state.District;
import app.state.Precinct;
import app.state.State;
import com.vividsolutions.jts.JTSVersion;
import com.vividsolutions.jts.geom.Geometry;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;


/**
 * Place all http ajax requests here
 *
 */

@Controller
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
            sm.saveMap(email, name); //REMEMBER TO CHANGE THIS TO MODIFIED ONE
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

        @RequestMapping(value = "/stateConst", method = RequestMethod.GET)
        public @ResponseBody
        String getStateConst(@RequestParam ("stateName") String state, @RequestParam("stateID") int stateID) throws Throwable {
            return sm.getStateConstitution(state);
        }

        @RequestMapping(value = "/manualMove", method = RequestMethod.GET)
        public @ResponseBody
        String tempMove(@RequestParam ("src") Integer src, @RequestParam("dest") Integer dest, @RequestParam("precinct") Integer precinct, @RequestParam("lock") Boolean lock) throws Throwable {
            System.out.println("inputs are: " + src+" "+ dest+" "+ precinct);
            if(sm.getClonedState() == null) {
                sm.cloneState(sm.getCurrentState().getName());
                JTSConverter.buildNeighbor(sm.getClonedState().getAllPrecincts());
            }
            State currentState = sm.getClonedState();
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
//            boolean destIsNeighbor = false;
//            for(Precinct neighbor : p.getNeighbors()){
//                if(neighbor.getDistrict().getID() == dest) {
//                    destIsNeighbor = true;
//                    break;
//                }
//            }
//            if(!destIsNeighbor){
//                return "{ \"value\" : \"-1\", " +
//                        "\"valid\" : false, " +
//                        "\"message\" : \"precinct not adjacent to the district\" }";
//            }
//            Geometry intersection = n.getGeometry().intersection(p.getGeometry());
//            System.out.println("PERI" + intersection.getLength());
//            System.out.println("AREA" + intersection.getArea());
            currentState.getDistrict(src).calculateBoundaryPrecincts();
            currentState.getDistrict(dest).calculateBoundaryPrecincts();
            System.out.println("before mov:");
            System.out.println("src: "+currentState.getDistrict(src).getPrecinctMap().size());
            System.out.println("dest: "+currentState.getDistrict(dest).getPrecinctMap().size());
//
//            boolean isBorder = currentState.getDistrict(src).getBorderPrecincts().contains(p);
//            System.out.println("is border: "+  isBorder);
            // move
            Move move = new Move(currentState.getDistrict(src), currentState.getDistrict(dest), p);
            move.execute();
            currentState.getDistrict(src).calculateBoundaryPrecincts();
            currentState.getDistrict(dest).calculateBoundaryPrecincts();

            System.out.println("after mov:");
            System.out.println("src: "+currentState.getDistrict(src).getPrecinctMap().size());
            System.out.println("dest: "+currentState.getDistrict(dest).getPrecinctMap().size());

            double functionValue = 0;

//            if(currentState.getDistrict(src).isCutoff() || currentState.getDistrict(dest).isCutoff()) {
//                System.out.println("cuts off");
//            }

            boolean cutOff = currentState.getDistrict(src).isCutoff();
            System.out.println("is cut off: "+cutOff);

            // undo if it is not a locking move
            if(!lock) {
                move.undo();
//                cutOff = currentState.getDistrict(src).isCutoff();
//                System.out.println("is cut off: "+cutOff);
                currentState.getDistrict(src).calculateBoundaryPrecincts();
                currentState.getDistrict(dest).calculateBoundaryPrecincts();
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

        @RequestMapping(value = "/startAlgorithm", method = RequestMethod.GET)
        public @ResponseBody
        String startAlgo(@RequestParam("algorithmType") String algorithmType, @RequestParam ("popEqual") Double popEqualityMetric, @RequestParam("partFairness") Double partFairnessMetric, @RequestParam("compactness") Double compactnessMetric ) throws Throwable {
            handler.send("{\"console_log\":\"Server received connection...\"}");
            sm.cloneState(sm.getCurrentState().getName());
            handler.send("{\"console_log\":\"Building precinct neighbors...\"}");
            HashMap<Integer, District> districtMap = sm.getClonedState().getDistrictMap();
            JTSConverter.buildNeighbor(sm.getClonedState().getAllPrecincts());
            handler.send("{\"console_log\":\"Retrieving election data...\"}");
            sm.loadElectionData();
            handler.send("{\"console_log\":\"Setting up algorithm...\"}");
            switch(algorithmType){
                case "Simulated Annealing":
                    solver.addAlgorithm(beanFactory.getBean(Annealing.class));
                    break;
                case "Region Growing":
                    solver.addAlgorithm(beanFactory.getBean(RegionGrow.class));
                    break;
            }
            solver.setState(sm.getClonedState());
            solver.setFunctionWeights(partFairnessMetric/100, compactnessMetric/100, popEqualityMetric/100);
            solver.initAlgorithm();
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
