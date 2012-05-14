/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Data;

import BLASTGrabber.Facade.BLASTGrabberHit;
/**
 *
 * @author Petter
 */
public class RNAHit extends BLASTGrabberHit{

    public RNAHit(BLASTGrabberHit BGHit){
        this.BLASTGrabberID = BGHit.BLASTGrabberID;
        this.SequenceHeader = BGHit.SequenceHeader;
        this.Statistics = BGHit.Statistics;
    }

    @Override
    public String toString(){ return SequenceHeader; }

}
