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
        String binPath = "bin/";

        String osExtension = "";
        if (OsDetector.isWindows())
            osExtension = ".exe";

        Runtime runtime = Runtime.getRuntime();

        // sequence data | RNAFold -noPS | RNAPlot -o svg
        try {
            Process rnaFold = runtime.exec(binPath + "RNAfold" + osExtension + " -p -d2 -noLP");

            OutputStream rnaFoldIn = rnaFold.getOutputStream();
            rnaFoldIn.write(sequence.getBytes());
            rnaFoldIn.flush();
            rnaFoldIn.close();

            Process rnaPlot = runtime.exec(binPath + "RNAplot" + osExtension + " -o svg");

            Piper pipe = new Piper(rnaFold.getInputStream(), rnaPlot.getOutputStream());
            new Thread(pipe).start();
            rnaPlot.waitFor();

        } catch (IOException e) {
            System.out.println(e.getMessage());
            return false;
        } catch (InterruptedException e) {
            System.out.println(e.getMessage());
            return false;
        }
        return true;
    }
}
