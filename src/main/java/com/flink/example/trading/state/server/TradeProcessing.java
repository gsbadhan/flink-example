package com.flink.example.trading.state.server;

import org.apache.flink.api.common.state.ValueState;
import org.apache.flink.api.common.state.ValueStateDescriptor;
import org.apache.flink.api.common.typeinfo.TypeInformation;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.functions.co.CoProcessFunction;
import org.apache.flink.util.Collector;

import com.flink.example.trading.state.server.SoldOutStock.Status;

public class TradeProcessing extends CoProcessFunction<StockPrices, BuyStock, SoldOutStock> {

	private ValueState<StockPrices> stockPricesState;

	@Override
	public void open(Configuration parameters) throws Exception {
		ValueStateDescriptor<StockPrices> stateDescriptor = new ValueStateDescriptor<>("stockPricesState",
				TypeInformation.of(StockPrices.class));
		stateDescriptor.setQueryable("stock-status");
		stockPricesState = getRuntimeContext().getState(stateDescriptor);
	}

	@Override
	public void processElement1(StockPrices stockPrices,
			CoProcessFunction<StockPrices, BuyStock, SoldOutStock>.Context ctx, Collector<SoldOutStock> out)
			throws Exception {
		// add or update list of stock
		stockPricesState.update(stockPrices);
	}

	@Override
	public void processElement2(BuyStock buyStock, CoProcessFunction<StockPrices, BuyStock, SoldOutStock>.Context ctx,
			Collector<SoldOutStock> out) throws Exception {
		StockPrices listedStock = stockPricesState.value();
		if (listedStock == null) {
			out.collect(new SoldOutStock(buyStock.getStock(), 0, Status.NOT_LISTED));
			return;
		} else if (listedStock.getQuantity() < buyStock.getQuantity()) {
			out.collect(new SoldOutStock(buyStock.getStock(), listedStock.getQuantity(), Status.INSUFFICIENT_QTY));
			return;
		}
		listedStock.setQuantity(listedStock.getQuantity() - buyStock.getQuantity());
		// if all quantity sold out, make stock unlisted
		if (listedStock.getQuantity() == 0) {
			stockPricesState.clear();
		} else {
			stockPricesState.update(listedStock);
		}
		out.collect(new SoldOutStock(buyStock.getStock(), listedStock.getQuantity(), Status.SOLD));
	}

}
