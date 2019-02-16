package com.flink.example.wordcount.state.server;

import java.io.IOException;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

public class WordStateProcessor extends KeyedProcessFunction<Tuple, Tuple2<String, Long>, Tuple2<String, Long>> {

	private ValueState<CountWithTimestamp> state;
	private static int maxCounter = 5;
	private static int eventTimeInterval = 20000;

	@Override
	public void open(Configuration parameters) throws Exception {
		System.out.println("open: params:" + parameters);
		state = getRuntimeContext().getState(new ValueStateDescriptor<>("mystate", CountWithTimestamp.class));
		System.out.println("open: state:" + state.value());
	}

	@Override
	public void processElement(Tuple2<String, Long> value,
			KeyedProcessFunction<Tuple, Tuple2<String, Long>, Tuple2<String, Long>>.Context ctx,
			Collector<Tuple2<String, Long>> out) throws Exception {
		System.out.println("in-value:" + value);
		// retrieve the current count
		CountWithTimestamp current = state.value();
		if (current == null) {
			current = new CountWithTimestamp();
			current.key = value.f0;
		}

		// update the state's count
		current.count = current.count + value.f1;

		// set the state's timestamp to the record's assigned event time timestamp
		current.lastModified = ctx.timestamp() == null ? System.currentTimeMillis() : ctx.timestamp();
		// write the state back
		state.update(current);
		// schedule the next timer X seconds from the current event time
		scheduleEvent(ctx, current);
	}

	private void scheduleEvent(KeyedProcessFunction<Tuple, Tuple2<String, Long>, Tuple2<String, Long>>.Context ctx,
			CountWithTimestamp current) {
		ctx.timerService().registerProcessingTimeTimer(current.lastModified + eventTimeInterval);
	}

	@Override
	public void onTimer(long timestamp,
			KeyedProcessFunction<Tuple, Tuple2<String, Long>, Tuple2<String, Long>>.OnTimerContext ctx,
			Collector<Tuple2<String, Long>> out) throws Exception {
		// get the state for the key that scheduled the timer
		CountWithTimestamp result = state.value();

		// check if the counter value >=5
		if (result.count >= maxCounter) {
			System.out.println("onTimer tick :" + state.value() + "-" + result.count);
			// emit the state on timeout
			out.collect(new Tuple2<String, Long>(result.key, result.count));
			// reset state's value counter
			reset(ctx, state);
		}
	}

	private void reset(KeyedProcessFunction<Tuple, Tuple2<String, Long>, Tuple2<String, Long>>.Context ctx,
			ValueState<CountWithTimestamp> state) throws IOException {
		state.value().setCount(state.value().getCount() - maxCounter);
		state.update(state.value());
		System.out.println("reset :" + state.value());
		// schedule again
		scheduleEvent(ctx, state.value());
	}

}
