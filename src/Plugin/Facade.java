/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Plugin;

import BLASTGrabber.Facade.BLASTGrabberQuery;
import java.util.HashMap;

/**
 * @author Ralf Neumann
 */
public interface Facade {
    public void initialize(BLASTGrabber.Facade facade);
    public String getName();
    public void displayMain();
    public boolean doesProcessSelectedClipboardData();
    public void processSelectedClipboardItems(HashMap<String, BLASTGrabberQuery> queries);
}