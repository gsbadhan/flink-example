package com.flink.example.trading.state.server;

public class BuyStock {
	public String stock;
	public int quantity;

	public BuyStock() {
	}

	public BuyStock(String stock, int quantity) {
		super();
		this.stock = stock;
		this.quantity = quantity;
	}

	public String getStock() {
		return stock;
	}

	public void setStock(String stock) {
		this.stock = stock;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	@Override
	public String toString() {
		return "BuyStock [stock=" + stock + ", quantity=" + quantity + "]";
	}

}
