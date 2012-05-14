package Data;

import java.util.ArrayList; 

/**
 * A data object containing a header/sequence string and alignment/mature sequence data
 *
 * @author Eirik Krogstad
 */
public class RNASequence {
    private String sequence;
    private ArrayList<String> matureSequences;
    private int alignmentStart;
    private int alignmentStop;
    private int matureStart;
    private int matureStop;

    public RNASequence() {
        sequence = null;
        alignmentStart = 0;
        alignmentStop = 0;
        matureStart = 0;
        matureStop = 0;
        matureSequences = new ArrayList<String>();
    }

    public RNASequence(String s, int aStart, int aStop, int mStart, int mStop) {
        sequence = s;
        alignmentStart = aStart;
        alignmentStop = aStop;

        matureStart = mStart;
        matureStop = mStop;
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

    @Override
    public String toString() {
        return sequence;
    }
}
