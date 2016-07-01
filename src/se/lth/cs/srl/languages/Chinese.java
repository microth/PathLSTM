package se.lth.cs.srl.languages;

import is2.lemmatizer.Lemmatizer;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.options.FullPipelineOptions;
import se.lth.cs.srl.preprocessor.SimpleChineseLemmatizer;
//import se.lth.cs.srl.preprocessor.tokenization.StanfordChineseSegmenterWrapper;
import se.lth.cs.srl.preprocessor.tokenization.Tokenizer;
import se.lth.cs.srl.util.FileExistenceVerifier;

public class Chinese extends Language {

	private static Pattern CALSPattern=Pattern.compile("^A0|A1|A2|A3|A4$");
	@Override
	public String getCoreArgumentLabelSequence(Predicate pred,Map<Word, String> proposition) {
		Sentence sen=pred.getMySentence();
		StringBuilder ret=new StringBuilder();
		for(int i=1,size=sen.size();i<size;++i){
			Word w=sen.get(i);
			if(pred==w){
				ret.append(" "+pred.getSense());
			}
			if(proposition.containsKey(w)){
				String label=proposition.get(w);
				if(CALSPattern.matcher(label).matches())
					ret.append(" "+label);
			}
		}
		return ret.toString();
	}

	@Override
	public String getDefaultSense(Predicate pred) {
		return pred.getLemma()+".01";
	}

	@Override
	public L getL() {
		return L.chi;
	}

	@Override
	public String getLexiconURL(Predicate pred) {
		return null;
	}

	@Override
	public String verifyLanguageSpecificModelFiles(FullPipelineOptions options) {
		if(options.loadPreprocessorWithTokenizer){
			File serDictionaryFile=new File(options.tokenizer,"dict-chris6.ser.gz");
			File ctbFile=new File(options.tokenizer,"ctb.gz");
			return FileExistenceVerifier.verifyFiles(serDictionaryFile,ctbFile);
		} else {
			return null;
		}
	}

	Tokenizer getTokenizerFromModelFile(File tokenModelFile) throws IOException {
		return null; // new StanfordChineseSegmenterWrapper(tokenModelFile);
	}
	Lemmatizer getLemmatizer(File lemmaModelFile) throws IOException{
		return new SimpleChineseLemmatizer();
	}

	@Override
	public String toLangNameString() {
		return "Chinese";
	}
}
