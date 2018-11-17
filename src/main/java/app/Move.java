package app;

import com.vividsolutions.jts.geom.Geometry;

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

    public Move(District src, District dest, Precinct precinct) {
        this.src = src;
        this.dest = dest;
        this.precinct = precinct;
        this.precinctID = precinct.getID();
        this.srcDistrict = src.getID();
        this.destDistrict = dest.getID();
    }
    public void execute(){
        precinct.setDistrict(dest);

        Geometry precinctGeometry = precinct.getGeometry();
        dest.addToCurrentGeometry(precinctGeometry);
        src.subtractFromCurrentGeometry(precinctGeometry);

        int precinctPopulation = precinct.getPopulation();
        dest.addPopulation(precinctPopulation);
        src.removePopulation(precinctPopulation);

        ElectionData precinctVotes = precinct.getElectionData();
        int demVotes = precinctVotes.getNumVotesForDem();
        int repVotes = precinctVotes.getNumVotesForRep();
        dest.addVotes(Parties.DEMOCRAT, demVotes);
        dest.addVotes(Parties.REPUBLICAN, repVotes);
        src.removeVotes(Parties.DEMOCRAT, demVotes);
        src.removeVotes(Parties.REPUBLICAN, repVotes);

    }

    public void undo(){
        precinct.setDistrict(src);

        Geometry precinctGeometry = precinct.getGeometry();
        src.addToCurrentGeometry(precinctGeometry);
        dest.subtractFromCurrentGeometry(precinctGeometry);

        int precinctPopulation = precinct.getPopulation();
        src.addPopulation(precinctPopulation);
        dest.removePopulation(precinctPopulation);

        ElectionData precinctVotes = precinct.getElectionData();
        int demVotes = precinctVotes.getNumVotesForDem();
        int repVotes = precinctVotes.getNumVotesForRep();
        src.addVotes(Parties.DEMOCRAT, demVotes);
        src.addVotes(Parties.REPUBLICAN, repVotes);
        dest.removeVotes(Parties.DEMOCRAT, demVotes);
        dest.removeVotes(Parties.REPUBLICAN, repVotes);

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
