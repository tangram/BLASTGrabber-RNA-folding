package RNAFolding;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

/**
 * This class implements a browser to show a set of HTML pages. Old code.
 *
 * @author Eirik Krogstad
 */
public class HelpBrowser extends JFrame {
	private static final int REL_X = 512;
	private static final int REL_Y = 0;
	private static final int SET_WIDTH = 512;
	private static final int SET_HEIGHT = 600;
	private String path, pageShown;
	private JEditorPane helpDisplay;
	private JButton home, back, forward;
	private ArrayList<String> history;
	private int index = 0;

	/**
	 * Constructor for HelpBrowser, initializes components and shows the given page
	 *
	 * @param page	String with a URL for the page to be shown (relative to working directory/doc)
	 */
	public HelpBrowser(String page) {
		setBounds(REL_X, REL_Y, SET_WIDTH, SET_HEIGHT);
		setTitle("Help browser for miRNA Folding");
		setLayout(new BorderLayout());

		// navigation
		JPanel nav = new JPanel();
		nav.setLayout(new BoxLayout(nav, BoxLayout.X_AXIS));
		home = new JButton("Index");
		home.addActionListener(new NavigationListener());
		nav.add(home);
		back = new JButton("<<");
		back.addActionListener(new NavigationListener());
		back.setEnabled(false);
		nav.add(back);
		forward = new JButton(">>");
		forward.addActionListener(new NavigationListener());
		forward.setEnabled(false);
		nav.add(forward);
		add(nav, BorderLayout.NORTH);

		history = new ArrayList<String>();

		path = "file:///" + FrmClipboard.BASEPATH + "doc/";

		try {
			helpDisplay = new JEditorPane(path + page);
			history.add(path + page);
		} catch (IOException e) {
			helpDisplay = new JEditorPane("text/html", "Page not found.");
		}
		helpDisplay.setEditable(false);
		helpDisplay.setMargin(new Insets(2, 12, 2, 12));
		helpDisplay.addHyperlinkListener(new LinkListener());

		add(new JScrollPane(helpDisplay), BorderLayout.CENTER);
	}

	/**
	 * Help method to show a page
	 *
	 * @param page	String with an URL for the page to be shown
	 */
	private void setPage(String page) {
		try {
			helpDisplay.setPage(page);
		} catch(IOException ioe) {
			helpDisplay.setText("Page not found.");
		}
	}

	/**
	 * Shows the given page in browser - for external calls
	 *
	 * @param page	String with the URL to be shown (relative to working directory/doc)
	 */
	public void showPage(String page) {
		// lag ny gren av historie og vis angitt page
		if (history.size() > 1)
			history.subList(index + 1, history.size()).clear();
		back.setEnabled(true);
		forward.setEnabled(false);
		history.add(path + page);
		setPage(path + page);
		index = history.size() - 1;
	}

    class NavigationListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == home) {
                // make new history branch and show index
                if (history.size() > 1)
                    history.subList(index + 1, history.size()).clear();
                back.setEnabled(true);
                forward.setEnabled(false);
                pageShown = path + "index.html";
                history.add(pageShown);
                setPage(pageShown);
                index = history.size() - 1;
            } else if (e.getSource() == back) {
                // show last page of history, if possible
                forward.setEnabled(true);
                index--;
                if (index == 0)
                    back.setEnabled(false);
                pageShown = history.get(index);
                setPage(pageShown);
            } else if (e.getSource() == forward) {
                // show next page of history, if possible
                back.setEnabled(true);
                index++;
                if (index == history.size() - 1)
                    forward.setEnabled(false);
                pageShown = history.get(index);
                setPage(pageShown);
            }
        }
    }

    class LinkListener implements HyperlinkListener {
        @Override
        public void hyperlinkUpdate(HyperlinkEvent e) {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                // make new history branch and show page
                if (history.size() > 1)
                    history.subList(index + 1, history.size()).clear();
                back.setEnabled(true);
                forward.setEnabled(false);
                pageShown = e.getURL().toString();
                history.add(pageShown);
                setPage(pageShown);
                index = history.size() - 1;
            }
        }
    }
}

