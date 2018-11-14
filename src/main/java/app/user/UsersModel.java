package app.user;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by Andrew on 11/13/2018.
 */

@Entity
@Table(
        name = "USERS"
)
public class UsersModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(
            name = "USERNAME"
    )
    private String username;

    @Column(
            name = "PASSWORD"
    )
    private String password;

    @Column(
            name = "EMAIL"
    )
    private String email;

    public UsersModel(String username, String password, String email) throws Exception {

        this.username = username;
        this.password = password;
        this.email = email;

    }

    public UsersModel() {
    }

    public String getUsername(){
        return this.username;
    }

    public String getPassword(){
        return this.password;
    }

    public String getEmail(){
        return this.email;
    }
}