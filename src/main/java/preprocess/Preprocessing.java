package preprocess;

import gerrymandering.HibernateManager;
import gerrymandering.model.State;

public class Preprocessing {
    public static void main(String args[]) throws Throwable {
        HibernateManager hb = HibernateManager.getInstance();
        State state = new State("Dinkleberg", "DB", "SampleText");
//        state.setShortName("NY");
//        state.setName("New York");
//        state.setStateId(1);
//        state.setConstitutionText("SampleText");
        boolean result = hb.persistToDB(state);
        System.out.println(result);
    }
}
