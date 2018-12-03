package app.controllers;

import app.user.UsersModel;
import gerrymandering.HibernateManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.servlet.http.HttpServletResponse;

import java.io.Serializable;


/**
 * Created by Andrew on 11/13/2018.
 */

@Controller
public class RegisterHandler {

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public String registration(@RequestParam("username") String username,
                      @RequestParam("password") String password,
                      @RequestParam("email") String email, HttpServletResponse resp) throws Throwable {

        HibernateManager hm = HibernateManager.getInstance();
        UsersModel usersModel = new UsersModel(username, password, email, "user");
        boolean persisted = hm.persistToDB(usersModel);
        System.out.println(persisted);

        return "../static/templates/login.html";
    }

}
