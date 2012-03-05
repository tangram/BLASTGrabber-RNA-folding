package miRNAFolding;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import javax.swing.JOptionPane;

/**
 * RNAFolder contains methods for folding a sequence using the Vienna RNA package command line tools.
 *
 * @author Eirik Krogstad
 * @author Petter Hannevold
 */
public class RNAFolder {
    public final static String BINPATH = "bin/";
    public final static String EXTENSION = OsDetector.getOSExtension();
    public final static String BASEPATH = FrmClipboard.BASEPATH;

    /**
     * General method for running processes with a given input
     *
     * @param input         String containing the standard input for the process
     * @param commandline   String containing the actual command, including parameters
     * @param wait          If true, waits for the process to finish before continuing
     * @return              String containing the standard output from the process
     */
    private static String runProcess(String input, String commandline, boolean wait) {
        Runtime runtime = Runtime.getRuntime();
        StringBuilder sb = new StringBuilder();
        byte[] buffer = new byte[512];
        int read = 1;

        try {
            Process process = runtime.exec(commandline);

            BufferedOutputStream stdin = new BufferedOutputStream(process.getOutputStream());
            BufferedInputStream stdout = new BufferedInputStream(process.getInputStream());
            stdin.write(input.getBytes());
            stdin.flush();
            stdin.close();

            while (read > -1) {
                read = stdout.read(buffer);
                if (read > -1)
                    sb.append(new String(buffer, 0, read));
            }
            stdout.close();

            if (wait)
                process.waitFor();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        } catch (InterruptedException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
        }

        return sb.toString();
    }

    /**
     * Folds an RNA sequence using the Vienna RNA package command line tool RNAfold
     *
     * @param sequence  String containing a single input sequence in FASTA format
     * @param options   String containing command line parameters for RNAfold
     * @return          String containing the standard output of RNAfold
     */
    public static String foldSequence(String sequence, String options) {
        return runProcess(sequence, BASEPATH + BINPATH + "RNAfold" + EXTENSION + options, false);
    }

    /**
     * Folds multiple suboptimal RNA sequences using the Vienna RNA package command line tool RNAsubopt
     *
     * @param sequence  String containing a single input sequence in FASTA format
     * @param options   String containing command line parameters for RNAsubopt
     * @return          String containing the standard output of RNAsubopt
     */
    public static String foldSuboptimals(String sequence, String options) {
        return runProcess(sequence, BASEPATH + BINPATH + "RNAsubopt" + EXTENSION + options, false);
    }

    /**
     * Calculates Free Energy for given structures. Structures must contain alternating sequences and
     * folding notation as produced by other Vienna tools, one of each per structure, all separated by newlines.
     *
     * @param structures    String containing alternating sequences and structures for a series of foldings
     * @param options       String containing command line parameters for RNAeval
     * @return              String containing the standard output of RNAeval
     */
    public static String evalSuboptimals(String structures, String options) {
        return runProcess(structures, BASEPATH + BINPATH + "RNAeval" + EXTENSION + options, false);
    }

    /**
     * Generates an SVG structure plot using the Vienna RNA package command line tool RNAplot
     *
     * @param structure  String containing a folding notation as produced by eg. RNAfold
     */
    public static void generatePlots(String structure) {
        runProcess(structure, BASEPATH + BINPATH + "RNAplot" + EXTENSION + " -t 0 -o svg", true);
    }
}
