# PathLSTM 

This repository contains code for the PathLSTM semantic role labeler introduced in [Roth and Lapata, 2016][1]. It is built on top of the [mate-tools semantic role labeler][2]. The PathLSTM model achieves state-of-the-art results on in the in-domain (87.9) and out-of-domain (76.6) test sets of the CoNLL-2009 data set.

# Dependencies

The following libraries and model files need to be downloaded in order to run PathLSTM on English text:

 * Bernd Bohnet's dependency parser and model files ([`anna-3.3.jar` and `CoNLL2009-ST-English*.model`](http://code.google.com/p/mate-tools/downloads/))<sup>1</sup>
 * The WSJ tokenizer from Stanford CoreNLP ([`stanford-corenlp-3.x.jar`](http://nlp.stanford.edu/software/corenlp.shtml)) 
 * The most recent _PathLSTM_ SRL model (June 2015), available from Google Drive [here][3] 

# Running PathLSTM  

If copies of all required libraries and models are available in the subdirectories `lib/` and `models/`, respectively, PathLSTM can simply be executed as a standalone application using the script `scripts/parse.sh`. These scripts run necessary preprocessing tools on a given input text file (assuming one sentence per line), and apply our state-of-the-art model for identifying and role labeling of semantic predicate-argument structures.

It is also possible to apply the PathLSTM model on already preprocessed text in the CoNLL 2009 format, using the Java class `se.lth.cs.srl.Parse`. Since PathLSTM is trained based on preprocessed input from specific pipelines, however, we strongly recommend to use the complete pipeline to achieve best performance. 

# References

[1]: http://arxiv.org/abs/1605.07515 
[2]: http://code.google.com/p/mate-tools/
[3]: http://drive.google.com/uc?id=0B5aLxfs6OvZBYUk2b0hLZjNqY3c&export=download

If you are using PathLSTM in your work--and we highly recommend you do!--please cite the following publication:

Michael Roth and Mirella Lapata (2016). Neural Semantic Role Labelling with Dependency Path Embeddings. In Proceedings of the 54th Annual Meeting of the Association for Computational Linguistics. To appear.

If you are using the built-in preprocessing pipeline, please also cite the following publication: 

Bernd Bohnet (2010). Very high accuracy and fast dependency parsing is not a contradiction. The 23rd International Conference on Computational Linguistics (COLING), Beijing, China. 



<hr/>
<font size="-1"><sup>1</sup> To reproduce our evaluation results on the CoNLL-2009 data set, preprocessing components must be retrained on the training split only, using 10-fold jackknifing.</font> 
