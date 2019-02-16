package com.flink.example.trading.state.server;

public class StockPrices {
	public String stock;
	public double price;
	public int quantity;

	public StockPrices() {
	}

	public StockPrices(String stock, double price, int quantity) {
		super();
		this.stock = stock;
		this.price = price;
		this.quantity = quantity;
	}

	public String getStock() {
		return stock;
	}

	public void setStock(String stock) {
		this.stock = stock;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	@Override
	public String toString() {
		return "StockPrices [stock=" + stock + ", price=" + price + ", quantity=" + quantity + "]";
	}

}
