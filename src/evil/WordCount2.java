package evil;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.Normalizer;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.mapreduce.lib.reduce.LongSumReducer;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.util.ToolRunner;
import org.apache.log4j.Logger;
import org.commoncrawl.hadoop.mapred.ArcInputFormat;
import org.commoncrawl.hadoop.mapred.ArcRecord;

import com.cybozu.labs.langdetect.Detector;
import com.cybozu.labs.langdetect.DetectorFactory;
import com.cybozu.labs.langdetect.LangDetectException;
import com.google.common.collect.Sets;

import de.l3s.boilerpipe.extractors.ArticleExtractor;

/**
 * An example showing how to use the Common Crawl 'textData' files to
 * efficiently work with Common Crawl corpus text content.
 */
public class WordCount2 extends Configured implements Tool {

	private static final Logger LOG = Logger.getLogger(WordCount2.class);
	private static final String ARGNAME_INPATH = "-in";
	private static final String ARGNAME_OUTPATH = "-out";
	private static final String ARGNAME_CONF = "-conf";
	private static final String ARGNAME_OVERWRITE = "-overwrite";
	private static final String ARGNAME_MAXFILES = "-maxfiles";
	private static final String ARGNAME_NUMREDUCE = "-numreducers";
	private static final String FILEFILTER = ".arc.gz";

	protected static enum MAPPERCOUNTER {
		RECORDS_IN, EMPTY_PAGE_TEXT, EXCEPTIONS
	}

	/**
	 * Perform a simple word count mapping on text data from the Common Crawl
	 * corpus.
	 */
	protected static class PairGeneratorMapper extends
			Mapper<Text, ArcRecord, Text, LongWritable> {
		
		private final LongWritable outVal = new LongWritable(1);

		// From http://www.lextek.com/manuals/onix/stopwords1.html
		private final Set<String> blacklist = Sets.newHashSet("about", "above",
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
				"done", "down", "down", "downed", "downing", "downs", "during",
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
				"state", "states", "still", "still", "such", "sure", "t",
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
				"you", "young", "younger", "youngest", "your", "yours", "z");

		/*
		 * Sets.newHashSet("a", "and", "of", "for", "by", "on", "is", "this",
		 * "with", "all", "you", "your", "are", "about", "or", "from", "be",
		 * "at", "that", "it", "us", "as", "more", "not", "an", "have", "i",
		 * "no", "can", "will", "our", "we", "if", "do", "may", "has", "other",
		 * "my", "but", "out", "get", "what", "in", "need", "to", "the", "any",
		 * "after", "also", "how", "these", "when", "been", "able", "they",
		 * "just", "so", "am", "was", "de", "en", "la", "der", "die", "und",
		 * "con", "du", "un", "des", "al", "por", "que", "den", "el", "auf",
		 * "les", "y", "here", "me", "here", "he", "his", "ago", "das", "ist",
		 * "sich", "sie", "avec", "et", "zu", "mit", "au", "del", "lo", "up",
		 * "go", "auch", "not", "le", "een", "van", "aus", "von", "ce", "los",
		 * "than");
		 */

		private static Set<String> dictionary = new HashSet<String>();
		private static Set<String> names = Sets.newHashSet("apple", "total", "microsoft", "ibm", "hp");
		
		static {
			try {
				DetectorFactory.loadProfile("src/resource/profiles");
			} catch (LangDetectException e) {
				e.printStackTrace();
			}
			
			try {
				BufferedReader reader = new BufferedReader(new FileReader("/usr/share/dict/words"));
				String word;
				while((word = reader.readLine()) != null) {
					word = word.toLowerCase();
					
					dictionary.add(word);
					dictionary.add(word.replace("'", ""));
				}
				
				System.out.println("Dictionary size: " + dictionary.size());
			} catch(Exception e) {
				e.printStackTrace();
			}
		}

		private String extractContent(ArcRecord record) {
			try {
				return ArticleExtractor.INSTANCE.getText(new InputStreamReader(record.getHttpResponse().getEntity().getContent()));
			} catch(Exception e) {
				return "";
			}
		}
		
		private String getLanguage(String content) {
			try {
				Detector detector = DetectorFactory.create();
				detector.append(content);
				return detector.detect();
			} catch(Exception e) {
				return null;
			}
		}
		
		private String[] splitIntoSentences(String content) {
			return content.split("\\\n|(?=[a-zA-Z0-9]*)[\\.\\?\\!]+[ \r\n\t]+(?=[A-Z0-9])");
		}
		
		private String removeTextMarkup(String sentence) {
			return sentence.replaceAll("[^a-zA-Z']+", " ");
		}
		
		private Set<String> getInterestingWords(String[] words) {
			Set<String> result = new HashSet<String>();
			
			for(String word : words) {
				if (word.matches("[a-z]{2,12}") && !blacklist.contains(word)) { // && (dictionary.contains(word) || isName(word))
					result.add(word);
				}
			}
			
			return result;
		}
		
		private boolean isName(String word) {
			return names.contains(word) || !dictionary.contains(word);
		}

		@Override
		public void map(Text key, ArcRecord record, Context context) throws IOException {
			context.getCounter(MAPPERCOUNTER.RECORDS_IN).increment(1);
			
			try {
				String content = extractContent(record);
				String language = getLanguage(content);
				
				// We only process english text
				if (language == null || !language.equals("en")) {
					return;
				}
			
				Set<String> pairs = new HashSet<String>();
				
				String[] sentences = splitIntoSentences(content);
				for(String sentence : sentences) {
					// Skip sentences that are code
					if(sentence.contains("{")) {
						continue;
					}
					
					// Ignore all sentences that contains urls
					/*
					if(sentence.contains("\\.")) {
						continue;
					}
					*/
					
					// Remove accents
					sentence = Normalizer.normalize(sentence, Normalizer.Form.NFD);
					sentence = sentence.replaceAll("[^\\p{ASCII}]", "");
					
					sentence = removeTextMarkup(sentence).toLowerCase();
					
					String[] words = sentence.split(" +");
					
					for(String word : words) {
						//if(!dictionary.contains(word)) {
							context.write(new Text(word), outVal);
						//}
					}
				}
			} catch (Exception ex) {
				LOG.error("Caught Exception", ex);
				context.getCounter(MAPPERCOUNTER.EXCEPTIONS).increment(1);
			}
		}
	}

