package nl.utwente.evilgeniuses.traitor.hadoop;

import java.util.ArrayList;
import java.util.List;

import nl.utwente.evilgeniuses.traitor.hadoop.TraitorTool.MAPPERCOUNTER;
import nl.utwente.evilgeniuses.traitor.model.Document;
import nl.utwente.evilgeniuses.traitor.model.Sentence;
import nl.utwente.evilgeniuses.traitor.model.Word;
import nl.utwente.evilgeniuses.traitor.model.WordPair;
import nl.utwente.evilgeniuses.traitor.services.DocumentExtractor;
import nl.utwente.evilgeniuses.traitor.services.PairFinder;
import nl.utwente.evilgeniuses.traitor.services.WordExtractor;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.commoncrawl.hadoop.mapred.ArcRecord;

import com.google.common.collect.Iterables;

/**
 * Maps each (key,ArcRecord) pair to a list of pairs ("Entity\tTrait",1).
 * 
 * @author participant
 * 
 */
public class TraitorMapper extends Mapper<Text, ArcRecord, Text, LongWritable> {

	private final static LongWritable COUNT_1 = new LongWritable(1);

	@Override
	protected void map(final Text key, final ArcRecord value, final Context context)
			throws java.io.IOException, InterruptedException {
		context.getCounter(MAPPERCOUNTER.RECORDS_IN).increment(1);

		try {
			Thread t = new Thread() {
				public void run() {
					try {
						if(value.getHttpStatusCode() == 200 && value.getContentType().toLowerCase().equals("text/html") && value.getContentLength() < 100000) {
							// Read input
							Document maybeDocument = DocumentExtractor.fromArchive(value);
							if (maybeDocument == null || maybeDocument.isEmpty()) {
								context.getCounter(MAPPERCOUNTER.EMPTY_PAGE_TEXT).increment(1);
								return;
							}
							
							// Process: Get all interesting words from a sentence.
							List<Iterable<WordPair>> wordPairs = new ArrayList<Iterable<WordPair>>();
							for (Sentence sentence : maybeDocument.getSentences()) {
								List<Word> words = WordExtractor.sentenceToWords(sentence);
								wordPairs.add(PairFinder.generatePairs(words));
							}
				
							/*
							 * Now, we have lists (of iterables) for each sentence. To flatten
							 * these iterables into a single iterables, we use {@link
							 * Iterables.concat}. This function (supposedly) concatenates the
							 * "lists" lazily (or rather: virtually iterates through each list
							 * as if it were one long list). ==> Performance++ ?
							 * 
							 * Duplicate wordpairs (in different sentences) *are* (probably)
							 * counted multiple times, due to the concatenation of sets
							 * "as iterables".
							 */
							Iterable<WordPair> allPairs = Iterables.concat(wordPairs);
							
							// Output to aggregate
							for (WordPair pair : allPairs) {
								Text pairText = new Text(pair.getFirst() + "\t" + pair.getSecond());
								context.write(pairText, COUNT_1);
							}
						}
					} catch (Throwable t) {
						TraitorTool.LOG.error("Caught Exception", t);
						context.getCounter(MAPPERCOUNTER.EXCEPTIONS).increment(1);
					}
				}
			};
			
			t.start();
			t.join(60000);
			t.stop();
		} catch (Throwable t) {
			TraitorTool.LOG.error("Caught Exception", t);
			context.getCounter(MAPPERCOUNTER.EXCEPTIONS).increment(1);
		}
	}
}
