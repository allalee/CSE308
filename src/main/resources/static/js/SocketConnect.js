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
    con.reading_interval = 1;
    con.message_pointer = 0;

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

    con.onMessage = function(func){
        con.process_message = func
    }

    con.pop_and_read = function(){
        var message_body = con.message_queue.shift()
        if(message_body != undefined){
            con.process_message(message_body)
        }
    }

    con.start_reading = function(){
        con.is_reading = true;
        con.timer = setInterval(
        con.pop_and_read,
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
