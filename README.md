# News

August 2017: The FrameNet-based model has been updated to FrameNet 1.7 and now uses Stanford CoreNLP for preprocessing. 

April 2017: The repository now also contains a compiled archive (`pathlstm.jar`) of the PathLSTM source code. Feel free to use this if you are unable or unwilling to compile the code yourself.

May 2017: The source code and pre-compiled jar file are updated with additional code to support the FrameNet-based SRL model described in Roth (ICCG 2016). Note that this model requires syntactic preprocessing using external tools.  

# PathLSTM 

This repository contains code for the PathLSTM semantic role labeler introduced in [Roth and Lapata, 2016][1]. It is built on top of the [mate-tools semantic role labeler][2]. The PathLSTM model achieves state-of-the-art results on the in-domain (87.9) and out-of-domain (76.6) test sets of the CoNLL-2009 data set.

## Dependencies

The following libraries and model files need to be downloaded in order to run the PathLSTM PropBank/NomBank model on English text:

 * Bernd Bohnet's dependency parser and model files ([`anna-3.3.jar` and `CoNLL2009-ST-English*.model`](http://code.google.com/p/mate-tools/downloads/)<sup>1</sup>
 * The WSJ tokenizer from Stanford CoreNLP ([`stanford-corenlp-3.x.jar`](http://nlp.stanford.edu/software/corenlp.shtml)) 
 * The most recent _PathLSTM_ SRL model file (July 2016), available on Google Drive [here][3] 

The SRL classes can easily be compiled using maven (`mvn compile`).

For Frame-Semantic Role Labeling, the following dependencies are required: 

 * Stanford CoreNLP 3.8.0 (https://stanfordnlp.github.io/CoreNLP/, make sure to use `-stanford`!)
 * A copy of FrameNet version 1.7 (http://framenet.icsi.berkeley.edu/, make sure to use `-framenet [FNDIR]`!)
 * The most recent _PathLSTM_ Frame-SRL model file (August 2017), available on Google Drive [here][4]
 
To replicate the results from the abstract published at ICCG 2016, please contact me personally.

## Running PathLSTM  

If copies of all required libraries and models are available in the subdirectories `lib/` and `models/`, respectively, PathLSTM can simply be executed as a standalone application using the script `scripts/parse.sh`. These scripts run necessary preprocessing tools on a given input text file (assuming one sentence per line), and apply our state-of-the-art model for identifying and role labeling of semantic predicate-argument structures.

It is also possible to apply the PathLSTM model on already preprocessed text in the CoNLL 2009 format, using the Java class `se.lth.cs.srl.Parse`. Since PathLSTM is trained based on preprocessed input from specific pipelines, however, we strongly recommend to use the complete pipeline to achieve best performance. 

## References

[1]: http://arxiv.org/abs/1605.07515 
[2]: http://code.google.com/p/mate-tools/
[3]: http://drive.google.com/uc?id=0B5aLxfs6OvZBYUk2b0hLZjNqY3c&export=download
[4]: https://drive.google.com/file/d/0B5aLxfs6OvZBV3BJendwdXZGaW8/view?usp=sharing
[5]: https://dataverse.harvard.edu/dataset.xhtml?persistentId=doi:10.7910/DVN/8DY73F

If you are using the PathLSTM SRL model in your work--and we highly recommend you do!--please cite the following publication:

Michael Roth and Mirella Lapata (2016). Neural semantic role labelling with dependency path embeddings. In Proceedings of the 54th Annual Meeting of the Association for Computational Linguistics. Berlin, Germany, August, pp. 1192--1202.

For the Frame-SRL model, please cite the following publication:

Michael Roth (2016). Improving frame semantic parsing via dependency path embeddings. Book of Abstracts of the 9th International Conference on Construction Grammar, Juiz de Fora, Brazil, October, pp. 165--167.

For the built-in preprocessing pipeline, please also cite the following publication: 

Bernd Bohnet (2010). Very high accuracy and fast dependency parsing is not a contradiction. The 23rd International Conference on Computational Linguistics (COLING), Beijing, China. 



<hr/>
<font size="-1"><sup>1</sup> To reproduce our evaluation results on the CoNLL-2009 data set, preprocessing components must be retrained on the training split only, using 10-fold jackknifing.</font> 
