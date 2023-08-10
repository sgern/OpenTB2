package com.sgern.OpenTB2;

public class Card {
	
	private int cf;
	private int rn;
	private int result;
	private boolean cut;
	private String ringPosition;
	private int kd;
	private int kd2;
	private int kor;
	private int kdc;
	private String j1;
	private String j2;
	private String j3;
	private int injury;
	private int specialAction;
	
	public int getCF() {
		return cf;
	}
	
	public void setCF(int cf) {
		this.cf = cf;
	}
	
	public int getRN() {
		return rn > 80 ? result : rn;
	}
	
	public int getExtendedRN() {
		return rn;
	}
	
	public void setRN(int rn) {
		this.rn = rn;
	}
	
	public int getResult() {
		return result;
	}
	
	public void setResult(int result) {
		this.result = result;
	}

	public boolean isCut() {
		return cut;
	}

	public void setCut(boolean cut) {
		this.cut = cut;
	}

	public String getRingPosition() {
		return ringPosition;
	}

	public void setRingPosition(String ringPosition) {
		this.ringPosition = ringPosition;
	}

	public int getKD() {
		return kd;
	}

	public void setKD(int kd) {
		this.kd = kd;
	}

	public int getKD2() {
		return kd2;
	}

	public void setKD2(int kd2) {
		this.kd2 = kd2;
	}

	public int getKOR() {
		return kor;
	}

	public void setKOR(int kor) {
		this.kor = kor;
	}

	public int getKDC() {
		return kdc;
	}

	public void setKDC(int kdc) {
		this.kdc = kdc;
	}

	public String getJ1() {
		return j1;
	}

	public void setJ1(String j1) {
		this.j1 = j1;
	}

	public String getJ2() {
		return j2;
	}

	public void setJ2(String j2) {
		this.j2 = j2;
	}

	public String getJ3() {
		return j3;
	}

	public void setJ3(String j3) {
		this.j3 = j3;
	}

	public int getInjury() {
		return injury;
	}

	public void setInjury(int injury) {
		this.injury = injury;
	}

	public int getSpecialAction() {
		return specialAction;
	}

	public void setSpecialAction(int specialAction) {
		this.specialAction = specialAction;
	}
	
}
