mRNA folding plugin for BLASTGrabber
====================================

The bioinformatics program BLASTGrabber is a tool to organize and analyze sequence data from RNA, DNA and proteins.

BLAST searches will frequently return a large number of hits. When searching for short sequences of microRNA (mRNA), usually 18-24 bases in length, the number of hits can be overwhelming. A large number of sequences will not be actual candidates for mRNA.

This plugin allows the user to fold precursor sequences of mRNA according to the RNAfold algorithm, to see if the segment forms a stable stem/hairpin loop structure, and thereby judge the validity of candidate hits.