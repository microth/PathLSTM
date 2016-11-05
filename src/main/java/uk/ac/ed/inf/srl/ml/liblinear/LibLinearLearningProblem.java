package uk.ac.ed.inf.srl.ml.liblinear;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import uk.ac.ed.inf.srl.ml.LearningProblem;
import uk.ac.ed.inf.srl.ml.Model;

public class LibLinearLearningProblem implements LearningProblem {

	private File trainDataFile;
	private PrintWriter out;

	private ProblemWriter problemWriter;

	public LibLinearLearningProblem(File trainDataFile, boolean histogram) {
		this.trainDataFile = trainDataFile;
		try {
			this.out = new PrintWriter(new BufferedWriter(new FileWriter(
					trainDataFile)));
			problemWriter = histogram ? new HistogramProblemWriter(out)
					: new BinaryProblemWriter(out);
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void addInstance(int label, Collection<Integer> indices,
			Map<Integer, Double> nonbinFeats) {
		out.print(label);
		out.print(' ');
		problemWriter.writeIndices(indices, nonbinFeats);
		out.println();
	}

	@Override
	public void done() {
		out.close();
	}

	private static abstract class ProblemWriter {
		protected PrintWriter out;

		protected ProblemWriter(PrintWriter out) {
			this.out = out;
		}

		abstract void writeIndices(Collection<Integer> indices,
				Map<Integer, Double> nonbinFeats);
	}

	private static class HistogramProblemWriter extends ProblemWriter {

		protected HistogramProblemWriter(PrintWriter out) {
			super(out);
		}

		@Override
		void writeIndices(Collection<Integer> indices,
				Map<Integer, Double> nonbinFeats) {
			int[] allFeats = new int[indices.size() + nonbinFeats.size()];
			int size = 0;
			for (Integer f : indices)
				allFeats[size++] = f;
			for (Integer f : nonbinFeats.keySet())
				allFeats[size++] = f;

			Arrays.sort(allFeats);
			for (int i = 0; i < size; i++) {
				Integer f = allFeats[i];
				out.print(f);
				out.print(':');
				if (indices.contains(f)) {
					int count = 1;
					while (i + 1 < size && allFeats[i + 1] == f) {
						count++;
						i++;
					}
					out.print(count);
				} else {
					out.print(nonbinFeats.get(f).floatValue());
				}
				out.print(' ');
			}
		}

	}

	private static class BinaryProblemWriter extends ProblemWriter {

		protected BinaryProblemWriter(PrintWriter out) {
			super(out);
		}

		@Override
		public void writeIndices(Collection<Integer> indices,
				Map<Integer, Double> nonbinFeats) {
			for (Integer index : indices) {
				out.print(index);
				out.print(":1 ");
			}
			for (Integer index : nonbinFeats.keySet()) {
				out.print(index);
				out.print(":");
				out.print(nonbinFeats.get(index).floatValue());
				out.print(" ");
			}
		}

	}

	public void flush() {
		out.flush();
	}

	@Override
	public Model train() {
		// TODO Auto-generated method stub
		return null;
	}

}
