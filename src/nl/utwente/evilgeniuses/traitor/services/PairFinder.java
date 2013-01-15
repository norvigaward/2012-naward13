package nl.utwente.evilgeniuses.traitor.services;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nl.utwente.evilgeniuses.traitor.model.Word;
import nl.utwente.evilgeniuses.traitor.model.WordPair;

/**
 * @author participant
 * 
 */
public class PairFinder {
	private static final int MAX_PAIRS_GENERATED = 225;

	/**
	 * Determines all interesting word pairs that a sentence may contain.
	 * <em>Assumes</em> that words in one sentence are "related".
	 * 
	 * @param words
	 *            The list of words
	 * @return Iterable of word pairs
	 */
	public static Iterable<WordPair> generatePairs(List<Word> words) {
		if (words.isEmpty()) // Small optimization: early exit
			return Collections.emptySet();

		// Now, return the Cartesian product of both sets, wrapped in
		// WordPairs. Note that, by using a Set (rather than a Bag), we
		// lose the ability to *count* word pairs. Multiple occurrences
		// of word pairs in the same sentence will only be counted *once*,
		// consequently. Acceptable, given that (short) sentences that repeat
		// the same words (and yet provide new semantic information).
		Set<WordPair> pairs = new HashSet<WordPair>();

		// Limit the amount of pairs generated at maximum.
		int curPairs = MAX_PAIRS_GENERATED;

		outer: for (int i = 0; i < words.size(); i++)
			for (int j = i + 1; j < words.size(); j++) {
				Word a = words.get(i);
				Word b = words.get(j);
				// We will avoid tuples (x,x).
				if (a.equals(b))
					continue;

				pairs.add(new WordPair(a, b));

				curPairs--;
				if (curPairs == 0)
					break outer;
			}

		return pairs;
	}

}
