package app.user;

/**
 * Created by Andrew on 11/13/2018.
 */
public class UserObj {
    private String username;
    private String password;
    private String email;

    public UserObj(String username, String password, String email){
        this.username = username;
        this.password = password;
        this.email = email;
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
