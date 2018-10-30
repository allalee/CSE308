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

    // connect
    con.connect = function(){
        con.socket = new SockJS(con.server)
        stomp = Stomp.over(con.socket)
        stomp.connect({}, function(frame){
            stomp.subscribe(con.room, con.handle_socket_message);
        });
        con.stomp = stomp;
    }

    // disconnect
    con.disconnect = function(){
        con.stomp.disconnect()
    }

    //handler
    con.handle_socket_message = function (message){
        console.log("Got a message: ")
        console.log(message.body);
    }

    return con;
}
connector = makeConnector();
