package app;

import java.util.HashMap;

public class ElectionData {
    private HashMap<Parties, Integer> voterDistribution;
    public ElectionData (){

    }
    public int getNumVotesForDem() {
        return voterDistribution.get(Parties.DEMOCRAT);
    }
    public int getNumVotesForRep() {
        return voterDistribution.get(Parties.REPUBLICAN);
    }
}
