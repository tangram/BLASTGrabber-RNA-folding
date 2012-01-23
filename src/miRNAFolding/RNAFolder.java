package miRNAFolding;

import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Eirik Krogstad
 * @author Petter Hannevold
 */
public class RNAFolder {
    public static boolean runSequencePlot(String sequence, FrmOptions options) {
        //String path = RNAFolder.class.getProtectionDomain().getCodeSource().getLocation().getPath();

        String osExtension = "";
        if (OsDetector.isWindows())
            osExtension = ".exe";

        Runtime runtime = Runtime.getRuntime();

        // sequence data | RNAFold -noPS | RNAPlot -o svg
        try {
            Process rnaFold = runtime.exec("RNAfold" + osExtension + " -noPS");

            OutputStream rnaFoldIn = rnaFold.getOutputStream();
            rnaFoldIn.write(sequence.getBytes());
            rnaFoldIn.flush();
            rnaFoldIn.close();

            Process rnaPlot = runtime.exec("RNAplot" + osExtension + " -o svg");

            Piper pipe = new Piper(rnaFold.getInputStream(), rnaPlot.getOutputStream());
            new Thread(pipe).start();
            rnaPlot.waitFor();

        } catch (IOException io) {
            System.out.println(io.toString());
            return false;
        } catch (InterruptedException ie) {
            System.out.println(ie.toString());
            return false;
        }
        return true;
    }
}
