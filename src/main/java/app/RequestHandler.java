package app;

import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;


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
            sm.createState(state, stateID);
            return "Works";
        }

}
