package com.flink.example.connect.wordcount.server;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

public class ComputingWords {

	public static void main(String args[]) throws Exception {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		DataStreamSource<String> stream1 = env.socketTextStream("127.0.0.1", 7001);
		DataStreamSource<String> stream2 = env.socketTextStream("127.0.0.1", 7002);
		long windowSize = 10;
		countWords(stream1, stream2, windowSize);
		env.execute("connect socket based word count started..");
	}

	private static void countWords(DataStreamSource<String> stream1, DataStreamSource<String> stream2, long window) {
		DataStream<WordWithCount> st1 = stream1.flatMap(new FlatMapFunction<String, WordWithCount>() {
			@Override
			public void flatMap(String words, Collector<WordWithCount> wordAndCount) throws Exception {
				for (String word : words.split("\\s")) {
					wordAndCount.collect(new WordWithCount(word, 1));
				}
			}
		}).keyBy(new NameKeySelector());
		DataStream<WordWithCount> st2 = stream2.flatMap(new FlatMapFunction<String, WordWithCount>() {
			@Override
			public void flatMap(String words, Collector<WordWithCount> wordAndCount) throws Exception {
				for (String word : words.split("\\s")) {
					wordAndCount.collect(new WordWithCount(word, 1));
				}
			}
		}).keyBy(new NameKeySelector());
		//
		DataStream<WordWithCount> connectedStreams = st1.connect(st2).flatMap(
				new CoFlatMapFunction<ComputingWords.WordWithCount, ComputingWords.WordWithCount, WordWithCount>() {
					@Override
					public void flatMap1(WordWithCount wc, Collector<WordWithCount> cl) throws Exception {
						cl.collect(wc);
					}

					@Override
					public void flatMap2(WordWithCount wc, Collector<WordWithCount> cl) throws Exception {
						cl.collect(wc);
					}
				}).keyBy(new NameKeySelector()).timeWindow(Time.seconds(window))
				.reduce(new ReduceFunction<ComputingWords.WordWithCount>() {
					@Override
					public WordWithCount reduce(WordWithCount value1, WordWithCount value2) throws Exception {
						return new WordWithCount(value1.word, value1.count + value2.count);
					}
				});
		//
		connectedStreams.print();
	}

	public static class WordWithCount {
		public String word;
		public long count;

		public WordWithCount() {
		}

		public WordWithCount(String word, long count) {
			this.word = word;
			this.count = count;
		}

		@Override
		public String toString() {
			return word + " : " + count;
		}
	}

	public static class NameKeySelector implements KeySelector<WordWithCount, String> {
		@Override
		public String getKey(WordWithCount value) {
			return value.word;
		}
	}
}
