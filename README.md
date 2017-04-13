# News

April 2017: Please note that an incorrect package name caused incompatibility errors with `srl-ACL16-eng.model`. This has now been fixed. The repository now also contains a compiled archive (`pathlstm.jar`) of the PathLSTM source code. Feel free to use this if you are unable or unwilling to compile the code yourself.


# PathLSTM 

This repository contains code for the PathLSTM semantic role labeler introduced in [Roth and Lapata, 2016][1]. It is built on top of the [mate-tools semantic role labeler][2]. The PathLSTM model achieves state-of-the-art results on the in-domain (87.9) and out-of-domain (76.6) test sets of the CoNLL-2009 data set.

## Dependencies

The following libraries and model files need to be downloaded in order to run the PathLSTM PropBank/NomBank model on English text:

 * Bernd Bohnet's dependency parser and model files ([`anna-3.3.jar` and `CoNLL2009-ST-English*.model`](http://code.google.com/p/mate-tools/downloads/)<sup>1</sup>
 * The WSJ tokenizer from Stanford CoreNLP ([`stanford-corenlp-3.x.jar`](http://nlp.stanford.edu/software/corenlp.shtml)) 
 * The most recent _PathLSTM_ SRL model file (July 2016), available on Google Drive [here][3] 

The SRL classes can easily be compiled using maven (`mvn compile`).

For Frame-Semantic Role Labeling, text files should be preprocessed using the following toolkits:

 * Emory NLP4J for POS tagging (https://github.com/emorynlp/nlp4j)
 * BISTparser for dependency parsing (https://github.com/elikip/bist-parser)
 
In addition, the following dependencies are required to run the Frame-SRL model:
 
 * A copy of FrameNet version 1.5 (http://framenet.icsi.berkeley.edu/
 * The most recent _PathLSTM_ Frame-SRL model file (October 2016), available on Google Drive [here][4]

## Running PathLSTM  

If copies of all required libraries and models are available in the subdirectories `lib/` and `models/`, respectively, PathLSTM can simply be executed as a standalone application using the script `scripts/parse.sh`. These scripts run necessary preprocessing tools on a given input text file (assuming one sentence per line), and apply our state-of-the-art model for identifying and role labeling of semantic predicate-argument structures.

It is also possible to apply the PathLSTM model on already preprocessed text in the CoNLL 2009 format, using the Java class `se.lth.cs.srl.Parse`. Since PathLSTM is trained based on preprocessed input from specific pipelines, however, we strongly recommend to use the complete pipeline to achieve best performance. 

## References

[1]: http://arxiv.org/abs/1605.07515 
[2]: http://code.google.com/p/mate-tools/
[3]: http://drive.google.com/uc?id=0B5aLxfs6OvZBYUk2b0hLZjNqY3c&export=download
[4]: http://drive.google.com/uc?id=0B5aLxfs6OvZBOXRCbGUtN2JLZlk&export=download

If you are using the PathLSTM SRL model in your work--and we highly recommend you do!--please cite the following publication:

Michael Roth and Mirella Lapata (2016). Neural semantic role labelling with dependency path embeddings. In Proceedings of the 54th Annual Meeting of the Association for Computational Linguistics. Berlin, Germany, August, pp. 1192--1202.

For the Frame-SRL model, please cite the following publication:

Michael Roth (2016). Improving frame semantic parsing via dependency path embeddings. Book of Abstracts of the 9th International Conference on Construction Grammar, Juiz de Fora, Brazil, October, pp. 165--167.

For the built-in preprocessing pipeline, please also cite the following publication: 

Bernd Bohnet (2010). Very high accuracy and fast dependency parsing is not a contradiction. The 23rd International Conference on Computational Linguistics (COLING), Beijing, China. 



<hr/>
<font size="-1"><sup>1</sup> To reproduce our evaluation results on the CoNLL-2009 data set, preprocessing components must be retrained on the training split only, using 10-fold jackknifing.</font> 
