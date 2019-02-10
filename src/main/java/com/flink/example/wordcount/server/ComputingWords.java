package com.flink.example.wordcount.server;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.common.functions.ReduceFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.util.Collector;

public class ComputingWords {

	public static void main(String args[]) throws Exception {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		DataStreamSource<String> stream = env.socketTextStream("127.0.0.1", 7001);
		countWords(stream, 7);
		env.execute("socket based word count started..");
	}

	private static void countWords(DataStreamSource<String> stream, int window) {
		DataStream<WordWithCount> dataStream = stream.flatMap(new FlatMapFunction<String, WordWithCount>() {
			@Override
			public void flatMap(String words, Collector<WordWithCount> wordAndCount) throws Exception {
				for (String word : words.split("\\s")) {
					wordAndCount.collect(new WordWithCount(word, 1));
				}
			}
		}).keyBy("word").timeWindow(Time.seconds(window)).reduce(new ReduceFunction<WordWithCount>() {
			@Override
			public WordWithCount reduce(WordWithCount word1, WordWithCount word2) throws Exception {
				return new WordWithCount(word1.word, (word1.count + word2.count));
			}
		});
		dataStream.print().setParallelism(1);
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
}
