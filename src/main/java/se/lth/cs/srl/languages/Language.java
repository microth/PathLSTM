package se.lth.cs.srl.languages;

import is2.data.SentenceData09;
import is2.lemmatizer.Lemmatizer;
import is2.parser.Parser;
import is2.tag.Tagger;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.regex.Pattern;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Word;
import se.lth.cs.srl.options.FullPipelineOptions;
import se.lth.cs.srl.preprocessor.IllinoisPreprocessor;
import se.lth.cs.srl.preprocessor.PipelinedPreprocessor;
import se.lth.cs.srl.preprocessor.Preprocessor;
import se.lth.cs.srl.preprocessor.StanfordPreprocessor;
import se.lth.cs.srl.preprocessor.tokenization.Tokenizer;
import se.lth.cs.srl.preprocessor.tokenization.WhiteSpaceTokenizer;
import se.lth.cs.srl.preprocessor.tokenization.OpenNLPToolsTokenizerWrapper;
import se.lth.cs.srl.util.BohnetHelper;

public abstract class Language {

	public enum L {
		cat, chi, cze, eng, ger, jap, spa, swe, fre, nul, ont5, fnet, uiuc,
	}

	private static Language language;
	static final Pattern BAR_PATTERN = Pattern.compile("\\|");

	public abstract String toLangNameString();

	public static Language getLanguage() {
		return language;
	}

	public static String getLsString() {
		return "chi, eng, ger";
	}

	public static Language setLanguage(L l) {
		switch (l) {
		case cze:
			language = new Czech();
			break;
		case chi:
			language = new Chinese();
			break;
		case eng:
			language = new English();
			break;
		case ont5:
			language = new English();
			((English)language).setL(L.ont5);
			break;
		case fnet:
			language = new English();
			((English)language).setL(L.fnet);
			break;
		case uiuc:
			language = new English();
			((English)language).setL(L.uiuc);
			break;
		case ger:
			language = new German();
			break;
		case spa:
			language = new Spanish();
			break;
		case nul:
			language = new NullLanguage();
			break;
		default:
			throw new IllegalArgumentException("Unknown language: '" + l + "'");
		}
		return language;
	}

	public Pattern getFeatSplitPattern() {
		return BAR_PATTERN;
	}

	public abstract String getDefaultSense(Predicate pred);

	public abstract String getCoreArgumentLabelSequence(Predicate pred,
			Map<Word, String> proposition);

	public abstract L getL();

	public abstract String getLexiconURL(Predicate pred);

	public Preprocessor getPreprocessor(FullPipelineOptions options)
			throws IOException {
		Preprocessor pp;
		if (options.mstserver != null) {
			pp = new Preprocessor() {
				
				@Override
				protected SentenceData09 preprocess(SentenceData09 sentence) {
					return sentence;
				}
				
				@Override
				public boolean hasParser() {
					// TODO Auto-generated method stub
					return true;
				}
				
				@Override
				public StringBuilder getStatus() {
					// TODO Auto-generated method stub
					return new StringBuilder();
				}
			};
		} else if(options.stanford) {
			Tokenizer tokenizer = (options.loadPreprocessorWithTokenizer ? getTokenizer(options.tokenizer)
					: null);
			//Tokenizer tokenizer = (options.loadPreprocessorWithTokenizer ? new WhiteSpaceTokenizer() : null);
			pp = new StanfordPreprocessor(tokenizer);
		} else if(options.uiucparser != null) {
			Tokenizer tokenizer = (options.loadPreprocessorWithTokenizer ? getTokenizer(options.tokenizer)
					: null);
			pp = new IllinoisPreprocessor(tokenizer, options.uiucparser);
		} else {
			Tokenizer tokenizer = (options.loadPreprocessorWithTokenizer ? getTokenizer(options.tokenizer)
					: null);
			Lemmatizer lemmatizer = getLemmatizer(options.lemmatizer);
			Tagger tagger = options.tagger == null ? null : BohnetHelper
					.getTagger(options.tagger);
			is2.mtag.Tagger mtagger = options.morph == null ? null
					: BohnetHelper.getMTagger(options.morph);
			Parser parser = options.parser == null ? null : BohnetHelper
					.getParser(options.parser);
			pp = new PipelinedPreprocessor(tokenizer, lemmatizer, tagger,
					mtagger, parser);
		}
		return pp;
	}

	public abstract String verifyLanguageSpecificModelFiles(
			FullPipelineOptions options);

	Tokenizer getDefaultTokenizer() {
		return new WhiteSpaceTokenizer();
	}

	public Tokenizer getTokenizer(File tokenModelFile) throws IOException {
		if (tokenModelFile == null)
			return getDefaultTokenizer();
		else
			return getTokenizerFromModelFile(tokenModelFile);
	}

	Tokenizer getTokenizerFromModelFile(File tokenModelFile) throws IOException {
		return OpenNLPToolsTokenizerWrapper
				.loadOpenNLPTokenizer(tokenModelFile);
	}

	Lemmatizer getLemmatizer(File lemmaModelFile) throws IOException {
		if (lemmaModelFile == null)
			return null;
		return BohnetHelper.getLemmatizer(lemmaModelFile);
	}

}
