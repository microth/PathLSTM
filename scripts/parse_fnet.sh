#!/usr/bin/env bash
# please download these dependencies and adjust their locations accordingly
SRL_MODEL=models/srl-ICCG16-eng.model
FN_DATA=framenet/fndata-1.5/

RERANKER="-aibeam 7 -acbeam 3 -alfa 0.75 -reranker -externalNNs -globalFeats"

# java 1.8+ is required
JAVA=java 

# note: the file to be parsed, $1, needs to be a syntactically preprocessed input file in the CoNLL-2009 format 
$JAVA -Xmx60g -cp target/classes/ se.lth.cs.srl.Parse fnet $1 $SRL_MODEL $RERANKER -framenet $FN_DATA -printXML out.xml
# note: make sure that the compiled class files (run "mvn compile") are located in target/classes/ or adjust class path to include precompiled .jar
