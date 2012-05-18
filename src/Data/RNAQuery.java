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

    public RNAQuery(BLASTGrabberQuery BGQuery){
        this.Name = BGQuery.Name;
        this.BLASTGrabberID = BGQuery.BLASTGrabberID;
        this.RNAHits = convertHits(BGQuery.Hits);
    }

    @Override
    public String toString () { return Name; }

    private ArrayList<RNAHit> convertHits(ArrayList<BLASTGrabberHit> bghits){
        ArrayList<RNAHit> hits = new ArrayList<RNAHit>();
        for(BLASTGrabberHit hit : bghits){
            hits.add(new RNAHit(hit));
        }
        return hits;
    }
}
