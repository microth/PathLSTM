package se.lth.cs.srl.corpus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Predicate extends Word {
	private static final long serialVersionUID = 1L;

	private Map<Word, String> argmap;
	private String sense; // This is PredLemmaSense in CoNLL2008
	private String upsense; // This is the lemma sense from the ontology(!)

	private Map<Word, float[]> argCEmbeds;
	private Map<Word, float[]> argIEmbeds;

	private List<ArgMap> candArgMaps;


	/**
	 * Used to replace an old word with a predicate (updates dependencies). Used
	 * to make a predicate from a word during predicate identification.
	 * 
	 * @param w
	 *            The Word
	 */
	public Predicate(Word w) {
		this(w, null);
	}

	public Predicate(Word w, Sentence s) {
		super(w, s);
		argmap = new TreeMap<>(mySentence.wordComparator);
	}

	/**
	 * Only use this constructor if you manually add the other attributes later
	 * on (i.e. in constructor Word(String CoNLL2009String))
	 * 
	 * @param sense
	 *            the sense label of the predicate
	 */
	public Predicate(String[] CoNLL2009Columns, Sentence s, int idx) {
		super(CoNLL2009Columns, s, idx);

		int offset = 0;
		if (!CoNLL2009Columns[12].equals("_")
				&& !CoNLL2009Columns[12].equals("Y"))
			offset++;

		if (CoNLL2009Columns.length > 13 + offset)
			this.sense = CoNLL2009Columns[13 + offset];
		argmap = new TreeMap<>(mySentence.wordComparator);
	}

	public Map<Word, String> getArgMap() {
		return argmap;
	}

	public void setArgMap(Map<Word, String> argmap) {
		this.argmap = argmap;
	}

	public void addArgMap(Word w, String label) {
		argmap.put(w, label);
	}

	public String getSense() {
		return sense;
	}

	public void setSense(String sense) {
		this.sense = sense;
	}

	public void setUpSense(String sense) {
		this.upsense = upsense;
	}

	public String getAttr(WordData attr) {
		if (attr == WordData.Pred)
			return sense;
		else if (attr == WordData.OntPred) {
			if (upsense == null)
				upsense = args == null ? sense : super.getArg(mySentence
						.getPredicates().size());
			return upsense;
		} else
			return super.getAttr(attr);
	}

	public String getArgumentTag(Word w) {
		return argmap.get(w);
	}

	public String toString() {
		return super.toString() + "\tY\t" + sense;
	}

	public void putCPathEmbedding(Word word, float[] emb) {
		argCEmbeds.put(word, emb);
	}
	
	public void putIPathEmbedding(Word word, float[] emb) {
		argIEmbeds.put(word, emb);
	}
	
	public float[] getACPathEmbedding(Word word) {
		if(argCEmbeds==null) argCEmbeds = new HashMap<>();
		return argCEmbeds.containsKey(word)?argCEmbeds.get(word):null;
	}
	
	public float[] getAIPathEmbedding(Word word) {
		if(argIEmbeds==null) argIEmbeds = new HashMap<>();
		return argIEmbeds.containsKey(word)?argIEmbeds.get(word):null;
	}

	public void setCandidates(List<ArgMap> candArgMaps) {
		this.candArgMaps = candArgMaps;
	}
	
	public List<ArgMap> getCandidates() {
		return candArgMaps;
	}
}
