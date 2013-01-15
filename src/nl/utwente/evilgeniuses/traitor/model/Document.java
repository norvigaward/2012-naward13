package nl.utwente.evilgeniuses.traitor.model;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.hadoop.thirdparty.guava.common.base.Objects;

/**
 * Describes content text.
 * <p>
 * This class simply wraps {@link String}.
 * 
 * @author participant
 * 
 */
public class Document implements Iterable<Sentence> {
	private final List<Sentence> sentences;

	/**
	 * Instantiates {@link Document}.
	 * 
	 * @param sentences
	 *            Non-null list of sentences.
	 */
	public Document(List<Sentence> sentences) {
		if (sentences == null)
			throw new IllegalArgumentException("ContentText may not be null");
		this.sentences = sentences;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Document)
			return Objects.equal(this.sentences, ((Document) obj).sentences);
		return false;
	}

	@Override
	public int hashCode() {
		return sentences.hashCode();
	}

	@Override
	public String toString() {
		return sentences.toString();
	}

	/**
	 * Gets the unmodifiable list of sentences.
	 * 
	 * @return List of sentences.
	 */
	public List<Sentence> getSentences() {
		return Collections.unmodifiableList(sentences);
	}

	@Override
	public Iterator<Sentence> iterator() {
		return sentences.iterator();
	}

	/**
	 * Determines if the document is empty.
	 * 
	 * @return {@code true} if the document is empty; otherwise {@code false}
	 */
	public boolean isEmpty() {
		if (sentences.isEmpty())
			return true;
		for (Sentence sentence : sentences)
			if (sentence.isEmpty())
				return true;

		return false;
	}
}
