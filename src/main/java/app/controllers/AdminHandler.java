package app.controllers;

import app.user.UsersModel;
import gerrymandering.HibernateManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Iterator;
import java.util.List;

/**
 * Created by Andrew on 11/18/2018.
 */

@Controller
public class AdminHandler {
    int numberOfUsers;
    String currentEmail = null;

    @RequestMapping(value="/admin", method = RequestMethod.GET)
    public String admin(Model model) throws Throwable {
        HibernateManager hm = HibernateManager.getInstance();
        List<Object> users = hm.getAllRecords(UsersModel.class);
        numberOfUsers = users.size();
        model.addAttribute("numberOfUsers", numberOfUsers);
        model.addAttribute("listOfUsers", users);

        return "../static/templates/admin.html";
    }

    @RequestMapping(value="/adminCurEmail", method = RequestMethod.GET)
    public void adminEmail(@RequestParam("currentEmail") String currentEmail) throws Throwable {
        this.currentEmail = currentEmail;
    }

    @RequestMapping(value="/adminEdit", method = RequestMethod.POST)
    public String adminEdit(@RequestParam("username") String username,
                            @RequestParam("password") String password,
                            @RequestParam("email") String email, Model model) throws Throwable {
        HibernateManager hm = HibernateManager.getInstance();
        List<Object> users = hm.getAllRecords(UsersModel.class);
        Iterator<Object> itr = users.iterator();
        String currentUsername = "";
        String currentPassword = "";
        if(!currentEmail.equals("")) {
            while (itr.hasNext()) {
                UsersModel user = (UsersModel) itr.next();
                if (user.getEmail().equals(currentEmail)) {
                    currentUsername = user.getUsername();
                    currentPassword = user.getPassword();
                }
            }
        }

        if(!username.equals("") && !password.equals("")) {
            UsersModel usersModel = new UsersModel(currentUsername, currentPassword, currentEmail, "user");
            boolean persisted = hm.removeFromDB(usersModel);
            currentEmail = "";
            UsersModel newUsersModel = new UsersModel(username, password, email, "user");
            hm.persistToDB(newUsersModel);
            System.out.println(persisted);
        }
        return "redirect:http://localhost:8080/admin";
    }

    @RequestMapping(value="/adminAdd", method = RequestMethod.POST)
    public String adminAdd(@RequestParam("username") String username,
                           @RequestParam("password") String password,
                           @RequestParam("email") String email, Model model) throws Throwable {
        HibernateManager hm = HibernateManager.getInstance();
        UsersModel usersModel = new UsersModel(username, password, email, "user");
        boolean persisted = hm.persistToDB(usersModel);
        System.out.println(persisted);

        return "redirect:http://localhost:8080/admin";
    }


    @RequestMapping(value="/adminDelete", method = RequestMethod.POST)
    public String adminDelete(@RequestParam("email") String email, Model model) throws Throwable {
        HibernateManager hm = HibernateManager.getInstance();
        String username = "";
        String password = "";
        List<Object> users = hm.getAllRecords(UsersModel.class);
        Iterator<Object> itr = users.iterator();

        while(itr.hasNext()) {
            UsersModel user = (UsersModel) itr.next();
            if (user.getEmail().equals(email)) {
                username = user.getUsername();
                password = user.getPassword();
            }
        }

        //only if the account exists we remove it.
        if(!username.equals("") && !password.equals("")) {
            UsersModel usersModel = new UsersModel(username, password, email, "user");
            boolean persisted = hm.removeFromDB(usersModel);
            System.out.println(persisted);
        }

        return "redirect:http://localhost:8080/admin";
    }
}
