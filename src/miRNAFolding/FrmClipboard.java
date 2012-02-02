/*
 * frmClipboard.java
 */
package miRNAFolding;

import BLASTGrabber.Facade.BLASTGrabberHit;
import BLASTGrabber.Facade.BLASTGrabberQuery;
import java.awt.Color;
import java.awt.Dimension;
import java.util.HashMap;
import java.util.Iterator;
import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;

/**
 * @author Eirik Krogstad
 * @author Petter Hannevold
 */
public class FrmClipboard extends javax.swing.JInternalFrame {
    private HashMap<String, BLASTGrabberQuery> queries;
    private JDesktopPane desktop;
    private BLASTGrabber.Facade facade;

    private DefaultListModel listModel;
    private DefaultTableModel suboptimalTableModel;
    private DefaultTableModel multipleTableModel;
    private String[] standardColumns = {"Number", "ID", "Structure", "kCal/mol"};
    private String[] suboptimalRandomColumns = {"Number", "ID", "Structure"};

   // actual pre-miRNA from human X chromosome for testing
   private String test = ">hsa-let-7f-2 MI0000068\nUGUGGGAUGAGGUAGUAGAUUGUAUAGUUUUAGGGUCAUACCCCAUCUUGGAGAUAACUAUACAGUCUACUGUCUUUCCCACG";

    /** Creates new form FrmClipboard */
    public FrmClipboard() {
        // needed to ensure proper formatting of JFormattedTextFields
        Locale.setDefault(Locale.ENGLISH);
        initComponents();
    }

    public void init(HashMap<String, BLASTGrabberQuery> queries, JDesktopPane desktop, BLASTGrabber.Facade facade) {
        this.queries = queries;
        this.desktop = desktop;
        this.facade = facade;

        listModel = new DefaultListModel();
        jListQueries.setModel(listModel);

        suboptimalTableModel = new DefaultTableModel(standardColumns, 0);
        jTableSuboptimal.setModel(suboptimalTableModel);
        jTableSuboptimal.getSelectionModel().addListSelectionListener(new TableListener());

        multipleTableModel = new DefaultTableModel(suboptimalRandomColumns, 0);
        jTableMultiple.setModel(multipleTableModel);
        jTableMultiple.getSelectionModel().addListSelectionListener(new TableListener());

        int pos = 0;
        Iterator<String> itQ = queries.keySet().iterator();

        while (itQ.hasNext()) {
            String queryName = itQ.next();
            pos = listModel.getSize();
            listModel.add(pos, queryName + ":");
            Iterator<BLASTGrabberHit> itH = queries.get(queryName).Hits.iterator();
            while (itH.hasNext()) {
                pos = listModel.getSize();
                listModel.add(pos, "  " + itH.next().SequenceHeader);
            }
        }

        /*
        ArrayList<String> fasta = facade.getFASTACustomDBSequences(queries);

        Iterator<String> itS = fasta.iterator();

        while (itS.hasNext()) {
            jTextAreaTemp.setText(itS.next());
        }
        */
    }

    private void generatePlot(String folding) {
        RNAFolder.generatePlots(folding);

        jTextAreaFoldOutput.setText(folding);

        String svgToAnnotate = folding.split("\\s")[0].substring(1);
        if (svgToAnnotate.length() > 42)
            svgToAnnotate = svgToAnnotate.substring(0, 42);
        svgToAnnotate += "_ss.svg";
        String svgPath = ColorAnnotator.annnotateSVG(svgToAnnotate, jRadioButtonPositionalEntropy.isSelected());

        loadSVG(svgPath);
    }

    /**
     * Build a String with common command line parameters for Vienna RNA command line tools from
     * selected GUI components.
     *
     * @return  String with selected command line parameters
     */
    private String buildOptionsString() {
        StringBuilder sb = new StringBuilder();

        if (jRadioButtonTemperature.isSelected()) {
            sb.append(" -T ");
            sb.append(jFormattedTextFieldTemperature.getText());
        } else {

        }
        if (jCheckBoxCirc.isSelected())
            sb.append(" --circ");
        if (jCheckBoxNoLP.isSelected())
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

        return sb.toString();
    }

