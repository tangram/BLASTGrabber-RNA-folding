/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package BLASTGrabber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

/**
 *
 * @author ralfne
 */
public interface Facade {
    public JDesktopPane getDesktopPane();
    public ArrayList<String> getBLASTAlignments(HashMap<String, Facade.BLASTGrabberQuery> queries);
    public ArrayList<String> getFASTAQueries(HashMap<String, Facade.BLASTGrabberQuery> queries);
    public ArrayList<String> getFASTACustomDBSequences(HashMap<String, Facade.BLASTGrabberQuery> queries);

    public class BLASTGrabberQuery{
        public int BLASTGrabberID=-1;
        public String Name="";
        public ArrayList<Facade.BLASTGrabberHit> Hits=new ArrayList<Facade.BLASTGrabberHit>();
    }
    
    public class BLASTGrabberHit{
        public int BLASTGrabberID=-1;
        public String SequenceHeader;
        public ArrayList<Facade.BLASTGrabberStatistic> Statistics= new ArrayList<Facade.BLASTGrabberStatistic>();
    }
    
    public class BLASTGrabberStatistic{
        public String Name="";
        public String Key="";
        public double Value=0;
    }

}
