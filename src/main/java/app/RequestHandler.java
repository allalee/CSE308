package app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


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

        @RequestMapping(value = "/loadState", method = RequestMethod.POST)
        public @ResponseBody
        String sayHello(@RequestBody String stateName){
            System.out.println(stateName);
            sm.createState(stateName);
            return "Works";
        }

}
