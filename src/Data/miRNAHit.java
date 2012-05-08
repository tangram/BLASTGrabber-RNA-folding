
package Data;

import BLASTGrabber.Facade.BLASTGrabberHit;
/**
 * miRNAHit contains a series of statistics on a hit from BLASTGrabber.
 * The only difference from BLASTGrabber hit is the toString() method which now shows the sequenceheader of the hit.
 * 
 * @author Petter
 */
public class miRNAHit extends BLASTGrabberHit{
    
    /**
     * 
     * @param BGHit
     */
    public miRNAHit(BLASTGrabberHit BGHit){
        this.BLASTGrabberID = BGHit.BLASTGrabberID;
        this.SequenceHeader = BGHit.SequenceHeader;
        this.Statistics = BGHit.Statistics;
    }
    
    @Override
    public String toString(){ return SequenceHeader; }
    
}
