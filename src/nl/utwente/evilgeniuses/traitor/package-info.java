/**
 * The "Traitor" Hadoop program counts (non-trivial) pair-wise co-occurrences
 * of words in English sentences. The co-occurrence of a pair of words is 
 * supposed to indicate a 'relation' between the words. A separate interactive
 * application will use these relations to visualize concepts and their mutual
 * similarities and differences.
 * 
 * <h3>Mapper</h3>
 * 
 * The {@link nl.utwente.evilgeniuses.traitor.hadoop.TraitorMapper TraitorMapper} processes HTML documents as follows:
 * <dl>
 * <dt>Raw HTML</dt>
 * 	<dd>&darr; {@link nl.utwente.evilgeniuses.traitor.services.DocumentExtractor DocumentExtractor}</dd>  
 * <dt>Plain text {@linkplain nl.utwente.evilgeniuses.traitor.model.Document document} with {@linkplain nl.utwente.evilgeniuses.traitor.model.Sentence sentence}s
 * 	<dd>&darr; {@link nl.utwente.evilgeniuses.traitor.services.WordExtractor WordExtractor}</dd>
 * <dt>List of non-trivial {@linkplain nl.utwente.evilgeniuses.traitor.model.Word word}s (per sentence)
 * 	<dd>&darr; {@link nl.utwente.evilgeniuses.traitor.services.PairFinder PairFinder}</dd>
 * <dt>Tuples ({@linkplain nl.utwente.evilgeniuses.traitor.model.WordPair word pair}, 1) are emitted to the reducer</dt>
 * </dl>
 * 
 * <h4>Extracting Sentences</h4>
 * 
 * Sentences are {@linkplain nl.utwente.evilgeniuses.traitor.services.DocumentExtractor#fromArchive(org.commoncrawl.hadoop.mapred.ArcRecord) retrieved}
 * from documents on the World Wide Web. The input data is {@linkplain nl.utwente.evilgeniuses.traitor.services.DocumentExtractor#stringToSentences crudely filtered}
 * to improve the reliability of the results, at the risk of ignoring usable
 * input unnecessarily. This pre-processing entails: <ul>
 * 	<li>Documents are split into sentences by a simple regular expression that 
 *      matches on punctuation marks and formatting characters (new line, tab etc.).</li>
 *  <li>To ignore (computer program) source code, sentences that contain '<tt>{</tt>',
 *  '<tt>@</tt>' or '<tt>.</tt>' are skipped.</li>
 *  <li>Non-ASCII-characters an are removed.</li>
 *  <li>Non-alphabetic (non-apostrophe) characters are replaced by space.</li> 
 * </ul>
 * 
 * <h4>Extracting Words</h4>
 * From each 'valid' sentence in each document, 'valid' words are {@linkplain nl.utwente.evilgeniuses.traitor.services.WordExtractor#sentenceToWords(nl.utwente.evilgeniuses.traitor.model.Sentence) extracted}.
 * <p>
 * A 'valid' sentence contains at least one word from a predefined set of 
 * {@linkplain nl.utwente.evilgeniuses.traitor.services.WordExtractor#ENGLISH_WORDS "characteristically English words"}.
 * These words occur often in text and are mostly 'trivial' (e.g., "are", "in").
 * This constraint is supposed to guarantee that only English sentences are processed.
 * <p>
 * A 'valid' word...<ul>
 *  <li>Consists of 2 to 20 alphabetic characters; checked by regular expression.</li>
 *  <li>Is not contained in a predefined {@linkplain nl.utwente.evilgeniuses.traitor.services.WordExtractor#BLACKLISTED_WORDS "blacklist"}.
 *      This blacklist contains most "characteristically English words", as
 *      these are trivial. This constraint is supposed to prevent uninteresting
 *      word pairs from dominating the results.</li>
 * </ul>
 * 
 * <h4>Finding Pairs</h4>
 * Given the extracted list of 'non-trivial' words in a sentence, we {@linkplain nl.utwente.evilgeniuses.traitor.services.PairFinder#generatePairs(java.util.List) pair}
 * each word to any word further in the sentence, provided: <ul>
 *  <li>The two words are unequal.</li>
 *  <li>No more than 100 word pairs (per sentence) have been constructed.</li>
 * </ul>
 * The pairs are contained in a set, such that duplicates (in one sentence) are
 * ignored. A {@link nl.utwente.evilgeniuses.traitor.model.WordPair WordPair}
 * is constructed from two words; the {@code WordPair} denotes the 'smallest'
 * (i.e., lexicographically earliest) word as its 'first' word; the 'largest'
 * word is its 'second' word. 
 * 
 * <h3>Reducer</h3>
 * 
 * The reducer processes tuples of the form ({@link nl.utwente.evilgeniuses.traitor.model.WordPair WordPair},<em>count</em>)
 * by simply summing the counts. The {@link nl.utwente.evilgeniuses.traitor.hadoop.LBoundedLongSumReducer LBoundedLongSumReducer}
 * will not emit the resulting sum, if the sum is lower than {@linkplain nl.utwente.evilgeniuses.traitor.hadoop.LBoundedLongSumReducer#MIN_COUNT a minimum of 2}.
 * By removing co-occurrences that occur only once, which are likely already 
 * uninteresting, we drastically reduce bandwidth/storage requirements.
 */
package nl.utwente.evilgeniuses.traitor;

