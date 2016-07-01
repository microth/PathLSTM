package se.lth.cs.srl;

import java.util.zip.ZipFile;

import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.io.AllCoNLL09Reader;
import se.lth.cs.srl.io.CoNLL09Writer;
import se.lth.cs.srl.io.DepsOnlyCoNLL09Reader;
import se.lth.cs.srl.io.FrameNetXMLWriter;
import se.lth.cs.srl.io.SRLOnlyCoNLL09Reader;
import se.lth.cs.srl.io.SentenceReader;
import se.lth.cs.srl.io.SentenceWriter;
import se.lth.cs.srl.options.ParseOptions;
import se.lth.cs.srl.pipeline.Pipeline;
import se.lth.cs.srl.pipeline.Reranker;
import se.lth.cs.srl.pipeline.Step;
import se.lth.cs.srl.util.Util;

public class Parse {
	public static ParseOptions parseOptions;

	public static void main(String[] args) throws Exception {
		long startTime = System.currentTimeMillis();
		parseOptions = new ParseOptions(args);

		SemanticRoleLabeler srl;
 
		if (parseOptions.useReranker) {
			srl = new Reranker(parseOptions);
			// srl =
			// Reranker.fromZipFile(zipFile,parseOptions.skipPI,parseOptions.global_alfa,parseOptions.global_aiBeam,parseOptions.global_acBeam);
		} else {
			ZipFile zipFile = new ZipFile(parseOptions.modelFile);
			
			srl = parseOptions.skipAI ? Pipeline.fromZipFile(zipFile,
					new Step[] { Step.ac })
					: parseOptions.skipPD ? Pipeline.fromZipFile(zipFile,
							new Step[] { Step.ai, Step.ac })
							: parseOptions.skipPI ? Pipeline.fromZipFile(zipFile,
									new Step[] { Step.pd, Step.ai, Step.ac})
							: Pipeline.fromZipFile(zipFile);
			zipFile.close();
		}

		SentenceWriter writer = null;
		if (parseOptions.printXML)
			writer = new FrameNetXMLWriter(parseOptions.output);
		else
			writer = new CoNLL09Writer(parseOptions.output);

		SentenceReader reader = parseOptions.skipAI ? new AllCoNLL09Reader(
				parseOptions.inputCorpus) : parseOptions.skipPI ? new SRLOnlyCoNLL09Reader(
				parseOptions.inputCorpus) : new DepsOnlyCoNLL09Reader(
				parseOptions.inputCorpus);
		int senCount = 0;
		for (Sentence s : reader) {
			senCount++;
			if (senCount % 100 == 0)
				System.out.println("Parsing sentence " + senCount);
			srl.parseSentence(s);
			if (parseOptions.writeCoref)
				writer.specialwrite(s);
			else
				writer.write(s);
		}
		writer.close();
		reader.close();
		long totalTime = System.currentTimeMillis() - startTime;
		System.out.println("Done.");
		System.out.println(srl.getStatus());
		System.out.println();
		System.out.println("Total execution time: "
				+ Util.insertCommas(totalTime) + "ms");
	}
}
