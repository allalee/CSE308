package app.user;

/**
 * Created by Andrew on 11/13/2018.
 */
public class UserObj {
    private String username;
    private String password;
    private String email;
    private String role;

    public UserObj(String username, String password, String email, String role){
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
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

    public String getRole(){
        return this.role;
    }
}
