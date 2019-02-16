package com.flink.example.trading.state.server;

import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;

public class TradeStreaming {

	public static void main(String[] args) throws Exception {
		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
		// user input: <stock name> <price> <quantity> e.g. ABC 679 10, TOM 456 50
		DataStreamSource<String> stockListStream = env.socketTextStream("127.0.0.1", 7001);

		// user input: <stock name> <quantity> e.g. ABC 5, TOM 10
		DataStreamSource<String> buyStockOrdersStream = env.socketTextStream("127.0.0.1", 7002);

		tradeStreaming(stockListStream, buyStockOrdersStream);
		env.execute("socket based stocks list and buy orders started..");
	}

	private static void tradeStreaming(DataStreamSource<String> stockListStream,
			DataStreamSource<String> buyStockOrdersStream) {
		DataStream<StockPrices> stocks = stockListStream.flatMap(new FlatMapFunction<String, StockPrices>() {
			@Override
			public void flatMap(String value, Collector<StockPrices> out) throws Exception {
				String[] data = value.split("\\s");
				out.collect(new StockPrices(data[0], Double.parseDouble(data[1]), Integer.parseInt(data[2])));
			}
		}).keyBy("stock");

		DataStream<BuyStock> buyOrders = buyStockOrdersStream.flatMap(new FlatMapFunction<String, BuyStock>() {
			@Override
			public void flatMap(String value, Collector<BuyStock> out) throws Exception {
				String[] data = value.split("\\s");
				out.collect(new BuyStock(data[0], Integer.parseInt(data[1])));
			}
		}).keyBy("stock");

		stocks.connect(buyOrders).process(new TradeProcessing()).print();

	}

}
