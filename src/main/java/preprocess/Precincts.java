package preprocess;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import utils.Validator;

@Entity
@Table(
        name = "PRECINCT"
)
public class Precincts implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(
            name = "PRECINCT_ID"
    )
    private int precinctId;
    @Column(
            name = "CENTER_POINT"
    )
    private String centerPointJSON;
    @Column(
            name = "BOUNDARY"
    )
    private String boundaryJSON;
    @Column(
            name = "DISTRICT_ID"
    )
    private int districtId;

    public Precincts(int precinctID, int districtId, String centerPointJSON, String boundaryJSON) throws Exception {
        if (!Validator.isJSONValid(boundaryJSON)) {
            throw new Exception("boundaryJSON value is not a valid JSON");
        } else if (!Validator.isJSONValid(boundaryJSON)) {
            throw new Exception("centerPointJSON value is not a valid JSON");
        } else {
            this.precinctId = precinctID;
            this.centerPointJSON = centerPointJSON;
            this.boundaryJSON = boundaryJSON;
            this.districtId = districtId;
        }
    }

    public Precincts() {
    }

    public int getPrecinctId() {
        return this.precinctId;
    }

    public void setPrecinctId(int precinctId) {
        this.precinctId = precinctId;
    }

    public String getCenterPointJSON() {
        return this.centerPointJSON;
    }

    public void setCenterPointJSON(String centerPointJSON) {
        this.centerPointJSON = centerPointJSON;
    }

    public String getBoundaryJSON() {
        return this.boundaryJSON;
    }

    public void setBoundary(String boundaryJSON) {
        this.boundaryJSON = boundaryJSON;
    }

    public int getDistrictId() {
        return this.districtId;
    }

    public void setDistrictId(int districtId) {
        this.districtId = districtId;
    }
}