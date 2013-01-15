package nl.utwente.evilgeniuses.traitor.services;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import nl.utwente.evilgeniuses.traitor.model.Sentence;
import nl.utwente.evilgeniuses.traitor.model.Word;

import org.apache.hadoop.thirdparty.guava.common.collect.Sets;

/**
 * Extracts words from documents' sentences.
 * 
 * @author participant
 * 
 */
public class WordExtractor {

	private static int MIN_WORD_LENGTH = 2;
	private static int MAX_WORD_LENGTH = 20;
	private static Pattern VALID_WORD_REGEX_PATTERN = Pattern
			.compile("\\p{Alpha}{" + MIN_WORD_LENGTH + "," + MAX_WORD_LENGTH
					+ "}");

	/**
	 * Extracts the words from a sentence.
	 * 
	 * @param sentence
	 *            The sentence.
	 * @return A list of words
	 */
	public static List<Word> sentenceToWords(Sentence sentence) {
		final String[] tokens = sentence.getWordTokens();
		if (tokens.length == 0)
			return Collections.emptyList();

		// We want to return proper words iff the sentence is recognized as an
		// English one. Tweak the initial value to improve reliability of the
		// guessing that the sentence is indeed 'English'.
		int correctLanguageIndications = 1;// + tokens.length / 15;

		// We will analyze each token and add valid words to the list of words.
		List<Word> words = new ArrayList<Word>();
		for (String token : tokens) {
			if (VALID_WORD_REGEX_PATTERN.matcher(token).matches()) {
				String identifier = token.toLowerCase();

				if (correctLanguageIndications != 0
						&& ENGLISH_WORDS.contains(identifier))
					correctLanguageIndications--;

				if (!BLACKLISTED_WORDS.contains(identifier))
					words.add(new Word(token, identifier));
			}
		}

		// If the sentence was not in English, ignore any word we may have found
		// and return the empty list.
		if (correctLanguageIndications != 0)
			return Collections.emptyList();

		// Otherwise, return our list of words.
		return Collections.unmodifiableList(words);
	}

	/**
	 * English stop words.
	 * 
	 * @see <a
	 *      href="http://www.lextek.com/manuals/onix/stopwords1.html">Source</a>
	 */
	public static final Set<String> BLACKLISTED_WORDS = Sets.newHashSet(
			"about", "above", "across", "after", "again", "against", "all",
			"almost", "alone", "along", "already", "also", "although",
			"always", "among", "an", "and", "another", "any", "anybody",
			"anyone", "anything", "anywhere", "are", "area", "areas", "around",
			"as", "ask", "asked", "asking", "asks", "at", "away", "b", "back",
			"backed", "backing", "backs", "be", "became", "because", "become",
			"becomes", "been", "before", "began", "behind", "being", "beings",
			"best", "better", "between", "big", "both", "but", "by", "c",
			"came", "can", "cannot", "case", "cases", "certain", "certainly",
			"clear", "clearly", "come", "could", "d", "did", "differ",
			"different", "differently", "do", "does", "done", "dont", "down",
			"down", "downed", "downing", "downs", "during", "e", "each",
			"early", "either", "end", "ended", "ending", "ends", "enough",
			"even", "evenly", "ever", "every", "everybody", "everyone",
			"everything", "everywhere", "f",
			"far", "felt", "few", "find", "finds", "first", "for", "four",
			"from", "full", "fully", "further", "furthered", "furthering",
			"furthers", "g", "gave", "generally", "get", "gets",
			"give", "given", "gives", "go", "going", "got",
			"h", "had", "has", "have", "having", "he", "her", "here",
			"herself", "high", "higher", "highest", "him",
			"himself", "his", "how", "however", "i", "if", "in",
			"into", "is",
			"it", "its", "itself", "j", "just", "k", "keep", "keeps", "kind",
			"knew", "know", "known", "knows", "l", "large", "largely", "last",
			"later", "latest", "least", "less", "let", "lets", "like",
			"likely", "m", "made", "make",
			"making", "man", "many", "may", "me", "member", "members", "men",
			"might", "more", "most",
			"mostly", "mr", "mrs", "much", "must", "my", "myself", "n",
			"never", "new", "new", "newer", "newest", "next", "no",
			"nobody", "non", "noone", "not", "nothing", "now", "nowhere",
			"o", "of", "off", "often", "old", "older",
			"oldest", "on", "once", "one", "only",
			"or", "other",
			"others", "our", "out", "over", "p",
			"per", "perhaps", "put", "puts",
			"q", "quite", "r", "rather", "really", "right", "s", "said", "same", "saw", "say", "says",
			"see", "seem", "seemed", "seeming", "seems", "sees",
			"several", "shall", "she", "should", "show", "showed", "showing",
			"shows", "side", "sides", "since",
			"so", "some", "somebody", "someone", "something", "somewhere",
			"still", "still", "such", "sure", "t", "take", "taken", "than",
			"that", "the", "their", "them", "then", "there", "therefore",
			"these", "they", "thing", "things", "think", "thinks", "this",
			"those", "though", "thought", "thoughts", "three", "through",
			"thus", "to", "today", "together", "too", "took", "toward", "turn",
			"turned", "turning", "turns", "two", "u", "under", "until", "up",
			"upon", "us", "use", "used", "uses", "v", "very", "w", "want",
			"wanted", "wanting", "wants", "was", "way", "ways", "we", "well",
			"wells", "went", "were", "what", "when", "where", "whether",
			"which", "while", "who", "whole", "whose", "why", "will", "with",
			"within", "without", "work", "worked", "working", "works", "would",
			"x", "y", "year", "years", "yet", "you", "young", "younger",
			"youngest", "your", "yours", "z",

			"www", "com", "http", "site", "using", "page", "web", "website",
			"de", "sites", "en", "am", "co", "own", "please", "feed", "rss",
			"below", "click", "em", "href", "strong", "title", "abbr", "cite",
			"strike", "del",

			"home", "business", "save", "day", "online", "free", "life",
			"little", "looking", "look", "able", "getting", "spend", "doing",
			"real", "feel", "try", "time", "people", "lot", "help");

