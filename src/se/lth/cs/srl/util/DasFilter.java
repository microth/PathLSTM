package se.lth.cs.srl.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import se.lth.cs.srl.corpus.Predicate;
import se.lth.cs.srl.corpus.Sentence;
import se.lth.cs.srl.corpus.Word;

public class DasFilter {

	public class Span {

	}

	public static Word[] pass(Predicate p, Word a) {
		// predicate equals argument
		if (p.getIdx() == a.getIdx())
			return new Word[] { a, a };

		// filter from Hermann et al. (2014)
		/*
		 * if(p.getHead().getChildren().contains(a)) { Word leftmost = a; Word
		 * rightmost = a; while(leftmost.getLeftmostDep()!=null) leftmost =
		 * leftmost.getLeftmostDep(); while(rightmost.getRightmostDep()!=null)
		 * rightmost = rightmost.getRightmostDep(); if(a.getIdx()<p.getIdx() &&
		 * rightmost.getIdx()>=p.getIdx()) rightmost =
		 * p.getMySentence().get(p.getIdx()-1); if(a.getIdx()>p.getIdx() &&
		 * leftmost.getIdx()<=p.getIdx()) leftmost =
		 * p.getMySentence().get(p.getIdx()+1); return new
		 * int[]{leftmost.getBegin(), rightmost.getEnd()-1}; }
		 * 
		 * // filter from Xue and Palmer (2004) for(Word w : Word.pathToRoot(p,
		 * new LinkedList<Word>())) { if(w.getChildren().contains(a)) { Word
		 * leftmost = a; Word rightmost = a;
		 * while(leftmost.getLeftmostDep()!=null) leftmost =
		 * leftmost.getLeftmostDep(); while(rightmost.getRightmostDep()!=null)
		 * rightmost = rightmost.getRightmostDep();
		 * 
		 * // frame element fillers cannot overlap with frame-evoking elements
		 * if(a.getIdx()<p.getIdx() && rightmost.getIdx()>=p.getIdx())
		 * //leftmost = rightmost = a; return null; if(a.getIdx()>p.getIdx() &&
		 * leftmost.getIdx()<=p.getIdx()) //leftmost = rightmost = a; return
		 * null;
		 * 
		 * return new int[]{leftmost.getBegin(), rightmost.getEnd()-1}; } }
		 */

		// all sub-trees
		Word leftmost = (Word) a.getSpan().toArray()[0];
		Word rightmost = (Word) a.getSpan().toArray()[a.getSpan().size() - 1];
		// while(leftmost.getLeftmostDep()!=null) leftmost =
		// leftmost.getLeftmostDep();
		// while(rightmost.getRightmostDep()!=null) rightmost =
		// rightmost.getRightmostDep();

		if (a.getIdx() < p.getIdx() && rightmost.getIdx() >= p.getIdx()) {
			// leftmost = rightmost = a;
			if (p.getPOS().startsWith("V"))
				return null;
			rightmost = p.getMySentence().get(p.getIdx() - 1);
		}

		if (a.getIdx() > p.getIdx() && leftmost.getIdx() <= p.getIdx()) {
			// leftmost = rightmost = a; //->Recall=0.64146 (6668.0/10395.0)
			// Precision=0.68481 (6668.0/9737.0) Fscore=0.66243
			if (p.getPOS().startsWith("V"))
				return null; // -> Recall=0.62217 (6467.5/10395.0)
								// Precision=0.72178 (6467.5/8960.5)
								// Fscore=0.66829

			leftmost = p.getMySentence().get(p.getIdx() + 1); // ->
																// Recall=0.64767
																// (6732.5/10395.0)
																// Precision=0.69143
																// (6732.5/9737.0)
																// Fscore=0.66884
		}
		while (leftmost.getPOS().equals(",") || leftmost.getPOS().equals(".")) {
			if (p.getMySentence().size() == leftmost.getIdx() + 1)
				return null;
			leftmost = p.getMySentence().get(leftmost.getIdx() + 1);
		}
		while (rightmost.getPOS().equals(",") || rightmost.getPOS().equals("."))
			rightmost = p.getMySentence().get(rightmost.getIdx() - 1);

		return new Word[] { leftmost, rightmost };
	}

	public static List<Word[]> merge(List<Word[]> list) {
		List<Word[]> retval = new LinkedList<Word[]>();
		retval.add(list.remove(0));
		LIST: for (Word[] begin_end : list) {
			// for(Word w : begin_end) {
			// if(w.getSpan().size()>1) {
			// retval.add(begin_end);
			// break LIST;
			// }
			// }

			boolean add = true;
			for (Word[] existing : retval) {
				if (begin_end[0].getIdx() >= existing[0].getIdx()
						&& begin_end[1].getIdx() <= existing[1].getIdx()) {
					// completely surrounded
					add = false;
					break;
				}
				if (begin_end[0].getIdx() == existing[1].getIdx() + 1
						|| (begin_end[0].getIdx() < existing[1].getIdx() && begin_end[1]
								.getIdx() > existing[1].getIdx())) {
					existing[1] = begin_end[1]; // extend end
					add = false;
					break;
				}
				if (begin_end[1].getIdx() == existing[0].getIdx() - 1
						|| (begin_end[1].getIdx() > existing[0].getIdx() && begin_end[0]
								.getIdx() < existing[0].getIdx())) {
					existing[0] = begin_end[0]; // extend start
					add = false;
					break;
				}
			}
			if (add)
				retval.add(begin_end); // completely separate
		}
		return retval;
	}

	public static void resolveConflictingSpans(
			Map<String, List<Word[]>> newlabel2spans) {
		for (String label1 : newlabel2spans.keySet()) {
			for (String label2 : newlabel2spans.keySet()) {
				if (label1.equals(label2))
					continue;

				List<Word[]> spans1 = newlabel2spans.get(label1);
				List<Word[]> spans2 = newlabel2spans.get(label2);
				int size1 = spans1.size();
				int size2 = spans2.size();

				for (int i = 0; i < size1; i++) {
					for (int j = 0; j < size2; j++) {
						int begin1 = spans1.get(i)[0].getIdx();
						int begin2 = spans2.get(j)[0].getIdx();
						int end1 = spans1.get(i)[spans1.get(i).length - 1]
								.getIdx();
						int end2 = spans2.get(j)[spans2.get(j).length - 1]
								.getIdx();
						boolean change1 = false;
						boolean change2 = false;

						// span1 (partially) subsumes span2
						if (begin1 == begin2) {
							if (end1 > end2) {
								begin1 = end2 + 1;
								change1 = true;
							} else if (end1 < end2) {
								begin2 = end1 + 1;
								change2 = true;
							}
						}

						// update span
						if (change1) {
							Word[] oldspan = spans1.remove(i);
							Sentence s = oldspan[0].getMySentence();
							Word[] span = new Word[2];
							span[0] = s.get(begin1);
							span[1] = s.get(end1);

							/*for (int x = oldspan[0].getIdx(); x <= oldspan[1]
									.getIdx(); x++)
								System.out.print(s.get(x).getForm() + " ");
							System.out.print("->");
							for (int x = span[0].getIdx(); x <= span[1]
									.getIdx(); x++)
								System.out.print(" " + s.get(x).getForm());
							System.out.println();*/

							spans1.add(i, span);
						}

						// update span
						if (change2) {
							Word[] oldspan = spans2.remove(j);
							Sentence s = oldspan[0].getMySentence();
							Word[] span = new Word[2];
							span[0] = s.get(begin2);
							span[1] = s.get(end2);
							spans2.add(j, span);
						}
					}
				}
			}
		}

	}

}