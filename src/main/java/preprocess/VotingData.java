package preprocess;

import app.ElectionType;
import app.Parties;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table (
        name = "VOTING_DATA"
)
public class VotingData implements Serializable{
    @Id
    @GeneratedValue
    @Column (
            name = "VOTING_DATA_ID"
    )
    private int voting_data_id;
    @Column(
            name = "COUNTY"
    )
    private String county;
    @Column(
            name = "VOTE_COUNT"
    )
    private int vote_count;
    @Column(
            name = "PRECINCT_ID"
    )
    private int precinct_id;
    @Column(
            name = "REPRESENTATIVE"
    )
    private String representative;
    @Column(
            name = "PARTY"
    )
    private Parties party;
    @Column(
            name = "ELECTION_TYPE"
    )
    private ElectionType election_type;
    @Column(
            name = "YEAR"
    )
    private int year;

    public VotingData(String county, int vote_count, int precinct_id, String representative, Parties party, ElectionType election_type, int year){
        this.county = county;
        this.vote_count = vote_count;
        this.precinct_id = precinct_id;
        this.representative = representative;
        this.party = party;
        this.election_type = election_type;
        this.year = year;
    }
    public VotingData(){

    }

    public int getVotingID(){
        return this.voting_data_id;
    }
    public String getCounty(){
        return this.county;
    }
    public int getVoteCount(){
        return this.vote_count;
    }
    public int getPrecinctID(){
        return this.precinct_id;
    }
    public String getRepresentative(){
        return this.representative;
    }
    public Parties getParty(){
        return this.party;
    }
    public ElectionType getElectionType(){
        return this.election_type;
    }
    public int getYear(){
        return this.year;
    }
}
