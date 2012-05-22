package RNAFolding;

import BLASTGrabber.Facade.BLASTGrabberQuery;
import BLASTGrabber.Facade.BLASTGrabberStatistic;
import Data.MatureData;
import Data.MatureInfo;
import Data.RNAHit;
import Data.RNAQuery;
import Data.RNASequence;
import com.kitfox.svg.SVGElement;
import java.util.HashMap;
import java.util.Iterator;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Locale;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * FrmClipboard is the main view and controller class for the RNA Folding plugin.
 *
 * @author Eirik Krogstad
 * @author Petter Hannevold
 */
public class FrmClipboard extends javax.swing.JInternalFrame {
    public final static String BASEPATH = new File(
            FrmClipboard.class.getProtectionDomain().getCodeSource().getLocation().getPath()
            ).getParent().concat("/");

    private final static int FOLDWARNINGLIMIT = 50;

    private HashMap<String, RNAQuery> hits;
    private BLASTGrabber.Facade facade;
    private MatureData matureData;

    private HashMap<String, String> querySequences;
    private HashMap<String, String> dbSequences;

    private ArrayList<RNASequence> currentSequences = new ArrayList<RNASequence>();
    private int selectedSequence = 0;
    private String lastFoldOutput = "";
    private RNASequence lastRNASequence = new RNASequence();

    private DefaultTableModel suboptimalTableModel;
    private DefaultTableModel multipleTableModel;

    private final static String[] STANDARDCOLUMNS = {"Number", "ID", "Structure", "kcal/mol"};
    private final static String[] TEMPRANGECOLUMNS = {"Temperature", "ID", "Structure", "kcal/mol"};

    private SVGElement plot;
    private SVGElement legend;

    private double[] plotTransform = {0.0, 0.0, 0.0, 0.0};
    private double[] legendTransform = {0.0, 0.0, 0.0, 0.0};
    private double[] plotResizeTranslate = {0.0, 0.0};
    private double plotResizeScale = 0.0;
    private double legendResizeTranslate = 0.0;

    private int lastX;
    private int lastY;
    private int lastW = 400;
    private int lastH = 400;

    private boolean treeCollapsed = true;

    /** Creates new form FrmClipboard */
    public FrmClipboard() {
        // needed to ensure proper formatting of JFormattedTextFields
        Locale.setDefault(Locale.ENGLISH);
        initComponents();
    }

    /**
     * Initialization of plugin data.
     *
     * @param queries   A set of queries, as transferred from BLASTGrabber
     * @param hits      A set of corresponding hits, as transferred from BLASTGrabber
     * @param facade    A reference to the BLASTGrabber Facade object
     */
    public void init(
        HashMap<String, BLASTGrabberQuery> queries,
        HashMap<String, BLASTGrabberQuery> hits,
        BLASTGrabber.Facade facade) {

        this.hits = convertQueries(hits);
        this.facade = facade;
        this.dbSequences = facade.getFASTACustomDBSequences(hits);
        this.querySequences = facade.getFASTAQueries(hits);
        this.currentSequences = new ArrayList<RNASequence>();

        DataUpdate.updatemiRBaseData();
        this.matureData = new MatureData(DataUpdate.BASEPATH + DataUpdate.DATAPATH + "miRNA.dat");

        initTree();

        suboptimalTableModel = new DefaultTableModel(STANDARDCOLUMNS, 0);
        jTableSuboptimal.setModel(suboptimalTableModel);
        jTableSuboptimal.getSelectionModel().addListSelectionListener(new TableSuboptimalListener());

        multipleTableModel = new DefaultTableModel(STANDARDCOLUMNS, 0);
        jTableMultiple.setModel(multipleTableModel);
        jTableMultiple.getSelectionModel().addListSelectionListener(new TableMultipleListener());
    }

    /**
     * Build a String with common command line parameters for Vienna RNA command line tools from
     * selected GUI components.
     *
     * @param tool  String containing the tool to use
     * @return      String with selected command line parameters
     */
    private String buildOptionsString(String tool) {
        StringBuilder sb = new StringBuilder();

        if (jCheckBoxTemperature.isSelected()) {
            sb.append(" -T ");
            sb.append(jFormattedTextFieldTemperature.getText());
        }
        if (jCheckBoxCirc.isSelected())
            sb.append(" --circ");
        if (jCheckBoxNoLP.isSelected() && !tool.equals("RNAeval"))
            sb.append(" --noLP");
        if (jRadioButtonD0.isSelected())
            sb.append(" -d0");
        else if (jRadioButtonD1.isSelected())
            sb.append(" -d1");
        else if (jRadioButtonD1.isSelected())
            sb.append(" -d2");
        else
            sb.append(" -d3");
        if (jRadioButtonNoCloseGU.isSelected())
            sb.append(" --noClosingGU");
        else if (jRadioButtonNoGU.isSelected())
            sb.append(" --noGU");
        if (jCheckBoxParamFile.isSelected()) {
            File file = new File(jTextFieldParamFile.getText());
            if (file.exists()) {
                sb.append(" -P ");
                sb.append(file.getPath());
            }
        }

        return sb.toString();
    }

    /**
     * Load an SVG file from filename into svgPanel, set current SVGElements plot and legend for easy reference.
     * Takes into consideration current scale level, even if no prior SVG was loaded
     *
     * @param filename  String containing the filename to load
     */
    private void loadSVG(String filename) {
        try {
            svgPanel.getSvgUniverse().clear();
            URI uri = new File(filename).toURI();
            svgPanel.setSvgURI(uri);

            SVGElement root = svgPanel.getSvgUniverse().getDiagram(uri).getRoot();
            plot = root.getChild(1);
            legend = root.getChild(2);
            plotTransform = plot.getPresAbsolute("transform").getDoubleList();
            legendTransform = legend.getPresAbsolute("transform").getDoubleList();

            updatePlot();
            updateLegend();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Error loading SVG: " + e);
        }
        svgPanel.repaint();
    }

