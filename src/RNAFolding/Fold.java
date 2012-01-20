package RNAFolding;

<<<<<<< HEAD
import java.io.*;

public class Fold{
	public static void main(String args[]) throws IOException {
            new FoldGraphic(new HairpinStructure("acgtgccacgauucaacgtggcacag"));
            //new Fold();
=======
public class Fold {
	public static void main(String args[]) {
		new FoldGraphic(new HairpinStructure("acgugccacgauucaacguggcacag"));
>>>>>>> 5be7e0bccdbb88f95a5cfb2b38fe6a21da64c8e7
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