package evil;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class NameFinderReducer extends Reducer<Text, Text, Text, Text> {
	public void reduce(Text pair, Iterable<Text> bs, Context context) {
		long s1 = 0;
		long s2 = 0;
		
		for(Text counts : bs) {
			String[] c = counts.toString().split(" ");
			
			long c1 = Integer.parseInt(c[0]);
			long c2 = Integer.parseInt(c[1]);
			
			s1 += c1;
			s2 += c2;
		}
		
		try {
			context.write(pair, new Text(s1 + " " + s2));
		} catch (Exception e) {
			
		}
	}
}
