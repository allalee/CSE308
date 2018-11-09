package app;

import com.vividsolutions.jts.geom.Geometry;

public class Precinct {
    private int ID;
    private Geometry geometry;
    private District district;

    public Precinct(int ID, Geometry geometry){
        this.ID = ID;
        this.geometry = geometry;
    }

    public void setDistrict(District d){
        this.district = d;
    }

    public Geometry getGeometry() {
        return geometry;
    }
}
