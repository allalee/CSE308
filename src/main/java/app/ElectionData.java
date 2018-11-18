package app;

import java.util.HashMap;
import java.util.List;

public class ElectionData {
    //ElectionData needs to be preprocessed before hand
    private HashMap<Parties, Integer> voterDistribution;
    private int electionYear;
    private ElectionType electionType;
    private int totalVotes;
    private List<Representative> reps;
    private Parties winner;
    public ElectionData(){

    }
    public int getNumVotesForDem() {
        return voterDistribution.get(Parties.DEMOCRATIC);
    }
    public int getNumVotesForRep() {
        return voterDistribution.get(Parties.REPUBLICAN);
    }
    public HashMap<Parties, Integer> getVoterDistribution() {
        return voterDistribution;
    }
    public int getElectionYear(){
        return electionYear;
    }
    public ElectionType getElectionType() {
        return electionType;
    }
    public int getTotalVotes() {
        return totalVotes;
    }
    public List<Representative> getReps() {
        return reps;
    }
    public Parties getWinner() {
        return winner;
    }

    public void setVoterDistribution(HashMap<Parties, Integer> voterDistribution) {
        this.voterDistribution = voterDistribution;
    }

    public void setTotalVotes(int totalVotes) {
        this.totalVotes = totalVotes;
    }

    public void setReps(List<Representative> reps) {
        this.reps = reps;
    }

    public void setWinner(Parties winner) {
        this.winner = winner;
    }
}
