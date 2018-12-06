package app.user;

import utils.Validator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * Created by Andrew on 12/4/2018.
 */
@Entity
@Table(
        name = "MAPS"
)
public class Maps implements Serializable{
    private static final long serialVersionUID = 1L;

    @Id
    @Column(
            name = "NAME"
    )
    private String name;

    @Column(
            name = "EMAIL"
    )
    private String email;

    @Column(
            name = "SAVEDMAP"
    )
    private String savedmap;

    @Column(
            name = "STATE_ID"
    )
    private int stateId;

    @Column(
            name = "INDEX"
    )
    private int index;

    public Maps(String name, String email, String savedmapJSON, int stateId, int index) throws Exception {
            this.name = name;
            this.email = email;
            this.savedmap = savedmapJSON;
            this.stateId = stateId;
            this.index = index;
    }

    public Maps(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSavedmap() {
        return savedmap;
    }

    public void setSavedmap(String savedmap) {
        this.savedmap = savedmap;
    }

    public int getStateId() {
        return stateId;
    }

    public void setStateId(int stateId) {
        this.stateId = stateId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

}
