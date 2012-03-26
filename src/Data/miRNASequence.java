package Data;

/**
 *
 * @author Eirik Krogstad
 */
public class miRNASequence {
    private String sequence;
    private int alignmentStart;
    private int alignmentStop;
    private int matureStart;
    private int matureStop;

    public miRNASequence() {
        sequence = null;
        alignmentStart = 0;
        alignmentStop = 0;
        matureStart = 0;
        matureStop = 0;
    }

    public miRNASequence(String s, int aStart, int aStop) {
        sequence = s;
        alignmentStart = aStart;
        alignmentStop = aStop;
        matureStart = 0;
        matureStop = 0;
    }

    public int getAlignmentStart() {
        return alignmentStart;
    }

    public int getAlignmentStop() {
        return alignmentStop;
    }

    @Override
    public String toString() {
        return sequence;
    }
}
