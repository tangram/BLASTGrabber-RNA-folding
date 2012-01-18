package RNAFolding;

import java.io.*;

public class Fold{
	public static void main(String args[]) throws IOException {
            new FoldGraphic(new HairpinStructure("acgtgccacgauucaacgtggcacag"));
            //new Fold();
	}
        
        public Fold() throws IOException{
            
            File file = new File("bin/test.txt");
            System.out.println(file.getAbsolutePath());
            file.createNewFile();
            //System.out.println(file.exists());
            //BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            //writer.write("Hei");
            //writer.close();
            
        }
}