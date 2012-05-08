package Data;

/**
 * A small class containing indexing data for mature miRNA within the hairpin
 * 
 * @author Petter
 */
public class MatureInfo {

    public int start, stop;
    
    /**
     * Name of the mature miRNA sequence on the form MIMAT#######
     */
    public String accession; 

    /**
     * 
     * @param start         Start of the mature miRNA
     * @param stop          End of the mature miRNA
     * @param accession     Name of the mature miRNA sequence on the form MIMAT#######
     */
    public MatureInfo(int start, int stop, String accession){
        this.start = start;
        this.stop = stop;
        this.accession = accession;
    }
}
