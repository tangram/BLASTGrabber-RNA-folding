
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
public class miRNAQuery extends BLASTGrabberQuery{
    
    /**
     * ArrayList containing all hits on this query from BLASTGrabber
     */
    public ArrayList<miRNAHit> miRNAHits;
    
    /**
     * 
     * @param BGQuery
     */
    public miRNAQuery(BLASTGrabberQuery BGQuery){
        this.Name = BGQuery.Name;
        this.BLASTGrabberID = BGQuery.BLASTGrabberID;
        this.miRNAHits = convertHits(BGQuery.Hits);
    }
    
    @Override
    public String toString () { return Name; }
    
    private ArrayList<miRNAHit> convertHits(ArrayList<BLASTGrabberHit> bghits){
        ArrayList<miRNAHit> hits = new ArrayList<miRNAHit>();
        for(BLASTGrabberHit hit : bghits){
            hits.add(new miRNAHit(hit));
        }
        return hits;
    }
}
