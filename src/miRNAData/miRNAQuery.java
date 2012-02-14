/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package miRNAData;

import BLASTGrabber.Facade.BLASTGrabberQuery;
import java.util.HashMap;

/**
 *
 * @author Petter
 */
public class miRNAQuery extends BLASTGrabberQuery{
    
    @Override
    public String toString(){
        return this.Name;
    }
    
    public HashMap<String, BLASTGrabberQuery> getBGQuery(){
        HashMap<String, BLASTGrabberQuery> q = new HashMap<String, BLASTGrabberQuery>();
        q.put(Name, this);
        return q;
    }
}
