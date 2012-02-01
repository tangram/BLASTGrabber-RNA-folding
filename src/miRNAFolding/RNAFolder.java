package miRNAFolding;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

/**
 * RNAFolder contains a method for folding a sequence using the Vienna RNA package command line tools.
 *
 * @author Eirik Krogstad
 * @author Petter Hannevold
 */
public class RNAFolder {
    /**
     * Folds an RNA sequence using the Vienna RNA package command line tool RNAfold, and passes its output
     * to RNAplot. RNAplot generates an SVG structure plot.
     *
     * @param sequence  String containing a single input sequence in FASTA format
     * @param options   String containing command line parameters for RNAfold
     * @return          String containing the standard output of RNAfold
     */
    public static String foldSequence(String sequence, String options) {
        long before = System.currentTimeMillis();
        //String path = RNAFolder.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String binPath = "bin/";

        String osExtension = "";
        if (OsDetector.isWindows())
            osExtension = ".exe";

        Runtime runtime = Runtime.getRuntime();
        StringBuilder sb = new StringBuilder();

        try {
            Process rnaFold = runtime.exec(binPath + "RNAfold" + osExtension + options);

            BufferedOutputStream rnaFoldIn = new BufferedOutputStream(rnaFold.getOutputStream());
            rnaFoldIn.write(sequence.getBytes());
            rnaFoldIn.flush();
            rnaFoldIn.close();

            Process rnaPlot = runtime.exec(binPath + "RNAplot" + osExtension + " -o svg");

            BufferedInputStream rnaFoldOut = new BufferedInputStream(rnaFold.getInputStream());
            BufferedOutputStream rnaPlotIn = new BufferedOutputStream(rnaPlot.getOutputStream());

            byte[] buffer = new byte[512];
            int read = 1;
            while (read > -1) {
                read = rnaFoldOut.read(buffer);
                if (read > -1) {
                    rnaPlotIn.write(buffer, 0, read);
                    sb.append(new String(buffer, 0, read));
                }
            }
            rnaFoldOut.close();
            rnaPlotIn.close();

            rnaPlot.waitFor();

        } catch (IOException e) {
            System.out.println(e.getMessage());
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
        }
        long after = System.currentTimeMillis();
        System.out.println("runSequencePlot: " + (after - before));
        return sb.toString();
    }
}
