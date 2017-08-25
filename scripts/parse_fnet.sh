#!/usr/bin/env bash
# please download these dependencies and adjust their locations accordingly
SRL_MODEL=models/srl-ICCG16-stanford-eng.model
FN_DATA=framenet/fndata-1.7/

#RERANKER="-aibeam 7 -acbeam 3 -alfa 0.75 -reranker -externalNNs -globalFeats"
RERANKER="-reranker -externalNNs -globalFeats"

# java 1.8+ is required
JAVA=java 

$JAVA -Xmx60g -cp target/classes/:lib/anna-3.3.jar:lib/stanford-corenlp-3.8.0.jar:lib/stanford-corenlp-3.8.0-models.jar se.lth.cs.srl.CompletePipeline fnet -test $1 -srl $SRL_MODEL $RERANKER -tokenize -framenet $FN_DATA -stanford -out out.conll
# note: make sure that the compiled class files (run "mvn compile") are located in target/classes/ or adjust class path to include precompiled .jar
