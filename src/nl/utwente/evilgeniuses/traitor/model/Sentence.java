package nl.utwente.evilgeniuses.traitor.model;

import org.apache.hadoop.thirdparty.guava.common.base.Objects;

/**
 * Describes a sentence.
 * <p>
 * This class wraps a {@link String}.
 * 
 * @author participant
 * 
 */
public class Sentence {
	private final String sentenceLiteral;
	private String[] wordTokens = null;

	/**
	 * Instantiates {@link Sentence}.
	 * 
	 * @param sentenceLiteral
	 *            Non-null literal sentence.
	 */
	public Sentence(String sentenceLiteral) {
		if (sentenceLiteral == null)
			throw new IllegalArgumentException("SentenceLiteral may not be null");
		this.sentenceLiteral = sentenceLiteral;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Sentence)
			return Objects.equal(this.sentenceLiteral, ((Sentence) obj).sentenceLiteral);
		return false;
	}

	@Override
	public int hashCode() {
		return sentenceLiteral.hashCode();
	}

	@Override
	public String toString() {
		return sentenceLiteral;
	}

	/**
	 * Determines if the sentence is empty.
	 * 
	 * @return {@code true} if the sentence is empty; otherwise {@code false}
	 */
	public boolean isEmpty() {
		return sentenceLiteral.isEmpty();
	}

	/**
	 * Determines if this sentence contains the specified phrase/word.
	 * 
	 * @param phrase
	 *            The substring.
	 * @return {@code true} if the phrase is contained in this sentence;
	 *         otherwise {@code false}.
	 */
	public boolean contains(String phrase) {
		return this.sentenceLiteral.contains(phrase);
		// FIXME: Simple containment may be a bit crude. Perhaps
		// "word boundaries", superfluous spaces/newlines may need to be
		// handled.
	}

	/**
	 * Gets all word tokens, split by one or more whitespace characters.
	 * 
	 * @return String-array
	 */
	public synchronized String[] getWordTokens() {
		if (this.wordTokens == null)
			this.wordTokens = sentenceLiteral.split("\\b+"); // \\s+
		return wordTokens;
	}
}
