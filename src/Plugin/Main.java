package Plugin;

import BLASTGrabber.Facade.BLASTGrabberQuery;
import miRNAFolding.FrmClipboard;
import java.util.HashMap;

/**
 * @author Ralf Neumann
 * @author Eirik Krogstad
 * @author Petter Hannevold
 */
public class Main implements Facade {
    private static Main m_Me = null;
    private BLASTGrabber.Facade m_BLASTGrabberFacade = null;

    public static final String NAME = "miRNA Folding";

    public static Main getMain() {
        return m_Me;
    }

    public BLASTGrabber.Facade getBLASTGrabberFacade() {
        return m_BLASTGrabberFacade;
    }

    @Override
    public void initialize(BLASTGrabber.Facade blastgrabberFacade) {
        m_BLASTGrabberFacade = blastgrabberFacade;
        m_Me = this;
    }

    @Override
    public void displayMain() {
        miRNAFolding.FrmMain frm = new miRNAFolding.FrmMain();
        m_BLASTGrabberFacade.displayInternalFrame(frm);
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
        FrmClipboard frm = new FrmClipboard();
        frm.init(queries);
        m_BLASTGrabberFacade.displayInternalFrame(frm);
    }
}
