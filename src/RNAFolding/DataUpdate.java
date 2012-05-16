package RNAFolding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.zip.GZIPInputStream;
import javax.swing.JOptionPane;

/**
 * DataUpdate contains methods to download or update miRNA data from http://www.mirbase.org/
 *
 * @author Eirik Krogstad
 */
public class DataUpdate {
    public final static String BASEPATH = FrmClipboard.BASEPATH;
    public final static String DATAPATH = "dat/";
    public final static int MAXTRANSFER = 10000000;

    /**
     * Checks if miRBase data is present, if not, prompts the user with the choice to download.
     * Upon accepting, the file is downloaded and extracted.
     */
    public static void updatemiRBaseData() {
        if (!new File(BASEPATH + DATAPATH + "miRNA.dat").exists()) {
            Object[] options = {"Yes", "No", "Never"};
            int choice = JOptionPane.showOptionDialog(null,
                    "No miRNA data exists. Download from http://www.mirbase.org/?\n" +
                    "This will take a few seconds, depending on your connection",
                    "miRNA data download",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null, options, options[0]);
            if (choice == 0) {
                try {
                    new File(BASEPATH + DATAPATH).mkdir();
                    URL miRNADataURL = new URL("ftp://mirbase.org/pub/mirbase/CURRENT/miRNA.dat.gz");
                    ReadableByteChannel channel = Channels.newChannel(miRNADataURL.openStream());
                    FileOutputStream outputstream = new FileOutputStream(BASEPATH + DATAPATH + "miRNA.dat.gz");
                    outputstream.getChannel().transferFrom(channel, 0, MAXTRANSFER);
                    outputstream.close();

                    GZIPInputStream inputstream = new GZIPInputStream(new FileInputStream(BASEPATH + DATAPATH + "miRNA.dat.gz"));
                    outputstream = new FileOutputStream(BASEPATH + DATAPATH + "miRNA.dat");
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputstream.read(buffer)) > 0) {
                        outputstream.write(buffer, 0, length);
                    }
                    inputstream.close();
                    outputstream.close();

                    new File(BASEPATH + DATAPATH + "miRNA.dat.gz").delete();
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(null, "Download and decompression of miRNA.dat failed:\n" + e.getMessage());
                }
            } else if (choice == 2) {
                try {
                    new File(BASEPATH + DATAPATH).mkdir();
                    new File(BASEPATH + DATAPATH + "miRNA.dat").createNewFile();
                } catch (Exception e) { }
            }
        }
    }
}