    /**
     * Generate SVG plot, colorize it, and load it in the SVG view
     *
     * @param folding   A String with name, sequence and folding information, as output by RNAfold
     */
    private void generatePlot(String folding, boolean hasDotPlot) {
        RNAFolder.generatePlots(folding);
        lastFoldOutput = folding;
        formatFoldOutput(folding);

        String svgPath = ColorAnnotator.annotateSVG(
                lastRNASequence,
                jRadioButtonPositionalEntropy.isSelected(),
                hasDotPlot);
        loadSVG(svgPath);
    }

    /**
     * Updates the plot with new transform attributes (scale, translate) and repaints
     */
    private void updatePlot() {
        if (plot != null) {
            StringBuilder sb = new StringBuilder("scale(");
            sb.append(plotTransform[0] + plotResizeScale).append(" ");
            sb.append(plotTransform[1] + plotResizeScale).append(") translate(");
            sb.append(plotTransform[2] + plotResizeTranslate[0]).append(" ");
            sb.append(plotTransform[3] + plotResizeTranslate[1]).append(")");
            try {
                plot.setAttribute("transform", 2, sb.toString());
                plot.updateTime(0);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error updating plot: " + e);
            }

            svgPanel.repaint();
        }
    }

    /**
     * Updates the plot legend with new transform attributes (scale, translate) and repaints
     */
    private void updateLegend() {
        if (legend != null) {
            StringBuilder sb = new StringBuilder("scale(");
            sb.append(legendTransform[0]).append(" ");
            sb.append(legendTransform[1]).append(") translate(");
            sb.append(legendTransform[2] + legendResizeTranslate).append(" ");
            sb.append(legendTransform[3]).append(")");
            try {
                legend.setAttribute("transform", 2, sb.toString());
                legend.updateTime(0);
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Error updating legend: " + e);
            }
            svgPanel.repaint();
        }
    }

    /**
     * Method to prettify the output from the folding tools for display
     *
     * @param output    A String containing the output from RNAfold et al.
     */
    private void formatFoldOutput(String output) {
        jTextAreaFoldOutput.setText(output);
        String[] lines = output.split("[\r\n]+");
        if (lines.length >= 3) {
            String freeEnergy = stripParantheses(splitFirstSpace(lines[2])[1]);
            jTextAreaFoldOutput.append("\nFree energy for the optimal secondary structure (shown) is " + freeEnergy + " kcal/mol");
        }
        if (lines.length >= 4) {
            String freeEnergy = stripParantheses(splitFirstSpace(lines[3])[1]);
            jTextAreaFoldOutput.append("\nFree energy of the thermodynamic ensemble is " + freeEnergy + " kcal/mol");

            String[] centroidData = splitFirstSpace(stripParantheses(splitFirstSpace(lines[4])[1]));
            jTextAreaFoldOutput.append("\nFree energy for the centroid secondary structure is " +
                    centroidData[0] + " kcal/mol, d = " + centroidData[1].substring(2));

            String seq = lastRNASequence.toString().split("[\r\n]+")[1].replace('T', 'U');
            if (lastRNASequence.getAlignmentStart() > 0 && lastRNASequence.getAlignmentStop() > 0)
                jTextAreaFoldOutput.append("\n\nAlignment between query and hit:\n" +
                    seq.substring(lastRNASequence.getAlignmentStart()-1, lastRNASequence.getAlignmentStop()-1));
            if (lastRNASequence.getMatureStart() > 0 && lastRNASequence.getMatureStop() > 0)
                jTextAreaFoldOutput.append("\n\nMature miRNA sequence found in miRBase data:\n" +
                    seq.substring(lastRNASequence.getMatureStart()-1, lastRNASequence.getMatureStop()-1));
        }
    }

    /**
     * Simple method to resize the columns of a given table. Results depend on autoResizeMode.
     *
     * @param table     JTable to resize
     * @param sizes     int... of sizes or int[] of sizes
     */
    private void setColumnSizes(JTable table, int... sizes) {
        TableColumnModel cols = table.getColumnModel();
        for (int i = 0; i < sizes.length; i++) {
            cols.getColumn(i).setPreferredWidth(sizes[i]);
        }
        table.doLayout();
    }

    /**
     * Strip the first and last character of a string (for stripping parantheses).
     *
     * @param s     The String to strip
     * @return      A String with the first and last character removed
     */
    private String stripParantheses(String s) {
        return s.substring(1, s.length()-1).trim();
    }

    /**
     * Split a string on the first space, return a String array with the string leading to the
     * first space, and the string after the first space
     *
     * @param s     The String to split
     * @return      A String[] with the String components
     */
    private String[] splitFirstSpace(String s) {
        String[] sa = new String[2];
        int p = s.indexOf(" ");
        sa[0] = s.substring(0, p);
        sa[1] = s.substring(p+1);
        return sa;
    }

    /**
     * Initialize the tree list with query and hit names
     */
    private void initTree() {
        DefaultMutableTreeNode top = new DefaultMutableTreeNode();

        RNAQuery query;
        DefaultMutableTreeNode currentNode;

        for(String i : hits.keySet()) {
            query = hits.get(i);
            currentNode = new DefaultMutableTreeNode(query);
            top.add(currentNode);
            for(RNAHit j : query.RNAHits)
                currentNode.add(new DefaultMutableTreeNode(j));
        }

        jTreeQueries.setModel(new DefaultTreeModel(top));
        jTreeQueries.addTreeSelectionListener(new TreeListener());
    }

    /**
     * BLASTGrabber is extended as RNAQuery with a toString() method in the Data package,
     * to be able to simply add them to jTreeQueries. This method converts a HashMap of the former to a
     * HashMap of the latter.
     *
     * @param BGQueries     A HashMap<String, BLASTGrabberQuery> as transferred from BLASTGrabber
     * @return              A translated HashMap<String, RNAQuery>
     */
    private HashMap<String, RNAQuery> convertQueries(HashMap<String, BLASTGrabberQuery> BGQueries) {
        HashMap<String, RNAQuery> rnaQueries = new HashMap<String, RNAQuery>();

        Iterator<String> queryIterator = BGQueries.keySet().iterator();

        String currentKey;
        while(queryIterator.hasNext()){
            currentKey = queryIterator.next();
            rnaQueries.put(currentKey, new RNAQuery(BGQueries.get(currentKey)));
        }

        return rnaQueries;
    }