	/**
	 * Words that tend to be included in this language. If a sentence contains
	 * one of these words (sanitized), one may conclude the sentence is written
	 * in this language.
	 */
	public static final Set<String> ENGLISH_WORDS = Sets.newHashSet("about",
			"above", "across", "after", "again", "against", "all", "almost",
			"alone", "along", "already", "also", "although", "always", "among",
			"and", "another", "any", "anybody", "anyone", "anything",
			"anywhere", "are", "area", "areas", "around", "ask", "asked",
			"asking", "asks", "away", "back", "backed", "backing", "backs",
			"became", "because", "become", "becomes", "been", "before",
			"began", "behind", "being", "beings", "best", "better", "between",
			"big", "both", "but", "came", "can", "cannot", "case", "cases",
			"certain", "certainly", "clear", "clearly", "come", "could", "did",
			"differ", "different", "differently", "does", "done", "down",
			"down", "downed", "downing", "downs", "during", "each", "early",
			"either", "end", "ended", "ending", "ends", "enough", "even",
			"evenly", "ever", "every", "everybody", "everyone", "everything",
			"everywhere", "face", "faces", "fact", "facts", "far", "felt",
			"few", "find", "finds", "first", "for", "four", "from", "full",
			"fully", "further", "furthered", "furthering", "furthers", "gave",
			"general", "generally", "get", "gets", "give", "given", "gives",
			"going", "good", "goods", "got", "great", "greater", "greatest",
			"group", "grouped", "grouping", "groups", "had", "has", "have",
			"having", "her", "here", "herself", "high", "high", "high",
			"higher", "highest", "him", "himself", "his", "how", "however",
			"important", "interest", "interested", "interesting", "interests",
			"into", "its", "itself", "just", "keep", "keeps", "kind", "knew",
			"know", "known", "knows", "large", "largely", "last", "later",
			"latest", "least", "less", "let", "lets", "like", "likely", "long",
			"longer", "longest", "made", "make", "making", "man", "many",
			"may", "member", "members", "men", "might", "more", "most",
			"mostly", "much", "must", "myself", "necessary", "need", "needed",
			"needing", "needs", "never", "new", "newer", "newest", "next",
			"nobody", "noone", "not", "nothing", "now", "nowhere", "number",
			"numbers", "off", "often", "old", "older", "oldest", "once", "one",
			"only", "open", "opened", "opening", "opens", "order", "ordered",
			"ordering", "orders", "other", "others", "our", "out", "over",
			"part", "parted", "parting", "parts", "perhaps", "place", "places",
			"point", "pointed", "pointing", "points", "possible", "present",
			"presented", "presenting", "presents", "problem", "problems",
			"puts", "quite", "rather", "really", "right", "right", "room",
			"rooms", "said", "same", "saw", "say", "says", "second", "seconds",
			"see", "seem", "seemed", "seeming", "seems", "sees", "several",
			"shall", "she", "should", "show", "showed", "showing", "shows",
			"side", "sides", "since", "small", "smaller", "smallest", "so",
			"some", "somebody", "someone", "something", "somewhere", "state",
			"states", "still", "still", "such", "sure", "take", "taken",
			"than", "that", "the", "their", "them", "then", "there",
			"therefore", "these", "they", "thing", "things", "think", "thinks",
			"this", "those", "though", "thought", "thoughts", "three",
			"through", "thus", "today", "together", "too", "took", "toward",
			"turn", "turned", "turning", "turns", "two", "under", "until",
			"upon", "use", "used", "uses", "very", "want", "wanted", "wanting",
			"wants", "was", "way", "ways", "well", "wells", "went", "were",
			"what", "when", "where", "whether", "which", "while", "who",
			"whole", "whose", "why", "will", "with", "within", "without",
			"work", "worked", "working", "works", "would", "year", "years",
			"yet", "you", "young", "younger", "youngest", "your", "yours");

}
