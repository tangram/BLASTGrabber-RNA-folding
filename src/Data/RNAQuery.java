package Data;

import BLASTGrabber.Facade.BLASTGrabberHit;
import BLASTGrabber.Facade.BLASTGrabberQuery;
import java.util.ArrayList;

/**
 * An extension of BLASTGrabberQuery with a toString() method
 *
 * @author Petter Hannevold
 */
public class RNAQuery extends BLASTGrabberQuery{

    public ArrayList<RNAHit> RNAHits;
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
