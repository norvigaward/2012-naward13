package nl.utwente.evilgeniuses.traitor.services;

import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import nl.utwente.evilgeniuses.traitor.model.Document;
import nl.utwente.evilgeniuses.traitor.model.Sentence;

import org.commoncrawl.hadoop.mapred.ArcRecord;

import de.l3s.boilerpipe.extractors.ArticleExtractor;

/**
 * Extracts (plain text) documents with sentences from HTML-content by filtering
 * on HTML-tags.
 * 
 * @author participant
 * 
 */
public class DocumentExtractor {
	private static Pattern STRING_SPLIT_PATTERN = Pattern.compile("\\\n|(?=[a-zA-Z0-9]*)[\\.\\?\\!]+[ \r\n\t]+(?=[A-Z0-9])");

	/**
	 * Extracts content document from the HTML source code.
	 * 
	 * @param record
	 *            The (non-null) HTML source code.
	 * @return Plain content document
	 * @throws ExtractionException
	 *             Thrown when extraction failed.
	 */
	public static Document fromArchive(ArcRecord record)
			throws ExtractionException {
		assert record != null;

		// Extract plain text
		String plainText;
		try {
			plainText = ArticleExtractor.INSTANCE
				.getText(new InputStreamReader(record.getHttpResponse().getEntity().getContent()));
		} catch (Throwable e) {
			return null;
		}


		// Split into sentences
		List<Sentence> sentences = stringToSentences(plainText);

		return new Document(sentences);
	}

	protected static List<Sentence> stringToSentences(String documentLiteral) {
		String[] sentenceLiterals = STRING_SPLIT_PATTERN.split(documentLiteral, 0); //documentLiteral.split("\\\n|(?=[a-zA-Z0-9]*)[\\.\\?\\!]+[ \r\n\t]+(?=[A-Z0-9])");
		List<Sentence> sentences = new ArrayList<Sentence>(sentenceLiterals.length);
		for (String sentenceLiteral : sentenceLiterals) {
			// Skip sentences that are code
			if (sentenceLiteral.contains("{") || sentenceLiteral.contains("@")
					|| sentenceLiteral.contains(".")) {
				continue;
			}

			// Remove accents
			sentenceLiteral = Normalizer.normalize(sentenceLiteral,
					Normalizer.Form.NFD);
			sentenceLiteral = sentenceLiteral.replaceAll("[^\\p{ASCII}]", "");

			// Remove mark-up
			sentenceLiteral.replaceAll("[^a-zA-Z']+", " ");

			// Add
			sentences.add(new Sentence(sentenceLiteral));
		}
		return sentences;
	}

	/**
	 * Occurs when text extraction fails.
	 * 
	 * @author participant
	 * 
	 */
	public static class ExtractionException extends Exception {

		/** */
		private static final long serialVersionUID = -2582392671335166283L;

		protected ExtractionException(Throwable cause) {
			super(cause);
		}
	}

}
