package RNAFolding;
public class Hairpin {
	
		public String strandTop;
		public String strandBottom;
		public int startIndex, topStrandLength, bottomStrandLength, loopLength;
		
		public Hairpin(String structure, int startIndex) {
			this.startIndex = startIndex;
			int tempIndexStart = 0;
			int tempIndexEnd = 0;
			char[] charStructure = structure.toCharArray();
			
			// finds first bond on top of strand
			while(charStructure[tempIndexStart] != '(')
				tempIndexStart++;
			topStrandLength = tempIndexStart;
			
			// finds the end of the loop
			tempIndexEnd = tempIndexStart;
			while(charStructure[tempIndexEnd] != ')')
				tempIndexEnd++;
			
			// finds the start of the loop (last bond on top of strand)
			while(charStructure[tempIndexEnd-1] != '(') {
				tempIndexEnd--;
				loopLength++;	
			}

			strandTop = structure.substring(tempIndexStart, tempIndexEnd);
			
			tempIndexEnd = charStructure.length - 1;
			
			while(charStructure[tempIndexEnd] != ')')
			tempIndexEnd--;
			
			strandBottom = structure.substring(tempIndexStart + strandTop.length() + loopLength, tempIndexEnd + 1);
			
			bottomStrandLength = structure.length() - topStrandLength - strandTop.length() - loopLength - strandBottom.length();
			System.out.println(bottomStrandLength);
		}
}