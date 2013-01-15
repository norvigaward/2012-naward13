package nl.utwente.evilgeniuses.traitor.model;


import org.apache.hadoop.thirdparty.guava.common.base.Objects;

/**
 * Describes the relation between two words, paired alphabetically.
 * 
 * @author participant
 * 
 */
public class WordPair {
	private final Word first;
	private final Word second;

	/**
	 * Instantiates {@link WordPair} with the specified words.
	 * 
	 * @param a
	 *            Non-null word.
	 * @param b
	 *            Non-null word.
	 */
	public WordPair(Word a, Word b) {
		assert a != null;
		assert b != null;
		if (a.compareTo(b) <= 0) {
			this.first = a;
			this.second = b;
		} else {
			this.first = b;
			this.second = a;
		}
	}

	/**
	 * Gets the first word.
	 * 
	 * @return A word
	 */
	public Word getFirst() {
		return first;
	}

	/**
	 * Gets the second word.
	 * 
	 * @return A word
	 */
	public Word getSecond() {
		return second;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof WordPair) {
			WordPair other = (WordPair) obj;
			return (Objects.equal(this.first, other.first) && Objects.equal(
					this.second, other.second));
		}
		return false;
	}

	@Override
	public int hashCode() {
		return first.hashCode() * 31 + second.hashCode() * 7;
	}

	@Override
	public String toString() {
		return String.format("WordPair(%s,%s)", first, second);
	}
}
