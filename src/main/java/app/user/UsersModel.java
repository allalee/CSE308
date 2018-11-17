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
            name = "EMAIL"
    )
    private String email;

    @Column(
            name = "USERNAME"
    )
    private String username;

    @Column(
            name = "PASSWORD"
    )
    private String password;

    @Column(
            name = "ROLE"
    )
    private String role;

    public UsersModel(String username, String password, String email, String role) throws Exception {

        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
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

    public String getType() { return this.role; }
}