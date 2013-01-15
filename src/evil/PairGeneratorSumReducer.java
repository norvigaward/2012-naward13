package evil;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class PairGeneratorSumReducer extends Reducer<Text, LongWritable, Text, LongWritable> {
	private static final int MIN_COUNT = 2;

	public void reduce(Text pair, Iterable<LongWritable> bs, Context context) {
		long sum = 0;
		
		for(LongWritable count : bs) {
			sum += count.get();
		}
		
		if(sum >= MIN_COUNT) {
			try {
				context.write(pair, new LongWritable(sum));
			} catch (Exception e) {
				
			}
		}
	}
}
