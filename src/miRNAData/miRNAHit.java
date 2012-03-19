/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package miRNAData;

import BLASTGrabber.Facade.BLASTGrabberHit;
import BLASTGrabber.Facade.BLASTGrabberQuery;
import java.util.ArrayList;
import java.util.HashMap;
import BLASTGrabber.Facade;

/**
 *
 * @author Petter
 */
public class miRNAHit extends BLASTGrabberHit{
    
    private String alignedSequence, fullSequence;
    private miRNAQuery query;
    
    @Override
    public String toString(){
        return this.SequenceHeader;
    }
    
    public miRNAHit(String name, miRNAQuery query){
        this.SequenceHeader = name;
        this.query = query;
    }
    
    private void initiateAlignmentData(miRNAQuery query){
        
        ArrayList<String> DBSequence;
        HashMap<String, miRNAQuery>  BGQuery = new HashMap<String, miRNAQuery>();
        StringBuilder sequenceBuilder = new StringBuilder();
        
        BGQuery.put(query.Name, query);
        
        
    }
}
