package Data;

/**
 * A data class for mature miRNA data
 *
 * @author Petter Hannevold
 */
public class MatureInfo {
    public int start, stop;
    public String accession;

    public MatureInfo(int start, int stop, String accession){
        this.start = start;
        this.stop = stop;
        this.accession = accession;
    }
}