    /**
     * Gets start and stop indices for mature miRNA from miRBase database, if available
     *
     * @param name      Name of the query or hit to search for
     * @return          Array of int, where [0] is start index and [1] is stop index
     */
    private int[] getMatureStartStop(String name) {
        int matureStart = 0;
        int matureStop = 0;
        Pattern pattern = Pattern.compile("MI\\d+");
        Matcher matcher = pattern.matcher(name);
        if (matcher.find()) {
            name = matcher.group();

            ArrayList<MatureInfo> matureInfo = matureData.getMatureIndexes(name);

            if (matureInfo != null) {
                for (MatureInfo i : matureInfo) {
                    pattern = Pattern.compile(name);
                    for (String j : querySequences.keySet()) {
                        matcher = pattern.matcher(j);
                        if (matcher.find()) {
                            matureStart = i.start;
                            matureStop = i.stop;
                        }
                    }
                }
            }
        }
        int[] matureArray = {matureStart, matureStop};
        return matureArray;
    }

    private RNASequence lookUpRNASequence(Object object) {
        if (object instanceof RNAHit) {
            RNAHit hit = (RNAHit) object;
            int start = 0, stop = 0, qstart = 0, qstop = 0, length = 0;

            for (BLASTGrabberStatistic i : hit.Statistics) {
                if (i.Key.equals("SbjctFrom"))
                    start = (int) i.Value;
                else if (i.Key.equals("SbjctTo"))
                    stop = (int) i.Value;
                else if (i.Key.equals("QueryFrom"))
                    qstart = (int) i.Value;
                else if (i.Key.equals("QueryTo"))
                    qstop = (int) i.Value;
                else if (i.Name.equals("Query length"))
                    length = (int) i.Value;
            }

            if (stop < start) {
                int temp = stop;
                stop = start + qstart;
                start = temp - (length - qstop);
            } else {
                start -= qstart;
                stop += length - qstop;
            }

            String name = hit.SequenceHeader;
            String sequence = dbSequences.get(name);
            int end = sequence.length();
            if (start > end || stop > end)
                sequence = null;
            else
                sequence = sequence.substring(start, stop);

            // get mature data
            int[] matureArray = getMatureStartStop(name);
            int matureStart = matureArray[0];
            int matureStop = matureArray[1];

            if (name != null && sequence != null) {
                return new RNASequence(name + "\n" + sequence, qstart, qstop, matureStart, matureStop);
            } else {
                JOptionPane.showMessageDialog(null, "No sequence found for this hit");
            }

        } else if (object instanceof RNAQuery) {
            RNAQuery query = (RNAQuery) object;

            String name = ">" + query.Name;
            String sequence = querySequences.get(name);

            // get mature data
            int[] matureArray = getMatureStartStop(name);
            int matureStart = matureArray[0];
            int matureStop = matureArray[1];

            if (name != null && sequence != null) {
                return new RNASequence(name + "\n" + sequence, 0, 0, matureStart, matureStop);
            } else {
                JOptionPane.showMessageDialog(null, "No sequence found for this query");
            }
        }
        return null;
    }

    private RNASequence lookUpRNASequence(String name) {
        Object root = jTreeQueries.getModel().getRoot();
        Enumeration enumeration = ((DefaultMutableTreeNode) root).breadthFirstEnumeration();
        DefaultMutableTreeNode node;
        Object object;
        String objectName;
        while (enumeration.hasMoreElements()) {
            node = (DefaultMutableTreeNode) enumeration.nextElement();
            object = node.getUserObject();
            objectName = "";
            if (object instanceof RNAHit)
                objectName = ((RNAHit) object).SequenceHeader;
            else if (object instanceof RNAQuery)
                objectName = ">" + ((RNAQuery) object).Name;
            if (objectName.equals(name))
                return lookUpRNASequence(object);
        }
        return null;
    }

