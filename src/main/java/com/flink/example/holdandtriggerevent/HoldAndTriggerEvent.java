package com.flink.example.holdandtriggerevent;

import java.util.Date;

import org.apache.flink.api.java.tuple.Tuple;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.KeyedProcessFunction;
import org.apache.flink.util.Collector;

@SuppressWarnings("serial")
public class HoldAndTriggerEvent extends KeyedProcessFunction<Tuple, Tuple2<String, String>, Tuple2<String, String>> {

	@Override
	public void open(Configuration conf) {
	}

	/** Called for each processed event. */
	@Override
	public void processElement(Tuple2<String, String> in, Context ctx, Collector<Tuple2<String, String>> out)
			throws Exception {
		long eventStartTime = ctx.timerService().currentProcessingTime();
		// 2 minutes to hold the event
		Date eventoOutTime = new Date(eventStartTime + (2 * 60 * 1000));
		ctx.timerService().registerProcessingTimeTimer(eventoOutTime.getTime());
		System.out.println("in:" + in + " out:" + out + " intime:" + eventStartTime + " outtime:" + eventoOutTime);
	}

	/** Called when a timer fires. */
	@Override
	public void onTimer(long timestamp, OnTimerContext ctx, Collector<Tuple2<String, String>> out) {
		eventExpired(ctx, out);
	}

	/** business logic on event expiration */
	private void eventExpired(OnTimerContext ctx, Collector<Tuple2<String, String>> out) {
		System.out.println("onTimer-  out:" + out + " timestamp:" + new Date(ctx.timestamp()));
	}
}
