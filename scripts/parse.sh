#!/usr/bin/env bash
# please download these models and adjust their locations accordingly
LEMMA_MODEL=models/lemma-eng.model
POS_MODEL=models/tagger-eng.model
PARSER_MODEL=models/parse-eng.model
SRL_MODEL=models/srl-ACL2016-eng.model

RERANKER="-reranker -externalNNs"
TOKENIZE="-tokenize"

# java 1.8+ is required
JAVA=/usr/lib/jvm/java-1.8.0/bin/java

# parse $1
$JAVA -Xmx60g se.lth.cs.srl.CompletePipeline eng -lemma $LEMMA_MODEL -parser $PARSER_MODEL -tagger $POS_MODEL -srl $SRL_MODEL $RERANKER $TOKENIZE -test $1
