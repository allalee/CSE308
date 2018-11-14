package app.user;

import gerrymandering.HibernateManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;


/**
 * Created by Andrew on 11/13/2018.
 */

@Controller
public class RegisterHandler {

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public @ResponseBody
    void registration(@RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("email") String email) throws Throwable {

        HibernateManager hb = HibernateManager.getInstance();
        UsersModel usersModel = new UsersModel(username, password, email);
//        EntityManagerFactory emf = Persistence.createEntityManagerFactory("lions_JPA");
//        EntityManager em = emf.createEntityManager();
        hb.persistToDB(usersModel);
//        return new RedirectView("localhost:8080/login.html");
//        em.getTransaction().begin();
//        em.persist(usersModel);
//        em.getTransaction().commit();
//        em.close();
//        emf.close();
    }

}
