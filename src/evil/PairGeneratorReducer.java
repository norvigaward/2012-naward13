package evil;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class PairGeneratorReducer extends Reducer<Text, Text, Text, Text> {
	private static final int MIN_COUNT = 2;

	public void reduce(Text a, Iterable<Text> bs, Context context) {
		Map<String, Integer> counts = new HashMap<String, Integer>();

		// Count words
		for(Text bt : bs) {
			String b = bt.toString();
			
			Integer count = counts.get(b);
			
			if(count == null) {
				count = 0;
			}
			
			counts.put(b, count + 1);
		}
		
		// Sort by count
		TreeMap<Integer, Set<String>> sorted = new TreeMap<Integer, Set<String>>();
		for(String entity : counts.keySet()) {
			int count = counts.get(entity);
			
			if(count >= MIN_COUNT) {
				Set<String> entities = sorted.get(count);
				
				if(entities == null) {
					entities = new TreeSet<String>();
				}
				
				entities.add(entity);
				sorted.put(count, entities);
			}
		}
		
		counts = null;
		
		StringBuilder sb = new StringBuilder();
		
		// Create value
		for(Integer count : sorted.keySet()) {
			Set<String> words = sorted.get(count);
			sb.append(count);
			sb.append(":");
			
			for(String word : words) {
				sb.append(word);
				sb.append(" ");
			}
		}
		
		// Write value
		try {
			if(sorted.size() > 0) {
				context.write(a, new Text(sb.toString()));
			}
		} catch (Exception e) {
			PairGenerator.LOG.error("Caught Exception", e);
		}
	}
}
