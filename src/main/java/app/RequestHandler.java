package app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;



/**
 * Place all http ajax requests here
 *
 */

@Controller
public class HttpHandler {

        @Autowired
        SocketHandler socketHandler;

        // template of ajax handler
        @RequestMapping(value = "/helloworld", method = RequestMethod.GET)
        public @ResponseBody
        String sayhHello(){
            Test t = new Test();
            return t.print();
        }

}
