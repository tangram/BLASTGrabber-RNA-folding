package Data;

import org.biojava3.alignment.Alignments;
import org.biojava3.alignment.Alignments.PairwiseSequenceAlignerType;
import org.biojava3.alignment.SimpleGapPenalty;
import org.biojava3.alignment.SubstitutionMatrixHelper;
import org.biojava3.alignment.template.SequencePair;
import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.DNASequence;
import org.biojava3.core.sequence.compound.AmbiguityDNACompoundSet;
import org.biojava3.core.sequence.compound.NucleotideCompound;

/**
 * @author Eirik Krogstad
 * @author Petter Hannevold
 */
public class MatureAlignment {

    /**
     * Gets the likely location of a mature miRNA sequence in a pre-miRNA candidate.
     * Aligns the parent (query) miRNA sequence to the child (hit) sequence.
     *
     * @param targetSeq     The pre-miRNA candidate as String
     * @param querySeq      The mature miRNA query as String
     * @return              Array of int, where [0] is start index and [1] is stop index
     */
    public static int[] getLocalAlignment(String targetSeq, String querySeq) {

        targetSeq = targetSeq.replace('U', 'T');
        querySeq = querySeq.replace('U', 'T');

        DNASequence target = new DNASequence(targetSeq,
                AmbiguityDNACompoundSet.getDNACompoundSet());
        DNASequence query = new DNASequence(querySeq,
				AmbiguityDNACompoundSet.getDNACompoundSet());
        SubstitutionMatrix<NucleotideCompound> matrix = SubstitutionMatrixHelper.getNuc4_4();
        SimpleGapPenalty gapP = new SimpleGapPenalty();
        gapP.setOpenPenalty((short)10);
        SequencePair<DNASequence, NucleotideCompound> psa =
                Alignments.getPairwiseAlignment(query, target,
                PairwiseSequenceAlignerType.LOCAL, gapP, matrix);

        int start = psa.getIndexInTargetAt(1);
        int end = start + psa.getLength();

        int[] a = {start, end};
        return a;
    }
}
