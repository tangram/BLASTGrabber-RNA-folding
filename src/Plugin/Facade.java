package Plugin;

import BLASTGrabber.Facade.BLASTGrabberQuery;
import java.util.HashMap;

/**
 * Interface for a BLASTGrabber plugin, must be implemented in Plugin.Main
 *
 * @author Ralf Neumann
 */
public interface Facade {
    public void initialize(BLASTGrabber.Facade facade);
    public String getName();
    public void displayMain();
    public boolean doesProcessSelectedClipboardData();
    public void processSelectedClipboardItems
        (HashMap<String, BLASTGrabberQuery> queries, HashMap<String, BLASTGrabberQuery> hits);
}