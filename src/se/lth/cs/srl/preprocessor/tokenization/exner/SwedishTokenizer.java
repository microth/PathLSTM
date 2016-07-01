/**
 * SweNLP is a framework for performing parallel processing of text. 
 * Copyright � 2011 Peter Exner
 * 
 * This file is part of SweNLP.
 *
 * SweNLP is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SweNLP is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SweNLP.  If not, see <http://www.gnu.org/licenses/>.
 */

package se.lth.cs.srl.preprocessor.tokenization.exner;

import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class SwedishTokenizer {
	public ArrayList<String> tokenize(String sentence, Charset charset) {
		ArrayList<String> tokens = new ArrayList<String>();

		try {
			sentence = preProcessSentence(sentence);

			Tokenizer swedishTokenizer = new Tokenizer(new StringReader(
					new String(sentence.getBytes(charset),
							Charset.forName("UTF-8"))));

			while (swedishTokenizer.getNextToken() >= 0) {
				tokens.add(swedishTokenizer.yytext());
			}

		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return tokens;
	}

	private static String preProcessSentence(String sentence) {
		// Done to make the flex rules match

		sentence = sentence.replaceAll(" \\.", "\\.");
		sentence = sentence.replaceAll(" \\)", "\\)");

		return sentence;
	}
}
