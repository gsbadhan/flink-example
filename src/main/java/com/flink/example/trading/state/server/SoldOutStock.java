package com.flink.example.trading.state.server;

public class SoldOutStock {
	public String stock;
	public int quantity;
	public Status status;

	public enum Status {
		SOLD, INSUFFICIENT_QTY, INSUFFICIENT_PRICE, NOT_LISTED
	}

	public SoldOutStock() {
	}

	public SoldOutStock(String stock, int quantity, Status status) {
		super();
		this.stock = stock;
		this.quantity = quantity;
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}

	public void setStatus(Status status) {
		this.status = status;
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
		return "SoldOutStock [stock=" + stock + ", quantity=" + quantity + ", status=" + status + "]";
	}

}
