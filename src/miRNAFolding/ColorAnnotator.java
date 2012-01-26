package miRNAFolding;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Pattern;

/**
 * ColorAnnotator contains methods for reading pair probabilities and computing positional entropy from RNAfold dot plot
 * files. These can be passed to annotateSVG and written to a new SVG file.
 * 
 * @author Eirik Krogstad
 */
public class ColorAnnotator {
    
    /**
     * Reads pair probabilities from an RNAfold dot plot PostScript file.
     * 
     * @param filepath  String containing filepath to PostScript file
     * @return          ArrayList containing pair identifiers and pair probabilities
     */
    public static ArrayList<String[]> readPairProbabilities(String filepath) {
        Pattern dataRegex = Pattern.compile("(\\d+)\\s+(\\d+)\\s+([0-9.Ee-]+)\\s+ubox");
        ArrayList data = new ArrayList();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath));
            String line = null;
            String[] splitLine = null;

            while ((line = reader.readLine()) != null) {
                if (line.endsWith("ubox")) {
                    splitLine = dataRegex.split(line);
                    data.add(splitLine);
                }
            }
        } catch (FileNotFoundException e) {

        } catch (IOException e) {

        }

        return data;
    }

     /**
     * Computes positional entropy from pair probability data as outputted from method readPairProbabilities. 
     * 
     * @param data      ArrayList containing pair identifiers and pair probabilities
     * @return          ArrayList containing pair identifiers and positional entropy
     */
    public static ArrayList<String[]> computePositionalEntropy(ArrayList<String[]> data) {
        for (String[] pair : data) {
            double p = Double.parseDouble(pair[2]);
            double pSq = p * p;
            Double entropy = (pSq > 0) ? pSq * Math.log(pSq) : 0;
            pair[2] = entropy.toString();
        }
        return data;
    }

     /**
     * Calls XML routines to write pair probabilities or positional entropy as color annnotation to a new SVG. file
     * 
     * @param data      ArrayList containing pair identifiers and pair probabilities or positional entropy
     * @return          String containing filepath to new SVG file
     */
    public static String annnotateSVG(String filename, ArrayList<String[]> data) {
        String annotatedFile = "";
        // call XML routines from another class
        // returns filepath for new SVG
        return annotatedFile;
    }
}
