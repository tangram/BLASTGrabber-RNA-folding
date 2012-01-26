package BLASTGrabber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import javax.swing.JDesktopPane;

/**
 * @author Ralf Neumann
 */
public interface Facade {
    public JDesktopPane getDesktopPane();
    public ArrayList<String> getBLASTAlignments(HashMap<String, BLASTGrabberQuery> queries);
    public ArrayList<String> getFASTAQueries(HashMap<String, BLASTGrabberQuery> queries);
    public ArrayList<String> getFASTACustomDBSequences(HashMap<String, BLASTGrabberQuery> queries);

    public class BLASTGrabberQuery {
        public int BLASTGrabberID = -1;
        public String Name = "";
        public ArrayList<BLASTGrabberHit> Hits = new ArrayList<BLASTGrabberHit>();
    }

    public class BLASTGrabberHit {
        public int BLASTGrabberID = -1;
        public String SequenceHeader;
        public Hashtable<String,String> Statistics = new Hashtable<String, String>();
    }
}
