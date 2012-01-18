package RNAFolding;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

public class HairpinStructure {
	public String dnaSequence;
	public String foldSequence;
	public ArrayList<Hairpin> hairpins = new ArrayList<Hairpin>();
	
	public HairpinStructure(String dnaSequence) {
		this.dnaSequence = dnaSequence.toUpperCase();
		this.foldSequence = getFoldSequence(dnaSequence);
	
		hairpins.add(new Hairpin(foldSequence, 0));
		// TODO make logic to recognize separate hairpin structures
	}

	private static String getFoldSequence(String dnaSeq) {  
		String line = new String();
		OutputStream stdin;
		@SuppressWarnings("unused") 
		InputStream stderr;
		InputStream stdout;

		try {
			// RNAFold.exe is available at http://www.tbi.univie.ac.at/RNA/windoze/
			Process process = Runtime.getRuntime().exec("RNAFold.exe");
			stdin = process.getOutputStream();
			//stderr = process.getErrorStream();
			stdout = process.getInputStream();

			stdin.write(dnaSeq.getBytes());
			stdin.flush();
			stdin.close();
			
			BufferedReader reader = new BufferedReader(new InputStreamReader(stdout));
			reader.readLine();
			line = reader.readLine().substring(0, dnaSeq.length());


		} catch (Exception err) {
			err.printStackTrace();
		}

		return line;
	}
}