    /**
     * This method is called from within the constructor to
     * initialize the form.
     *
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupColorAnnotation = new javax.swing.ButtonGroup();
        buttonGroupDanglingEnds = new javax.swing.ButtonGroup();
        buttonGroupGUPairs = new javax.swing.ButtonGroup();
        buttonGroupSuboptimal = new javax.swing.ButtonGroup();
        svgPanel = new com.kitfox.svg.app.beans.SVGPanel();
        jRadioButtonPairProbabilities = new javax.swing.JRadioButton();
        jRadioButtonPositionalEntropy = new javax.swing.JRadioButton();
        jTabbedPane = new javax.swing.JTabbedPane();
        jPanelMFE = new javax.swing.JPanel();
        jButtonFold = new javax.swing.JButton();
        jScrollPaneFoldOutput = new javax.swing.JScrollPane();
        jTextAreaFoldOutput = new javax.swing.JTextArea();
        jPanelSuboptimal = new javax.swing.JPanel();
        jScrollPaneSuboptimal = new javax.swing.JScrollPane();
        jTableSuboptimal = new javax.swing.JTable();
        jButtonFoldSuboptimal = new javax.swing.JButton();
        jRadioButtonRandom = new javax.swing.JRadioButton();
        jRadioButtonRange = new javax.swing.JRadioButton();
        jFormattedTextFieldRandom = new javax.swing.JFormattedTextField();
        jFormattedTextFieldRange = new javax.swing.JFormattedTextField();
        jLabelRange = new javax.swing.JLabel();
        jLabelRandom = new javax.swing.JLabel();
        jPanelMultiple = new javax.swing.JPanel();
        jScrollPaneMultiple = new javax.swing.JScrollPane();
        jTableMultiple = new javax.swing.JTable();
        jButtonFoldMultiple = new javax.swing.JButton();
        jCheckBoxTemperatureRange = new javax.swing.JCheckBox();
        jFormattedTextFieldTemperatureLower = new javax.swing.JFormattedTextField();
        jLabelHyphen = new javax.swing.JLabel();
        jFormattedTextFieldTemperatureUpper = new javax.swing.JFormattedTextField();
        jLabelTemperatureRange = new javax.swing.JLabel();
        jPanelOptions = new javax.swing.JPanel();
        jFormattedTextFieldTemperature = new javax.swing.JFormattedTextField();
        jLabelTemperature = new javax.swing.JLabel();
        jRadioButtonGU = new javax.swing.JRadioButton();
        jRadioButtonNoCloseGU = new javax.swing.JRadioButton();
        jRadioButtonNoGU = new javax.swing.JRadioButton();
        jCheckBoxCirc = new javax.swing.JCheckBox();
        jRadioButtonD0 = new javax.swing.JRadioButton();
        jRadioButtonD1 = new javax.swing.JRadioButton();
        jRadioButtonD2 = new javax.swing.JRadioButton();
        jRadioButtonD3 = new javax.swing.JRadioButton();
        jCheckBoxNoLP = new javax.swing.JCheckBox();
        jButtonRefold = new javax.swing.JButton();
        jCheckBoxTemperature = new javax.swing.JCheckBox();
        jTextFieldParamFile = new javax.swing.JTextField();
        jButtonParamFile = new javax.swing.JButton();
        jCheckBoxParamFile = new javax.swing.JCheckBox();
        jScrollPaneQueries = new javax.swing.JScrollPane();
        jTreeQueries = new javax.swing.JTree();
        jButtonExpandCollapse = new javax.swing.JButton();
        jButtonSelectQueries = new javax.swing.JButton();
        jButtonSelectHits = new javax.swing.JButton();
        jButtonSelectAll = new javax.swing.JButton();
        jButtonHelp = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setPreferredSize(new java.awt.Dimension(1101, 705));

        svgPanel.setBackground(new java.awt.Color(255, 255, 255));
        svgPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(180, 180, 180)));
        svgPanel.setFont(new java.awt.Font("SansSerif", 0, 11));
        svgPanel.setPreferredSize(new java.awt.Dimension(400, 400));
        svgPanel.setUseAntiAlias(true);
        svgPanel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) {
                svgPanelMouseWheelMoved(evt);
            }
        });
        svgPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                svgPanelMousePressed(evt);
            }
        });
        svgPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                svgPanelResized(evt);
            }
        });
        svgPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                svgPanelMouseDragged(evt);
            }
        });

        jRadioButtonPairProbabilities.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroupColorAnnotation.add(jRadioButtonPairProbabilities);
        jRadioButtonPairProbabilities.setSelected(true);
        jRadioButtonPairProbabilities.setText("Show pair probabilities");
        jRadioButtonPairProbabilities.setMaximumSize(new java.awt.Dimension(141, 23));
        jRadioButtonPairProbabilities.setMinimumSize(new java.awt.Dimension(141, 23));
        jRadioButtonPairProbabilities.setOpaque(false);
        jRadioButtonPairProbabilities.setPreferredSize(new java.awt.Dimension(141, 23));
        jRadioButtonPairProbabilities.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonPairProbabilitiesActionPerformed(evt);
            }
        });

        jRadioButtonPositionalEntropy.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroupColorAnnotation.add(jRadioButtonPositionalEntropy);
        jRadioButtonPositionalEntropy.setText("Show positional entropy");
        jRadioButtonPositionalEntropy.setOpaque(false);
        jRadioButtonPositionalEntropy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonPositionalEntropyActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout svgPanelLayout = new javax.swing.GroupLayout(svgPanel);
        svgPanel.setLayout(svgPanelLayout);
        svgPanelLayout.setHorizontalGroup(
            svgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(svgPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(svgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonPairProbabilities, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jRadioButtonPositionalEntropy, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap(251, Short.MAX_VALUE))
        );
        svgPanelLayout.setVerticalGroup(
            svgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, svgPanelLayout.createSequentialGroup()
                .addContainerGap(344, Short.MAX_VALUE)
                .addComponent(jRadioButtonPairProbabilities, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonPositionalEntropy)
                .addContainerGap())
        );

        jTabbedPane.setPreferredSize(new java.awt.Dimension(555, 400));

        jButtonFold.setText("Fold");
        jButtonFold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFoldActionPerformed(evt);
            }
        });

        jTextAreaFoldOutput.setColumns(20);
        jTextAreaFoldOutput.setEditable(false);
        jTextAreaFoldOutput.setFont(new java.awt.Font("Monospaced", 0, 11));
        jTextAreaFoldOutput.setRows(5);
        jTextAreaFoldOutput.setCursor(new java.awt.Cursor(java.awt.Cursor.TEXT_CURSOR));
        jScrollPaneFoldOutput.setViewportView(jTextAreaFoldOutput);

        javax.swing.GroupLayout jPanelMFELayout = new javax.swing.GroupLayout(jPanelMFE);
        jPanelMFE.setLayout(jPanelMFELayout);
        jPanelMFELayout.setHorizontalGroup(
            jPanelMFELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMFELayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelMFELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPaneFoldOutput, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 630, Short.MAX_VALUE)
                    .addComponent(jButtonFold))
                .addContainerGap())
        );
        jPanelMFELayout.setVerticalGroup(
            jPanelMFELayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMFELayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneFoldOutput, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonFold)
                .addContainerGap())
        );

        jTabbedPane.addTab("MFE structure", jPanelMFE);

        jTableSuboptimal.setAutoCreateRowSorter(true);
        jTableSuboptimal.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTableSuboptimal.getTableHeader().setReorderingAllowed(false);
        jScrollPaneSuboptimal.setViewportView(jTableSuboptimal);

        jButtonFoldSuboptimal.setText("Fold suboptimal structures");
        jButtonFoldSuboptimal.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFoldSuboptimalActionPerformed(evt);
            }
        });

        buttonGroupSuboptimal.add(jRadioButtonRandom);
        jRadioButtonRandom.setSelected(true);
        jRadioButtonRandom.setText("Random sample of");

        buttonGroupSuboptimal.add(jRadioButtonRange);
        jRadioButtonRange.setText("Within range");

        jFormattedTextFieldRandom.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextFieldRandom.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormattedTextFieldRandom.setText("10");

        jFormattedTextFieldRange.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0.00"))));
        jFormattedTextFieldRange.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormattedTextFieldRange.setText("1.0");

        jLabelRange.setText("kcal/mol of MFE structure");

        jLabelRandom.setText("suboptimal structures");

        javax.swing.GroupLayout jPanelSuboptimalLayout = new javax.swing.GroupLayout(jPanelSuboptimal);
        jPanelSuboptimal.setLayout(jPanelSuboptimalLayout);
        jPanelSuboptimalLayout.setHorizontalGroup(
            jPanelSuboptimalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelSuboptimalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelSuboptimalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPaneSuboptimal, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 630, Short.MAX_VALUE)
                    .addGroup(jPanelSuboptimalLayout.createSequentialGroup()
                        .addGroup(jPanelSuboptimalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelSuboptimalLayout.createSequentialGroup()
                                .addComponent(jRadioButtonRange)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(jFormattedTextFieldRange, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelSuboptimalLayout.createSequentialGroup()
                                .addComponent(jRadioButtonRandom)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jFormattedTextFieldRandom, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelSuboptimalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelSuboptimalLayout.createSequentialGroup()
                                .addComponent(jLabelRandom)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 209, Short.MAX_VALUE)
                                .addComponent(jButtonFoldSuboptimal))
                            .addComponent(jLabelRange))))
                .addContainerGap())
        );
        jPanelSuboptimalLayout.setVerticalGroup(
            jPanelSuboptimalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelSuboptimalLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneSuboptimal, javax.swing.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                .addGap(11, 11, 11)
                .addGroup(jPanelSuboptimalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanelSuboptimalLayout.createSequentialGroup()
                        .addGroup(jPanelSuboptimalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jRadioButtonRange)
                            .addComponent(jFormattedTextFieldRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelRange))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanelSuboptimalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jRadioButtonRandom)
                            .addComponent(jFormattedTextFieldRandom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabelRandom)))
                    .addComponent(jButtonFoldSuboptimal))
                .addContainerGap())
        );

        jTabbedPane.addTab("Suboptimal structures", jPanelSuboptimal);

        jTableMultiple.setAutoCreateRowSorter(true);
        jTableMultiple.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jTableMultiple.getTableHeader().setReorderingAllowed(false);
        jScrollPaneMultiple.setViewportView(jTableMultiple);

        jButtonFoldMultiple.setText("Fold selected sequences");
        jButtonFoldMultiple.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonFoldMultipleActionPerformed(evt);
            }
        });

        jCheckBoxTemperatureRange.setText("Fold for temperature range");

        jFormattedTextFieldTemperatureLower.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextFieldTemperatureLower.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormattedTextFieldTemperatureLower.setText("37");

        jLabelHyphen.setText("-");

        jFormattedTextFieldTemperatureUpper.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextFieldTemperatureUpper.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormattedTextFieldTemperatureUpper.setText("37");

        jLabelTemperatureRange.setText("°C");

        javax.swing.GroupLayout jPanelMultipleLayout = new javax.swing.GroupLayout(jPanelMultiple);
        jPanelMultiple.setLayout(jPanelMultipleLayout);
        jPanelMultipleLayout.setHorizontalGroup(
            jPanelMultipleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMultipleLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelMultipleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPaneMultiple, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 630, Short.MAX_VALUE)
                    .addGroup(jPanelMultipleLayout.createSequentialGroup()
                        .addComponent(jCheckBoxTemperatureRange)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jFormattedTextFieldTemperatureLower, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelHyphen)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jFormattedTextFieldTemperatureUpper, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelTemperatureRange)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 224, Short.MAX_VALUE)
                        .addComponent(jButtonFoldMultiple)))
                .addContainerGap())
        );
        jPanelMultipleLayout.setVerticalGroup(
            jPanelMultipleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMultipleLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneMultiple, javax.swing.GroupLayout.DEFAULT_SIZE, 315, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelMultipleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonFoldMultiple)
                    .addComponent(jCheckBoxTemperatureRange)
                    .addComponent(jFormattedTextFieldTemperatureLower, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelTemperatureRange)
                    .addComponent(jFormattedTextFieldTemperatureUpper, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelHyphen))
                .addContainerGap())
        );

        jTabbedPane.addTab("Multiple runs", jPanelMultiple);

        jFormattedTextFieldTemperature.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextFieldTemperature.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormattedTextFieldTemperature.setText("37");

        jLabelTemperature.setText("°C");

        buttonGroupGUPairs.add(jRadioButtonGU);
        jRadioButtonGU.setSelected(true);
        jRadioButtonGU.setText("Allow GU pairs");

        buttonGroupGUPairs.add(jRadioButtonNoCloseGU);
        jRadioButtonNoCloseGU.setText("Disallow GU pairs at ends of helices");

        buttonGroupGUPairs.add(jRadioButtonNoGU);
        jRadioButtonNoGU.setText("Disallow GU pairs");

        jCheckBoxCirc.setText("Assume circular RNA");

        buttonGroupDanglingEnds.add(jRadioButtonD0);
        jRadioButtonD0.setText("Ignore dangling ends");

        buttonGroupDanglingEnds.add(jRadioButtonD1);
        jRadioButtonD1.setText("Only unpaired bases can participate in at most one dangling end");

        buttonGroupDanglingEnds.add(jRadioButtonD2);
        jRadioButtonD2.setSelected(true);
        jRadioButtonD2.setText("Dangling energies will be added for the bases adjacent to a helix on both sides");

        buttonGroupDanglingEnds.add(jRadioButtonD3);
        jRadioButtonD3.setText("Allow coaxial stacking of adjacent helices in multi-loops");

        jCheckBoxNoLP.setSelected(true);
        jCheckBoxNoLP.setText("Disallow lonely pairs (helices of length 1)");

        jButtonRefold.setText("Refold");
        jButtonRefold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRefoldActionPerformed(evt);
            }
        });

        jCheckBoxTemperature.setText("Scale to temperature");

        jTextFieldParamFile.setText("par/rna_andronescu2007.par");
        jTextFieldParamFile.setPreferredSize(new java.awt.Dimension(60, 20));

        jButtonParamFile.setText("...");
        jButtonParamFile.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jButtonParamFile.setMaximumSize(new java.awt.Dimension(19, 19));
        jButtonParamFile.setMinimumSize(new java.awt.Dimension(19, 19));
        jButtonParamFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonParamFileActionPerformed(evt);
            }
        });

        jCheckBoxParamFile.setSelected(true);
        jCheckBoxParamFile.setText("Use parameter file");

        javax.swing.GroupLayout jPanelOptionsLayout = new javax.swing.GroupLayout(jPanelOptions);
        jPanelOptions.setLayout(jPanelOptionsLayout);
        jPanelOptionsLayout.setHorizontalGroup(
            jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanelOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelOptionsLayout.createSequentialGroup()
                        .addComponent(jCheckBoxTemperature)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jFormattedTextFieldTemperature, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabelTemperature))
                    .addComponent(jCheckBoxCirc, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jCheckBoxNoLP, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonD0, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonD1, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonD2, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonGU, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jRadioButtonNoCloseGU, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanelOptionsLayout.createSequentialGroup()
                        .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioButtonNoGU)
                            .addComponent(jRadioButtonD3)
                            .addGroup(jPanelOptionsLayout.createSequentialGroup()
                                .addComponent(jCheckBoxParamFile)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jTextFieldParamFile, javax.swing.GroupLayout.DEFAULT_SIZE, 151, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jButtonParamFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 282, Short.MAX_VALUE)
                        .addComponent(jButtonRefold)))
                .addContainerGap())
        );
        jPanelOptionsLayout.setVerticalGroup(
            jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jCheckBoxTemperature)
                    .addComponent(jFormattedTextFieldTemperature, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelTemperature))
                .addGap(18, 18, 18)
                .addComponent(jCheckBoxCirc)
                .addGap(18, 18, 18)
                .addComponent(jCheckBoxNoLP)
                .addGap(18, 18, 18)
                .addComponent(jRadioButtonD0)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonD1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonD2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonD3)
                .addGap(18, 18, 18)
                .addComponent(jRadioButtonGU)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonNoCloseGU)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jRadioButtonNoGU)
                .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelOptionsLayout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                        .addComponent(jButtonRefold)
                        .addContainerGap())
                    .addGroup(jPanelOptionsLayout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckBoxParamFile)
                            .addComponent(jTextFieldParamFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButtonParamFile, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
        );

        jTabbedPane.addTab("Options", jPanelOptions);
        jPanelOptions.getAccessibleContext().setAccessibleName("");

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        jTreeQueries.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        jTreeQueries.setRootVisible(false);
        jScrollPaneQueries.setViewportView(jTreeQueries);

        jButtonExpandCollapse.setText("Expand/collapse");
        jButtonExpandCollapse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonExpandCollapseActionPerformed(evt);
            }
        });

        jButtonSelectQueries.setText("Select queries");
        jButtonSelectQueries.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectQueriesActionPerformed(evt);
            }
        });

        jButtonSelectHits.setText("Select hits");
        jButtonSelectHits.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectHitsActionPerformed(evt);
            }
        });

        jButtonSelectAll.setText("Select all");
        jButtonSelectAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonSelectAllActionPerformed(evt);
            }
        });

        jButtonHelp.setText("Help");
        jButtonHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonHelpActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(jScrollPaneQueries, javax.swing.GroupLayout.DEFAULT_SIZE, 952, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jButtonHelp, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonExpandCollapse, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonSelectQueries, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonSelectHits, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jButtonSelectAll, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(svgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 655, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButtonExpandCollapse)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonSelectQueries)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonSelectHits)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButtonSelectAll)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 102, Short.MAX_VALUE)
                        .addComponent(jButtonHelp))
                    .addComponent(jScrollPaneQueries, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane, javax.swing.GroupLayout.Alignment.TRAILING, 0, 0, Short.MAX_VALUE)
                    .addComponent(svgPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 399, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Listener class for the Suboptimal structures table
     * Folds the selected row
     * Special case, since dot plots are not available for suboptimal foldings
     */
    private class TableSuboptimalListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            int row = jTableSuboptimal.getSelectedRow();
            if (row > -1) {
                String name = jTableSuboptimal.getValueAt(row, 1).toString();
                RNASequence selected = lookUpRNASequence(name);
                if (selected != null) {
                    lastRNASequence = selected;
                    String folding = (String) jTableSuboptimal.getValueAt(row, 2);
                    String freeEnergy = (String) jTableSuboptimal.getValueAt(row, 3);
                    String structure = selected.toString() + "\n" + folding + " (" + freeEnergy + ")";
                    generatePlot(structure, false);
                    lastFoldOutput = "";
                }
            }
        }
    }

    /**
     * Listener class for the Multiple runs table
     * Folds the selected row
     */
    private class TableMultipleListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            int row = jTableMultiple.getSelectedRow();
            if (row > -1) {
                String name = jTableMultiple.getValueAt(row, 1).toString();
                RNASequence selected = lookUpRNASequence(name);
                if (selected != null) {
                    lastRNASequence = selected;
                    String output = RNAFolder.foldSequence(
                            lastRNASequence.toString(),
                            " -p" + buildOptionsString("RNAFold"));
                    generatePlot(output, true);
                }
            }
        }
    }

    /**
     * Listener class for the tree list
     * Populates current and selected sequences containers with data
     */
    private class TreeListener implements TreeSelectionListener {
        @Override
        public void valueChanged(TreeSelectionEvent e) {
            currentSequences.clear();
            for (TreePath path : jTreeQueries.getSelectionPaths()) {
                Object node = path.getLastPathComponent();
                Object current = ((DefaultMutableTreeNode) node).getUserObject();
                RNASequence currentSequence = lookUpRNASequence(current);
                if (currentSequence != null)
                    currentSequences.add(currentSequence);
                selectedSequence = currentSequences.size() - 1;
            }
        }
    }

    private void jButtonFoldActionPerformed(java.awt.event.ActionEvent evt) {
        if (!currentSequences.isEmpty()) {
            lastRNASequence = currentSequences.get(selectedSequence);
            String output = RNAFolder.foldSequence(
                    lastRNASequence.toString(),
                    " -p" + buildOptionsString("RNAFold"));
            generatePlot(output, true);
        }
    }

    private void jButtonRefoldActionPerformed(java.awt.event.ActionEvent evt) {
        jButtonFoldActionPerformed(evt);
    }

    private void jRadioButtonPairProbabilitiesActionPerformed(java.awt.event.ActionEvent evt) {
        if (!lastFoldOutput.isEmpty())
            generatePlot(lastFoldOutput, true);
    }

    private void jRadioButtonPositionalEntropyActionPerformed(java.awt.event.ActionEvent evt) {
        jRadioButtonPairProbabilitiesActionPerformed(evt);
    }

    private void jButtonFoldSuboptimalActionPerformed(java.awt.event.ActionEvent evt) {
        if (!currentSequences.isEmpty()) {
            StringBuilder sb = new StringBuilder(buildOptionsString("RNAsubopt"));
            if (jRadioButtonRange.isSelected()) {
                sb.append(" -e ");
                sb.append(jFormattedTextFieldRange.getText());
            } else {
                sb.append(" -p ");
                sb.append(jFormattedTextFieldRandom.getText());
            }

            String output = RNAFolder.foldSuboptimals(
                    currentSequences.get(selectedSequence).toString(),
                    sb.toString());
            String[] outputLines = output.split("[\r\n]+");
            String name = outputLines[0];

            if (jRadioButtonRandom.isSelected()) {
                sb = new StringBuilder();
                for (int i = 2; i < outputLines.length; i++)
                    sb.append(
                            currentSequences.get(selectedSequence).toString()
                            .split("\n")[1])
                            .append("\n")
                            .append(outputLines[i])
                            .append("\n");
                output = RNAFolder.evalSuboptimals(sb.append("@").toString(), buildOptionsString("RNAeval"));
                outputLines = output.split("[\r\n]+");
            }

            jTableSuboptimal.clearSelection();
            FoldingTableModel newModel = new FoldingTableModel(STANDARDCOLUMNS, 0);
            if (jRadioButtonRange.isSelected()) {
                for (int i = 3; i < outputLines.length; i++) {
                    String[] line = splitFirstSpace(outputLines[i]);
                    Object[] row = {(Integer) (i-2), name, line[0], stripParantheses(line[1])};
                    newModel.addRow(row);
                }
            } else {
                for (int i = 1; i < outputLines.length; i += 2) {
                    String[] line = splitFirstSpace(outputLines[i]);
                    Object[] row = {(Integer) ((i/2)+1), name, line[0], stripParantheses(line[1])};
                    newModel.addRow(row);
                }
            }
            jTableSuboptimal.setModel(newModel);
            setColumnSizes(jTableSuboptimal, 10, 100, 300, 10);
            DefaultTableCellRenderer r = new DefaultTableCellRenderer();
            r.setHorizontalAlignment(SwingConstants.RIGHT);
            jTableSuboptimal.getColumn("Number").setCellRenderer(r);
            jTableSuboptimal.getColumn("kcal/mol").setCellRenderer(r);
        }
    }

    private void jButtonParamFileActionPerformed(java.awt.event.ActionEvent evt) {
        JFileChooser chooser = new JFileChooser(BASEPATH + "par/");
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Parameter file", "par");
        chooser.setFileFilter(filter);
        int value = chooser.showOpenDialog(this);
        if (value == JFileChooser.APPROVE_OPTION) {
            jCheckBoxParamFile.setSelected(true);
            jTextFieldParamFile.setText(chooser.getSelectedFile().getPath());
        }
    }

    private void jButtonFoldMultipleActionPerformed(java.awt.event.ActionEvent evt) {
        if (!currentSequences.isEmpty()) {
            if (jCheckBoxTemperatureRange.isSelected()) {
                jCheckBoxTemperature.setSelected(false);
                int lower = Integer.valueOf(jFormattedTextFieldTemperatureLower.getText());
                int upper = Integer.valueOf(jFormattedTextFieldTemperatureUpper.getText());
                if (lower < upper) {
                    jTableMultiple.clearSelection();
                    FoldingTableModel newModel = new FoldingTableModel(TEMPRANGECOLUMNS, 0);
                    for (int i = lower; i <= upper; i++) {
                        String output = RNAFolder.foldSequence(
                                currentSequences.get(selectedSequence).toString(),
                                " -T " + i + buildOptionsString("RNAfold"));
                        String[] outputLines = output.split("[\r\n]+");
                        String[] line = splitFirstSpace(outputLines[2]);
                        Object[] row = {i + "°C", outputLines[0], line[0], stripParantheses(line[1])};
                        newModel.addRow(row);
                    }
                    jTableMultiple.setModel(newModel);
                    setColumnSizes(jTableMultiple, 10, 100, 300, 10);
                    DefaultTableCellRenderer r = new DefaultTableCellRenderer();
                    r.setHorizontalAlignment(SwingConstants.RIGHT);
                    jTableMultiple.getColumn("kcal/mol").setCellRenderer(r);
                }
            } else {
                jTableMultiple.clearSelection();
                FoldingTableModel newModel = new FoldingTableModel(STANDARDCOLUMNS, 0);
                for (int i = 0; i < currentSequences.size(); i++) {
                    String output = RNAFolder.foldSequence(
                                currentSequences.get(i).toString(),
                                " -p " + buildOptionsString("RNAfold"));
                        String[] outputLines = output.split("[\r\n]+");
                        String[] line = splitFirstSpace(outputLines[2]);
                        Object[] row = {(Integer) (i+1), outputLines[0], line[0], stripParantheses(line[1])};
                        newModel.addRow(row);
                }
                jTableMultiple.setModel(newModel);
                setColumnSizes(jTableMultiple, 10, 100, 300, 10);
                DefaultTableCellRenderer r = new DefaultTableCellRenderer();
                r.setHorizontalAlignment(SwingConstants.RIGHT);
                jTableMultiple.getColumn("Number").setCellRenderer(r);
                jTableMultiple.getColumn("kcal/mol").setCellRenderer(r);
            }
        }
    }

    private void svgPanelMouseWheelMoved(java.awt.event.MouseWheelEvent evt) {//GEN-FIRST:event_svgPanelMouseWheelMoved
        if (plot != null) {
            int rot = evt.getWheelRotation();
            double trans = rot * 0.05;
            plotTransform[0] += -trans;
            plotTransform[1] += -trans;
            updatePlot();
        }
    }//GEN-LAST:event_svgPanelMouseWheelMoved

    private void svgPanelMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_svgPanelMouseDragged
        if (plot != null) {
            int x = evt.getX();
            int y = evt.getY();
            plotTransform[2] += -(lastX - x);
            plotTransform[3] += -(lastY - y);
            lastX = x;
            lastY = y;
            updatePlot();
        }
    }//GEN-LAST:event_svgPanelMouseDragged

    private void svgPanelMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_svgPanelMousePressed
        lastX = evt.getX();
        lastY = evt.getY();
    }//GEN-LAST:event_svgPanelMousePressed

    private void svgPanelResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_svgPanelResized
        int w = svgPanel.getWidth();
        int h = svgPanel.getHeight();
        int hDiff = h - lastH;
        int wDiff = w - lastW;
        if (w > h) {
            plotResizeScale += hDiff * 0.0019;
            plotResizeTranslate[0] += wDiff * 0.25;
        } else {
            plotResizeScale += wDiff * 0.0019;
            plotResizeTranslate[1] += hDiff * 0.25;
        }
        legendResizeTranslate += wDiff * 1.13;
        lastW = w;
        lastH = h;
        updatePlot();
        updateLegend();
    }//GEN-LAST:event_svgPanelResized

    private void jButtonExpandCollapseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonExpandCollapseActionPerformed
        if (treeCollapsed) {
            treeCollapsed = false;
            for (int i = 0; i < jTreeQueries.getRowCount(); i++)
                jTreeQueries.expandRow(i);
        } else {
            treeCollapsed = true;
            for (int i = 0; i < jTreeQueries.getRowCount(); i++)
                jTreeQueries.collapseRow(i);
        }
    }//GEN-LAST:event_jButtonExpandCollapseActionPerformed

    private void jButtonSelectQueriesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectQueriesActionPerformed
        for (int i = 0; i < jTreeQueries.getRowCount(); i++) {
            Object node = jTreeQueries.getPathForRow(i).getLastPathComponent();
            Object object = ((DefaultMutableTreeNode) node).getUserObject();
            if (object instanceof RNAQuery)
                jTreeQueries.addSelectionRow(i);
        }
    }//GEN-LAST:event_jButtonSelectQueriesActionPerformed

    private void jButtonSelectHitsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectHitsActionPerformed
        for (int i = 0; i < jTreeQueries.getRowCount(); i++) {
            Object node = jTreeQueries.getPathForRow(i).getLastPathComponent();
            Object object = ((DefaultMutableTreeNode) node).getUserObject();
            if (object instanceof RNAHit)
                jTreeQueries.addSelectionRow(i);
        }
    }//GEN-LAST:event_jButtonSelectHitsActionPerformed

    private void jButtonSelectAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonSelectAllActionPerformed
        for (int i = 0; i < jTreeQueries.getRowCount(); i++)
            jTreeQueries.addSelectionRow(i);
    }//GEN-LAST:event_jButtonSelectAllActionPerformed

    private void jButtonHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonHelpActionPerformed
        HelpBrowser browser = new HelpBrowser("index.html");
        browser.setVisible(true);
    }//GEN-LAST:event_jButtonHelpActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupColorAnnotation;
    private javax.swing.ButtonGroup buttonGroupDanglingEnds;
    private javax.swing.ButtonGroup buttonGroupGUPairs;
    private javax.swing.ButtonGroup buttonGroupSuboptimal;
    private javax.swing.JButton jButtonExpandCollapse;
    private javax.swing.JButton jButtonFold;
    private javax.swing.JButton jButtonFoldMultiple;
    private javax.swing.JButton jButtonFoldSuboptimal;
    private javax.swing.JButton jButtonHelp;
    private javax.swing.JButton jButtonParamFile;
    private javax.swing.JButton jButtonRefold;
    private javax.swing.JButton jButtonSelectAll;
    private javax.swing.JButton jButtonSelectHits;
    private javax.swing.JButton jButtonSelectQueries;
    private javax.swing.JCheckBox jCheckBoxCirc;
    private javax.swing.JCheckBox jCheckBoxNoLP;
    private javax.swing.JCheckBox jCheckBoxParamFile;
    private javax.swing.JCheckBox jCheckBoxTemperature;
    private javax.swing.JCheckBox jCheckBoxTemperatureRange;
    private javax.swing.JFormattedTextField jFormattedTextFieldRandom;
    private javax.swing.JFormattedTextField jFormattedTextFieldRange;
    private javax.swing.JFormattedTextField jFormattedTextFieldTemperature;
    private javax.swing.JFormattedTextField jFormattedTextFieldTemperatureLower;
    private javax.swing.JFormattedTextField jFormattedTextFieldTemperatureUpper;
    private javax.swing.JLabel jLabelHyphen;
    private javax.swing.JLabel jLabelRandom;
    private javax.swing.JLabel jLabelRange;
    private javax.swing.JLabel jLabelTemperature;
    private javax.swing.JLabel jLabelTemperatureRange;
    private javax.swing.JPanel jPanelMFE;
    private javax.swing.JPanel jPanelMultiple;
    private javax.swing.JPanel jPanelOptions;
    private javax.swing.JPanel jPanelSuboptimal;
    private javax.swing.JRadioButton jRadioButtonD0;
    private javax.swing.JRadioButton jRadioButtonD1;
    private javax.swing.JRadioButton jRadioButtonD2;
    private javax.swing.JRadioButton jRadioButtonD3;
    private javax.swing.JRadioButton jRadioButtonGU;
    private javax.swing.JRadioButton jRadioButtonNoCloseGU;
    private javax.swing.JRadioButton jRadioButtonNoGU;
    private javax.swing.JRadioButton jRadioButtonPairProbabilities;
    private javax.swing.JRadioButton jRadioButtonPositionalEntropy;
    private javax.swing.JRadioButton jRadioButtonRandom;
    private javax.swing.JRadioButton jRadioButtonRange;
    private javax.swing.JScrollPane jScrollPaneFoldOutput;
    private javax.swing.JScrollPane jScrollPaneMultiple;
    private javax.swing.JScrollPane jScrollPaneQueries;
    private javax.swing.JScrollPane jScrollPaneSuboptimal;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JTable jTableMultiple;
    private javax.swing.JTable jTableSuboptimal;
    private javax.swing.JTextArea jTextAreaFoldOutput;
    private javax.swing.JTextField jTextFieldParamFile;
    private javax.swing.JTree jTreeQueries;
    private com.kitfox.svg.app.beans.SVGPanel svgPanel;
    // End of variables declaration//GEN-END:variables
}
