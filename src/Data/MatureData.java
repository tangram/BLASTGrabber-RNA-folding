package Data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * A utility class for finding mature miRNA data in a miRBase database file
 *
 * @author Petter Hannevold
 */
public class MatureData {

    private HashMap<String, ArrayList<MatureInfo>> matches = new HashMap<String, ArrayList<MatureInfo>>();

    public ArrayList<MatureInfo> getMatureIndexes(String hairPinIdentifier) {
        return matches.get(hairPinIdentifier);
    }

    public MatureData(String filePath) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            Pattern hairpinPattern = Pattern.compile("MI[\\d]+");
            Pattern maturePattern = Pattern.compile("\\d+");
            Pattern matureIndexPattern = Pattern.compile("miRNA");
            Pattern accessionPattern = Pattern.compile("MIMAT[\\d]+");

            Matcher matcher;
            ArrayList<MatureInfo> matureInfoList;

            String accession;
            String hairpin;

            int start, stop;

            String line = reader.readLine();
            while (line != null) {

                if (line.substring(0, 2).equals("AC")) {
                    matcher = hairpinPattern.matcher(line);
                    matureInfoList = new ArrayList<MatureInfo>();

                    if (matcher.find()) {
                        hairpin = matcher.group();

                        while (!line.substring(0, 2).equals("FT"))
                            line = reader.readLine();

                        while (line.substring(0,2).equals("FT")) {
                            matcher = matureIndexPattern.matcher(line);
                            if (matcher.find()){
                                matcher = maturePattern.matcher(line);
                                if (matcher.find())
                                    start = Integer.parseInt(matcher.group());
                                else
                                    throw new IOException("Something wrong with mature indexing in miRNA.dat");
                                if (matcher.find())
                                    stop = Integer.parseInt(matcher.group());
                                else
                                    throw new IOException("Something wrong with mature indexing in miRNA.dat " + hairpin);

                                line = reader.readLine();
                                matcher = accessionPattern.matcher(line);
                                if (matcher.find())
                                    accession = matcher.group();
                                else
                                    throw new IOException("Something wrong with mature indexing in miRNA.dat");

                                matureInfoList.add(new MatureInfo(start, stop, accession));
                            }
                            line = reader.readLine();
                        }
                        matches.put(hairpin, matureInfoList);
                    }
                    else
                        throw new IOException("Cannot find proper MI-index");
                }
                line = reader.readLine();
            }
            reader.close();

        } catch (IOException e) {
            System.out.println("miRNA data error: " + e);
        }
    }

}
