package nl.utwente.evilgeniuses.traitor.hadoop;

import nl.utwente.evilgeniuses.traitor.hadoop.TraitorTool.MAPPERCOUNTER;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.lib.reduce.LongSumReducer;

/**
 * @author participant
 * 
 * @param <KEY>
 */
public class LBoundedLongSumReducer<KEY> extends LongSumReducer<KEY> {
	/**
	 * The minimal value the resulting rum must have to be emitted.
	 */
	public static final int MIN_COUNT = 2;

	@Override
	public void reduce(KEY key, Iterable<LongWritable> counts, Context context) {
		long sum = 0;
		
		context.getCounter(MAPPERCOUNTER.RECORDS_IN).increment(1);

		for (LongWritable count : counts) {
			sum += count.get();
		}

		if (sum >= MIN_COUNT) {
			try {
				context.write(key, new LongWritable(sum));
			} catch (Exception e) {

			}
		}
	}
}
