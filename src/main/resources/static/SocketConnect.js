/**
connect(): connect socket.
disconnect(): disconnect socket.
handler: handle incoming messages, override this with your custom handler.
*/
function makeConnector(){
    con = {}
    con.server = "/server_socket"
    con.room = "/client_socket/algorithm"
    con.socket = null;
    con.stomp = null;
    con.message_queue = [];
    con.is_reading = false;
    con.reading_interval = 200; //Time is in milliseconds
    con.message_pointer = 0;

    // connect
    con.connect = function(){
        con.socket = new SockJS(con.server)
        stomp = Stomp.over(con.socket)
        stomp.connect({}, function(frame){
            stomp.subscribe(con.room, con.handle_socket_message);
        });
        stomp.debug = null;
        con.stomp = stomp;
    }

    // disconnect
    con.disconnect = function(){
        con.stomp.disconnect()
    }

    //handler
    con.handle_socket_message = function (message){
        var body = $.parseJSON(message.body)
        con.message_queue.push(body)
    }

//    con.process_message = function(){
//        //var message_body = con.message_queue.shift()
//        if ( con.message_pointer < con.message_queue.length ){
//        var message_body = con.message_queue[con.message_pointer++];
//        if (message_body != undefined)
//            dynamic_color_changer.color(message_body);
//            //color_district(message_body.precinct, 'red')
//            }
//    }

    con.process_message = function(){
        var console = document.getElementById("console")
        var message_body = con.message_queue.shift()
        if(message_body != undefined){
            console.appendChild(document.createElement("br"))
            console.append(message_body["console_log"])
        }
//        console.log(message_body)
//        if(con.message_pointer < con.message_queue.length){
//            var message_body = con.message_queue[con.message_pointer++];
//            if(message_body != undefined){
//                console.log(message_body)
//            }
//        }
    }

    con.start_reading = function(){
        con.is_reading = true;
        con.timer = setInterval(
        con.process_message,
        con.reading_interval);
    }

    con.stop_reading = function(){
        con.is_reading = false;
        clearInterval(con.timer);
    }

    con.clear_message = function(){
        con.message_queue = []
    }

    return con;
}
