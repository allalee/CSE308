package app;

import gerrymandering.HibernateManager;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
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

        @RequestMapping(value = "/loadPrecinctData", method = RequestMethod.GET)
        public @ResponseBody
        String loadPrecinctData(@RequestParam ("districtID") Integer districtID, @RequestParam("precinctID") Integer precinctID) throws Throwable {
            return sm.loadPrecinctData(districtID, precinctID);
        }

        @RequestMapping(value = "/stateConst", method = RequestMethod.GET)
        public @ResponseBody
        String getStateConst(@RequestParam ("stateName") String state, @RequestParam("stateID") int stateID) throws Throwable {
            return sm.getStateConstitution(state);
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
            for(District d : districtMap.values()){
                JTSConverter.buildNeighbor(d.getAllPrecincts());
            }
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
        //            solver.run();
            return "Algo started";
        }

}
