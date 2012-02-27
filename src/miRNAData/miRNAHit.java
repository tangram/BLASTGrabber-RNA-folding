/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package miRNAData;

import BLASTGrabber.Facade.BLASTGrabberHit;

/**
 *
 * @author Petter
 */
public class miRNAHit extends BLASTGrabberHit{
    
    @Override
    public String toString(){
        return this.SequenceHeader;
    }
    
    public miRNAHit(String name){
        this.SequenceHeader = name;
    }
}
