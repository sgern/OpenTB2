package com.sgern.TitleBoutII;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

public class Deck {
	
	private List<Card> drawPile;
	private List<Card> discardPile;
	
	public Deck(List<Card> drawPile) {
		this.drawPile = drawPile;
	}
	
	public Card drawCard() {
		Card card = drawPile.remove(0);
		discardPile.add(card);
		return card;
	}
	
	public Card drawAndReturn() {
		Card card = drawPile.remove(0);
		drawPile.add(card);
		return card;
	}
	
	public List<Card> drawStack(int cards) {
		List<Card> stack = new ArrayList<>();
		for (int i = 0; i < cards && !this.isEmpty(); i++) {
			stack.add(this.drawCard());
		}
		return stack;
	}
	
	public void shuffleDeck() {
		drawPile.addAll(discardPile);
		discardPile.clear();
		Collections.shuffle(drawPile);
	}
	
	public boolean isEmpty() {
		return drawPile.isEmpty();
	}
	
}
