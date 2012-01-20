package miRNAFolding;

import java.io.IOException;

/**
 * @author Eirik Krogstad
 * Adapted from http://blog.bensmann.com/piping-between-processes
 */
public class Piper implements java.lang.Runnable {

    private java.io.InputStream input;
    private java.io.OutputStream output;

    public Piper(java.io.InputStream input, java.io.OutputStream output) {
        this.input = input;
        this.output = output;
    }

    @Override
    public void run() {
        try {
            byte[] b = new byte[512];
            int read = 1;
            while (read > -1) {
                read = input.read(b, 0, b.length);
                if (read > -1) {
                    output.write(b, 0, read);
                }
            }
            input.close();
            output.close();
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
}
