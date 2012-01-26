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
    private BLASTGrabber.Facade blastGrabberFacade = null;
    private JDesktopPane BLASTGrabberDesktop = null;

    public static final String NAME = "miRNA Folding";

    public static Main getMain() {
        return me;
    }

    public BLASTGrabber.Facade getBLASTGrabberFacade() {
        return blastGrabberFacade;
    }

    @Override
    public void initialize(BLASTGrabber.Facade blastGrabberFacade) {
        me = this;
        this.blastGrabberFacade = blastGrabberFacade;
        BLASTGrabberDesktop = this.blastGrabberFacade.getDesktopPane();
    }

    @Override
    public void displayMain() {
        FrmMain frmMain = new FrmMain();
        BLASTGrabberDesktop.add(frmMain);
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
        frmClipboard.init(queries, BLASTGrabberDesktop);
        BLASTGrabberDesktop.add(frmClipboard);
        frmClipboard.setVisible(true);
    }
}
