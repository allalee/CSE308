package preprocess.preprocessctmd;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Andrew on 11/25/2018.
 */
//IF VOTING DATA COUNTY COLUMN IS EQUAL TO ANY OF THESE, THEN THE PRECINCT IS IN CONNECTICUT. ELSE, MARYLAND.
public class ConnCounties {
    private String[] counties = {"Andover", "Ansonia", "Ashford", "Avon",
            "Barkhamsted", "Beacon Falls", "Berlin", "Bethany", "Bethel", "Bethlehem", "Bloomfield", "Bolton", "Bozrah", "Branford", "Bridgeport",
            "Bridgewater", "Bristol", "Brookfield", "Brooklyn", "Burlington", "Canaan", "Canterbury", "Canton", "Chaplin", "Cheshire",
            "Chester", "Clinton", "Colchester", "Colebrook", "Columbia", "Cornwall", "Coventry", "Cromwell", "Danbury", "Darien", "Deep River",
            "Derby", "Durham", "East Granby", "East Haddam", "East Hampton", "East Hartford", "East Haven", "East Lyme", "East Windsor", "Eastford", "Easton", "Ellington", "Enfield",
            "Essex", "Fairfield", "Farmington", "Franklin", "Glastonbury", "Goshen", "Granby", "Greenwich", "Griswold", "Groton", "Guilford", "Haddam",
            "Hamden", "Hampton", "Hartford", "Hartland", "Harwinton", "Hebron", "Kent", "Killingly", "Killingworth", "Lebanon",
            "Ledyard", "Lisbon", "Litchfield", "Lyme", "Madison", "Manchester", "Mansfield", "Marlborough", "Meriden", "Middlebury", "Middlefield", "Middletown",
            "Milford", "Monroe", "Montville", "Morris", "Naugatuck", "New Britain", "New Canaan", "New Fairfield", "New Hartford",
            "New Haven", "New London", "New Milford", "Newington", "Newtown", "Norfolk", "North Branford", "North Canaan", "North Haven",
            "North Stonington", "Norwalk", "Norwich", "Old Lyme", "Old Saybrook", "Orange", "Oxford",
            "Plainfield", "Plainville", "Plymouth", "Pomfret", "Portland", "Preston", "Prospect",
            "Putnam", "Redding", "Ridgefield", "Rocky Hill", "Roxbury", "Salem", "Salisbury", "Scotland", "Seymour",
            "Sharon", "Shelton", "Sherman", "Simsbury", "Somers", "South Windsor", "Southbury", "Southington", "Sprague", "Stafford",
            "Stamford", "Sterling", "Stonington", "Stratford", "Suffield", "Thomaston", "Thompson", "Tolland", "Torrington", "Trumbull", "Union",
            "Vernon", "Voluntown", "Wallingford", "Warren", "Washington", "Waterbury", "Waterford", "Watertown", "West Hartford", "West Haven", "Westbrook",
            "Weston", "Westport", "Wethersfield", "Willington", "Wilton", "Winchester", "Windham", "Windsor", "Windsor Locks",
            "Wolcott", "Woodbridge", "Woodbury", "Woodstock" };


    public ConnCounties(){

    }

    public String[] getCounties() {
        return counties;
    }

    public ArrayList<String> getCountiesArrayList(){
        return new ArrayList<>(Arrays.asList(counties));
    }
}
