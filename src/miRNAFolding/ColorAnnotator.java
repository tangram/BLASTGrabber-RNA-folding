package miRNAFolding;

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.Serializer;

/**
 * ColorAnnotator contains methods for reading pair probabilities and computing positional entropy from RNAfold dot plot
 * files. These can be passed to annotateSVG and written to a new SVG file.
 *
 * @author Eirik Krogstad
 */
public class ColorAnnotator {
    
    /**
     * A small data structure to combine data points and pair info
     */
    public static class Dataset {
        public ArrayList<String[]> data;
        public ArrayList<String[]> pairs;
        public boolean isEntropy;
    }
    
    /**
     * Reads pair probabilities from an RNAfold dot plot PostScript file.
     *
     * @param filepath  String containing filepath to PostScript file
     * @return          Dataset containing pair identifiers and pair probabilities as String[]
     */
    public static Dataset readPairProbabilities(String filepath) {
        String ubox = "\\d+\\s+\\d+\\s+[0-9.Ee-]+\\s+ubox";
        String lbox = "\\d+\\s+\\d+\\s+[0-9.Ee-]+\\s+lbox";
        ArrayList<String[]> data = new ArrayList();
        ArrayList<String[]> pairs = new ArrayList();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath));
            String line = null;
            String[] splitLine = null;

            while ((line = reader.readLine()) != null) {
                if (line.matches(ubox)) {
                     // drop " ubox", split by spaces
                    splitLine = line.substring(0, line.length() - 5).split("\\s");
                    data.add(splitLine);
                } else if (line.matches(lbox)) {
                    // drop " (float) lbox", split by spaces
                    splitLine = line.substring(0, line.length() - 15).split("\\s");
                    pairs.add(splitLine);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        Dataset dataset = new Dataset();
        dataset.data = data;
        dataset.pairs = pairs;
        dataset.isEntropy = false;
        
        return dataset;
    }

     /**
     * Computes positional entropy from pair probability data as outputted from method readPairProbabilities.
     *
     * @param data      Dataset containing pair identifiers and pair probabilities as String[]
     * @return          Dataset containing pair identifiers and positional entropy as String[]
     */
    public static Dataset computePositionalEntropy(Dataset dataset) {
        for (String[] pair : dataset.data) {
            double p = Double.parseDouble(pair[2]);
            double pSq = p * p;
            Double entropy = (pSq > 0) ? pSq * Math.log(pSq) : 0;
            pair[2] = entropy.toString();
        }
        dataset.isEntropy = true;
        return dataset;
    }

    /**
     * Builds an array of n Colors in rainbow hues, 1.5f gives red -> blue
     *
     * @param n         The number of colors to generate
     * @return          An array of Color, size n
     */
    public static Color[] generateColors(int n) {
        Color[] cols = new Color[n];
        for(int i = 0; i < n; i++)
            cols[i] = Color.getHSBColor((float) i / (float) (n * 1.5f), 0.8f, 1.0f);
        return cols;
    }

    /**
     * Get the XML/CSS RGB hex String for a given Color
     *
     * @param color     Color to get hex string for
     * @return          String on the form "#ffffff"
     */
    public static String getHex(Color color) {
        return "#" + Integer.toHexString(color.getRGB()).substring(2, 8);
    }

     /**
     * Calls XML routines to write pair probabilities or positional entropy as color annotation to a new SVG file.
     * Should only ever be called for SVGs output by RNAplot. Bases are circled and colored according to a color map.
     * The plot is also slightly scaled and translated to make room for circles.
     *
     * @param filepath  String containing filepath to the SVG file to be annotated
     * @param data      Dataset containing pair identifiers and pair probabilities or positional entropy
     * @return          String containing filepath to new SVG file
     */
    public static String annnotateSVG(String filepath, Dataset dataset) {
        Builder parser = new Builder();
        Document doc = null;

        dataset = readPairProbabilities("test_dp.ps");

        // read svg file
        try {
            doc = parser.build(new File(filepath));
        } catch (ParsingException e) {
            System.out.println(e.getMessage());
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        String svg = "http://www.w3.org/2000/svg";

        Element root = doc.getRootElement();
        root.setNamespaceURI(svg);
        Element g = root.getFirstChildElement("g", svg);

        // get transform data from first g element, set new values so circles do not overflow bounds
        String[] transform = g.getAttributeValue("transform")
                .split("\\s");

        String[] scale = transform[0].split(",");
        scale[0] = scale[0].substring(6);
        scale[1] = scale[1].substring(0, scale[1].length()-1);

        String[] translate = transform[1].split(",");
        translate[0] = translate[0].substring(10);
        translate[1] = translate[1].substring(0, translate[1].length()-1);

        // scale slightly down, and translate slightly down and to the left
        for (int i = 0; i < 2; i++) {
            Float f = Float.parseFloat(scale[i]) * 0.97f;
            scale[i] = f.toString();
            f = Float.parseFloat(translate[i]) + 4.4f;
            translate[i] = f.toString();
        }
        g.getAttribute("transform").setValue(
                "scale(" + scale[0] + "," + scale[1] + ") " +
                "translate(" + translate[0] + "," + translate[1] + ")");

        // get a series of comma separated coordinate pairs for each base position
        // first index is blank with this approach
        String[] coord = g.getFirstChildElement("polyline", svg)
                .getAttributeValue("points")
                .split("\\s+");

        // build circle elements
        Element circles = new Element("g", svg);
        circles.addAttribute(new Attribute("style", "stroke: black; stroke-width: 0.5"));
        circles.addAttribute(new Attribute("id", "circles"));
        g.insertChild(circles, 0);

        int n = coord.length - 1;
        
        Color[] colors = generateColors(n);
        Iterator<String[]> it = dataset.pairs.iterator();
        String[] p = it.next();

        for (int i = 0; i < n; i++) {
            // coordinate pairs are comma separated
            String[] point = coord[i+1].split(",");
            Element circle = new Element("circle", svg);
            circle.addAttribute(new Attribute("cx", point[0]));
            circle.addAttribute(new Attribute("cy", point[1]));
            circle.addAttribute(new Attribute("r", "7"));
            if (dataset.isEntropy) {

            } else {
                
            }
            circle.addAttribute(new Attribute("fill", getHex(colors[i])));
            circles.appendChild(circle);
        }

        String newFilepath = filepath.substring(0, filepath.length() - 4) + "_color.svg";

        // write new svg file
        try {
            FileOutputStream fStream = new FileOutputStream(new File(newFilepath));
            BufferedOutputStream bStream = new BufferedOutputStream(fStream);
            OutputStreamWriter out = new OutputStreamWriter(bStream);
            Serializer serializer = new Serializer(bStream);
            serializer.setIndent(2);
            serializer.setMaxLength(80);
            serializer.write(doc);
            out.flush();
            out.close();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

        return newFilepath;
    }
}
