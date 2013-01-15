package nl.utwente.evilgeniuses.traitor.model;


/**
 * Describes a word.
 * <p>
 * This class simply wraps {@link String}.
 * 
 * @author participant
 * 
 */
public class Word implements Comparable<Word> {
	private final String literal;
	private final String identifier;

	/**
	 * Instantiates {@link Word}.
	 * 
	 * @param literal
	 *            Non-null literal word.
	 * @param identifier
	 *            Non-null identifier that determines equality between
	 *            (differently spelled) words.
	 */
	public Word(String literal, String identifier) {
		assert literal != null;
		assert identifier != null;

		this.literal = literal;
		this.identifier = identifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Word)
			return this.identifier.equals(((Word) obj).identifier);
		return false;
	}

	@Override
	public int hashCode() {
		return identifier.hashCode();
	}

	@Override
	public String toString() {
		return getIdentifier();
	}

	/**
	 * Gets the word's literal String.
	 * 
	 * @return A String
	 */
	public String getLiteral() {
		return literal;
	}

	/**
	 * Get the word's identifier String.
	 * 
	 * @return A String
	 */
	public String getIdentifier() {
		return identifier;
	}

	@Override
	public int compareTo(Word o) {
		return this.identifier.compareTo(o.identifier);
	}

}