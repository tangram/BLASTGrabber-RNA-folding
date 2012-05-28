package Data;

import BLASTGrabber.Facade.BLASTGrabberHit;
import BLASTGrabber.Facade.BLASTGrabberQuery;
import java.util.ArrayList;

/**
 * miRNAQuery contains hits from a query from BLASTGrabber.
 * The class converts all hits from BLASTGrabberHit to miRHAHit upon construction, 
 * in addition, the toString() method is overridden to show the name of the query
 * 
 * @author Petter
 */
public class RNAQuery extends BLASTGrabberQuery{
    /**
     * ArrayList containing all hits on this query from BLASTGrabber
     */
    public ArrayList<RNAHit> RNAHits;
    /**
     * 
     * @param BGQuery Query from BLASTGrabber
     */
    public int nextHitID;

    public RNAQuery(BLASTGrabberQuery bgquery, int nextHitID){
        this.nextHitID = nextHitID;
        this.Name = bgquery.Name;
        this.BLASTGrabberID = bgquery.BLASTGrabberID;
        this.RNAHits = convertHits(bgquery.Hits);
    }

    @Override
    public String toString () {
        return Name;
    }

    private ArrayList<RNAHit> convertHits(ArrayList<BLASTGrabberHit> bghits){
        ArrayList<RNAHit> hits = new ArrayList<RNAHit>();
        for(BLASTGrabberHit hit : bghits) {
            hit.BLASTGrabberID = nextHitID++;
            hits.add(new RNAHit(hit));
        }
        return hits;
    }
}
