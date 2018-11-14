package app.user;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.io.Serializable;


/**
 * Created by Andrew on 11/13/2018.
 */

@Controller
public class RegisterHandler {

    @RequestMapping(value = "/registration", method = RequestMethod.POST)
    public void registration(@RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("email") String email) throws Throwable {

        UsersModel usersModel = new UsersModel(username, password, email);
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("lions-jpa");
        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        em.persist(usersModel);
        em.getTransaction().commit();

    }

}
