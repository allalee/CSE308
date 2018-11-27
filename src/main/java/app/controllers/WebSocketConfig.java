package app.controllers;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * Configuration class.
 * Enables websocket and configures routing.
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {

        // enable a subscription point (any endpoint with this prefix).
        // used by client to listen in
        config.enableSimpleBroker("/client_socket");

        //this is for recieving messages
        //config.setApplicationDestinationPrefixes("/server_socket_prefix");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        // server's socket signature. used by client to open connection.
        registry.addEndpoint("/server_socket").withSockJS();
    }

}