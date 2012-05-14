
package Data;

import BLASTGrabber.Facade.BLASTGrabberHit;
/**
 * miRNAHit contains a series of statistics on a hit from BLASTGrabber.
 * The only difference from BLASTGrabber hit is the toString() method which now shows the sequenceheader of the hit.
 * 
 * @author Petter
 */
    
public class RNAHit extends BLASTGrabberHit{

     /**
     * 
     * @param BGHit Hit from BLASTGrabber
     */
    public RNAHit(BLASTGrabberHit BGHit){
        this.BLASTGrabberID = BGHit.BLASTGrabberID;
        this.SequenceHeader = BGHit.SequenceHeader;
        this.Statistics = BGHit.Statistics;
    }

    @Override
    public String toString(){ return SequenceHeader; }

}
