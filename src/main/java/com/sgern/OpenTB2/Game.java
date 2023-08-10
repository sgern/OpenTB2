package com.sgern.OpenTB2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class Game {
	
	private int roundNumber = 1;
	private int finalRoundNumber;
	private boolean controlFound = false;
	private boolean inKI = false;
	private boolean inOvertime = false;
	private boolean gameOver = false;
	private boolean inControlCheck = false;
	private String ringPosition = "Ring Center";
	private Referee referee;
	private String doctorRating;
	private Fighter fighter1;
	private Fighter fighter2;
	private Fighter[] fighters = new Fighter[2];
	private Fighter attacker;
	private Fighter defender;
	private Fighter pinner;
	private Fighter pinned;
	private Fighter previousWinner = null;
	private List<Card> fullDeck;
	private Deck deck1 = new Deck(new ArrayList<Card>());
	private Deck deck2 = new Deck(new ArrayList<Card>());
	private Deck activeDeck;
	private Deck inactiveDeck;
	private Deck kiStack;
	private XSSFSheet kdkoTable;
	private XSSFSheet cutsSwellingTable;
	
	public Game(Fighter fighter1, Fighter fighter2, List<Card> fullDeck, Referee referee, String doctorRating, Options options, XSSFSheet cutsSwellingTable, XSSFSheet kdkoTable) {
		this.fighter1 = fighter1;
		this.fighter2 = fighter2;
		this.fullDeck = fullDeck;
		this.doctorRating = doctorRating;
		this.referee = referee;
		this.cutsSwellingTable = cutsSwellingTable;
		this.kdkoTable = kdkoTable;
		
		this.fighters[0] = fighter1;
		this.fighters[1] = fighter2;
		this.finalRoundNumber = options.getMaxRounds();
	}
	
	public int getRoundNumber() {
		return roundNumber;
	}
	
	public void incrementRoundNumber() {
		roundNumber++;
	}
	
	public int getFinalRoundNumber() {
		return finalRoundNumber;
	}

	public void setFinalRoundNumber(int finalRoundNumber) {
		this.finalRoundNumber = finalRoundNumber;
	}

	public boolean isControlFound() {
		return controlFound;
	}

	public void setControlFound(boolean controlFound) {
		this.controlFound = controlFound;
	}

	public boolean isInKI() {
		return inKI;
	}

	public void setInKI(boolean inKI) {
		this.inKI = inKI;
	}

	public boolean isInOvertime() {
		return inOvertime;
	}

	public void setInOvertime(boolean inOvertime) {
		this.inOvertime = inOvertime;
	}

	public boolean isGameOver() {
		return gameOver;
	}

	public void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
	}

	public boolean isInControlCheck() {
		return inControlCheck;
	}

	public void setInControlCheck(boolean inControlCheck) {
		this.inControlCheck = inControlCheck;
	}

	public String getRingPosition() {
		return ringPosition;
	}
	
	public void setRingPosition(String ringPosition) {
		this.ringPosition = ringPosition;
	}

	public Referee getReferee() {
		return referee;
	}

	public void setReferee(Referee referee) {
		this.referee = referee;
	}

	public String getDoctorRating() {
		return doctorRating;
	}

	public Fighter getFighter1() {
		return fighter1;
	}

	public void setFighter1(Fighter fighter1) {
		this.fighter1 = fighter1;
	}

	public Fighter getFighter2() {
		return fighter2;
	}

	public void setFighter2(Fighter fighter2) {
		this.fighter2 = fighter2;
	}

	public Fighter[] getFighters() {
		return fighters;
	}
	
	public Fighter getOpponentOf(Fighter fighter) {
		return fighter.equals(fighter1) ? fighter2 : fighter1;
	}

	public Fighter getAttacker() {
		return attacker;
	}

	public void setAttacker(Fighter attacker) {
		this.attacker = attacker;
		this.defender = this.attacker.equals(fighter1) ? fighter2 : fighter1;
	}

	public Fighter getDefender() {
		return defender;
	}

	public Fighter getPinner() {
		return pinner;
	}

	public void setPinner(Fighter pinner) {
		this.pinner = pinner;
	}

	public Fighter getPinned() {
		return pinned;
	}
	
	public void setPinned(Fighter pinned) {
		this.pinned = pinned;
	}

	public Fighter getPreviousWinner() {
		return previousWinner;
	}

	public void setPreviousWinner(Fighter previousWinner) {
		this.previousWinner = previousWinner;
	}

	public Deck getDeck1() {
		return deck1;
	}

	public void setDeck1(Deck deck1) {
		this.deck1 = deck1;
	}

	public Deck getDeck2() {
		return deck2;
	}

	public void setDeck2(Deck deck2) {
		this.deck2 = deck2;
	}

	public Deck getActiveDeck() {
		return activeDeck;
	}

	public void setActiveDeck(Deck activeDeck) {
		this.activeDeck = activeDeck;
		this.inactiveDeck = this.activeDeck.equals(deck1) ? deck2 : deck1;
	}

	public Deck getInactiveDeck() {
		return inactiveDeck;
	}
	
	public void makeDecks() {
		deck1.clearDeck();
		deck2.clearDeck();
		Collections.shuffle(fullDeck);
		ListIterator<Card> listIterator = fullDeck.listIterator();
		while (listIterator.hasNext()) {
			deck1.addCard(listIterator.next());
			deck2.addCard(listIterator.next());
		}
	}

	public Deck getKIStack() {
		return kiStack;
	}

	public void setKIStack(Deck kiStack) {
		this.kiStack = kiStack;
	}

	public String getKDKO(int kd, int kd1) {
		if (kdkoTable.getRow(kd).getCell(kd1 + 1).getCellType().equals(CellType.NUMERIC)) {
			return Integer.toString((int) kdkoTable.getRow(kd).getCell(kd1 + 1).getNumericCellValue());
		}
		return kdkoTable.getRow(kd).getCell(kd1 + 1).getStringCellValue();
	}

	public XSSFSheet getCutsSwellingTable() {
		return cutsSwellingTable;
	}

	public int getCondition(int rn, int ko) {
		if (ko <= 1) {
			if (rn <= 75) return 0;
			if (rn <= 78) return 1;
			if (rn == 79) return 2;
			return 3;
		} else if (ko <= 3) {
			if (rn <= 70) return 0;
			if (rn <= 76) return 1;
			if (rn <= 78) return 2;
			return 3;
		} else {
			if (rn <= 65) return 0;
			if (rn <= 74) return 1;
			if (rn <= 77) return 2;
			return 3;
		}
	}
	
	public String getFoul(int rn, String foul) {
		switch (foul) {
			case "A":
				if (rn <= 66) return "tells both fighters to keep it clean";
				if (rn <= 68) return "for hitting below the belt (low blow)";
				if (rn <= 70) return "for leading with the head (head butt)";
				if (rn <= 72) return "for hitting behind the head (rabbit punching)";
				if (rn <= 73) return "for using an arm to push his opponent's head down";
				if (rn <= 74) return "for following up a punch with an elbow";
				if (rn <= 75) return "for hitting on the break";
				if (rn <= 77) return "for refusing to break cleanly";
				return "for eye gouging";
			case "B":
				if (rn <= 64) return "tells both fighters to keep it clean";
				if (rn <= 67) return "for hitting below the belt (low blow)";
				if (rn <= 69) return "for leading with the head (head butt)";
				if (rn <= 71) return "for hitting behind the head (rabbit punching)";
				if (rn <= 72) return "for using an arm to push his opponent's head down";
				if (rn <= 73) return "for following up a punch with an elbow";
				if (rn <= 74) return "for hitting on the break";
				if (rn <= 76) return "for refusing to break cleanly";
				return "for eye gouging";
			case "C":
				if (rn <= 62) return "tells both fighters to keep it clean";
				if (rn <= 66) return "for hitting below the belt (low blow)";
				if (rn <= 69) return "for leading with the head (head butt)";
				if (rn <= 70) return "for hitting behind the head (rabbit punching)";
				if (rn <= 71) return "for using an arm to push his opponent's head down";
				if (rn <= 72) return "for following up a punch with an elbow";
				if (rn <= 73) return "for hitting on the break";
				if (rn <= 75) return "for refusing to break cleanly";
				return "for eye gouging";
			case "D":
				if (rn <= 57) return "tells both fighters to keep it clean";
				if (rn <= 63) return "for hitting below the belt (low blow)";
				if (rn <= 66) return "for leading with the head (head butt)";
				if (rn <= 68) return "for hitting behind the head (rabbit punching)";
				if (rn <= 69) return "for using an arm to push his opponent's head down";
				if (rn <= 70) return "for following up a punch with an elbow";
				if (rn <= 72) return "for hitting on the break";
				if (rn <= 74) return "for refusing to break cleanly";
				return "for eye gouging";
			default:
				if (rn <= 52) return "tells both fighters to keep it clean";
				if (rn <= 57) return "for hitting below the belt (low blow)";
				if (rn <= 62) return "for leading with the head (head butt)";
				if (rn <= 66) return "for hitting behind the head (rabbit punching)";
				if (rn <= 67) return "for using an arm to push his opponent's head down";
				if (rn <= 69) return "for following up a punch with an elbow";
				if (rn <= 72) return "for hitting on the break";
				if (rn <= 74) return "for refusing to break cleanly";
				return "for eye gouging";
		}
	}
	
}
