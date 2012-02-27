/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Data;

import BLASTGrabber.Facade.BLASTGrabberHit;
import BLASTGrabber.Facade.BLASTGrabberQuery;
import java.util.ArrayList;

/**
 *
 * @author Petter
 */
public class miRNAQuery extends BLASTGrabberQuery{
    
    public ArrayList<miRNAHit> miRNAHits;
    
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
