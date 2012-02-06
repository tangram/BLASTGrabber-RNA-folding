package miRNAFolding;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * RNAFolder contains methods for folding a sequence using the Vienna RNA package command line tools.
 *
 * @author Eirik Krogstad
 * @author Petter Hannevold
 */
public class RNAFolder {
    public final static String BINPATH = "bin/";
    public final static String EXTENSION = OsDetector.getOSExtension();

    /**
     * General method for running processes with a given input
     *
     * @param input         String containing the standard input for the process
     * @param commandline   String containing the actual command, including parameters
     * @param wait          If true, waits for the process to finish before continuing
     * @return              String containing the standard output from the process
     */
    private static String runProcess(String input, String commandline, boolean wait) {
        //private static String path = RNAFolder.class.getProtectionDomain().getCodeSource().getLocation().getPath();
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
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
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
        return runProcess(sequence, BINPATH + "RNAfold" + EXTENSION + options, false);
    }

    /**
     * Folds multiple suboptimal RNA sequences using the Vienna RNA package command line tool RNAsubopt
     *
     * @param sequence  String containing a single input sequence in FASTA format
     * @param options   String containing command line parameters for RNAsubopt
     * @return          String containing the standard output of RNAsubopt
     */
    public static String foldSuboptimals(String sequence, String options) {
        return runProcess(sequence, BINPATH + "RNAsubopt" + EXTENSION + options, false);
    }

    /**
     * Generates an SVG structure plot using the Vienna RNA package command line tool RNAplot
     *
     * @param structure  String containing a folding notation as produced by eg. RNAfold
     */
    public static void generatePlots(String structure) {
        runProcess(structure, BINPATH + "RNAplot" + EXTENSION + " -t 0 -o svg", true);
    }
}
