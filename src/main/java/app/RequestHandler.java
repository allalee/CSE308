package app;

import org.json.simple.parser.ParseException;
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

        @Autowired
        SocketHandler socketHandler;

        // template of ajax handler
        @RequestMapping(value = "/helloworld", method = RequestMethod.GET)
        public @ResponseBody
        String sayhHello(){
            Test t = new Test();
            return t.print();
        }

        @RequestMapping(value = "/getState", method = RequestMethod.GET)
        public @ResponseBody
        String getState(@RequestParam ("stateName") String state, @RequestParam("stateID") Integer stateID) throws IOException, ParseException {
            //sm.createState(state, stateID);
            return "Works";
        }

        @RequestMapping(value = "/loadKansas", method = RequestMethod.GET)
        public @ResponseBody
        String loadKansas() throws IOException, ParseException,com.vividsolutions.jts.io.ParseException {

            State kansas = new State("Kansas", 123);
            JTSConverter jtsConverter = new JTSConverter();

            jtsConverter.loadAndSetUpKansas(kansas);

            sm.addState("Kansas_2", kansas);
            sm.setState("Kansas_2");

            return "Kansas Loaded";
        }

        @RequestMapping(value = "/getNeighbor", method = RequestMethod.GET)
        public @ResponseBody
        String getState(@RequestParam ("id") String precinctId) throws IOException, ParseException {
            Set<Precinct> neighbors = sm.getNeighborPrecincts(Integer.parseInt(precinctId));

            String neighborJSON = "[";
            for(Precinct p : neighbors)
                neighborJSON += p.getID() + ",";
            neighborJSON += "-1]";

            return neighborJSON;
        }

}
