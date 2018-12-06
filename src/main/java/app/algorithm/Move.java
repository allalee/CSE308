package app.algorithm;

import app.state.District;
import app.election.ElectionData;
import app.state.Precinct;
import app.enums.Parties;
import com.vividsolutions.jts.geom.Geometry;

import java.util.HashMap;

/**
 * Created by Yixiu Liu on 11/11/2018.
 */
public class Move {
    private int precinctID;
    private int srcDistrict;
    private int destDistrict;

    private District src;
    private District dest;
    private Precinct precinct;

    //Annealing Constructor
    public Move(District src, District dest, Precinct precinct) {
        this.src = src;
        this.dest = dest;
        this.precinct = precinct;
        this.precinctID = precinct.getID();
        this.srcDistrict = src.getID();
        this.destDistrict = dest.getID();
    }
    //Region Growing Constructor
    public Move(District dest, Precinct precinct){
        this.dest = dest;
        this.precinct = precinct;
        this.precinctID = precinct.getID();
        this.destDistrict = dest.getID();
    }

    public void execute(){
        precinct.setDistrict(dest);
        dest.addPrecinct(precinctID, precinct);
        src.removePrecinct(precinct);

        Geometry precinctGeometry = precinct.getGeometry();
        dest.addToCurrentGeometry(precinctGeometry);
        src.subtractFromCurrentGeometry(precinctGeometry);

        int precinctPopulation = precinct.getPopulation();
        dest.addPopulation(precinctPopulation);
        src.addPopulation(-(precinctPopulation));

        ElectionData precinctVotes = precinct.getElectionData();
        HashMap<Parties, Integer> voterDistribution = precinctVotes.getVoterDistribution();
        if(voterDistribution.containsKey(Parties.DEMOCRATIC)){
            dest.addVotes(Parties.DEMOCRATIC, voterDistribution.get(Parties.DEMOCRATIC));
            src.addVotes(Parties.DEMOCRATIC, -(voterDistribution.get(Parties.DEMOCRATIC)));
        }
        if(voterDistribution.containsKey(Parties.REPUBLICAN)){
            dest.addVotes(Parties.REPUBLICAN, voterDistribution.get(Parties.REPUBLICAN));
            src.addVotes(Parties.REPUBLICAN, -(voterDistribution.get(Parties.REPUBLICAN)));
        }

    }

    public void execute(boolean regionGrowing){
        precinct.setDistrict(dest);
        dest.addPrecinct(precinctID, precinct);
        Geometry precinctGeometry = precinct.getGeometry();
        dest.addToCurrentGeometry(precinctGeometry);
        int precinctPopulation = precinct.getPopulation();
        dest.addPopulation(precinctPopulation);
        ElectionData precinctVotes = precinct.getElectionData();
        HashMap<Parties, Integer> voterDistribution = precinctVotes.getVoterDistribution();
        if(voterDistribution.containsKey(Parties.DEMOCRATIC)){
            dest.addVotes(Parties.DEMOCRATIC, voterDistribution.get(Parties.DEMOCRATIC));
        }
        if(voterDistribution.containsKey(Parties.REPUBLICAN)) {
            dest.addVotes(Parties.REPUBLICAN, voterDistribution.get(Parties.REPUBLICAN));
        }
    }

    public void undo(){
        precinct.setDistrict(src);
        dest.removePrecinct(precinct);
        src.addPrecinct(precinctID, precinct);

        Geometry precinctGeometry = precinct.getGeometry();
        src.addToCurrentGeometry(precinctGeometry);
        dest.subtractFromCurrentGeometry(precinctGeometry);

        int precinctPopulation = precinct.getPopulation();
        src.addPopulation(precinctPopulation);
        dest.addPopulation(-(precinctPopulation));

        ElectionData precinctVotes = precinct.getElectionData();
        HashMap<Parties, Integer> voterDistribution = precinctVotes.getVoterDistribution();
        if(voterDistribution.containsKey(Parties.DEMOCRATIC)){
            dest.addVotes(Parties.DEMOCRATIC, -voterDistribution.get(Parties.DEMOCRATIC));
            src.addVotes(Parties.DEMOCRATIC, (voterDistribution.get(Parties.DEMOCRATIC)));
        }
        if(voterDistribution.containsKey(Parties.REPUBLICAN)){
            dest.addVotes(Parties.REPUBLICAN, -voterDistribution.get(Parties.REPUBLICAN));
            src.addVotes(Parties.REPUBLICAN, (voterDistribution.get(Parties.REPUBLICAN)));
        }
    }

    public void undo(boolean regionGrow){
        precinct.setDistrict(null);
        dest.removePrecinct(precinct);
        Geometry precinctGeometry = precinct.getGeometry();
        dest.subtractFromCurrentGeometry(precinctGeometry);
        int precinctPopulation = precinct.getPopulation();
        dest.addPopulation(-(precinctPopulation));
        ElectionData precinctVotes = precinct.getElectionData();
        HashMap<Parties, Integer> voterDistribution = precinctVotes.getVoterDistribution();
        if(voterDistribution.containsKey(Parties.DEMOCRATIC)){
            dest.addVotes(Parties.DEMOCRATIC, -voterDistribution.get(Parties.DEMOCRATIC));
        }
        if(voterDistribution.containsKey(Parties.REPUBLICAN)){
            dest.addVotes(Parties.REPUBLICAN, -voterDistribution.get(Parties.REPUBLICAN));
        }

    }

    public String toString(){
        String json = "{\"console_log\": \"Precinct with ID: " + getPrecinctID() + ", moved to district with ID: " + getDestDistrict() + "\"";
        json += ",\"src\":\""+getSrcDistrict();
        json += "\",\"dest\":\""+getDestDistrict();
        json += "\",\"precinct\":\""+getPrecinctID();
        json += "\"}";
        System.out.println(json);
        return json;
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