	public void usage() {
		System.out
				.println("\n  org.commoncrawl.examples.ExampleTextWordCount \n"
						+ "                           " + ARGNAME_INPATH
						+ " <inputpath>\n" + "                           "
						+ ARGNAME_OUTPATH + " <outputpath>\n"
						+ "                         [ " + ARGNAME_OVERWRITE
						+ " ]\n" + "                         [ "
						+ ARGNAME_NUMREDUCE + " <number_of_reducers> ]\n"
						+ "                         [ " + ARGNAME_CONF
						+ " <conffile> ]\n" + "                         [ "
						+ ARGNAME_MAXFILES + " <maxfiles> ]");
		System.out.println("");
		GenericOptionsParser.printGenericCommandUsage(System.out);
	}

	/**
	 * Implmentation of Tool.run() method, which builds and runs the Hadoop job.
	 * 
	 * @param args
	 *            command line parameters, less common Hadoop job parameters
	 *            stripped out and interpreted by the Tool class.
	 * @return 0 if the Hadoop job completes successfully, 1 if not.
	 */
	@Override
	public int run(String[] args) throws Exception {

		String inputPath = null;
		String outputPath = null;
		String configFile = null;
		boolean overwrite = false;
		int numReducers = 1;

		// Read the command line arguments. We're not using GenericOptionsParser
		// to prevent having to include commons.cli as a dependency.
		for (int i = 0; i < args.length; i++) {
			try {
				if (args[i].equals(ARGNAME_INPATH)) {
					inputPath = args[++i];
				} else if (args[i].equals(ARGNAME_OUTPATH)) {
					outputPath = args[++i];
				} else if (args[i].equals(ARGNAME_CONF)) {
					configFile = args[++i];
				} else if (args[i].equals(ARGNAME_MAXFILES)) {
					SampleFilter.setMax(Long.parseLong(args[++i]));
				} else if (args[i].equals(ARGNAME_OVERWRITE)) {
					overwrite = true;
				} else if (args[i].equals(ARGNAME_NUMREDUCE)) {
					numReducers = Integer.parseInt(args[++i]);
				} else {
					LOG.warn("Unsupported argument: " + args[i]);
				}
			} catch (ArrayIndexOutOfBoundsException e) {
				usage();
				throw new IllegalArgumentException();
			}
		}

		if (inputPath == null || outputPath == null) {
			usage();
			throw new IllegalArgumentException();
		}

		// Read in any additional config parameters.
		if (configFile != null) {
			LOG.info("adding config parameters from '" + configFile + "'");
			this.getConf().addResource(configFile);
		}

		// Create the Hadoop job.
		Configuration conf = getConf();
		Job job = new Job(conf);
		job.setJarByClass(WordCount2.class);
		job.setNumReduceTasks(numReducers);

		// Scan the provided input path for ARC files.
		LOG.info("setting input path to '" + inputPath + "'");
		SampleFilter.setFilter(FILEFILTER);
		FileInputFormat.addInputPath(job, new Path(inputPath));
		FileInputFormat.setInputPathFilter(job, SampleFilter.class);

		// Delete the output path directory if it already exists and user wants
		// to overwrite it.
		if (overwrite) {
			LOG.info("clearing the output path at '" + outputPath + "'");
			FileSystem fs = FileSystem.get(new URI(outputPath), conf);
			if (fs.exists(new Path(outputPath))) {
				fs.delete(new Path(outputPath), true);
			}
		}

		// Set the path where final output 'part' files will be saved.
		LOG.info("setting output path to '" + outputPath + "'");
		FileOutputFormat.setOutputPath(job, new Path(outputPath));
		FileOutputFormat.setCompressOutput(job, false);

		// Set which InputFormat class to use.
		job.setInputFormatClass(ArcInputFormat.class); // SequenceFileInputFormat.class

		// Set which OutputFormat class to use.
		job.setOutputFormatClass(TextOutputFormat.class);

		// Set the output data types.
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(LongWritable.class);

		// Set which Mapper and Reducer classes to use.
		job.setMapperClass(WordCount2.PairGeneratorMapper.class);
		job.setReducerClass(LongSumReducer.class);
		job.setCombinerClass(LongSumReducer.class);

		// Set the name of the job.
		job.setJobName("Norvig Award - Evil Pair Generator");

		if (job.waitForCompletion(true)) {
			return 0;
		} else {
			return 1;
		}
	}

	/**
	 * Main entry point that uses the {@link ToolRunner} class to run the
	 * example Hadoop job.
	 */
	public static void main(String[] args) throws Exception {
		int res = ToolRunner
				.run(new Configuration(), new WordCount2(), args);
		System.exit(res);
	}
}

		/*
		 * TokenStream tokenStream = new SentenceTokenizer(new StringReader(value.toString()));
			CharTermAttribute termAttribute = tokenStream.getAttribute(CharTermAttribute.class);
			
			while(tokenStream.incrementToken()) {
				boolean endOfSentence = false;
				
				String word = termAttribute.toString();
		 */