#!/usr/bin/env bash
# please download these models and adjust their locations accordingly
LEMMA_MODEL=models/lemma-eng.model
POS_MODEL=models/tagger-eng.model
PARSER_MODEL=models/parse-eng.model
SRL_MODEL=models/srl-ACL2016-eng.model

RERANKER="-reranker -externalNNs"

# Stanford CoreNLP (WSJTokenizer) needed for tokenization
TOKENIZE="-tokenize"
STANFORD=lib/stanford-corenlp-3.7.0.jar

# java 1.8+ is required
JAVA=java 

# parse $1
$JAVA -Xmx60g -cp lib/anna-3.3.jar:$STANFORD:target/classes/ se.lth.cs.srl.CompletePipeline eng -lemma $LEMMA_MODEL -parser $PARSER_MODEL -tagger $POS_MODEL -srl $SRL_MODEL $RERANKER $TOKENIZE -test $1
# note: make sure that the compiled class files (run "mvn compile") are located in target/classes/
