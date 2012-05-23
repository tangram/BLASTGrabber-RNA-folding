package Data;

import BLASTGrabber.Facade.BLASTGrabberHit;

/**
 * An extension of BLASTGrabberHit with a toString() method
 *
 * @author Petter Hannevold
 */
public class RNAHit extends BLASTGrabberHit {

    public RNAHit(BLASTGrabberHit BGHit) {
        this.BLASTGrabberID = BGHit.BLASTGrabberID;
        this.SequenceHeader = BGHit.SequenceHeader;
        this.Statistics = BGHit.Statistics;
    }

    @Override
    public String toString() {
        return SequenceHeader;
    }

}
