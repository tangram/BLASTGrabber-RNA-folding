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
public class miRNAHit extends BLASTGrabberHit{
    
    public miRNAHit(BLASTGrabberHit BGHit){
        this.BLASTGrabberID = BGHit.BLASTGrabberID;
        this.SequenceHeader = BGHit.SequenceHeader;
        this.Statistics = BGHit.Statistics;
    }
    
    @Override
    public String toString(){ return SequenceHeader; }
    
}
