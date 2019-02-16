package com.flink.example.wordcount.state.server;

public class CountWithTimestamp {
	public String key;
	public long count;
	public long lastModified;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public long getCount() {
		return count;
	}

	public void setCount(long count) {
		this.count = count;
	}

	public long getLastModified() {
		return lastModified;
	}

	public void setLastModified(long lastModified) {
		this.lastModified = lastModified;
	}

	@Override
	public String toString() {
		return "CountWithTimestamp [key=" + key + ", count=" + count + ", lastModified=" + lastModified + "]";
	}

}
