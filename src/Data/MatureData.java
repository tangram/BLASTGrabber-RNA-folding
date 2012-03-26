
package Data;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Pattern;
import java.util.regex.Matcher;


public class MatureData {
    
    private HashMap<String, ArrayList<MatureInfo>> matches = new HashMap<String, ArrayList<MatureInfo>>();
    
    
    public static void main(String[] args){
        
        long time = System.currentTimeMillis();
        MatureData data = new MatureData("C:\\Java\\BLASTGrabber\\plugins\\lib\\miRNA.dat");
        
        time = System.currentTimeMillis() - time;
        
        
        for (String i : data.matches.keySet()){
            System.out.println(i + ":");
            for(MatureInfo j : data.matches.get(i)){
                System.out.println("Accession: " + j.accession + "\tStart: " + j.start + "\tEnd: " + j.end);
            }
        }
        
        
        System.out.println(time);
    }
    
    public ArrayList<MatureInfo> getMatureIndexes(String hairPinIdentifier){
        return matches.get(hairPinIdentifier);
    }
    
    public MatureData(String filePath){
        
        try{
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            Pattern HairpinPattern = Pattern.compile("MI[\\d]+");
            Pattern maturePattern = Pattern.compile("\\d+");
            Pattern matureIndexPattern = Pattern.compile("miRNA");
            Pattern accessionPattern = Pattern.compile("MIMAT[\\d]+");
            
            Matcher matcher;
            ArrayList<MatureInfo> matureInfoList;
            
            String accession;
            String hairpin;
            
            int start, end;
            
            String line = reader.readLine();
            while (line != null){
                                
                if (line.substring(0, 2).equals("AC")){
                    matcher = HairpinPattern.matcher(line);
                    matureInfoList = new ArrayList<MatureInfo>();

                    if(matcher.find()){
                        hairpin = matcher.group();
                        
                        while (!line.substring(0, 2).equals("FT"))
                            line = reader.readLine();
                        
                        while (line.substring(0,2).equals("FT")){
                            matcher = matureIndexPattern.matcher(line);
                            if (matcher.find()){
                                matcher = maturePattern.matcher(line);
                                if (matcher.find())
                                    start = Integer.parseInt(matcher.group());
                                else 
                                    throw new IOException("Something wrong with mature indexing in miRNA.dat");
                                if (matcher.find())
                                    end = Integer.parseInt(matcher.group());
                                else
                                    throw new IOException("Something wrong with mature indexing in miRNA.dat " + hairpin);
                                
                                line = reader.readLine();
                                matcher = accessionPattern.matcher(line);
                                if (matcher.find())
                                    accession = matcher.group();
                                else
                                    throw new IOException("Something wrong with mature indexing in miRNA.dat");
                                
                                matureInfoList.add(new MatureInfo(start, end, accession));
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
            
        } catch (IOException e) { e.printStackTrace(); }
    
        
    }
    
    
    private void regexTest(){
                try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            while (true){
                System.out.println("Enter text to search:");
                Pattern pattern = Pattern.compile(reader.readLine());
                
                System.out.println("Enter regex:");
                Matcher matcher = pattern.matcher(reader.readLine());
                
                boolean found = false;
                while (matcher.find()){
                    System.out.println("I found the text \"" + matcher.group() +
                            "\" starting at index " + matcher.start() +
                            " and ending at index \n" + matcher.end());
                    found = true;
                }
                if(!found)
                    System.out.println("No match found");
                
            }
        } catch (Exception e) { e.printStackTrace();}
    }
    
}
