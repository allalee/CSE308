package preprocess;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(
        name = "POPULATION"
)
public class Populations implements Serializable {
    @Id
    @GeneratedValue
    @Column(
            name = "POPULATION_ID"
    )
    private int poputationId;
    @Column(
            name = "POPULATION"
    )
    private double population;
    @Column(
            name = "PRECINCT_ID"
    )
    private int precinctId;
    @Column(
            name = "DISTRICT_ID"
    )
    private int districtId;

    public Populations(double population, int precinctId, int districtId) {
        this.population = population;
        this.precinctId = precinctId;
        this.districtId = districtId;
    }

    public int getPoputationId() {
        return this.poputationId;
    }

    public void setPoputationId(int poputationId) {
        this.poputationId = poputationId;
    }

    public double getPopulation() {
        return this.population;
    }

    public void setPopulation(double population) {
        this.population = population;
    }

    public int getPrecinctId() {
        return this.precinctId;
    }

    public void setPrecinctId(int precinctId) {
        this.precinctId = precinctId;
    }

    public int getDistrictId() {
        return this.districtId;
    }

    public void setDistrictId(int districtId) {
        this.districtId = districtId;
    }

    public Populations() {
    }
}