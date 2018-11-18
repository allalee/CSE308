package app;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
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

    // template of ajax handler
        @RequestMapping(value = "/helloworld", method = RequestMethod.GET)
        public @ResponseBody
        String sayhHello() {
            Test t = new Test();
            return t.print();
        }

        @RequestMapping(value = "/getState", method = RequestMethod.GET)
        public @ResponseBody
        String getState(@RequestParam ("stateName") String state, @RequestParam("stateID") Integer stateID) throws Throwable {
            sm.createState(state, stateID);
            return "Works";
        }

        @Autowired BeanFactory beanFactory;

//        @RequestMapping(value = "/startAlgorithm", method = RequestMethod.GET)
//        public @ResponseBody
//        String startAlgo(@RequestParam ("state_name") String stateName) throws IOException, ParseException {
//
//            sm.setActiveState(stateName);
//            State state = sm.cloneState(stateName);
//
//            System.out.println("Districts: "+state.getAllDistricts().size());
//            System.out.println("Precincts: "+ state.getAllPrecincts().size());
//
//            solver.addAlgoirhtm(beanFactory.getBean(RegionGrow.class));
//            solver.setState(state);
//            solver.run();
//
//            return "Algo started";
//        }

}
