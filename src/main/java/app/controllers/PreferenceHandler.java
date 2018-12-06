package app.controllers;

import app.user.Preferences;
import gerrymandering.HibernateManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Andrew on 12/4/2018.
 */
@Controller
public class PreferenceHandler {
    @RequestMapping(value="/savePreferences", method = RequestMethod.GET)
    public void savePref(HttpServletRequest req,
                         @RequestParam("prefName") String name,
                         @RequestParam("popEqual") int popequality,
                         @RequestParam("partFairness") int partisan,
                         @RequestParam("compactness") int compactness) throws Throwable{
        String email = "";
        Cookie userCookie = getCookie(req, "user");
        if(userCookie != null) {
            email = userCookie.getValue();
        }
        HibernateManager hm = HibernateManager.getInstance();
        if(email != "" && name != "") {
            Preferences pref = new Preferences(name, email, popequality, partisan, compactness);
            List<Object> prefList = hm.getAllRecords(Preferences.class);
            Iterator<Object> itr = prefList.iterator();
            while (itr.hasNext()) {
                Preferences currentPref = (Preferences) itr.next();
                //IF EMAIL AND NAME EXISTS IN THIS TABLE, THEN DELETE AND PERSIST NEW ONE
                if (currentPref.getName().equals(name) && currentPref.getEmail().equals(email)) {
                    return;
                }
            }
            //NO SAVED DATA OF THE SAME NAME AND EMAIL, SO JUST PERSIST
            boolean persisted = hm.persistToDB(pref);
            System.out.println(persisted);
        }
    }

    @RequestMapping(value="/loadPreferences", method = RequestMethod.GET)
    public void loadPref(HttpServletRequest req, @RequestParam("name") String name) throws Throwable {
        String email = "";
        Cookie userCookie = getCookie(req, "user");
        if (userCookie != null) {
            email = userCookie.getValue();
        }
        HibernateManager hm = HibernateManager.getInstance();
        if (email != "" && name != "") {
            Map<String, Object> criteria = new HashMap<>();
            criteria.put("name", name);
            criteria.put("email", email);
            List<Object> prefList = hm.getRecordsBasedOnCriteria(Preferences.class, criteria);
            Iterator itr = prefList.iterator();
            int index = 0;
            while(itr.hasNext()){
                Preferences thisPreference = (Preferences) prefList.get(index);
                if(thisPreference.getName().equals(name)){
                    //LOAD TO FRONT END

                }
            }
        }
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

}
