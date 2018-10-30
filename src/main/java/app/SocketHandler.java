package app;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Socket handler
 *
 * IMPORTANT: Do not instantiate this object.
 * Use "@Autowired SocketHandler handler;" to obtian the object.
 *
 */
@Controller
public class SocketHandler {

    private String room = "/client_socket/algorithm";

    @Autowired
    private SimpMessagingTemplate template;

    public void send(String json){
        template.convertAndSend(room, json);
    }

}
