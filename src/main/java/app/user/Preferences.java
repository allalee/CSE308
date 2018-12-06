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
        name = "PREFERENCES"
)
public class Preferences implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(
            name = "NAME"
    )
    private String name;
    @Id
    @Column(
            name = "EMAIL"
    )
    private String email;
    @Column(
            name = "POPEQUALITY"
    )
    private int popequality;
    @Column(
            name = "PARTISAN"
    )
    private int partisan;
    @Column(
            name = "COMPACTNESS"
    )
    private int compactness;

    public Preferences(String name, String email, int popequality, int partisan, int compactness) throws Exception {
        this.name = name;
        this.email = email;
        this.popequality = popequality;
        this.partisan = partisan;
        this.compactness = compactness;
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

    public int getPopequality() {
        return popequality;
    }

    public void setPopequality(int popequality) {
        this.popequality = popequality;
    }

    public int getPartisan() {
        return partisan;
    }

    public void setPartisan(int partisan) {
        this.partisan = partisan;
    }

    public int getCompactness() {
        return compactness;
    }

    public void setCompactness(int compactness) {
        this.compactness = compactness;
    }

}
