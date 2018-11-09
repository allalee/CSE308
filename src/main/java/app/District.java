package app;

import java.util.Set;

public class District {
    private int ID;
    private State state;
    private Set<Precinct> precincts;

    public District(int ID, State state){
        this.ID = ID;
        this.state = state;
    }
}
