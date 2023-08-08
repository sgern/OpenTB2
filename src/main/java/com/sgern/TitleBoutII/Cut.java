package com.sgern.TitleBoutII;

public class Cut {
	
	private String cutType;
	private int dp;
	
	public Cut(String cutType, int dp) {
		this.cutType = cutType;
		this.dp = dp;
	}

	public String getCutType() {
		return cutType;
	}

	public int getDP() {
		return dp;
	}

	public void worsenCut() {
		dp++;
	}
}