    /**
     * Load an SVG file from filename into svgPanel
     *
     * @param filename  String containing the filename to load
     */
    private void loadSVG(String filename) {
        try {
            svgPanel.getSvgUniverse().clear();
            svgPanel.setSvgURI(new File(filename).toURI());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        svgPanel.repaint();
    }

    // to be removed - for testing purposes
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {
            System.out.println("Error setting native LAF: " + e);
        }
        JFrame main = new JFrame("Test frame");
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JDesktopPane desktop = new JDesktopPane();
        main.add(desktop);

        FrmClipboard intFrm = new FrmClipboard();

        BLASTGrabberHit testHit1 = new BLASTGrabberHit();
        testHit1.SequenceHeader = ">test Hit one-1 (EINs) uno-en [singular mono alpha]";
        BLASTGrabberHit testHit2 = new BLASTGrabberHit();
        testHit2.SequenceHeader = ">test Hit two22 duo-bi (ZWEI-too to2) II [dual plural beta no. 2]";

        BLASTGrabberQuery testQuery = new BLASTGrabberQuery();
        testQuery.Name = "Test query";
        testQuery.Hits.add(testHit1);
        testQuery.Hits.add(testHit2);

        HashMap<String, BLASTGrabberQuery> testData = new HashMap<String, BLASTGrabberQuery>();
        testData.put(">test category 1 [RANDOM garb] age-2 make [THIS seemlike] actualdata", testQuery);

        intFrm.init(testData, desktop, null);
        desktop.add(intFrm);
        intFrm.setVisible(true);
        desktop.setVisible(true);
        main.setVisible(true);
        desktop.setPreferredSize(new Dimension(1300, 750));
        desktop.setBackground(Color.BLACK);
        main.pack();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroupColorAnnotation = new javax.swing.ButtonGroup();
        buttonGroupTemperature = new javax.swing.ButtonGroup();
        buttonGroupDanglingEnds = new javax.swing.ButtonGroup();
        buttonGroupGUPairs = new javax.swing.ButtonGroup();
        buttonGroupSuboptimal = new javax.swing.ButtonGroup();
        jScrollPaneQueries = new javax.swing.JScrollPane();
        jListQueries = new javax.swing.JList();
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
        jRadioButtonTemperature = new javax.swing.JRadioButton();
        jRadioButtonTemperatureRange = new javax.swing.JRadioButton();
        jFormattedTextFieldTemperatureLower = new javax.swing.JFormattedTextField();
        jLabelTemperatureRange = new javax.swing.JLabel();
        jFormattedTextFieldTemperatureUpper = new javax.swing.JFormattedTextField();
        jLabelHyphen = new javax.swing.JLabel();
        jButtonRefold = new javax.swing.JButton();

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setPreferredSize(new java.awt.Dimension(1101, 705));

        jListQueries.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jScrollPaneQueries.setViewportView(jListQueries);

        svgPanel.setBackground(new java.awt.Color(255, 255, 255));
        svgPanel.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(180, 180, 180)));
        svgPanel.setFont(new java.awt.Font("SansSerif", 0, 11));
        svgPanel.setPreferredSize(new java.awt.Dimension(400, 400));
        svgPanel.setScaleToFit(true);
        svgPanel.setUseAntiAlias(true);

        jRadioButtonPairProbabilities.setBackground(new java.awt.Color(255, 255, 255));
        buttonGroupColorAnnotation.add(jRadioButtonPairProbabilities);
        jRadioButtonPairProbabilities.setSelected(true);
        jRadioButtonPairProbabilities.setText("Show pair probabilities");
        jRadioButtonPairProbabilities.setOpaque(false);
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
                    .addComponent(jRadioButtonPairProbabilities)
                    .addComponent(jRadioButtonPositionalEntropy, javax.swing.GroupLayout.Alignment.TRAILING))
                .addContainerGap(251, Short.MAX_VALUE))
        );
        svgPanelLayout.setVerticalGroup(
            svgPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, svgPanelLayout.createSequentialGroup()
                .addContainerGap(349, Short.MAX_VALUE)
                .addComponent(jRadioButtonPairProbabilities)
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
        jTextAreaFoldOutput.setFont(new java.awt.Font("Monospaced", 0, 11));
        jTextAreaFoldOutput.setRows(5);
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
                .addComponent(jScrollPaneFoldOutput, javax.swing.GroupLayout.PREFERRED_SIZE, 138, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 193, Short.MAX_VALUE)
                .addComponent(jButtonFold)
                .addContainerGap())
        );

        jTabbedPane.addTab("MFE structure", jPanelMFE);

        jTableSuboptimal.setAutoCreateRowSorter(true);
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

        jLabelRange.setText("kCal/mol of MFE structure");

        jLabelRandom.setText("suboptimal structures");

        javax.swing.GroupLayout jPanelSuboptimalLayout = new javax.swing.GroupLayout(jPanelSuboptimal);
        jPanelSuboptimal.setLayout(jPanelSuboptimalLayout);
        jPanelSuboptimalLayout.setHorizontalGroup(
            jPanelSuboptimalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelSuboptimalLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelSuboptimalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPaneSuboptimal, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 630, Short.MAX_VALUE)
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
                .addComponent(jScrollPaneSuboptimal, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanelSuboptimalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonRange)
                    .addComponent(jFormattedTextFieldRange, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelRange))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelSuboptimalLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonFoldSuboptimal)
                    .addComponent(jRadioButtonRandom)
                    .addComponent(jFormattedTextFieldRandom, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelRandom))
                .addGap(11, 11, 11))
        );

        jTabbedPane.addTab("Suboptimal structures", jPanelSuboptimal);

        jTableMultiple.setAutoCreateRowSorter(true);
        jScrollPaneMultiple.setViewportView(jTableMultiple);

        jButtonFoldMultiple.setText("Fold selected sequences");

        javax.swing.GroupLayout jPanelMultipleLayout = new javax.swing.GroupLayout(jPanelMultiple);
        jPanelMultiple.setLayout(jPanelMultipleLayout);
        jPanelMultipleLayout.setHorizontalGroup(
            jPanelMultipleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMultipleLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelMultipleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPaneMultiple, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 630, Short.MAX_VALUE)
                    .addComponent(jButtonFoldMultiple))
                .addContainerGap())
        );
        jPanelMultipleLayout.setVerticalGroup(
            jPanelMultipleLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelMultipleLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneMultiple, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonFoldMultiple)
                .addContainerGap())
        );

        jTabbedPane.addTab("Multiple runs", jPanelMultiple);

        jFormattedTextFieldTemperature.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextFieldTemperature.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormattedTextFieldTemperature.setText("37");

        jLabelTemperature.setText("°C");

        buttonGroupGUPairs.add(jRadioButtonGU);
        jRadioButtonGU.setText("Allow GU pairs");

        buttonGroupGUPairs.add(jRadioButtonNoCloseGU);
        jRadioButtonNoCloseGU.setText("Disallow GU pairs at ends of helices");

        buttonGroupGUPairs.add(jRadioButtonNoGU);
        jRadioButtonNoGU.setSelected(true);
        jRadioButtonNoGU.setText("Disallow GU pairs");

        jCheckBoxCirc.setText("Assume circular RNA");

        buttonGroupDanglingEnds.add(jRadioButtonD0);
        jRadioButtonD0.setSelected(true);
        jRadioButtonD0.setText("Ignore dangling ends");

        buttonGroupDanglingEnds.add(jRadioButtonD1);
        jRadioButtonD1.setText("Only unpaired bases can participate in at most one dangling end");

        buttonGroupDanglingEnds.add(jRadioButtonD2);
        jRadioButtonD2.setText("Dangling energies will be added for the bases adjacent to a helix on both sides");

        buttonGroupDanglingEnds.add(jRadioButtonD3);
        jRadioButtonD3.setText("Allow coaxial stacking of adjacent helices in multi-loops");

        jCheckBoxNoLP.setSelected(true);
        jCheckBoxNoLP.setText("Disallow lonely pairs (helices of length 1)");

        buttonGroupTemperature.add(jRadioButtonTemperature);
        jRadioButtonTemperature.setSelected(true);
        jRadioButtonTemperature.setText("Scale to temperature");

        buttonGroupTemperature.add(jRadioButtonTemperatureRange);
        jRadioButtonTemperatureRange.setText("Test temperature range");

        jFormattedTextFieldTemperatureLower.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextFieldTemperatureLower.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormattedTextFieldTemperatureLower.setText("37");

        jLabelTemperatureRange.setText("°C");

        jFormattedTextFieldTemperatureUpper.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));
        jFormattedTextFieldTemperatureUpper.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jFormattedTextFieldTemperatureUpper.setText("37");

        jLabelHyphen.setText("-");

        jButtonRefold.setText("Refold");
        jButtonRefold.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonRefoldActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanelOptionsLayout = new javax.swing.GroupLayout(jPanelOptions);
        jPanelOptions.setLayout(jPanelOptionsLayout);
        jPanelOptionsLayout.setHorizontalGroup(
            jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelOptionsLayout.createSequentialGroup()
                .addContainerGap(577, Short.MAX_VALUE)
                .addComponent(jButtonRefold)
                .addContainerGap())
            .addGroup(jPanelOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanelOptionsLayout.createSequentialGroup()
                        .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jRadioButtonTemperatureRange)
                            .addComponent(jRadioButtonTemperature))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanelOptionsLayout.createSequentialGroup()
                                .addComponent(jFormattedTextFieldTemperature, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabelTemperature))
                            .addGroup(jPanelOptionsLayout.createSequentialGroup()
                                .addComponent(jFormattedTextFieldTemperatureLower, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabelHyphen)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jFormattedTextFieldTemperatureUpper, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jLabelTemperatureRange))))
                    .addComponent(jCheckBoxCirc)
                    .addComponent(jCheckBoxNoLP)
                    .addComponent(jRadioButtonD0)
                    .addComponent(jRadioButtonD1)
                    .addComponent(jRadioButtonD2)
                    .addComponent(jRadioButtonD3)
                    .addComponent(jRadioButtonGU)
                    .addComponent(jRadioButtonNoCloseGU)
                    .addComponent(jRadioButtonNoGU))
                .addContainerGap(243, Short.MAX_VALUE))
        );
        jPanelOptionsLayout.setVerticalGroup(
            jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanelOptionsLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonTemperature)
                    .addComponent(jFormattedTextFieldTemperature, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelTemperature))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanelOptionsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButtonTemperatureRange)
                    .addComponent(jFormattedTextFieldTemperatureLower, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelTemperatureRange)
                    .addComponent(jFormattedTextFieldTemperatureUpper, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabelHyphen))
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 4, Short.MAX_VALUE)
                .addComponent(jButtonRefold)
                .addContainerGap())
        );

        jTabbedPane.addTab("Options", jPanelOptions);
        jPanelOptions.getAccessibleContext().setAccessibleName("");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(svgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(18, 18, 18)
                        .addComponent(jTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 655, Short.MAX_VALUE))
                    .addComponent(jScrollPaneQueries, javax.swing.GroupLayout.DEFAULT_SIZE, 1073, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPaneQueries, javax.swing.GroupLayout.DEFAULT_SIZE, 230, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane, 0, 0, Short.MAX_VALUE)
                    .addComponent(svgPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 404, Short.MAX_VALUE))
                .addContainerGap())
        );

        jTabbedPane.getAccessibleContext().setAccessibleName("");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private class TableListener implements ListSelectionListener {
        @Override
        public void valueChanged(ListSelectionEvent e) {
            JTable table = (JTable) ((DefaultListSelectionModel) e.getSource()).getListSelectionListeners()[1];
            int row = table.getSelectedRow();
            if (row > -1) {
                String sequence = test + "\n" + table.getValueAt(row, 2);
                generatePlot(sequence);
            }
        }
    }

    private void jButtonFoldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFoldActionPerformed
        String output = RNAFolder.foldSequence(test, " -p" + buildOptionsString());
        generatePlot(output);
    }//GEN-LAST:event_jButtonFoldActionPerformed

    private void jButtonRefoldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonRefoldActionPerformed
        jButtonFoldActionPerformed(evt);
    }//GEN-LAST:event_jButtonRefoldActionPerformed

    private void jRadioButtonPairProbabilitiesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonPairProbabilitiesActionPerformed
        String output = jTextAreaFoldOutput.getText();
        if (!output.isEmpty())
            generatePlot(output);
    }//GEN-LAST:event_jRadioButtonPairProbabilitiesActionPerformed

    private void jRadioButtonPositionalEntropyActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonPositionalEntropyActionPerformed
        jRadioButtonPairProbabilitiesActionPerformed(evt);
    }//GEN-LAST:event_jRadioButtonPositionalEntropyActionPerformed

    private void jButtonFoldSuboptimalActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonFoldSuboptimalActionPerformed
        StringBuilder sb = new StringBuilder(buildOptionsString());
        if (jRadioButtonRange.isSelected()) {
            sb.append(" -e ");
            sb.append(jFormattedTextFieldRange.getText());
        } else {
            sb.append(" -p ");
            sb.append(jFormattedTextFieldRandom.getText());
        }

        String output = RNAFolder.foldSuboptimals(test, sb.toString());
        String[] outputLines = output.split("[\r|\n]+");
        String name = outputLines[0];

        jTableSuboptimal.clearSelection();
        DefaultTableModel newModel;
        if (jRadioButtonRange.isSelected()) {
            newModel = new DefaultTableModel(standardColumns, 0);
            for (int i = 3; i < outputLines.length; i++) {
                String[] line = outputLines[i].split("\\s");
                Object[] row = {(Integer) (i-2), name, line[0], line[1]};
                newModel.addRow(row);
            }
        } else {
            newModel = new DefaultTableModel(suboptimalRandomColumns, 0);
            for (int i = 2; i < outputLines.length; i++) {
                Object[] row = {(Integer) (i-1), name, outputLines[i], "-"};
                newModel.addRow(row);
            }
        }
        jTableSuboptimal.setModel(newModel);
    }//GEN-LAST:event_jButtonFoldSuboptimalActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroupColorAnnotation;
    private javax.swing.ButtonGroup buttonGroupDanglingEnds;
    private javax.swing.ButtonGroup buttonGroupGUPairs;
    private javax.swing.ButtonGroup buttonGroupSuboptimal;
    private javax.swing.ButtonGroup buttonGroupTemperature;
    private javax.swing.JButton jButtonFold;
    private javax.swing.JButton jButtonFoldMultiple;
    private javax.swing.JButton jButtonFoldSuboptimal;
    private javax.swing.JButton jButtonRefold;
    private javax.swing.JCheckBox jCheckBoxCirc;
    private javax.swing.JCheckBox jCheckBoxNoLP;
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
    private javax.swing.JList jListQueries;
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
    private javax.swing.JRadioButton jRadioButtonTemperature;
    private javax.swing.JRadioButton jRadioButtonTemperatureRange;
    private javax.swing.JScrollPane jScrollPaneFoldOutput;
    private javax.swing.JScrollPane jScrollPaneMultiple;
    private javax.swing.JScrollPane jScrollPaneQueries;
    private javax.swing.JScrollPane jScrollPaneSuboptimal;
    private javax.swing.JTabbedPane jTabbedPane;
    private javax.swing.JTable jTableMultiple;
    private javax.swing.JTable jTableSuboptimal;
    private javax.swing.JTextArea jTextAreaFoldOutput;
    private com.kitfox.svg.app.beans.SVGPanel svgPanel;
    // End of variables declaration//GEN-END:variables
}
