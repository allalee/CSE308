package app.controllers;

import app.user.Maps;
import app.user.Preferences;
import app.user.UsersModel;
import gerrymandering.HibernateManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
            //remove account's associated preferences and maps first.
            List<Object> prefList = getUserPreferences(email);
            int index = 0;
            while(index < prefList.size()) {
                Preferences thisPreference = (Preferences) prefList.get(index);
                hm.removeFromDB(thisPreference);
                index = index + 1;
            }
            //remove account's associated maps
            List<Object> mapsList = getUserMaps(email);
            index = 0;
            while(index < mapsList.size()) {
                Maps thisMap = (Maps) mapsList.get(index);
                hm.removeFromDB(thisMap);
                index = index + 1;
            }
            UsersModel usersModel = new UsersModel(username, password, email, "user");
            boolean persisted = hm.removeFromDB(usersModel);
            System.out.println(persisted);
        }

        return "redirect:http://localhost:8080/admin";
    }


    public List<Object> getUserPreferences(String email) throws Throwable {
        HibernateManager hm = HibernateManager.getInstance();
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("email", email);
        List<Object> prefList = hm.getRecordsBasedOnCriteria(Preferences.class, criteria);
        return prefList;
    }

    public List<Object> getUserMaps(String email) throws Throwable{
        HibernateManager hm = HibernateManager.getInstance();
        Map<String, Object> criteria = new HashMap<>();
        criteria.put("email", email);
        List<Object> mapsList = hm.getRecordsBasedOnCriteria(Maps.class, criteria);
        return mapsList;
    }
}
