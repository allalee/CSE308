package app.user;

import gerrymandering.HibernateManager;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Created by Andrew on 11/18/2018.
 */

@Controller
public class AdminHandler {
    int numberOfUsers;

    @RequestMapping(value="/admin", method = RequestMethod.POST)
    public String admin(@RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("email") String email, Model model) throws Throwable {
        HibernateManager hm = HibernateManager.getInstance();
        List<Object> users = hm.getAllRecords(UsersModel.class);
        numberOfUsers = users.size();
        model.addAttribute("numberOfUsers", numberOfUsers);

        return "../static/admin.html";
    }

    @RequestMapping(value="/adminEdit", method = RequestMethod.POST)
    public String adminEdit(@RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("email") String email, Model model) throws Throwable {
//        HibernateManager hm = HibernateManager.getInstance();
//        UsersModel usersModel = new UsersModel(username, password, email, "user");
//        boolean persisted = hm.persistToDB(usersModel);
//        System.out.println(persisted);

        return "../static/admin.html";
    }

}
