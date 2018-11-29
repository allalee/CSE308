package app.controllers;


import app.json.PropertiesManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Starting point of application.
 */
@SpringBootApplication
public class App{

	public static void main(String[] args){
		PropertiesManager.init("src/main/resources/static/settings/RequestHandlerXML.xml");
		SpringApplication.run(App.class, args);
	}
}
