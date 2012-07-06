RNA Folding - a plugin for BLASTGrabber
=======================================

RNA Folding is a plugin for BLASTGrabber, a BLAST result management and analysis tool developed by the [Microbial Evolution Research Group](http://www.mn.uio.no/bio/english/research/groups/merg/) (MERG) at the Department of Biology, University of Oslo. RNA Folding not usable outside of the BLASTGrabber environment.

RNA Folding will fold any RNA sequence or set of sequences using the [Vienna RNA Package](http://www.tbi.univie.ac.at/~ivo/RNA/) RNAfold algorithm. However, RNA Folding was made to adress a very specific need for researchers working with microRNA (miRNA), and additional functionality is provided for this purpose. BLAST searches will frequently return a large number of hits. When searching for relatively short sequences of miRNA, the number of hits can be overwhelming. A number of these sequences may not be actual candidates for miRNA. The plugin allows researchers to see if the sequence forms a stable stem/hairpin loop structure with overlaid alignments and mature miRNA sequences, and thereby judge the validity of candidate hits.

This plugin is part of a computer science thesis, see the [project page](http://student.iu.hio.no/hovedprosjekter/2012/data/31/) (in Norwegian).