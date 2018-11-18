package app;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;

public class Annealing extends Algorithm{
    @Autowired
    SocketHandler handler;

    @Override
    void run() {
        Collection<Precinct> allPrecincts = state.getAllPrecincts();
        Collection<District> allDistricts = state.getAllDistricts();

        int stagnant_iterations = 0;
        int max_stagnant = 10;

        // only stop if the score stagnanted for X amt of loops
        while(running && stagnant_iterations < max_stagnant){
            for(District dis: allDistricts){    // loop thru districts and anneal neighbor districts
                //get other district precinct AND move to this district

            }
        }

        running = false;

        System.out.println("Algo finished");
    }


    private void updateClient(Move move){
        // make JSON
        System.out.println("Sent");
        String json = "{";
        json += "\"src\":\""+move.getSrcDistrict();
        json += "\",\"dest\":\""+move.getDestDistrict();
        json += "\",\"precinct\":\""+move.getPrecinctID();
        json += "\"}";
        handler.send(json);
    }

}
