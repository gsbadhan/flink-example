package com.flink.example.wordcount.state.server;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class WordsCounter {

	public static void main(String args[]) throws Exception {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		DataStreamSource<String> stream = env.socketTextStream("127.0.0.1", 7001);
		countWords(stream, 7);
		env.execute("socket based word count started..");
	}

	private static void countWords(DataStreamSource<String> stream, int window) {
		stream.flatMap(new FlatMapFunction<String, Tuple2<String, Long>>() {
			@Override
			public void flatMap(String words, Collector<Tuple2<String, Long>> out) throws Exception {
				for (String word : words.split("\\s")) {
					out.collect(Tuple2.of(word, 1L));
				}
			}
		}).keyBy(0).process(new WordStateProcessor()).print();
	}

}
