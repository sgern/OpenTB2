package com.sgern.TitleBoutII;

public class Referee {
	
	private String name;
	private String fouls;
	private String stoppage;
	private int consistency;
	private int boxerRate;
	private int availability;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getFouls() {
		return fouls;
	}
	
	public void setFouls(String fouls) {
		this.fouls = fouls;
	}
	
	public String getStoppage() {
		return stoppage;
	}
	
	public void setStoppage(String stoppage) {
		this.stoppage = stoppage;
	}
	
	public int getConsistency() {
		return consistency;
	}
	
	public void setConsistency(int consistency) {
		this.consistency = consistency;
	}
	
	public int getBoxerRate() {
		return boxerRate;
	}
	
	public void setBoxerRate(int boxerRate) {
		this.boxerRate = boxerRate;
	}
	
	public int getAvailability() {
		return availability;
	}
	
	public void setAvailability(int availability) {
		this.availability = availability;
	}
	
}
