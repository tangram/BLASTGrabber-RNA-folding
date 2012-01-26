package Plugin;

import BLASTGrabber.Facade.BLASTGrabberQuery;
import miRNAFolding.FrmMain;
import miRNAFolding.FrmClipboard;
import java.util.HashMap;
import javax.swing.JDesktopPane;

/**
 * @author Ralf Neumann
 * @author Eirik Krogstad
 * @author Petter Hannevold
 */
public class Main implements Facade {
    private static Main me = null;
    private BLASTGrabber.Facade facade = null;
    private JDesktopPane desktop = null;

    public static final String NAME = "miRNA Folding";

    public static Main getMain() {
        return me;
    }

    public BLASTGrabber.Facade getBLASTGrabberFacade() {
        return facade;
    }

    @Override
    public void initialize(BLASTGrabber.Facade blastGrabberFacade) {
        me = this;
        this.facade = blastGrabberFacade;
        desktop = this.facade.getDesktopPane();
    }

    @Override
    public void displayMain() {
        FrmMain frmMain = new FrmMain();
        desktop.add(frmMain);
        frmMain.setVisible(true);
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public boolean doesProcessSelectedClipboardData() {
        return true;
    }

    @Override
    public void processSelectedClipboardItems(HashMap<String, BLASTGrabberQuery> queries) {
        FrmClipboard frmClipboard = new FrmClipboard();
        frmClipboard.init(queries, desktop, facade);
        desktop.add(frmClipboard);
        frmClipboard.setVisible(true);
    }
}
