package se.lth.cs.srl.features;

public enum TargetWord {
	Pred, // This is the (potential) predicate
	PredParent, // This is the parent of the (potential) predicate
	Arg, // This is the (potential) argument
	LeftDep, // This is the leftmost dependent of the (potential) argument
	RightDep, // This is the rightmost dependent of the (potential) argument
	LeftSibling, // This is the left sibling of the (potential) argument
	RightSibling, // This is the right sibling of the (potential) argument
	PredSubj,

	FirstWord, LastWord, SecondWord,

}
