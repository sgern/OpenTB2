package com.sgern.TitleBoutII;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;

public class Game {
	
	private int roundNumber = 1;
	private boolean controlFound = false;
	private boolean inKI = false;
	private String ringPosition = "Ring Center";
	private Referee referee;
	private Options options;
	private Fighter fighter1;
	private Fighter fighter2;
	private Fighter attacker;
	private Fighter defender;
	private Fighter pinner;
	private Fighter pinned;
	private Fighter previousWinner;
	private List<Card> fullDeck;
	private Deck deck1;
	private Deck deck2;
	private Deck activeDeck;
	private Deck inactiveDeck;
	private XSSFSheet kdkoTable;
	private XSSFSheet cutsSwellingTable;
	
	public Game(Fighter fighter1, Fighter fighter2, List<Card> fullDeck, Options options, Referee referee, XSSFSheet cutsSwellingTable, XSSFSheet kdkoTable) {
		this.fighter1 = fighter1;
		this.fighter2 = fighter2;
		this.fullDeck = fullDeck;
		this.options = options;
		this.referee = referee;
		this.cutsSwellingTable = cutsSwellingTable;
		this.cutsSwellingTable.removeRow(this.cutsSwellingTable.getRow(0));
		this.kdkoTable = kdkoTable;
	}
	
	public int getRoundNumber() {
		return roundNumber;
	}
	
	public void setRoundNumber(int roundNumber) {
		this.roundNumber = roundNumber;
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

	public Options getOptions() {
		return options;
	}

	public void setOptions(Options options) {
		this.options = options;
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
	}

	public Deck getInactiveDeck() {
		return inactiveDeck;
	}

	public void setInactiveDeck(Deck inactiveDeck) {
		this.inactiveDeck = inactiveDeck;
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

	public String getKDKO(int kd, int kd1) {
		return kdkoTable.getRow(kd).getCell(kd1 + 1).getStringCellValue();
	}

	public XSSFSheet getCutsSwellingTable() {
		return cutsSwellingTable;
	}
	
}
