package evil;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.commoncrawl.hadoop.mapred.ArcRecord;

import com.google.common.collect.Sets;

import de.l3s.boilerpipe.extractors.ArticleExtractor;
import evil.PairGenerator.MAPPERCOUNTER;

/**
 * Perform a simple word count mapping on text data from the Common Crawl
 * corpus.
 */
public class BigramFinderMapper extends
		Mapper<Text, ArcRecord, Text, LongWritable> {
	
	private static int MIN_WORD_LENGTH = 2;
	private static int MAX_WORD_LENGTH = 20;
	private static String VALID_WORD_REGEX = "[a-zA-Z]{" + MIN_WORD_LENGTH + "," + MAX_WORD_LENGTH + "}";

	// From http://www.lextek.com/manuals/onix/stopwords1.html
	private Set<String> blacklist = Sets.newHashSet("about", "above",
			"across", "after", "again", "against", "all", "almost",
			"alone", "along", "already", "also", "although", "always",
			"among", "an", "and", "another", "any", "anybody", "anyone",
			"anything", "anywhere", "are", "area", "areas", "around", "as",
			"ask", "asked", "asking", "asks", "at", "away", "b", "back",
			"backed", "backing", "backs", "be", "became", "because",
			"become", "becomes", "been", "before", "began", "behind",
			"being", "beings", "best", "better", "between", "big", "both",
			"but", "by", "c", "came", "can", "cannot", "case", "cases",
			"certain", "certainly", "clear", "clearly", "come", "could",
			"d", "did", "differ", "different", "differently", "do", "does",
			"done", "dont", "down", "down", "downed", "downing", "downs", "during",
			"e", "each", "early", "either", "end", "ended", "ending",
			"ends", "enough", "even", "evenly", "ever", "every",
			"everybody", "everyone", "everything", "everywhere", "f",
			"face", "faces", "fact", "facts", "far", "felt", "few", "find",
			"finds", "first", "for", "four", "from", "full", "fully",
			"further", "furthered", "furthering", "furthers", "g", "gave",
			"general", "generally", "get", "gets", "give", "given",
			"gives", "go", "going", "good", "goods", "got", "great",
			"greater", "greatest", "group", "grouped", "grouping",
			"groups", "h", "had", "has", "have", "having", "he", "her",
			"here", "herself", "high", "high", "high", "higher", "highest",
			"him", "himself", "his", "how", "however", "i", "if",
			"important", "in", "interest", "interested", "interesting",
			"interests", "into", "is", "it", "its", "itself", "j", "just",
			"k", "keep", "keeps", "kind", "knew", "know", "known", "knows",
			"l", "large", "largely", "last", "later", "latest", "least",
			"less", "let", "lets", "like", "likely", "long", "longer",
			"longest", "m", "made", "make", "making", "man", "many", "may",
			"me", "member", "members", "men", "might", "more", "most",
			"mostly", "mr", "mrs", "much", "must", "my", "myself", "n",
			"necessary", "need", "needed", "needing", "needs", "never",
			"new", "new", "newer", "newest", "next", "no", "nobody", "non",
			"noone", "not", "nothing", "now", "nowhere", "number",
			"numbers", "o", "of", "off", "often", "old", "older", "oldest",
			"on", "once", "one", "only", "open", "opened", "opening",
			"opens", "or", "order", "ordered", "ordering", "orders",
			"other", "others", "our", "out", "over", "p", "part", "parted",
			"parting", "parts", "per", "perhaps", "place", "places",
			"point", "pointed", "pointing", "points", "possible",
			"present", "presented", "presenting", "presents", "problem",
			"problems", "put", "puts", "q", "quite", "r", "rather",
			"really", "right", "right", "room", "rooms", "s", "said",
			"same", "saw", "say", "says", "second", "seconds", "see",
			"seem", "seemed", "seeming", "seems", "sees", "several",
			"shall", "she", "should", "show", "showed", "showing", "shows",
			"side", "sides", "since", "small", "smaller", "smallest", "so",
			"some", "somebody", "someone", "something", "somewhere",
			"still", "still", "such", "sure", "t",
			"take", "taken", "than", "that", "the", "their", "them",
			"then", "there", "therefore", "these", "they", "thing",
			"things", "think", "thinks", "this", "those", "though",
			"thought", "thoughts", "three", "through", "thus", "to",
			"today", "together", "too", "took", "toward", "turn", "turned",
			"turning", "turns", "two", "u", "under", "until", "up", "upon",
			"us", "use", "used", "uses", "v", "very", "w", "want",
			"wanted", "wanting", "wants", "was", "way", "ways", "we",
			"well", "wells", "went", "were", "what", "when", "where",
			"whether", "which", "while", "who", "whole", "whose", "why",
			"will", "with", "within", "without", "work", "worked",
			"working", "works", "would", "x", "y", "year", "years", "yet",
			"you", "young", "younger", "youngest", "your", "yours", "z",
			
			"www", "com", "http", "site", "using", "page", "web", "website",
			"de", "sites", "en", "am", "co", "own", "please", "feed", "rss",
			"below", "click", "em", "href", "strong", "title", "abbr", "cite",
			"strike", "del",
			
			"home", "business", "save", "day", "online", "free", "life", "little",
			"looking", "look", "able", "getting", "spend", "doing", "real", "feel",
			"try", "time", "people", "lot", "help");
	
	private Set<String> englishWords = Sets.newHashSet("about", "above",
			"across", "after", "again", "against", "all", "almost",
			"alone", "along", "already", "also", "although", "always",
			"among", "and", "another", "any", "anybody", "anyone",
			"anything", "anywhere", "are", "area", "areas", "around",
			"ask", "asked", "asking", "asks", "away", "back",
			"backed", "backing", "backs", "became", "because",
			"become", "becomes", "been", "before", "began", "behind",
			"being", "beings", "best", "better", "between", "big", "both",
			"but", "came", "can", "cannot", "case", "cases",
			"certain", "certainly", "clear", "clearly", "come", "could",
			"did", "differ", "different", "differently", "does",
			"done", "down", "down", "downed", "downing", "downs", "during",
			"each", "early", "either", "end", "ended", "ending",
			"ends", "enough", "even", "evenly", "ever", "every",
			"everybody", "everyone", "everything", "everywhere",
			"face", "faces", "fact", "facts", "far", "felt", "few", "find",
			"finds", "first", "for", "four", "from", "full", "fully",
			"further", "furthered", "furthering", "furthers", "gave",
			"general", "generally", "get", "gets", "give", "given",
			"gives", "going", "good", "goods", "got", "great",
			"greater", "greatest", "group", "grouped", "grouping",
			"groups", "had", "has", "have", "having", "her",
			"here", "herself", "high", "high", "high", "higher", "highest",
			"him", "himself", "his", "how", "however",
			"important", "interest", "interested", "interesting",
			"interests", "into", "its", "itself", "just",
			"keep", "keeps", "kind", "knew", "know", "known", "knows",
			"large", "largely", "last", "later", "latest", "least",
			"less", "let", "lets", "like", "likely", "long", "longer",
			"longest", "made", "make", "making", "man", "many", "may",
			"member", "members", "men", "might", "more", "most",
			"mostly", "much", "must", "myself",
			"necessary", "need", "needed", "needing", "needs", "never",
			"new", "newer", "newest", "next", "nobody",
			"noone", "not", "nothing", "now", "nowhere", "number",
			"numbers", "off", "often", "old", "older", "oldest",
			"once", "one", "only", "open", "opened", "opening",
			"opens", "order", "ordered", "ordering", "orders",
			"other", "others", "our", "out", "over", "part", "parted",
			"parting", "parts", "perhaps", "place", "places",
			"point", "pointed", "pointing", "points", "possible",
			"present", "presented", "presenting", "presents", "problem",
			"problems", "puts", "quite", "rather",
			"really", "right", "right", "room", "rooms", "said",
			"same", "saw", "say", "says", "second", "seconds", "see",
			"seem", "seemed", "seeming", "seems", "sees", "several",
			"shall", "she", "should", "show", "showed", "showing", "shows",
			"side", "sides", "since", "small", "smaller", "smallest", "so",
			"some", "somebody", "someone", "something", "somewhere",
			"state", "states", "still", "still", "such", "sure",
			"take", "taken", "than", "that", "the", "their", "them",
			"then", "there", "therefore", "these", "they", "thing",
			"things", "think", "thinks", "this", "those", "though",
			"thought", "thoughts", "three", "through", "thus",
			"today", "together", "too", "took", "toward", "turn", "turned",
			"turning", "turns", "two", "under", "until", "upon",
			"use", "used", "uses", "very", "want",
			"wanted", "wanting", "wants", "was", "way", "ways",
			"well", "wells", "went", "were", "what", "when", "where",
			"whether", "which", "while", "who", "whole", "whose", "why",
			"will", "with", "within", "without", "work", "worked",
			"working", "works", "would", "year", "years", "yet",
			"you", "young", "younger", "youngest", "your", "yours");

	private String extractContent(ArcRecord record) {
		try {
			return ArticleExtractor.INSTANCE.getText(new InputStreamReader(record.getHttpResponse().getEntity().getContent()));
		} catch(Exception e) {
			return "";
		}
	}
	
	private String[] splitIntoSentences(String content) {
		return content.split("\\\n|(?=[a-zA-Z0-9]*)[\\.\\?\\!]+[ \r\n\t]+(?=[A-Z0-9])");
	}
	
	private String removeTextMarkup(String sentence) {
		return sentence.replaceAll("[^a-zA-Z']+", " ");
	}
	
	private Set<String> filter(String[] words) {
		Set<String> result = new HashSet<String>();
		
		for(String word : words) {
			if (word.matches(VALID_WORD_REGEX) && !blacklist.contains(word.toLowerCase())) { // && (dictionary.contains(word) || isName(word))
				result.add(word);
			}
		}
		
		return result;
	}
	
	private boolean isEnglish(String[] words) {
		int count = 0;
		
		for(String word : words) {
			if(englishWords.contains(word)) {
				count++;
			}
		}
		
		return count > 1;
	}

	@Override
	public void map(Text key, ArcRecord record, Context context) throws IOException {
		context.getCounter(MAPPERCOUNTER.RECORDS_IN).increment(1);
		
		try {
			String content = extractContent(record);
			
			Set<String> bigrams = new HashSet<String>();
		
			String[] sentences = splitIntoSentences(content);
			for(String sentence : sentences) {
				// Skip sentences that are code
				if(sentence.contains("{") || sentence.contains("@") || sentence.contains(".")) {
					continue;
				}
				
				// Remove accents
				sentence = Normalizer.normalize(sentence, Normalizer.Form.NFD);
				sentence = sentence.replaceAll("[^\\p{ASCII}]", "");
				
				// Remove markup
				sentence = removeTextMarkup(sentence).toLowerCase();
				
				// Split into words
				String[] words = sentence.split(" +");

				if(isEnglish(words)) {					
					// Filter for interesting words
					// Set<String> interesting = filter(words);
					
					String previous = null;
					for(String word : words) {
						if(previous != null) {
							if(previous.matches(VALID_WORD_REGEX) && word.matches(VALID_WORD_REGEX) && !blacklist.contains(previous) && !blacklist.contains(word)) {
								bigrams.add(previous + " " + word);
							}
						}
						previous = word;
					}
				}
			}
			
			for(String bigram : bigrams) {
				context.write(new Text(bigram), new LongWritable(1));
			}
		} catch (Throwable ex) {
			PairGenerator.LOG.error("Caught Exception", ex);
			context.getCounter(MAPPERCOUNTER.EXCEPTIONS).increment(1);
		}
	}
}