package app.controllers;

import app.algorithm.Move;
import app.algorithm.Solver;
import app.json.JTSConverter;
import app.state.District;
import app.state.Precinct;
import app.state.State;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;


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

            State currentState = sm.getCurrentState();
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
            boolean destIsNeighbor = false;
            for(Precinct neighbor : p.getNeighbors()){
                if(neighbor.getDistrict().getID() == dest)
                    destIsNeighbor = true;
            }
            if(!destIsNeighbor){
                return "{ \"value\" : \"-1\", " +
                        "\"valid\" : false, " +
                        "\"message\" : \"precinct not adjacent to the district\" }";
            }

            // move
            Move move = new Move(currentState.getDistrict(src), currentState.getDistrict(dest), p);
            move.execute();
            double functionValue = 0;

            // undo if it is not a locking move
            if(!lock)
                move.undo();

            return  "{ \"value\" : \""+functionValue+"\", " +
                    "\"valid\" : true, " +
                    "\"message\" : \"move value is: "+functionValue+"\" }";
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
}
