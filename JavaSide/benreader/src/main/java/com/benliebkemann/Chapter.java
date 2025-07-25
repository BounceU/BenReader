package com.benliebkemann;

public class Chapter {
	private String name;
	private int number;
	private boolean use;

	public Chapter(int number, String name) {
		this.name = name;
		this.number = number;
		this.use = true;
	}

	public boolean getShouldUse() {
		return this.use;
	}

	public void setShouldUse(boolean shouldUse) {
		this.use = shouldUse;
	}

	public String getName() {
		return name;
	}

	public int getNumber() {
		return number;
	}

	@Override
	public String toString() {
		return this.name;
	}

}
