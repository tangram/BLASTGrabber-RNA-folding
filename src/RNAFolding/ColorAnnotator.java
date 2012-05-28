package RNAFolding;

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import javax.swing.JOptionPane;
import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Serializer;
import Data.RNASequence;
import java.io.File;

/**
 * ColorAnnotator contains methods for reading pair probabilities from RNAfold dot plot PostScript files,
 * and extending an SVG structure plot from RNAplot with color annotation for either pair probability
 * (unpaired bases show probability of being unpaired) or positional entropy.
 *
 * @author Eirik Krogstad
 */
public class ColorAnnotator {

    /**
     * A small data structure to combine data points and pair info
     */
    private static class Dataset {
        public ArrayList<String[]> data = new ArrayList<String[]>();
        public HashMap<Integer, Integer> pairs = new HashMap<Integer, Integer>();
        public double max = 1.0;
    }

    /**
     * Reads pair probabilities from an RNAfold dot plot PostScript file.
     *
     * @param filepath  String containing filepath to PostScript file
     * @return          Dataset containing pair identifiers and pair probabilities as String[]
     */
    private static Dataset readPairProbabilities(String filepath) {
        String ubox = "\\d+\\s+\\d+\\s+[0-9.Ee-]+\\s+ubox";
        String lbox = "\\d+\\s+\\d+\\s+[0-9.Ee-]+\\s+lbox";
        Dataset dataset = new Dataset();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filepath));
            String line = null;
            String[] splitLine = null;
            boolean start = false;

            reader.skip(1900);  // we can safely skip ahead this much, saves time

            while ((line = reader.readLine()) != null) {
                if (line.equals("drawgrid"))
                    start = true;
                if (start) {
                    if (line.matches(ubox)) {
                         // drop " ubox", split by spaces
                        splitLine = line.substring(0, line.length() - 5).split("\\s");
                        dataset.data.add(splitLine);
                    } else if (line.matches(lbox)) {
                        // drop " (float) lbox", split by spaces
                        splitLine = line.substring(0, line.length() - 15).split("\\s");
                        dataset.pairs.put(Integer.valueOf(splitLine[0]), Integer.valueOf(splitLine[1]));
                    }
                }
            }
            reader.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to read dot plot;\n" + e);
            return null;
        }

        return dataset;
    }

    /**
     * Builds an array of n Colors in rainbow hues, 1.5f gives blue -> red for [0..n]
     *
     * @param n         The number of colors to generate
     * @param reverse   If true, gives the reverse array (red -> blue)
     * @return          An array of Color, size n
     */
    public static Color[] generateColors(int n, boolean reverse) {
        Color[] colors = new Color[n];
        for(int i = 0; i < n; i++)
            colors[n-1-i] = Color.getHSBColor((float) i / (float) (n * 1.5f), 0.9f, 0.9f);
        if (reverse)
             Collections.reverse(Arrays.asList(colors));
        return colors;
    }

    /**
     * Get the XML RGB hex String for a given Color
     *
     * @param color     Color to get hex string for
     * @return          String on the form "#ffffff"
     */
    public static String getHex(Color color) {
        return "#" + Integer.toHexString(color.getRGB() & 0xffffff);
    }

     /**
     * Calls XML routines to write pair probabilities or positional entropy as color annotation to a new SVG file.
     * Should only ever be called for SVGs output by RNAplot. Bases are circled and colored according to a color map.
     * The plot is also slightly scaled and translated to make room for circles. Annotation for subsequences are added.
     *
     * @param sequence          RNASequence object
     * @param computeEntropy    If true, compute entropy instead of pair probabilities
     * @param hasDotPlot        If false, will not compute entropy or pair probabilities, and no color is added
     * @return                  String containing filepath to new SVG file
     */
    protected static String annotateSVG(RNASequence sequence, boolean computeEntropy, boolean hasDotPlot) {
        Builder parser = new Builder();
        Document doc = null;

        String name = sequence.toString().split("\\s")[0].substring(1);
        if (name.length() > 42)
            name = name.substring(0, 42);
        name = RNAFolder.WORKINGDIR + File.separator + name;

        Dataset dataset = new Dataset();
        if (hasDotPlot)
            dataset = readPairProbabilities(name + "_dp.ps");

        // read svg file
        try {
            doc = parser.build(new BufferedReader(new FileReader(name + "_ss.svg")));
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "Unable to read structure plot;\n" + e);
            return null;
        }

        String svg = "http://www.w3.org/2000/svg";

        Element root = doc.getRootElement();
        root.setNamespaceURI(svg);
        root.addAttribute(new Attribute("preserveAspectRatio", "xMinYMin meet"));
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
            Float f = Float.valueOf(scale[i]) * 0.97f * 0.885f;
            scale[i] = f.toString();
            f = Float.valueOf(translate[i]) + 6.0f;
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

        // compute pair probabilities and positional entropy
        int n = coord.length - 1;

        double[] pp = new double[n];
        double[] values = new double[n];

        if (hasDotPlot) {
            for (String[] d : dataset.data) {
                Double p = Double.valueOf(d[2]);
                p = p * p;
                Integer i = Integer.valueOf(d[0]);
                Integer j = Integer.valueOf(d[1]);
                if (!computeEntropy) {
                    if (dataset.pairs.get(i) == j) {
                        values[i-1] = p;
                        values[j-1] = p;
                    }
                } else {
                    double e = (p > 0) ? p * Math.log(p) : 0;
                    values[i-1] += e;
                    values[j-1] += e;
                }
                pp[i-1] += p;
                pp[j-1] += p;
            }
            double log2 = Math.log(2.0);
            for (int i = 0; i < n; i++) {
                if (!computeEntropy) {
                    if (values[i] == 0.0)
                        values[i] = 1 - pp[i];
                } else {
                    values[i] += (pp[i] < 1) ? (1 - pp[i]) * Math.log(1 - pp[i]) : 0;
                    values[i] /= -log2;
                    if (values[i] > dataset.max)
                        dataset.max = values[i];
                }
            }
        }

        // build circle elements around bases
        Element circles = new Element("g", svg);
        circles.addAttribute(new Attribute("style", "stroke: black; stroke-width: 0.5"));
        circles.addAttribute(new Attribute("id", "circles"));
        g.insertChild(circles, 0);

        final int nCol = 100;
        Color[] colors = generateColors(nCol, computeEntropy);

        for (int i = 0; i < n; i++) {
            // coordinate pairs are comma separated
            String[] point = coord[i+1].split(",");
            Element circle = new Element("circle", svg);
            circle.addAttribute(new Attribute("cx", point[0]));
            circle.addAttribute(new Attribute("cy", point[1]));
            circle.addAttribute(new Attribute("r", "7"));
            if (hasDotPlot) {
                // colorize with pair probability or positional entropy
                int color = (int) ((values[i] / dataset.max) * (nCol - 1));
                circle.addAttribute(new Attribute("fill", getHex(colors[color])));
            } else {
                circle.addAttribute(new Attribute("fill", getHex(Color.white)));
            }
            boolean align = i >= sequence.getAlignmentStart()-1 && i < sequence.getAlignmentStop()-1;
            boolean mature = i >= sequence.getMatureStart()-1 && i < sequence.getMatureStop()-1;
            // outline alignment and/or mature sequence
            if (align && mature)
                circle.addAttribute(new Attribute("style", "stroke-dasharray: 2, 1, 1, 1; stroke-width: 1"));
            else if (align)
                circle.addAttribute(new Attribute("style", "stroke-dasharray: 1, 1; stroke-width: 1"));
            else if (mature)
                circle.addAttribute(new Attribute("style", "stroke-dasharray: 2, 1; stroke-width: 1"));
            circles.appendChild(circle);
        }

        // build legend
        Element legend = new Element("g", svg);
        legend.addAttribute(new Attribute("style", "stroke-width: 0"));
        legend.addAttribute(new Attribute("transform", "scale(0.885 0.885) translate(340 0)"));
        legend.addAttribute(new Attribute("id", "legend"));
        // color key legend entry
        if (hasDotPlot) {
            Element min = new Element("text", svg);
            min.addAttribute(new Attribute("x", "0"));
            min.addAttribute(new Attribute("y", "20"));
            min.appendChild("0");
            legend.appendChild(min);
            Element max = new Element("text", svg);
            max.addAttribute(new Attribute("x", "84"));
            max.addAttribute(new Attribute("y", "20"));
            max.appendChild(Double.toString(Math.round(10.0 * dataset.max) / 10.0));
            legend.appendChild(max);
            for (int i = 0; i < nCol; i += 5) {
                Element rect = new Element("rect", svg);
                rect.addAttribute(new Attribute("x", Integer.toString(i)));
                rect.addAttribute(new Attribute("y", "21"));
                rect.addAttribute(new Attribute("width", "5"));
                rect.addAttribute(new Attribute("height", "10"));
                String col = getHex(colors[i]);
                rect.addAttribute(new Attribute("style", "fill: " + col));
                legend.appendChild(rect);
            }
        }
        // alignment legend entry
        boolean hasAlign = sequence.getAlignmentStart() != 0 && sequence.getAlignmentStop() != 0;
        if (hasAlign) {
            Element dot = new Element("line", svg);
            dot.addAttribute(new Attribute("x1", "0"));
            dot.addAttribute(new Attribute("x2", "40"));
            dot.addAttribute(new Attribute("y1", "40"));
            dot.addAttribute(new Attribute("y2", "40"));
            dot.addAttribute(new Attribute("style", "stroke: black; stroke-dasharray: 2, 2; stroke-width: 2"));
            legend.appendChild(dot);
            Element align = new Element("text", svg);
            align.addAttribute(new Attribute("x", "40"));
            align.addAttribute(new Attribute("y", "45"));
            align.appendChild("Alignment");
            legend.appendChild(align);
        }
        // mature sequence legend entry
        if (sequence.getMatureStart() != 0 && sequence.getMatureStop() != 0) {
            Element dash = new Element("line", svg);
            dash.addAttribute(new Attribute("x1", "0"));
            dash.addAttribute(new Attribute("x2", "30"));
            String y = (hasAlign) ? "55" : "40";
            dash.addAttribute(new Attribute("y1", y));
            dash.addAttribute(new Attribute("y2", y));
            dash.addAttribute(new Attribute("style", "stroke: black; stroke-dasharray: 4, 2; stroke-width: 2"));
            legend.appendChild(dash);
            Element mature = new Element("text", svg);
            mature.addAttribute(new Attribute("x", "31"));
            y = (hasAlign) ? "60" : "45";
            mature.addAttribute(new Attribute("y", y));
            mature.appendChild("Mature seq.");
            legend.appendChild(mature);
        }
        root.appendChild(legend);

        String type = "pairprob";
        if (computeEntropy)
            type = "entropy";
        if (!hasDotPlot)
            type = "annot";
        String newFilepath = name + "_ss_" + type + ".svg";

        // write new svg file
        try {
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(newFilepath));
            Serializer serializer = new Serializer(stream);
            serializer.setIndent(2);
            serializer.setMaxLength(120);
            serializer.write(doc);
            stream.flush();
            stream.close();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Unable to write annotated structure plot;\n" + e);
            return null;
        }

        return newFilepath;
    }
}