package evil;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

public class VectorSumReducer extends Reducer<Text, Text, Text, Text> {
	@Override
	public void reduce(Text pair, Iterable<Text> vectors, Context context) {
		long[] sums = null;
		
		for(Text vector : vectors) {
			String[] elements = vector.toString().split(" ");
			
			if(sums == null) {
				sums = new long[elements.length];
			}
			
			for(int i = 0; i < elements.length; i++) {
				sums[i] += Integer.parseInt(elements[i]);
			}
		}

		try {
			String out = "";
			long totalsum = 0;
			
			for(long sum : sums) {
				out += sum + " ";
				totalsum += sum;
			}
			
			if(totalsum > 1) {
				context.write(pair, new Text(out));
			}
		} catch (Exception e) {
			
		}
	}
}
