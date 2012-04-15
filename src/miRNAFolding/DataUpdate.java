package miRNAFolding;

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

    public static void updatemiRBaseData() {
        if (!new File(BASEPATH + DATAPATH + "miRNA.dat").exists()) {
            int choice = JOptionPane.showConfirmDialog(null,
                    "No miRNA data exists. Download from http://www.mirbase.org/?\nThis will take a few seconds, depending on your connection",
                    "miRNA data download",
                    JOptionPane.YES_NO_OPTION);
            if (choice == JOptionPane.YES_OPTION) {
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
            }
        }
    }
}
