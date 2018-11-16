package app.user;

import gerrymandering.HibernateManager;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Controller;
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

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public @ResponseBody
    void login(@RequestParam("password") String password, @RequestParam("email") String email, HttpServletRequest req, HttpServletResponse resp) throws Throwable {

        resp.setHeader("Access-Control-Allow-Origin", "*");

        HibernateManager hm = HibernateManager.getInstance();
        List<Object> users = hm.getAllRecords(UsersModel.class);
        Iterator<Object> itr = users.iterator();

        boolean validUser = false;
        while(itr.hasNext()){
            UsersModel user = (UsersModel) itr.next();
            if(user.getEmail().equals(email) && user.getPassword().equals(password)) {
                validUser = true;
            }
        }

        if(validUser){
            //redirect to homepage with indication that user is logged in.
            //addCookie(resp, "user", email, 3600);
            HttpSession session = req.getSession(true);
            session.setAttribute("user", email);
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("text/plain");
            PrintWriter out = resp.getWriter();
            out.write("ok");
        }
        else{
            //redirect to login page stating invalid login
            resp.setCharacterEncoding("UTF-8");
            resp.setContentType("text/plain");
            PrintWriter out = resp.getWriter();
            out.write("Invalid Login Email or Password."); //message is passed as response to ajax in javascript
        }

    }

    @RequestMapping(value = "/logout", method = RequestMethod.GET)
    public @ResponseBody
    void logout(HttpServletRequest req, HttpServletResponse resp) throws Throwable {
        //removeCookie(resp, "user");
        HttpSession session = req.getSession(true);
        session.invalidate();
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

}
