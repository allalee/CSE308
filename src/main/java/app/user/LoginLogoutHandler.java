package app.user;

import gerrymandering.HibernateManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Andrew on 11/13/2018.
 */

@Controller
public class LoginLogoutHandler {

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String index(HttpServletRequest req, Model model) throws Throwable {
        Cookie userCookie = getCookie(req, "user");
        String email = "";
        String username = "";
        String usertype = null;
        if(userCookie != null) {
            email = userCookie.getValue();
        }
        List<Object> users = getUsers();
        Iterator<Object> itr = users.iterator();

        //Check to see if a user is logged in through cookie.
        while(itr.hasNext()){
            UsersModel user = (UsersModel) itr.next();
            if(user.getEmail().equals(email)) {
                username = user.getUsername();
                usertype = user.getType();
                model.addAttribute("username", username);
                model.addAttribute("usertype", usertype);
            }
        }

        return "../static/index.html";
    }

//    @RequestMapping(value = "/stateConst", method = RequestMethod.GET)
//    public String stateConst(HttpServletRequest req, Model model) throws Throwable {
//
//        return "redirect:http://localhost:8080/";
//    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public String login(@RequestParam("password") String password,
                        @RequestParam("email") String email,
                        HttpServletRequest req,
                        HttpServletResponse resp, Model model) throws Throwable {

        resp.setHeader("Access-Control-Allow-Origin", "*");

        String username = "";
        String usertype = null;
        List<Object> users = getUsers();
        Iterator<Object> itr = users.iterator();

        boolean validUser = false;
        while(itr.hasNext()){
            UsersModel user = (UsersModel) itr.next();
            if(user.getEmail().equals(email) && user.getPassword().equals(password)) {
                validUser = true;
                username = user.getUsername();
                usertype = user.getType();
            }
        }

        if(validUser){
            //redirect to homepage with indication that user is logged in.
            addCookie(resp, "user", email, 3600);
            model.addAttribute("username", username);
            model.addAttribute("usertype", usertype);
            return "redirect:http://localhost:8080/";
        }
        else{
            //redirect to login page stating invalid login
            model.addAttribute("invalid","");
            return "redirect:http://localhost:8080/";
        }

    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public String logout(HttpServletRequest req, HttpServletResponse resp, Model model) throws Throwable {
        removeCookie(resp, "user");
        model.addAttribute("username", null);
        model.addAttribute("usertype",null);
        return "redirect:http://localhost:8080/";

    }



    public void addCookie(HttpServletResponse resp, String cookieName, String value, int maxAge){
        Cookie userCookie = new Cookie(cookieName, value);
        userCookie.setMaxAge(maxAge);
        resp.addCookie(userCookie);
        userCookie.setPath("/");
    }

    public void removeCookie(HttpServletResponse resp, String cookieName){
        addCookie(resp, cookieName, null, 0);
    }

    public Cookie getCookie(HttpServletRequest req, String cookieName){
        Cookie[] cookies = req.getCookies();
        if(cookies != null) {
            for(Cookie cookie : cookies){
                if(cookie.getName().equals(cookieName)){
                    return cookie;
                }
            }
        }
        return null;
    }

    public List<Object> getUsers() throws Throwable {
        HibernateManager hm = HibernateManager.getInstance();
        List<Object> users = hm.getAllRecords(UsersModel.class);
        return users;
    }
}
