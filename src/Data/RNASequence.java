package Data;

import java.util.ArrayList; 

/**
 * A data object containing a header/sequence string and alignment/mature sequence data
 *
 * @author Eirik Krogstad
 */
public class RNASequence {

    private int id;
    private String name;
    private String sequence;
    private ArrayList<String> matureSequences;
    private int alignmentStart;
    private int alignmentStop;
    private int matureStart;
    private int matureStop;

    public RNASequence() {
        id = -1;
        name = "";
        sequence = "";
        alignmentStart = 0;
        alignmentStop = 0;
        matureStart = 0;
        matureStop = 0;
        matureSequences = new ArrayList<String>();
    }


    public RNASequence(int id, String name, String sequence,
                       int alignmentStart, int alignmentStop, int matureStart, int matureStop) {
        this.id = id;
        this.name = name;
        this.sequence = sequence;
        this.alignmentStart = alignmentStart;
        this.alignmentStop = alignmentStop;
        this.matureStart = matureStart;
        this.matureStop = matureStop;
    }

    public int getID() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSequence() {
        return name;
    }

    public int getAlignmentStart() {
        return alignmentStart;
    }

    public int getAlignmentStop() {
        return alignmentStop;
    }
    
    public void addMatureSequence(String s){
        matureSequences.add(s);
        System.out.println(s);
    }
    


    public int getMatureStart() {
        return matureStart;
    }

    public int getMatureStop() {
        return matureStop;
    }

    /**
     * The toString() method can be used for folding the sequence with e.g. RNAfold
     *
     * @return  FASTA compliant header name starting with BLASTGrabberID, followed by a sequence on the next line
     */
    @Override
    public String toString() {
        return ">" + id + " " + name.substring(1) + "\n" + sequence;
    }
}
