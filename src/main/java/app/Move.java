package app;

/**
 * Created by Yixiu Liu on 11/11/2018.
 */
public class Move {
    private int precinctID;
    private int srcDistrict;
    private int destDistrict;
    private int objectiveValue;

    private District src;
    private District dest;
    private Precinct precinct;

    public void move(District src, District dest, Precinct precinct){
        precinct.setDistrict(dest);

        precinctID = precinct.getID();
        srcDistrict = src.getID();
        destDistrict = dest.getID();

        this.src = src;
        this.dest = dest;
        this.precinct = precinct;
    }

    public void undo(){
        precinct.setDistrict(src);
    }

    public int getSrcDistrict() {
        return srcDistrict;
    }

    public int getDestDistrict() {
        return destDistrict;
    }

    public int getPrecinctID() {
        return precinctID;
    }

}
