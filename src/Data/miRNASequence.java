package Data;

import java.util.ArrayList; 

import org.biojava3.alignment.Alignments;
import org.biojava3.alignment.Alignments.PairwiseSequenceAlignerType;
import org.biojava3.alignment.SimpleGapPenalty;
import org.biojava3.alignment.SubstitutionMatrixHelper;
import org.biojava3.alignment.template.SequencePair;
import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.RNASequence;
import org.biojava3.core.sequence.compound.AmbiguityRNACompoundSet;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
import org.biojava3.core.sequence.compound.NucleotideCompound;
import org.biojava3.core.sequence.io.FastaReaderHelper;

/**
 *
 * @author Eirik Krogstad
 */
public class miRNASequence {
    private String sequence;
    private ArrayList<String> matureSequences;
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
        matureSequences = new ArrayList<String>();
    }
    
    public static void main(String[] args){
        String queryString = "GAGGUAGUAGGUUGUAUAGUU";
        String sequence ="UACACUGUGGAUCCGGGAGGUAGUAGGUUGUAUAGUUUGGAAUAUUACCACCGGUGAAC";
        
        RNASequence target = new RNASequence(sequence, AmbiguityRNACompoundSet.getRNACompoundSet());
        RNASequence query  = new RNASequence(queryString, AmbiguityRNACompoundSet.getRNACompoundSet());
        SubstitutionMatrix<NucleotideCompound> matrix = SubstitutionMatrixHelper.getNuc4_4();
        SimpleGapPenalty gapP = new SimpleGapPenalty();
        
        SequencePair<RNASequence, NucleotideCompound> sequencePair =
                Alignments.getPairwiseAlignment(query, target, PairwiseSequenceAlignerType.LOCAL, gapP, matrix);
        
        System.out.println(sequencePair.getIndexInTargetAt(1));
        
        
        
        
        
    }

    public miRNASequence(String s, int aStart, int aStop, int mStart, int mStop) {
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
    
    public MatureInfo alignMature(){
        RNASequence target = new RNASequence(sequence, AmbiguityRNACompoundSet.getRNACompoundSet());
        RNASequence query  = new RNASequence(matureSequences.get(0), AmbiguityRNACompoundSet.getRNACompoundSet());
        SubstitutionMatrix<NucleotideCompound> matrix = SubstitutionMatrixHelper.getNuc4_4();
        SimpleGapPenalty gapP = new SimpleGapPenalty();
        
        SequencePair<RNASequence, NucleotideCompound> sequencePair =
                Alignments.getPairwiseAlignment(query, target, PairwiseSequenceAlignerType.LOCAL, gapP, matrix);
        
        
        
        System.out.println(sequencePair.getIndexInTargetForQueryAt(0));
                
        return new MatureInfo(0,1, "test");
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
