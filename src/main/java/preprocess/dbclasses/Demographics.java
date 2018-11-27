package preprocess.dbclasses;

import java.io.Serializable;
import java.util.HashMap;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(
        name = "DEMOGRAPHICS"
)
public class Demographics implements Serializable{
    @Id
    @GeneratedValue
    @Column (
            name = "DEMOGRAPHICS_ID"
    )
    private int demographicsID;
    @Column(
            name = "PRECINCT_ID"
    )
    private int precinctID;
    @Column(
            name = "ASIAN"
    )
    private int asian;
    @Column(
            name = "CAUCASIAN"
    )
    private int caucasian;
    @Column(
            name = "HISPANIC"
    )
    private int hispanic;
    @Column(
            name = "AFRICAN_AMERICAN"
    )
    private int african_american;
    @Column(
            name = "NATIVE_AMERICAN"
    )
    private int native_american;
    @Column(
            name = "OTHER"
    )
    private int other;

    public Demographics(int precinctID, int asian, int caucasian, int hispanic, int african_american, int native_american, int other){
        this.precinctID = precinctID;
        this.asian = asian;
        this.caucasian = caucasian;
        this.hispanic = hispanic;
        this.african_american = african_american;
        this.native_american = native_american;
        this.other = other;
    }

    public Demographics(){

    }

    public int getDemographicsID(){
        return this.demographicsID;
    }
    public int getPrecinctID(){
        return this.precinctID;
    }

    public HashMap<String, Integer> getDemographicMap(){
        HashMap<String, Integer> hm = new HashMap<>();
        hm.put("Asian", this.asian);
        hm.put("Caucasian", this.caucasian);
        hm.put("Hispanic", this.hispanic);
        hm.put("African-American", this.african_american);
        hm.put("Native-American", this.native_american);
        hm.put("Other", this.other);
        return hm;
    }

}
