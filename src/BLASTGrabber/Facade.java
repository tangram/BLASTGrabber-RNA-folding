/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package BLASTGrabber;

import java.util.ArrayList;
import java.util.HashMap;
import javax.swing.JDesktopPane;

/**
 *
 * @author ralfne
 */
public interface Facade {
    public JDesktopPane getDesktopPane();
    public String getBLASTProgram();
    public void openBLASTGrabberFile(String filename);
    public ArrayList<String> getBLASTAlignments(HashMap<String, BLASTGrabberQuery> queries);
    public HashMap<String, String> getFASTAQueries(HashMap<String, BLASTGrabberQuery> queries);
    public HashMap<String, String> getFASTACustomDBSequences(HashMap<String, BLASTGrabberQuery> queries);

    public class BLASTGrabberQuery{
        public int BLASTGrabberID=-1;
        public String Name="";
        public ArrayList<BLASTGrabberHit> Hits=new ArrayList<BLASTGrabberHit>();
    }
    
    public class BLASTGrabberHit{
        public int BLASTGrabberID=-1;
        public String SequenceHeader;
        public ArrayList<BLASTGrabberStatistic> Statistics= new ArrayList<BLASTGrabberStatistic>();
    }
    
    public class BLASTGrabberStatistic{
        public String Name="";
        public String Key="";
        public double Value=0;
    }

}
