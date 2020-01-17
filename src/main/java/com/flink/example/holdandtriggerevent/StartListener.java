package com.flink.example.holdandtriggerevent;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class StartListener {

	/**
	 * start locally to test it
	 */
	public static void main(String args[]) throws Exception {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		DataStreamSource<String> stream = env.socketTextStream("127.0.0.1", 7001);
		pushEvents(stream);
		env.execute("socket based StartListener started..");
	}

	private static void pushEvents(DataStreamSource<String> stream) {
		stream.flatMap(new FlatMapFunction<String, Tuple2<String, String>>() {
			@Override
			public void flatMap(String rawInput, Collector<Tuple2<String, String>> out) throws Exception {
				String[] splitWords = rawInput.split("\\s");
				out.collect(Tuple2.of(splitWords[0], splitWords[1]));
			}
		}).keyBy(0).process(new HoldAndTriggerEvent());
	}

}
