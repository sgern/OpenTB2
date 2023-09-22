package com.sgern.OpenTB2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class App {
	
	static AppView view = new AppView();
	static Scanner in = new Scanner(System.in);
	static String pathname = "OpenTB2.xlsx";
	static XSSFWorkbook workbook;
	static XSSFSheet cutsSwellingTable;
	static {
    	File f = new File("options.ini");
    	if (!f.exists()) {
    		try {
				f.createNewFile();
			} catch (IOException e) {
				System.err.println("ERROR: Unable to create options.ini!");
			}
    	}
    	
	}
	static {
		try {
			workbook = new XSSFWorkbook(new FileInputStream(new File(pathname)));
			cutsSwellingTable = workbook.getSheet("Cuts and Swelling");
			cutsSwellingTable.removeRow(cutsSwellingTable.getRow(0));
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: " + pathname + " not found!");
		} catch (IOException e) {
			System.err.println("ERROR: Unable to read/write " + pathname + "!");
		}
	}
	static Options options;
	
	public static void gameLoop(Game game) {
		while (!game.isGameOver()) {
			roundStart(game);
			roundLoop(game);
			roundFinish(game);
		}
	}
	
	public static void roundStart(Game game) {
		for (Fighter fighter : game.getFighters()) {
			if (fighter.isNextRoundCheck()) {
				if (fighter.getDamage() >= 10 && fighter.getDamage() <= 15) {
					view.messageLog.addToLog("The referee considers calling the doctor over to take a look at " + fighter.getName() + ".");
					view.printGame(game, 2000);
					int cf = game.getInactiveDeck().drawAndReturn().getResult();
					int performDoctorCheck = 0;
					switch (game.getReferee().getFouls()) {
						case "Strict":
							performDoctorCheck = 14;
							break;
						case "Normal":
							performDoctorCheck = 10;
							break;
						case "Lenient":
							performDoctorCheck = 7;
							break;
						case "Very Lenient":
							performDoctorCheck = 5;
							break;
					}
					if (cf <= performDoctorCheck) {
						doctorCheck(game, fighter);
					}
				} else if (fighter.getDamage() <= 20) {
					doctorCheck(game, fighter);
				} else {
					endGame(game, game.getOpponentOf(fighter), "technical knockout");
					return;
				}
				fighter.setNextRoundCheck(false);
			}
		}
		
		// make decks and use first deck in even rounds, use second deck in odd rounds
		if (game.getRoundNumber() % 2 != 0) {
			game.makeDecks();
			game.setActiveDeck(game.getDeck1());
		} else {
			game.setActiveDeck(game.getDeck2());
		}

		Fighter winner = null;
		
		for (Fighter fighter : game.getFighters()) {
			// fighter with broken rib checks if they can continue
			if (fighter.getInjuries().contains(2)) {
				if (game.getInactiveDeck().drawAndReturn().getRN() <= 8) {
					view.messageLog.addToLog(fighter.getName() + " is unable to continue due to his broken rib!");
					view.printGame(game, 2000);
					if (winner != null) {
						endGame(game, null, "draw");
						return;
					} else {
						winner = game.getOpponentOf(fighter);
					}
				}
			}
		}
		if (winner != null) {
			endGame(game, winner, "technical knockout");
			return;
		}
		
		for (Fighter fighter : game.getFighters()) {
			// fighter with broken nose loses END
			if (fighter.getInjuries().contains(4)) {
				fighter.modifyEND(-5);
				view.messageLog.addToLog(fighter.getName() + " loses some stamina from his broken nose.");
				view.printGame(game, 2000);
			}
		}
		
		if (game.getRoundNumber() == game.getFinalRoundNumber()) {
			view.messageLog.addToLog(game.getFighter1().getName() + " and " + game.getFighter2().getName() + " touch gloves. This is the final round!");
			view.printGame(game, 2000);
		}
		
		view.messageLog.addToLog("Round " + game.getRoundNumber() + ", START!");
		view.printGame(game, 2000);
		
		// set starting attacker
		if (game.getFighter1().getAGG() > game.getFighter2().getAGG()) {
			game.setAttacker(game.getFighter1());
		} else if (game.getFighter2().getAGG() > game.getFighter1().getAGG()) {
			game.setAttacker(game.getFighter2());
		} else if (game.getRoundNumber() == 1) {
			if (game.getFighter1().getOR() > game.getFighter2().getOR()) {
				game.setAttacker(game.getFighter1());
			} else if (game.getFighter2().getOR() > game.getFighter1().getOR()) {
				game.setAttacker(game.getFighter2());
			} else {
				if (game.getPreviousWinner() == null) {
					int rn1 = game.getInactiveDeck().drawAndReturn().getRN();
					int rn2 = game.getInactiveDeck().drawAndReturn().getRN();
					while (rn1 == rn2) {
						rn2 = game.getInactiveDeck().drawAndReturn().getRN();
					}
					Fighter attacker = rn1 > rn2 ? game.getFighter1() : game.getFighter2();
					game.setAttacker(attacker);
				} else {
					game.setAttacker(game.getPreviousWinner());
				}
			}
		}
		view.messageLog.addToLog(game.getAttacker().getName() + " takes the initiative!");
		view.printGame(game, 2000);
		
		game.setControlFound(false);
	}
	
	public static void roundLoop(Game game) {
		if (options.getERatingSelectionFrequency().equals("PerRound")) {
			// E-rated fighters choose fighting style
			for (Fighter fighter : game.getFighters()) {
				if (fighter.getStyle().toUpperCase().equals("E")) {
					view.messageLog.addToLog("Choose " + fighter.getName() + "'s fighting style: (B/S)");
					boolean done = false;
					while (!done) {
						view.printGame(game, 2000);
						switch (in.next()) {
							case "B":
								done = true;
								fighter.setCurrentStyle("B");
								fighter.modifyKP(-2);
								break;
							case "S":
								done = true;
								if (fighter.getCurrentStyle().equals("B")) {
									fighter.modifyKP(2);
								}
								fighter.setCurrentStyle("S");
								break;
						}
					}
				}
			}
		}
		while (!game.getActiveDeck().isEmpty() && !game.isGameOver()) {
			
			if (options.getERatingSelectionFrequency().equals("PerTurn")) {
				// E-rated fighters choose fighting style
				for (Fighter fighter : game.getFighters()) {
					if (fighter.getStyle().toUpperCase().equals("E")) {
						view.messageLog.addToLog("Choose " + fighter.getName() + "'s fighting style: (B/S)");
						boolean done = false;
						while (!done) {
							view.printGame(game, 2000);
							switch (in.next()) {
								case "B":
									done = true;
									fighter.setCurrentStyle("B");
									fighter.modifyKP(-2);
									break;
								case "S":
									done = true;
									if (fighter.getCurrentStyle().equals("B")) {
										fighter.modifyKP(2);
									}
									fighter.setCurrentStyle("S");
									break;
							}
						}
					}
				}
			}
			
			// if not in KI, do control check
			if (!game.isInKI()) {
				controlCheck(game);
				game.setControlFound(false);
			}
			
			actionSelection(game);
			
			// if in KI and KI stack is empty, exit KI
			if (game.isInKI() && game.getKIStack().isEmpty()) {
				game.setInKI(false);
				view.messageLog.addToLog(game.getAttacker().getName() + " calms down a bit.");
				view.printGame(game, 2000);
			}
		}
	}
	
	public static void controlCheck(Game game) {
		game.setInControlCheck(true);
		while (!game.getActiveDeck().isEmpty() && !game.isControlFound()) {
			int cardCF = game.getActiveDeck().drawCard().getCF();
			int attackerCF = game.getDefender().getCurrentStyle().equals("B") ? game.getAttacker().getCFB() : game.getAttacker().getCFS();
			if (cardCF <= attackerCF) {
				game.setControlFound(true);
				view.messageLog.addToLog(game.getAttacker().getName() + " takes control!");
			} else {
				game.setAttacker(game.getDefender());
				view.messageLog.addToLog("The two fighters struggle for control...");
				view.printGame(game, 2000);
			}
		}
		game.setInControlCheck(false);
		view.printGame(game, 2000);
	}
	
	public static void actionSelection(Game game) {
		Deck deck = game.isInKI() && !game.getKIStack().isEmpty() ? game.getKIStack() : game.getActiveDeck();
		if (!deck.isEmpty()) {
			// get RN, including foul/injury/special
			int rn = deck.drawCard().getExtendedRN();
			
			// select action based on RN
			if (rn <= game.getAttacker().getKP()) {
				knockdown(game, false);
			} else if (rn <= game.getAttacker().getPL() + game.getDefender().getDEF()) {
				punchesLanded(game);
			} else if (rn <= game.getAttacker().getPM()) {
				if (rn >= game.getAttacker().getCPD()) {
					counterpunch(game);
				} else {
					punchesMissed(game);
				}
			} else if (rn <= game.getAttacker().getC()) {
				clinching(game);
			} else if (rn <= 80) {
				ringMovement(game);
			} else if (rn == 81) {
				foul(game);
			} else if (rn == 82) {
				injury(game);
			} else if (rn == 83) {
				special(game);
			}
		}
	}
	
	public static void punchesLanded(Game game) {
		Deck deck = game.isInKI() && !game.getKIStack().isEmpty() ? game.getKIStack() : game.getActiveDeck();
		
		// if in KI and the deck is empty, enter overtime to resolve landed punch
		game.setInOvertime(deck.isEmpty() && game.isInKI());
		
		if (!deck.isEmpty() || game.isInOvertime()) {
			
			// if in overtime, get card from inactive deck, otherwise get it from the normal place
			Card card = game.isInOvertime() ? game.getInactiveDeck().drawAndReturn() : deck.drawCard();
			int result = card.getResult();
			
			// select punch based on result
			if (result == 1) {
				game.getAttacker().scorePoints(1);
				view.messageLog.addToLog(game.getAttacker().getName() + " lands a blow, but " + game.getDefender().getName() + " partially blocks it.");
			} else if (result <= game.getAttacker().getJab3()) {
				game.getAttacker().scorePoints(3);
				view.messageLog.addToLog(game.getAttacker().getName() + " lands a hard jab on " + game.getDefender().getName() + "!");
			} else if (result <= game.getAttacker().getJab2()) {
				game.getAttacker().scorePoints(2);
				view.messageLog.addToLog(game.getAttacker().getName() + " lands a jab on " + game.getDefender().getName() + "!");
			} else if (result <= game.getAttacker().getHook3()) {
				game.getAttacker().scorePoints(3);
				view.messageLog.addToLog(game.getAttacker().getName() + " lands a hard hook on " + game.getDefender().getName() + "!");
			} else if (result <= game.getAttacker().getHook2()) {
				game.getAttacker().scorePoints(2);
				view.messageLog.addToLog(game.getAttacker().getName() + " lands a hook on " + game.getDefender().getName() + "!");
			} else if (result <= game.getAttacker().getCross3()) {
				game.getAttacker().scorePoints(3);
				view.messageLog.addToLog(game.getAttacker().getName() + " lands a hard cross on " + game.getDefender().getName() + "!");
			} else if (result <= game.getAttacker().getCross2()) {
				game.getAttacker().scorePoints(2);
				view.messageLog.addToLog(game.getAttacker().getName() + " lands a cross on " + game.getDefender().getName() + "!");
			} else if (result <= game.getAttacker().getCombination3()) {
				game.getAttacker().scorePoints(3);
				view.messageLog.addToLog(game.getAttacker().getName() + " lands a hard combination on " + game.getDefender().getName() + "!");
			} else if (result <= game.getAttacker().getCombination2()) {
				game.getAttacker().scorePoints(2);
				view.messageLog.addToLog(game.getAttacker().getName() + " lands a combination on " + game.getDefender().getName() + "!");
			} else if (result <= game.getAttacker().getUppercut3()) {
				game.getAttacker().scorePoints(3);
				view.messageLog.addToLog(game.getAttacker().getName() + " lands a hard uppercut on " + game.getDefender().getName() + "!");
			} else {
				game.getAttacker().scorePoints(2);
				view.messageLog.addToLog(game.getAttacker().getName() + " lands an uppercut on " + game.getDefender().getName() + "!");
			}
			view.printGame(game, 2000);
			
			// check for TKO based on in-round scoring
			tkoCheck(game);
			if (game.isGameOver()) {
				return;
			}
			
			// if the card denotes a cut, resolve a cut
			if (card.isCut()) {
				cut(game);
			}
			game.setControlFound(false);
		}
	}
	
	public static void knockdown(Game game, boolean isCounterpunch) {
		Deck deck = game.isInKI() && !game.getKIStack().isEmpty() ? game.getKIStack() : game.getActiveDeck();
		if (!deck.isEmpty() || game.isInOvertime()) {
			
			// if in overtime, get card from inactive deck, otherwise get it from the normal place
			Card card = game.isInOvertime() ? game.getInactiveDeck().drawAndReturn() : deck.drawCard();
			
			// use KD2 if the defender has a carryover effect or has been knocked down this round, otherwise use KD1
			int kd = game.getDefender().isCarryover() || game.getDefender().getKnockdowns() > 0 ? card.getKD2() : card.getKD();
			deck = game.isInKI() && !game.getKIStack().isEmpty() ? game.getKIStack() : game.getActiveDeck();
			
			// create variables to track actual point gain, used for threshold effects
			int oldPoints, newPoints;
			
			// cross-reference the card's chosen KD value and the defender's modified KD1 value on the KD/KO table to find the result
			switch (game.getKDKO(kd, game.getDefender().getKD1())) {
				case "4":
					game.getAttacker().scorePoints(4);
					view.messageLog.addToLog(game.getAttacker().getName() + " lands a critical hit on " + game.getDefender().getName() + "!");
					view.printGame(game, 2000);
					
					// check for TKO based on in-round scoring
					tkoCheck(game);
					if (game.isGameOver()) {
						return;
					}
					break;
				case "5":
					oldPoints = game.getAttacker().getPoints();
					game.getAttacker().scorePoints(5);
					view.messageLog.addToLog(game.getAttacker().getName() + " lands a critical hit on " + game.getDefender().getName() + "!");
					view.printGame(game, 2000);
					
					// check for TKO based on in-round scoring
					tkoCheck(game);
					if (game.isGameOver()) {
						return;
					}
					newPoints = game.getAttacker().getPoints();
					
					// if a counterpunch lands in KI that scores at least 5 points, exit KI and go to Ring Center
					if (game.isInKI() && isCounterpunch && newPoints - oldPoints >= 5) {
						game.getActiveDeck().returnStack(game.getKIStack());
						view.messageLog.addToLog(game.getDefender().getName() + " loses his momentum from the heavy counterattack!");
						view.printGame(game, 2000);
						
						// if not in Ring Center, move there and normalize CF and pinner/pinned
						if (!game.getRingPosition().equals("Ring Center")) {
							game.setRingPosition("Ring Center");
							game.getPinner().modifyCFB(-1);
							game.getPinner().modifyCFS(-2);
							view.messageLog.addToLog("The fighters move to the " + game.getRingPosition() + ".");
							game.setPinner(null);
							game.setPinned(null);
							view.printGame(game, 2000);
						}
					}
					break;
				case "5H":
					oldPoints = game.getAttacker().getPoints();
					game.getAttacker().scorePoints(5);
					view.messageLog.addToLog(game.getAttacker().getName() + " lands a critical hit on " + game.getDefender().getName() + "!");
					view.printGame(game, 2000);
					
					// check for TKO based on in-round scoring
					tkoCheck(game);
					if (game.isGameOver()) {
						return;
					}
					newPoints = game.getAttacker().getPoints();
					if (game.isInKI() && isCounterpunch && newPoints - oldPoints >= 5) {
						game.getActiveDeck().returnStack(game.getKIStack());
						view.messageLog.addToLog(game.getDefender().getName() + " loses his momentum from the heavy counterattack!");
						view.printGame(game, 2000);
						
						// if not in Ring Center, move there and normalize CF and pinner/pinned
						if (!game.getRingPosition().equals("Ring Center")) {
							game.setRingPosition("Ring Center");
							game.getPinner().modifyCFB(-1);
							game.getPinner().modifyCFS(-2);
							game.setPinner(null);
							game.setPinned(null);
							view.messageLog.addToLog("The fighters move to the " + game.getRingPosition() + ".");
							view.printGame(game, 2000);
						}
					}
					game.setControlFound(true);
					view.messageLog.addToLog(game.getAttacker().getName() + " presses the advantage!");
					view.printGame(game, 2000);
					break;
				case "5F":
					oldPoints = game.getAttacker().getPoints();
					game.getAttacker().scorePoints(5);
					view.messageLog.addToLog(game.getAttacker().getName() + " lands a critical hit on " + game.getDefender().getName() + "!");
					view.printGame(game, 2000);
					
					// check for TKO based on in-round scoring
					tkoCheck(game);
					if (game.isGameOver()) {
						return;
					}
					newPoints = game.getAttacker().getPoints();
					if (newPoints - oldPoints >= 5 && !game.isInKI()) {
						killerInstinct(game, true);
					} else if (game.isInKI() && isCounterpunch && newPoints - oldPoints >= 5) {
						game.getActiveDeck().returnStack(game.getKIStack());
						view.messageLog.addToLog(game.getDefender().getName() + " loses his momentum from the heavy counterattack!");
						view.printGame(game, 2000);
					}
					break;
				case "K1-10":
					game.getAttacker().scorePoints(6);
					view.messageLog.addToLog(game.getAttacker().getName() + " lands a critical hit on " + game.getDefender().getName() + "!");
					view.printGame(game, 2000);
					
					// check for TKO based on in-round scoring
					tkoCheck(game);
					if (game.isGameOver()) {
						return;
					}
					if (game.isInKI() && isCounterpunch) {
						game.getActiveDeck().returnStack(game.getKIStack());
						view.messageLog.addToLog(game.getDefender().getName() + " loses his momentum from the heavy counterattack!");
						view.printGame(game, 2000);
						
						// if not in Ring Center, move there and normalize CF and pinner/pinned
						if (!game.getRingPosition().equals("Ring Center")) {
							game.setRingPosition("Ring Center");
							game.getPinner().modifyCFB(-1);
							game.getPinner().modifyCFS(-2);
							game.setPinner(null);
							game.setPinned(null);
							view.messageLog.addToLog("The fighters move to the " + game.getRingPosition() + ".");
						}
					}
					card = game.isInOvertime() ? game.getInactiveDeck().drawAndReturn() : deck.drawCard();
					kd = game.getDefender().isCarryover() || game.getDefender().getKnockdowns() > 0 ? card.getKD2() : card.getKD();
					if (kd <= 10) {
						knockout(game);
						if (!game.isInKI() && !game.isGameOver()) {
							killerInstinct(game, false);
						}
					} else {
						game.getDefender().modifyKD1(game.getDefender().getKD2());
						view.messageLog.addToLog(game.getDefender().getName() + " feels a bit shaky...");
						view.printGame(game, 2000);
					}
					break;
				case "K":
					game.getAttacker().scorePoints(6);
					view.messageLog.addToLog(game.getAttacker().getName() + " lands a critical hit on " + game.getDefender().getName() + "!");
					view.printGame(game, 2000);
					
					// check for TKO based on in-round scoring
					tkoCheck(game);
					if (game.isGameOver()) {
						return;
					}
					if (game.isInKI() && isCounterpunch) {
						game.getActiveDeck().returnStack(game.getKIStack());
						view.messageLog.addToLog(game.getDefender().getName() + " loses his momentum from the heavy counterattack!");
						view.printGame(game, 2000);
						
						// if not in Ring Center, move there and normalize CF and pinner/pinned
						if (!game.getRingPosition().equals("Ring Center")) {
							game.setRingPosition("Ring Center");
							game.getPinner().modifyCFB(-1);
							game.getPinner().modifyCFS(-2);
							game.setPinner(null);
							game.setPinned(null);
							view.messageLog.addToLog("The fighters move to the " + game.getRingPosition() + ".");
							view.printGame(game, 2000);
						}
					}
					knockout(game);
					if (!game.isInKI() && !game.isGameOver()) {
						killerInstinct(game, false);
					}
					break;
				case "*":
					game.getAttacker().scorePoints(6);
					view.messageLog.addToLog(game.getAttacker().getName() + " lands a critical hit on " + game.getDefender().getName() + "!");
					view.printGame(game, 2000);
					
					// check for TKO based on in-round scoring
					tkoCheck(game);
					if (game.isGameOver()) {
						return;
					}
					if (game.isInKI() && isCounterpunch) {
						game.getActiveDeck().returnStack(game.getKIStack());
						view.messageLog.addToLog(game.getDefender().getName() + " loses his momentum from the heavy counterattack!");
						view.printGame(game, 2000);
						
						// if not in Ring Center, move there and normalize CF and pinner/pinned
						if (!game.getRingPosition().equals("Ring Center")) {
							game.setRingPosition("Ring Center");
							game.getPinner().modifyCFB(-1);
							game.getPinner().modifyCFS(-2);
							game.setPinner(null);
							game.setPinned(null);
							view.messageLog.addToLog("The fighters move to the " + game.getRingPosition() + ".");
							view.printGame(game, 2000);
						}
					}
					card = game.isInOvertime() ? game.getInactiveDeck().drawAndReturn() : deck.drawCard();
					kd = game.getDefender().isCarryover() || game.getDefender().getKnockdowns() > 0 ? card.getKD2() : card.getKD();
					if (kd == 1) {
						knockout(game);
						if (!game.isInKI() && !game.isGameOver()) {
							killerInstinct(game, false);
						}
					} else {
						game.getDefender().modifyKD1(game.getDefender().getKD2());
						view.messageLog.addToLog(game.getDefender().getName() + " feels a bit shaky...");
						view.printGame(game, 2000);
					}
					break;
			}
			
			// if the card denotes a cut, resolve a cut
			if (card.isCut()) {
				cut(game);
			}
		}
	}
	
	public static void knockout(Game game) {
		view.messageLog.addToLog(game.getDefender().getName() + " is knocked down!");
		view.printGame(game, 2000);
		Deck deck = game.isInKI() && !game.getKIStack().isEmpty() ? game.getKIStack() : game.getActiveDeck();
		if (!deck.isEmpty() || game.isInOvertime()) {
			
			// if in overtime, get card from inactive deck, otherwise get it from the normal place
			Card card = game.isInOvertime() ? game.getInactiveDeck().drawAndReturn() : deck.drawCard();
			int kor = card.getKOR();
			deck = game.isInKI() && !game.getKIStack().isEmpty() ? game.getKIStack() : game.getActiveDeck();
			
			switch (game.getKDKO(kor, game.getDefender().getKO())) {
			// all non-K results are the same
				case "4":
				case "5":
				case "5H":
				case "5F":
					card = game.isInOvertime() ? game.getInactiveDeck().drawAndReturn() : deck.drawCard();
					game.getDefender().addKnockdown();
					game.getDefender().addKDC(card.getKDC());
					view.messageLog.addToLog("");
					for (int i = 1; i <= card.getKDC(); i++) {
						view.messageLog.addToCurrentMessage(i + "... ");
						view.printGame(game, 1000);
					}
					view.messageLog.addToCurrentMessage("and he's up!");
					view.printGame(game, 2000);
					game.getDefender().modifyKD1(game.getDefender().getKD2());
					view.messageLog.addToLog(game.getDefender().getName() + " feels a bit shaky...");
					view.printGame(game, 2000);
					// if not in Ring Center, move there and normalize CF and pinner/pinned
					if (!game.getRingPosition().equals("Ring Center")) {
						game.setRingPosition("Ring Center");
						game.getPinner().modifyCFB(-1);
						game.getPinner().modifyCFS(-2);
						game.setPinner(null);
						game.setPinned(null);
						view.messageLog.addToLog("The fighters move to the " + game.getRingPosition() + ".");
						view.printGame(game, 2000);
					}
					break;
				case "K1-10":
					card = game.isInOvertime() ? game.getInactiveDeck().drawAndReturn() : deck.drawCard();
					kor = card.getKOR();
					if (kor <= 10) {
						view.messageLog.addToLog("");
						for (int i = 1; i <= 9; i++) {
							view.messageLog.addToCurrentMessage(i + "... ");
							view.printGame(game, 1000);
						}
						view.messageLog.addToCurrentMessage("10! Knockout!");
						view.printGame(game, 2000);
						endGame(game, game.getAttacker(), "knockout");
						return;
					} else {
						card = game.isInOvertime() ? game.getInactiveDeck().drawAndReturn() : deck.drawCard();
						game.getDefender().addKnockdown();
						game.getDefender().addKDC(card.getKDC());
						view.messageLog.addToLog("");
						for (int i = 1; i <= card.getKDC(); i++) {
							view.messageLog.addToCurrentMessage(i + "... ");
							view.printGame(game, 1000);
						}
						view.messageLog.addToCurrentMessage("and he's up!");
						view.printGame(game, 2000);
						game.getDefender().modifyKD1(game.getDefender().getKD2());
						view.messageLog.addToLog(game.getDefender().getName() + " feels a bit shaky...");
						view.printGame(game, 2000);
						// if not in Ring Center, move there and normalize CF and pinner/pinned
						if (!game.getRingPosition().equals("Ring Center")) {
							game.setRingPosition("Ring Center");
							game.getPinner().modifyCFB(-1);
							game.getPinner().modifyCFS(-2);
							game.setPinner(null);
							game.setPinned(null);
							view.messageLog.addToLog("The fighters move to the " + game.getRingPosition() + ".");
							view.printGame(game, 2000);
						}
					}
					break;
				case "K":
					view.messageLog.addToLog("");
					for (int i = 1; i <= 9; i++) {
						view.messageLog.addToCurrentMessage(i + "... ");
						view.printGame(game, 1000);
					}
					view.messageLog.addToCurrentMessage("10! Knockout!");
					view.printGame(game, 2000);
					endGame(game, game.getAttacker(), "knockout");
					return;
				case "*":
					card = game.isInOvertime() ? game.getInactiveDeck().drawAndReturn() : deck.drawCard();
					kor = card.getKOR();
					if (kor == 1) {
						view.messageLog.addToLog("");
						for (int i = 1; i <= 9; i++) {
							view.messageLog.addToCurrentMessage(i + "... ");
							view.printGame(game, 1000);
						}
						view.messageLog.addToCurrentMessage("10! Knockout!");
						view.printGame(game, 2000);
						endGame(game, game.getAttacker(), "knockout");
						return;
					} else {
						card = game.isInOvertime() ? game.getInactiveDeck().drawAndReturn() : deck.drawCard();
						game.getDefender().addKnockdown();
						game.getDefender().addKDC(card.getKDC());
						view.messageLog.addToLog("");
						for (int i = 1; i <= card.getKDC(); i++) {
							view.messageLog.addToCurrentMessage(i + "... ");
							view.printGame(game, 1000);
						}
						view.messageLog.addToCurrentMessage("and he's up!");
						view.printGame(game, 2000);
						game.getDefender().modifyKD1(game.getDefender().getKD2());
						view.messageLog.addToLog(game.getDefender().getName() + " feels a bit shaky...");
						view.printGame(game, 2000);
						// if not in Ring Center, move there and normalize CF and pinner/pinned
						if (!game.getRingPosition().equals("Ring Center")) {
							game.setRingPosition("Ring Center");
							game.getPinner().modifyCFB(-1);
							game.getPinner().modifyCFS(-2);
							game.setPinner(null);
							game.setPinned(null);
							view.messageLog.addToLog("The fighters move to the " + game.getRingPosition() + ".");
							view.printGame(game, 2000);
						}
					}
					break;
			}
		}
	}
	
	public static void punchesMissed(Game game) {
		view.messageLog.addToLog(game.getAttacker().getName()+ " misses...");
		view.printGame(game, 2000);
	}
	
	public static void counterpunch(Game game) {
		view.messageLog.addToLog(game.getAttacker().getName() + " misses... and opens himself up for a counterattack!");
		view.printGame(game, 2000);
		Deck deck = game.isInKI() && !game.getKIStack().isEmpty() ? game.getKIStack() : game.getActiveDeck();
		if (!deck.isEmpty()) {
			int rn = deck.drawCard().getRN();
			
			// truncated action selection based on RN
			if (rn <= game.getDefender().getKP()) {
				
				// swap attacker and defender
				game.setAttacker(game.getDefender());
				game.setControlFound(true);
				knockdown(game, true);
			} else if (rn <= game.getDefender().getPL() + game.getDefender().getCP()) {
				
				// swap attacker and defender
				game.setAttacker(game.getDefender());
				punchesLanded(game);
			} else {
				// punch misses
				view.messageLog.addToLog(game.getDefender().getName() + " misses...");
				view.printGame(game, 2000);
			}
		}
	}
	
	public static void clinching(Game game) {
		view.messageLog.addToLog(game.getAttacker().getName() + " clinches.");
		view.printGame(game, 2000);
	}
	
	public static void ringMovement(Game game) {
		Deck deck = game.isInKI() && !game.getKIStack().isEmpty() ? game.getKIStack() : game.getActiveDeck();
		boolean pinning = false;
		if (!deck.isEmpty()) {
			
			// if nobody is pinning
			if (game.getPinner() == null) {
				game.setRingPosition(deck.drawCard().getRingPosition());
				
				// if the new ring position is not Ring Center, adjust CF and pinner/pinned
				if (!game.getRingPosition().equals("Ring Center")) {
					game.setPinner(game.getAttacker());
					game.setPinned(game.getDefender());
					game.getPinner().modifyCFB(1);
					game.getPinner().modifyCFS(2);
					pinning = true;
				} else {
					view.messageLog.addToLog("The fighters stay in the " + game.getRingPosition() + ".");
				}
			} else {
				
				// otherwise if the attacker is pinning
				if (game.getPinner().equals(game.getAttacker())) {
					
					// move to a new ring position
					game.setRingPosition(deck.drawCard().getRingPosition());
					pinning = true;
				} else {
					
					// otherwise move to Ring Center
					game.setRingPosition("Ring Center");
				}
				
				// if the new ring position is Ring Center, normalize CF and remove pinner/pinned
				if (game.getRingPosition().equals("Ring Center")) {
					pinning = false;
					game.getPinner().modifyCFB(-1);
					game.getPinner().modifyCFS(-2);
					view.messageLog.addToLog(game.getAttacker().getName() + " moves the fight to the " + game.getRingPosition() + ".");
					game.setPinner(null);
					game.setPinned(null);
				}
			}
			if (pinning) {
				view.messageLog.addToLog(game.getAttacker().getName() + 
						" pins " + game.getDefender().getName() + " to the " + game.getRingPosition() + "!");
			}
			view.printGame(game, 2000);
		}
	}
	
	public static void killerInstinct(Game game, boolean useMinimum) {
		game.setInKI(true);
		
		//  get the number of cards in the KI stack
		int cards = game.getAttacker().getKI();
		if (useMinimum && cards < 6) {
			cards = 6;
		}
		
		// create the KI stack from cards in the active deck
		game.setKIStack(new Deck(game.getActiveDeck().drawStack(cards)));
		view.messageLog.addToLog(game.getAttacker().getName() + " goes in for the kill!");
		view.printGame(game, 2000);
	}
	
	public static void tkoCheck(Game game) {
		int modifier = 0;
		if (game.getReferee().getStoppage().equals("Early")) {
			modifier = -3;
		} else if (game.getReferee().getStoppage().equals("Late")) {
			modifier = 3;
		} else if (game.getReferee().getStoppage().equals("Very Late")) {
			modifier = 5;
		}
		Fighter winner = null;
		for (Fighter fighter : game.getFighters()) {
			if (fighter.getPoints() >= 30 + modifier || fighter.getPointsTwoRounds() >= 50 + modifier || fighter.getPointsThreeRounds() >= 60 + modifier) {
				if (winner != null) {
					endGame(game, null, "draw");
					return;
				} else {
					winner = fighter;
				}
			}
		}
		if (winner != null) {
			view.messageLog.addToLog(game.getOpponentOf(winner).getName() + " is unable to continue!");
			view.printGame(game, 2000);
			endGame(game, winner, "technical knockout");
		}
	}
	
	public static void cut(Game game) {
		// multiples of the same cut are treated as separate cuts, may be incorrect behavior
		Deck deck = game.isInKI() && !game.getKIStack().isEmpty() ? game.getKIStack() : game.getActiveDeck();
		if (!deck.isEmpty()) {
			int rn = deck.drawCard().getRN();
			XSSFSheet cutsSwellingTable = game.getCutsSwellingTable();
			Iterator<Row> rowIterator = cutsSwellingTable.rowIterator();
			Row cutRow = null;
			
			// get the correct row based on RN
			while (rowIterator.hasNext()) {
				cutRow = rowIterator.next();
				if (rn <= (int) cutRow.getCell(0).getNumericCellValue()) {
					break;
				}
			}
			
			// store the row contents in variables
			String type = cutRow.getCell(1).getStringCellValue();
			int cf = (int) cutRow.getCell(2).getNumericCellValue();
			int end = (int) cutRow.getCell(3).getNumericCellValue();
			int cut = (int) cutRow.getCell(4).getNumericCellValue();
			int def = (int) cutRow.getCell(5).getNumericCellValue();
			int pl = (int) cutRow.getCell(6).getNumericCellValue();
			int dp = (int) cutRow.getCell(7).getNumericCellValue();
			// String description = cutRow.getCell(8).getStringCellValue();
			
			// modify the defender's stats based on the row contents if the cut type is new
			if (!game.getDefender().getCutTypes().contains(type)) {
				game.getDefender().addCutType(type);
				game.getDefender().modifyCFB(cf);
				game.getDefender().modifyCFS(cf);
				game.getDefender().modifyENDDrain(end);
				game.getDefender().modifyCUT(cut);
				game.getDefender().modifyDEF(def);
				game.getDefender().modifyPL(pl);
			}
			
			// damage defender
			if (!type.equals("No Cut or Swelling Occurs")) {
				game.getDefender().modifyDamage(dp);
				game.getDefender().addCut(new Cut(type, dp));
				if (!type.contains("Swelling") || (type.contains("Cut") && type.contains("Swelling"))) {
					view.messageLog.addToLog(game.getDefender().getName() + " suffers a " + type.toLowerCase() + "!");
				} else {
					view.messageLog.addToLog(game.getDefender().getName() + " suffers some " + type.toLowerCase() + "!");
				}
				view.printGame(game, 2000);
			}
			
			// check if defender has taken enough damage to trigger a doctor check or TKO
			deck = game.isInKI() && !game.getKIStack().isEmpty() ? game.getKIStack() : game.getActiveDeck();
			if (!deck.isEmpty()) {
				if (dp > 0) {
					if (game.getDefender().getDamage() >= 10) {
						if (!deck.isEmpty() ) {
							if (game.getDefender().getDamage() <= 15) {
								view.messageLog.addToLog("The referee considers calling the doctor over to take a look at " + game.getDefender().getName() + ".");
								view.printGame(game, 2000);
								cf = game.getInactiveDeck().drawAndReturn().getResult();
								int performDoctorCheck = 0;
								switch (game.getReferee().getFouls()) {
									case "Strict":
										performDoctorCheck = 14;
										break;
									case "Normal":
										performDoctorCheck = 10;
										break;
									case "Lenient":
										performDoctorCheck = 7;
										break;
									case "Very Lenient":
										performDoctorCheck = 5;
										break;
								}
								if (cf <= performDoctorCheck) {
									doctorCheck(game, game.getDefender());
								}
							} else if (game.getDefender().getDamage() >= 16 && game.getDefender().getDamage() <= 20) {
								doctorCheck(game, game.getDefender());
							} else {
								endGame(game, game.getAttacker(), "technical knockout");
							}
						} else {
							game.getDefender().setNextRoundCheck(true);
						}
					}
				}
			}
		}
	}
	
	public static void doctorCheck(Game game, Fighter fighter) {
		view.messageLog.addToLog("The referee calls the doctor over to take a look at " + fighter.getName() + ".");
		view.printGame(game, 2000);
		Deck deck = game.isInKI() && !game.getKIStack().isEmpty() ? game.getKIStack() : game.getActiveDeck();
		if (!deck.isEmpty()) {
			int cf = deck.drawCard().getCF();
			
			// set CF thresholds based on doctor's Doctor Rating
			int allowToContinue = 0, oneMoreRound = 0;
			switch (game.getDoctorRating()) {
				case "Cautious":
					allowToContinue = 8;
					oneMoreRound = 13;
					break;
				case "Balanced Judgement":
					allowToContinue = 11;
					oneMoreRound = 16;
					break;
				case "Permissive":
					allowToContinue = 15;
					oneMoreRound = 18;
					break;
			}
			
			// select doctor's action based on CF
			if (cf <= allowToContinue) {
				if (game.getFinalRoundNumber() == options.getMaxRounds()) {
					view.messageLog.addToLog("The doctor allows the bout to continue.");
				} else {
					view.messageLog.addToLog("The doctor will still end the fight early after Round " + game.getFinalRoundNumber() + ".");
				}
				view.printGame(game, 2000);
			} else if (cf <= oneMoreRound) {
				if (game.getFinalRoundNumber() == options.getMaxRounds()) {
					game.setFinalRoundNumber(game.getRoundNumber() + 1);
					view.messageLog.addToLog("The doctor gives " + fighter.getName() + " one more round.");
				} else {
					view.messageLog.addToLog("The doctor will still end the fight early after Round " + game.getFinalRoundNumber() + ".");
				}
				view.printGame(game, 2000);
			} else {
				view.messageLog.addToLog("The doctor stops the bout! " + fighter.getName() + " is too hurt to continue!");
				view.printGame(game, 2000);
				endGame(game, game.getOpponentOf(fighter), "technical knockout");
			}
		}
	}
	
	public static void foul(Game game) {
		Deck deck = game.isInKI() && !game.getKIStack().isEmpty() ? game.getKIStack() : game.getActiveDeck();
		if (!deck.isEmpty()) {
			view.messageLog.addToLog(game.getAttacker().getName() + " may have committed a foul...");
			view.printGame(game, 2000);
			
			// set RN thresholds based on referee's Foul Rating
			int modifier = 0;
			int warningLimit = 0;
			int pointLossNum = 0;
			switch (game.getReferee().getFouls()) {
				case "Very Lenient":
					modifier = -10;
					warningLimit = 5;
					pointLossNum = 75;
					break;
				case "Lenient":
					modifier = -5;
					warningLimit = 4;
					pointLossNum = 70;
					break;
				case "Normal":
					warningLimit = 3;
					pointLossNum = 65;
					break;
				case "Strict":
					modifier = -10;
					warningLimit = 2;
					pointLossNum = 60;
					break;
			}
			
			int rn = deck.drawCard().getRN();
			String foul = game.getFoul(rn + modifier, game.getAttacker().getFOUL());
			
			if (!foul.equals("tells both fighters to keep it clean")) {
				game.getAttacker().addWarning();
				view.messageLog.addToLog("The referee stops the action and issues a stern warning " + foul + "!");
				view.printGame(game, 2000);
				
				deck = game.isInKI() && !game.getKIStack().isEmpty() ? game.getKIStack() : game.getActiveDeck();
				
				Card card = deck.isEmpty() ? game.getInactiveDeck().drawAndReturn() : deck.drawCard();
				if (game.getAttacker().getWarnings() > warningLimit) {
					if (card.getRN() <= pointLossNum) {
						game.getAttacker().addPointLost();
						view.messageLog.addToLog("The referee takes a point away from " + game.getAttacker().getName() + "!");
						view.printGame(game, 2000);
					} else {
						view.messageLog.addToLog("The referee disqualifies " + game.getAttacker().getName() + "!");
						view.printGame(game, 2000);
						endGame(game, game.getDefender(), "disqualification");
						return;
					}
				}
			} else {
				view.messageLog.addToLog("The referee is gesturing. He " + foul + "!");
				view.printGame(game, 2000);
			}
		}
	}
	
	public static void injury(Game game) {
		Deck deck = game.isInKI() && !game.getKIStack().isEmpty() ? game.getKIStack() : game.getActiveDeck();
		if (!deck.isEmpty()) {
			view.messageLog.addToLog(game.getAttacker().getName() + " may have suffered a serious injury...");
			view.printGame(game, 2000);
			int injury = deck.drawCard().getInjury();
			deck = game.isInKI() && !game.getKIStack().isEmpty() ? game.getKIStack() : game.getActiveDeck();
			switch (injury) {
				// no injury
				case 0:
					view.messageLog.addToLog("Thankfully, no injury occurs.");
					view.printGame(game, 2000);
					break;
				
				// fighters clash heads, random fighter loses 1CF
				case 1:
					if (!deck.isEmpty()) {
						int rn = deck.drawCard().getRN();
						Fighter fighter;
						if (rn <= 40) {
							fighter = game.getAttacker();
						} else {
							fighter = game.getDefender();
						}
						fighter.modifyCFB(-1);
						fighter.modifyCFS(-1);
						fighter.addInjury(injury);
						view.messageLog.addToLog("The two fighters clash heads! " + game.getAttacker().getName() + " suffers a bad gash!");
						view.printGame(game, 2000);
					}
					break;
				
				// fighter takes a 3-point body shot that might have broken a rib, check each round to see if it ends the fight
				case 2:
					if (!game.getAttacker().getInjuries().contains(injury)) {
						game.getDefender().scorePoints(3);
						game.getAttacker().addInjury(injury);
						view.messageLog.addToLog(game.getAttacker().getName() + " takes a huge body shot! He might have broken a rib!");
						view.printGame(game, 2000);
						tkoCheck(game);
					} else {
						view.messageLog.addToLog("Thankfully, no further injury occurs.");
						view.printGame(game, 2000);
					}
					break;
				
				// fighter pulls a leg muscle, if fighter is not a slugger they become a slugger, gaining 2KP and lose 1CF
				case 3:
					if (!game.getAttacker().getInjuries().contains(injury)) {
						if (game.getAttacker().getStyle() != "S") {
							game.getAttacker().modifyCFB(-1);
							game.getAttacker().modifyCFS(-1);
							game.getAttacker().setStyle("S");
							if (!game.getAttacker().getCurrentStyle().equals("S")) {
								game.getAttacker().setCurrentStyle("S");
								game.getAttacker().modifyKP(2);
							}
						}
						game.getAttacker().addInjury(injury);
						view.messageLog.addToLog(game.getAttacker().getName() + " pulls a leg muscle! His movements are slowed!");
					} else {
						view.messageLog.addToLog("Thankfully, no further injury occurs.");
					}
					view.printGame(game, 2000);
					break;
				
				// fighter walks into a 2-point counter that breaks their nose, lowering END by 5 each round
				case 4:
					if (!game.getAttacker().getInjuries().contains(injury)) {
						game.getDefender().scorePoints(2);
						game.getAttacker().addInjury(injury);
						view.messageLog.addToLog(game.getAttacker().getName() + " walks into a hard counter! His nose is broken!");
						view.printGame(game, 2000);
						tkoCheck(game);
					} else {
						view.messageLog.addToLog("Thankfully, no further injury occurs.");
						view.printGame(game, 2000);
					}
					break;
				
				// fighter lands a 2-point jab but injures their hand, turning 2-point punches into 1-point punches
				case 5:
					if (!game.getAttacker().getInjuries().contains(injury)) {
						game.getAttacker().scorePoints(2);
						game.getAttacker().addInjury(injury);
						view.messageLog.addToLog(game.getAttacker().getName() + " lands a solid jab... but he injures his hand!");
						view.printGame(game, 2000);
						tkoCheck(game);
					} else {
						view.messageLog.addToLog("Thankfully, no further injury occurs.");
						view.printGame(game, 2000);
					}
					break;
				
				// fighter lands a 4-point wicked shot but breaks their hand, reducing all points scored by 1
				case 6:
					if (!game.getAttacker().getInjuries().contains(injury)) {
						game.getAttacker().scorePoints(4);
						game.getAttacker().addInjury(injury);
						view.messageLog.addToLog(game.getAttacker().getName() + " lands a wicked shot... but he breaks his hand!");
						view.printGame(game, 2000);
						tkoCheck(game);
					} else {
						view.messageLog.addToLog("Thankfully, no further injury occurs.");
						view.printGame(game, 2000);
					}
					break;
				
				// fighter lands a 3-point hard shot but injures their hand, turning 3-point punches into 2-point punches
				case 7:
					if (!game.getAttacker().getInjuries().contains(injury)) {
						game.getAttacker().scorePoints(3);
						game.getAttacker().addInjury(injury);
						view.messageLog.addToLog(game.getAttacker().getName() + " lands a hard shot... but he injures his hand!");
						view.printGame(game, 2000);
						tkoCheck(game);
					} else {
						view.messageLog.addToLog("Thankfully, no further injury occurs.");
						view.printGame(game, 2000);
					}
					break;
				
				// fighter's jaw is broken by a 3-point punch, randomly either getting TKOed at the end of that round or losing 2CF
				case 8:
					if (!game.getAttacker().getInjuries().contains(injury)) {
						game.getDefender().scorePoints(3);
						game.getAttacker().addInjury(injury);
						view.messageLog.addToLog(game.getAttacker().getName() + " eats a punch to the side of the jaw! His jaw is broken!");
						view.printGame(game, 2000);
						tkoCheck(game);
					} else {
						view.messageLog.addToLog("Thankfully, no further injury occurs.");
						view.printGame(game, 2000);
					}
					break;
				
				// fighter pulls a muscle in their shoulder, randomly either getting TKOed at the end of that round or losing 1CF
				case 9:
					if (!game.getAttacker().getInjuries().contains(injury)) {
						game.getAttacker().addInjury(injury);
						view.messageLog.addToLog(game.getAttacker().getName() + " pulls a muscle in his shoulder!");
					} else {
						view.messageLog.addToLog("Thankfully, no further injury occurs.");
					}
					view.printGame(game, 2000);
					break;
				
				// fighter twists ankle, immediately checking if they become a slugger if not a slugger, gaining 2KP and lose 1CF
				case 10:
					if (!game.getAttacker().getInjuries().contains(injury)) {
						if (!deck.isEmpty()) {
							int rn = deck.drawCard().getRN();
							if (rn <= 20 && game.getAttacker().getStyle() != "S") {
								game.getAttacker().modifyCFB(-1);
								game.getAttacker().modifyCFS(-1);
								if (!game.getAttacker().getCurrentStyle().equals("S")) {
									game.getAttacker().setCurrentStyle("S");
									game.getAttacker().modifyKP(2);
								}
								game.getAttacker().addInjury(injury);
								view.messageLog.addToLog(game.getAttacker().getName() + " slips on the wet canvas and twists his ankle!"
										+ " His movements are slowed!");
							} else {
								view.messageLog.addToLog(game.getAttacker().getName() + " slips on the wet canvas! Thankfully, no injury occurs.");
							}
						}
					} else {
						view.messageLog.addToLog("Thankfully, no further injury occurs.");
					}
					view.printGame(game, 2000);
					break;
			}
		}
	}
	
	public static void special(Game game) {
		Deck deck = game.isInKI() && !game.getKIStack().isEmpty() ? game.getKIStack() : game.getActiveDeck();
		if (!deck.isEmpty()) {
			int special = deck.drawCard().getSpecialAction();
			switch (special) {
				
				// referee sponges up water in a corner, each fighter gains 5 END
				case 1:
					for (Fighter fighter : game.getFighters()) {
						fighter.modifyEND(5);
					}
					view.messageLog.addToLog("The referee halts the fight to have water in a corner sponged up. The fighters take a moment to recover.");
					view.printGame(game, 2000);
					break;
				
				// if fighter has Anger Issues and a Foul Rating of D or E, they bite their opponent and the referee takes away a point
				case 2:
					if (game.getAttacker().getSpecial().contains("Anger Issues") && game.getAttacker().getFOUL().equals("D")
							&& game.getAttacker().getFOUL().equals("E")) {
						game.getAttacker().addPointLost();
						game.getAttacker().addWarning();
						view.messageLog.addToLog(game.getAttacker().getName() + " gets mad... and he bites his opponent! The referee takes a point away!");
					} else {
						view.messageLog.addToLog(game.getAttacker().getName() + " gets mad... but he's able to keep it down.");
					}
					view.printGame(game, 2000);
					break;
				
				// fighter loses mouthpiece, each fighter gains 5 END
				case 3:
					for (Fighter fighter : game.getFighters()) {
						fighter.modifyEND(5);
					}
					view.messageLog.addToLog(game.getAttacker().getName() + " loses his mouthpiece. The referee halts the fight to clean and reinsert it.");
					view.messageLog.addToLog("The fighters take a moment to recover.");
					view.printGame(game, 2000);
					break;
				
				// fighter complains and gets hit with a 3-point hook
				case 4:
					game.getDefender().scorePoints(3);
					view.messageLog.addToLog(game.getAttacker().getName() + " turns to complain to the referee... and gets hit with a left hook!");
					view.printGame(game, 2000);
					tkoCheck(game);
					break;
				
				// fighters exchange punches, fighter with higher KP scores 3 points and their opponent scores 2 points, each fighter scores 2 points if tied
				case 5:
					if (game.getFighter1().getKP() == game.getFighter2().getKP()) {
						for (Fighter fighter : game.getFighters()) {
							fighter.scorePoints(2);
							view.messageLog.addToLog("The fighters exchange punches... and come out even!");
						}
					} else if (game.getFighter1().getKP() > game.getFighter2().getKP()) {
						game.getFighter1().scorePoints(3);
						game.getFighter2().scorePoints(2);
						view.messageLog.addToLog("The fighters exchange punches... and " + game.getFighter1().getName() + " comes out on top!");
					} else {
						game.getFighter2().scorePoints(3);
						game.getFighter1().scorePoints(2);
						view.messageLog.addToLog("The fighters exchange punches... and " + game.getFighter2().getName() + " comes out on top!");
					}
					view.printGame(game, 2000);
					tkoCheck(game);
					break;
				
				// fighter hits on break, his opponent loses 4 END, second time on a fighter gets a warning, third time gets a point loss
				case 6:
					game.getDefender().modifyEND(-4);
					view.messageLog.addToLog(game.getFighter1().getName() + " hits his opponent on the break!");
					view.printGame(game, 2000);
					game.getAttacker().addSpecial6();
					if (game.getAttacker().getSpecial6() == 2) {
						game.getAttacker().addWarning();
						view.messageLog.addToLog("The referee issues a stern warning!");
						view.printGame(game, 2000);
					} else if(game.getAttacker().getSpecial6() == 3) {
						game.getAttacker().addWarning();
						game.getAttacker().addPointLost();
						view.messageLog.addToLog("The referee takes a point away from " + game.getAttacker().getName() + "!");
						view.printGame(game, 2000);
					}
					break;
				
				// if not in KI, fighter's glove tape is repaired, each fighter gains 5 END
				case 7:
					if (!game.isInKI()) {
						for (Fighter fighter : game.getFighters()) {
							fighter.modifyEND(5);
						}
						view.messageLog.addToLog("The referee halts the fight to have the tape on " + game.getAttacker().getName() + "'s glove repaired.");
						view.messageLog.addToLog("The fighters take a moment to recover.");
					} else {
						view.messageLog.addToLog("The tape on " + game.getAttacker().getName()
								+ "'s glove needs to be repaired... but the fight is too intense to stop!");
					}
					view.printGame(game, 2000);
					break;
				
				// fighter's glove splits, each fighter gains 10 END, KI ends if in progress
				case 8:
					for (Fighter fighter : game.getFighters()) {
						fighter.modifyEND(10);
					}
					view.messageLog.addToLog(game.getAttacker().getName() + "'s glove splits. The referee halts the fight to have it replaced.");
					view.messageLog.addToLog("The fighters take a moment to recover.");
					if (game.isInKI()) {
						game.getActiveDeck().returnStack(game.getKIStack());
						view.messageLog.addToLog(game.getAttacker().getName() + " calms down a bit.");
					}
					view.printGame(game, 2000);
					break;
				
				// round ends early, only happens if 5 or fewer cards remain
				case 9:
					int remainingCards = game.getActiveDeck().getCardsLeft();
					if (game.isInKI()) {
						remainingCards += game.getKIStack().getCardsLeft();
					}
					if (remainingCards <= 5) {
						while (!game.getActiveDeck().isEmpty()) {
							game.getActiveDeck().drawCard();
						}
						while (!game.getKIStack().isEmpty()) {
							game.getKIStack().drawCard();
						}
						view.messageLog.addToLog("The bell is rung early! The round ends prematurely!");
					} else {
						view.messageLog.addToLog("Nothing seems to happen...");
					}
					view.printGame(game, 2000);
					break;
				
				// fighter breaks ropes, each fighter gains 15 END
				case 10:
					for (Fighter fighter : game.getFighters()) {
						fighter.modifyEND(15);
					}
					view.messageLog.addToLog(game.getAttacker().getName() + " leans into the ropes, and they give way!"
							+ " The fight is halted to tighten them.");
					view.messageLog.addToLog("The fighters take a moment to recover.");
					view.printGame(game, 2000);
					break;
				
				// fighter hits referee with a punch out of a clinch, each fighter gains 10 END
				case 11:
					for (Fighter fighter : game.getFighters()) {
						fighter.modifyEND(10);
					}
					view.messageLog.addToLog(game.getAttacker().getName() + " knocks down the referee with a punch out of a clinch!"
							+ " The fight is temporarily halted.");
					view.messageLog.addToLog("The fighters take a moment to recover.");
					view.printGame(game, 2000);
					break;
				
				// fans toss debris into ring, each fighter gains 5 END
				case 12:
					for (Fighter fighter : game.getFighters()) {
						fighter.modifyEND(5);
					}
					view.messageLog.addToLog("Unruly fans throw debris into the ring! The fight is temporarily halted.");
					view.messageLog.addToLog("The fighters take a moment to recover.");
					view.printGame(game, 2000);
					break;
			}
		}
	}
	
	public static void conditionCheck(Game game, Fighter fighter) {
		int oldCFB = fighter.getCFB();
		int oldDEF = fighter.getDEF();
		switch (fighter.getCondition()) {
			case 1:
				fighter.modifyCFB(1);
				fighter.modifyCFS(1);
				break;
			case 2:
				fighter.modifyCFB(1);
				fighter.modifyCFS(1);
				if (!fighter.isCondition3()) {
					fighter.modifyDEF(-2);
				}
				break;
			case 3:
				fighter.modifyCFB(1);
				fighter.modifyCFS(1);
				break;
		}
		if (game.getOpponentOf(fighter).getPoints() >= 25) {
			boolean oldCondition3 = fighter.isCondition3();
			game.getActiveDeck().shuffleDeck();
			int condition = game.getCondition(game.getActiveDeck().drawCard().getRN(), fighter.getKO());
			switch (condition) {
				case 1:
					fighter.modifyCFB(-1);
					fighter.modifyCFS(-1);
					break;
				case 2:
					fighter.modifyCFB(-1);
					fighter.modifyCFS(-1);
					if (!fighter.isCondition3()) {
						fighter.modifyDEF(2);
					}
					break;
				case 3:
					fighter.modifyCFB(-1);
					fighter.modifyCFS(-1);
					if (!fighter.isCondition3()) {
						fighter.modifyDEF(2);
					}
					fighter.setCondition3(true);
					break;
			}
			fighter.setCondition(condition);
			if (fighter.getCFB() > oldCFB) {
				view.messageLog.addToLog(fighter.getName() + "'s focus returns!");
				view.printGame(game, 2000);
			}
			if (fighter.getCFB() < oldCFB) {
				view.messageLog.addToLog(fighter.getName() + " loses focus from the intense punishment this round!");
				view.printGame(game, 2000);
			}
			if (fighter.getDEF() < oldDEF) {
				view.messageLog.addToLog(fighter.getName() + "'s defenses are back up!");
				view.printGame(game, 2000);
			}
			if (fighter.getDEF() > oldDEF) {
				if (!fighter.isCondition3()) {
					view.messageLog.addToLog(fighter.getName() + "'s defenses are down from the intense punishment this round!");
					view.printGame(game, 2000);
				} else {
					view.messageLog.addToLog(fighter.getName() + "'s defenses are shattered from the intense punishment this round!");
					view.printGame(game, 2000);
				}
			}
			if (fighter.getDEF() == oldDEF && !oldCondition3  && fighter.isCondition3()) {
				view.messageLog.addToLog(fighter.getName() + "'s already-lowered defenses are shattered from the intense punishment this round!");
				view.printGame(game, 2000);
			}
		}
	}
	
	public static void roundFinish(Game game) {
		if (game.isGameOver()) {
			return;
		}
		
		view.messageLog.addToLog("DING DING DING! Round " + game.getRoundNumber() + " is over!");
		view.printGame(game, 2000);
		
		// reshuffle active deck
		game.getActiveDeck().shuffleDeck();
		
		// move to ring center
		if (game.getPinner() != null) {
			game.setRingPosition("Ring Center");
			game.getPinner().modifyCFB(-1);
			game.getPinner().modifyCFS(-2);
			game.setPinner(null);
			game.setPinned(null);
		}
		
		// resolve certain injuries
		Fighter winner = null;
		
		for (Fighter fighter : game.getFighters()) {
			if (fighter.getInjuries().contains(8) && !fighter.isResolvedInjury8()) {
				if (game.getInactiveDeck().drawAndReturn().getRN() <= 40) {
					view.messageLog.addToLog(fighter.getName() + " is unable to continue due to his broken jaw!");
					view.printGame(game, 2000);
					if (winner != null) {
						endGame(game, null, "draw");
						return;
					} else {
						winner = game.getOpponentOf(fighter);
					}
				} else {
					fighter.modifyCFB(-2);
					fighter.modifyCFS(-2);
					view.messageLog.addToLog(fighter.getName() + " is feeling the hurt from his broken jaw... but he can keep going!");
					view.printGame(game, 2000);
				}
				fighter.setResolvedInjury8(true);
			}
		}
		if (winner != null) {
			endGame(game, winner, "technical knockout");
		}

		for (Fighter fighter : game.getFighters()) {
			if (fighter.getInjuries().contains(9) && !fighter.isResolvedInjury9()) {
				if (game.getInactiveDeck().drawAndReturn().getRN() <= 16) {
					view.messageLog.addToLog(fighter.getName() + " is unable to continue due to the pulled muscle in his shoulder!");
					view.printGame(game, 2000);
					if (winner != null) {
						endGame(game, null, "draw");
						return;
					} else {
						winner = game.getOpponentOf(fighter);
					}
				} else {
					fighter.modifyCFB(-1);
					fighter.modifyCFS(-1);
					view.messageLog.addToLog(fighter.getName() + " is feeling the hurt from the pulled muscle in his shoulder... but he can keep going!");
					view.printGame(game, 2000);
				}
				fighter.setResolvedInjury9(true);
			}
		}
		if (winner != null) {
			endGame(game, winner, "technical knockout");
		}
		
		// endurance and condition checks
		for (Fighter fighter : game.getFighters()) {
			fighter.modifyEND(fighter.getENDDrain());
			fighter.modifyEND(-game.getOpponentOf(fighter).getPoints());
			if (fighter.getEND() <= 0) {
				view.messageLog.addToLog(fighter.getName() + " is getting tired!");
				fighter.setFresh(false);
				fighter.zeroEND();
				view.printGame(game, 2000);
			}
			conditionCheck(game, fighter);
		}
		
		// corner man checks
		for (Fighter fighter : game.getFighters()) {
			List<Cut> targetCuts = new ArrayList<>();
			if (fighter.getCutList().size() == 1) {
				targetCuts.add(fighter.getCutList().get(0));
			} else if (fighter.getCutList().size() >= 2) {
				int maxDP = 0;
				Iterator<Cut> iterator = fighter.getCutList().listIterator();
				while (iterator.hasNext()) {
					Cut cut = iterator.next();
					maxDP = maxDP < cut.getDP() ? cut.getDP() : maxDP;
				}
				while (targetCuts.size() < 2) {
					for (int i = 0; i < fighter.getCutList().size(); i++) {
						Cut cut = fighter.getCutList().get(i);
						if (cut.getDP() == maxDP) {
							targetCuts.add(cut);
							if (targetCuts.size() == 2) {
								break;
							}
						}
					}
					maxDP--;
				}
			}
			for (int i = 0; i < targetCuts.size(); i++) {
				CornerMan cornerMan = i == 0 ? fighter.getCutMan() : fighter.getTrainer();
				int rn = game.getInactiveDeck().drawAndReturn().getRN();
				int majorReduction = 0, goodReduction = 0, averageReduction = 0, noReduction = 0;
				switch (cornerMan.getCutsSwell()) {
					case "Excellent":
						majorReduction = 6;
						goodReduction = 26;
						averageReduction = 69;
						noReduction = 79;
						break;
					case "Very Good":
						majorReduction = 5;
						goodReduction = 23;
						averageReduction = 64;
						noReduction = 78;
						break;
					case "Good":
						majorReduction = 4;
						goodReduction = 20;
						averageReduction = 61;
						noReduction = 77;
						break;
					case "Average":
						majorReduction = 3;
						goodReduction = 17;
						averageReduction = 58;
						noReduction = 76;
						break;
					case "Poor":
						majorReduction = 2;
						goodReduction = 14;
						averageReduction = 55;
						noReduction = 75;
						break;
				}
				int reductionAmount = 0;
				Cut cut = targetCuts.get(i);
				if (rn <= majorReduction) {
					reductionAmount = cut.getDP() < 3 ? cut.getDP() : 3;
					fighter.removeCut(cut);
					view.messageLog.addToLog("The corner men have done a great job of healing " + fighter.getName() + 
							"'s " + cut.getCutType().toLowerCase() + ".");
				} else if (rn <= goodReduction) {
					reductionAmount = cut.getDP() < 2 ? cut.getDP() : 2;
					fighter.removeCut(cut);
					view.messageLog.addToLog("The corner men have done a good job of healing " + fighter.getName() + 
							"'s " + cut.getCutType().toLowerCase() + ".");
				} else if (rn <= averageReduction) {
					reductionAmount = cut.getDP() < 1 ? cut.getDP() : 1;
					fighter.removeCut(cut);
					view.messageLog.addToLog("The corner men have done an okay job of healing " + fighter.getName() + 
							"'s " + cut.getCutType().toLowerCase() + ".");
				} else if (rn <= noReduction) {
					view.messageLog.addToLog("The corner men have failed to heal " + fighter.getName() + 
							"'s " + cut.getCutType().toLowerCase() + ".");
				} else {
					reductionAmount = -1;
					fighter.worsenCut(cut);
					view.messageLog.addToLog("The corner men have worsened " + fighter.getName() + 
							"'s " + cut.getCutType().toLowerCase() + ".");
				}
				fighter.modifyDamage(-reductionAmount);
				view.printGame(game, 2000);
			}
		}
		
		// resolve carryover effects and change KD1 accordingly
		for (Fighter fighter : game.getFighters()) {
			boolean oldCarryover = fighter.isCarryover();
			if (fighter.isCarryover()) {
				fighter.modifyKD1(-1);
				fighter.setCarryover(false);
			}
			int rn = game.getInactiveDeck().drawAndReturn().getRN();
			if (fighter.getKnockdowns() > 2 || fighter.getKDC() > 9 || rn <= fighter.getKDC() * 4) {
				fighter.modifyKD1(fighter.getKD2() + 1);
				fighter.setCarryover(true);
			}
			fighter.modifyKD1(-fighter.getKD2() * fighter.getKnockdowns());
			if (!oldCarryover && fighter.isCarryover()) {
				view.messageLog.addToLog(fighter.getName() + " is suffering from the aftereffects of his knockdown(s)!");
				view.printGame(game, 2000);
			} else if (oldCarryover && !fighter.isCarryover()) {
				view.messageLog.addToLog(fighter.getName() + " has recovered from the aftereffects of his earlier knockdown(s).");
				view.printGame(game, 2000);
			}
		}
		
		// end-of-round scoring
		if (game.getFighter1().getPoints() == game.getFighter2().getPoints()) {
			for (int judge = 1; judge <= 3; judge++) {
				game.getFighter1().setScore(judge, game.getRoundNumber(), 10 - game.getFighter1().getKnockdowns());
				game.getFighter2().setScore(judge, game.getRoundNumber(), 10 - game.getFighter2().getKnockdowns());
			}
		} else {
			Fighter hs, ls;
			if (game.getFighter1().getPoints() > game.getFighter2().getPoints()) {
				hs = game.getFighter1();
				ls = game.getFighter2();
			} else {
				hs = game.getFighter2();
				ls = game.getFighter1();
			}
			if (hs.getKnockdowns() == 0 && ls.getKnockdowns() == 1) {
				for (int judge = 1; judge <= 3; judge++) {
					hs.setScore(judge, game.getRoundNumber(), 10);
					ls.setScore(judge, game.getRoundNumber(), 8);
				}
			} else if (hs.getKnockdowns() == 1 && ls.getKnockdowns() == 0) {
				if (hs.getPoints() - ls.getPoints() >= 6 && hs.getPoints() - ls.getPoints() <= 12) {
					for (int judge = 1; judge <= 3; judge++) {
						hs.setScore(judge, game.getRoundNumber(), 9);
						ls.setScore(judge, game.getRoundNumber(), 10);
					}
				} else if (hs.getPoints() - ls.getPoints() >= 13) {
					for (int judge = 1; judge <= 3; judge++) {
						hs.setScore(judge, game.getRoundNumber(), 10);
						ls.setScore(judge, game.getRoundNumber(), 9);
					}
				} else {
					if (hs.getPoints() - ls.getPoints() >= 1 && hs.getPoints() - ls.getPoints() <= 6) {
						Card card = game.getActiveDeck().drawAndReturn();
						String[] judgments = {card.getJ1(), card.getJ2(), card.getJ3()};
						for (int judge = 1; judge <= 3; judge++) {
							switch (judgments[judge - 1]) {
								case "HS":
									hs.setScore(judge, game.getRoundNumber(), 10);
									ls.setScore(judge, game.getRoundNumber(), 9);
									break;
								case "LS":
									hs.setScore(judge, game.getRoundNumber(), 9);
									ls.setScore(judge, game.getRoundNumber(), 10);
									break;
								case "E":
									hs.setScore(judge, game.getRoundNumber(), 10);
									ls.setScore(judge, game.getRoundNumber(), 10);
									break;
							}
						}
					} else if (hs.getPoints() - ls.getPoints() >= 7 && hs.getPoints() - ls.getPoints() <= 15) {
						for (int judge = 1; judge <= 3; judge++) {
							hs.setScore(judge, game.getRoundNumber(), 10);
							ls.setScore(judge, game.getRoundNumber(), 9);
						}
					} else if (hs.getPoints() - ls.getPoints() >= 16 && hs.getPoints() - ls.getPoints() <= 18) {
						Card card = game.getActiveDeck().drawAndReturn();
						String[] judgments = {card.getJ1(), card.getJ2(), card.getJ3()};
						for (int judge = 1; judge <= 3; judge++) {
							switch (judgments[judge - 1]) {
								case "HS":
									hs.setScore(judge, game.getRoundNumber(), 10);
									ls.setScore(judge, game.getRoundNumber(), 8);
									break;
								case "LS":
								case "E":
									hs.setScore(judge, game.getRoundNumber(), 10);
									ls.setScore(judge, game.getRoundNumber(), 9);
									break;
							}
						}
					} else if (hs.getPoints() - ls.getPoints() >= 19 && hs.getPoints() - ls.getPoints() <= 27) {
						for (int judge = 1; judge <= 3; judge++) {
							hs.setScore(judge, game.getRoundNumber(), 10);
							ls.setScore(judge, game.getRoundNumber(), 8);
						}
					} else {
						for (int judge = 1; judge <= 3; judge++) {
							hs.setScore(judge, game.getRoundNumber(), 10);
							ls.setScore(judge, game.getRoundNumber(), 7);
						}
					}
				}
			} else if (ls.getKnockdowns() == 2) {
				if (hs.getKnockdowns() < 2) {
					for (int judge = 1; judge <= 3; judge++) {
						hs.setScore(judge, game.getRoundNumber(), 10);
						ls.setScore(judge, game.getRoundNumber(), 7);
					}
				} else {
					if (hs.getPoints() - ls.getPoints() >= 1 && hs.getPoints() - ls.getPoints() <= 6) {
						Card card = game.getActiveDeck().drawAndReturn();
						String[] judgments = {card.getJ1(), card.getJ2(), card.getJ3()};
						for (int judge = 1; judge <= 3; judge++) {
							switch (judgments[judge - 1]) {
								case "HS":
									hs.setScore(judge, game.getRoundNumber(), 8);
									ls.setScore(judge, game.getRoundNumber(), 7);
									break;
								case "LS":
									hs.setScore(judge, game.getRoundNumber(), 7);
									ls.setScore(judge, game.getRoundNumber(), 8);
									break;
								case "E":
									hs.setScore(judge, game.getRoundNumber(), 8);
									ls.setScore(judge, game.getRoundNumber(), 8);
									break;
							}
						}
					} else if (hs.getPoints() - ls.getPoints() >= 7 && hs.getPoints() - ls.getPoints() <= 15) {
						for (int judge = 1; judge <= 3; judge++) {
							hs.setScore(judge, game.getRoundNumber(), 8);
							ls.setScore(judge, game.getRoundNumber(), 7);
						}
					} else if (hs.getPoints() - ls.getPoints() >= 16 && hs.getPoints() - ls.getPoints() <= 18) {
						Card card = game.getActiveDeck().drawAndReturn();
						String[] judgments = {card.getJ1(), card.getJ2(), card.getJ3()};
						for (int judge = 1; judge <= 3; judge++) {
							switch (judgments[judge - 1]) {
								case "HS":
									hs.setScore(judge, game.getRoundNumber(), 8);
									ls.setScore(judge, game.getRoundNumber(), 6);
									break;
								case "LS":
								case "E":
									hs.setScore(judge, game.getRoundNumber(), 8);
									ls.setScore(judge, game.getRoundNumber(), 7);
									break;
							}
						}
					} else if (hs.getPoints() - ls.getPoints() >= 19 && hs.getPoints() - ls.getPoints() <= 27) {
						for (int judge = 1; judge <= 3; judge++) {
							hs.setScore(judge, game.getRoundNumber(), 8);
							ls.setScore(judge, game.getRoundNumber(), 6);
						}
					} else {
						for (int judge = 1; judge <= 3; judge++) {
							hs.setScore(judge, game.getRoundNumber(), 8);
							ls.setScore(judge, game.getRoundNumber(), 5);
						}
					}
				}
			} else if (hs.getKnockdowns() == 2) {
				for (int judge = 1; judge <= 3; judge++) {
					hs.setScore(judge, game.getRoundNumber(), 8);
					ls.setScore(judge, game.getRoundNumber(), 10);
				}
			} else {
				if (hs.getPoints() - ls.getPoints() >= 1 && hs.getPoints() - ls.getPoints() <= 6) {
					Card card = game.getActiveDeck().drawAndReturn();
					String[] judgments = {card.getJ1(), card.getJ2(), card.getJ3()};
					for (int judge = 1; judge <= 3; judge++) {
						switch (judgments[judge - 1]) {
							case "HS":
								hs.setScore(judge, game.getRoundNumber(), 10);
								ls.setScore(judge, game.getRoundNumber(), 9);
								break;
							case "LS":
								hs.setScore(judge, game.getRoundNumber(), 9);
								ls.setScore(judge, game.getRoundNumber(), 10);
								break;
							case "E":
								hs.setScore(judge, game.getRoundNumber(), 10);
								ls.setScore(judge, game.getRoundNumber(), 10);
								break;
						}
					}
				} else if (hs.getPoints() - ls.getPoints() >= 7 && hs.getPoints() - ls.getPoints() <= 15) {
					for (int judge = 1; judge <= 3; judge++) {
						hs.setScore(judge, game.getRoundNumber(), 10);
						ls.setScore(judge, game.getRoundNumber(), 9);
					}
				} else if (hs.getPoints() - ls.getPoints() >= 16 && hs.getPoints() - ls.getPoints() <= 18) {
					Card card = game.getActiveDeck().drawAndReturn();
					String[] judgments = {card.getJ1(), card.getJ2(), card.getJ3()};
					for (int judge = 1; judge <= 3; judge++) {
						switch (judgments[judge - 1]) {
							case "HS":
								hs.setScore(judge, game.getRoundNumber(), 10);
								ls.setScore(judge, game.getRoundNumber(), 8);
								break;
							case "LS":
							case "E":
								hs.setScore(judge, game.getRoundNumber(), 10);
								ls.setScore(judge, game.getRoundNumber(), 9);
								break;
						}
					}
				} else if (hs.getPoints() - ls.getPoints() >= 19 && hs.getPoints() - ls.getPoints() <= 27) {
					for (int judge = 1; judge <= 3; judge++) {
						hs.setScore(judge, game.getRoundNumber(), 10);
						ls.setScore(judge, game.getRoundNumber(), 8);
					}
				} else {
					for (int judge = 1; judge <= 3; judge++) {
						hs.setScore(judge, game.getRoundNumber(), 10);
						ls.setScore(judge, game.getRoundNumber(), 7);
					}
				}
				game.setPreviousWinner(hs);
			}
		}
		for (int judge = 1; judge <= 3; judge++) {
			int fighter1Score = game.getFighter1().getScore()[judge - 1][game.getRoundNumber() - 1];
			int fighter2Score = game.getFighter2().getScore()[judge - 1][game.getRoundNumber() - 1];
			if (fighter1Score > fighter2Score) {
				view.messageLog.addToLog("Judge " + judge + " scores Round " + game.getRoundNumber() + " " + fighter1Score + "-" + fighter2Score +
						" in favor of " + game.getFighter1().getName() + ".");
			} else if (fighter1Score < fighter2Score) {
				view.messageLog.addToLog("Judge " + judge + " scores Round " + game.getRoundNumber() + " " + fighter2Score + "-" + fighter1Score +
						" in favor of " + game.getFighter2().getName() + ".");
			} else {
				view.messageLog.addToLog("Judge " + judge + " scores Round " + game.getRoundNumber() + " " + fighter1Score + "-" + fighter2Score + ".");
			}
		}
		view.printGame(game, 2000);
		
		// reset stats
		for (Fighter fighter : game.getFighters()) {
			fighter.clearKDC();
			fighter.clearKnockdowns();
			fighter.clearPoints();
			fighter.clearPointsLost();
		}
		
		// if fight went the distance, determine winner
		if (game.getRoundNumber() == game.getFinalRoundNumber()) {
			if (game.getFinalRoundNumber() == options.getMaxRounds()) {
				view.messageLog.addToLog("The fight has gone the distance! The fight is over!");
			} else {
				view.messageLog.addToLog("The doctor ends the fight early. The fight is over!");
			}
			view.printGame(game, 2000);
			int fighter1Judges = 0, fighter2Judges = 0, drawJudges = 0;
			for (int judge = 1; judge <= 3; judge++) {
				int score = 0;
				for (int round = 1; round <= game.getRoundNumber(); round++) {
					score += game.getFighter1().getScore()[judge - 1][round - 1];
					score -= game.getFighter2().getScore()[judge - 1][round - 1];
				}
				if (score > 0) {
					fighter1Judges++;
				} else if (score < 0) {
					fighter2Judges++;
				} else {
					drawJudges++;
				}
			}
			if (fighter1Judges == 3) {
				endGame(game, game.getFighter1(), "unanimous decision");
			} else if (fighter2Judges == 3) {
				endGame(game, game.getFighter2(), "unanimous decision");
			} else if (fighter1Judges == 2 && fighter2Judges == 1) {
				endGame(game, game.getFighter1(), "split decision");
			} else if (fighter2Judges == 2 && fighter1Judges == 1) {
				endGame(game, game.getFighter2(), "split decision");
			} else if (fighter1Judges == 2 && drawJudges == 1) {
				endGame(game, game.getFighter1(), "majority decision");
			} else if (fighter2Judges == 2 && drawJudges == 1) {
				endGame(game, game.getFighter2(), "majority decision");
			} else if (drawJudges >= 2) {
				endGame(game, null, "draw");
			} else if (fighter1Judges == 1 && fighter2Judges == 1 && drawJudges == 1) {
				endGame(game, null, "draw");
			}
		} else {
			game.incrementRoundNumber();
		}
	}
	
	public static void endGame(Game game, Fighter winner, String outcome) {
		game.setGameOver(true);
		if (winner == null) {
			view.messageLog.addToLog("The fight has ended in a draw in round " + game.getRoundNumber() + "!");
		} else {
			view.messageLog.addToLog(winner.getName() + " has won in round " + game.getRoundNumber() + " by " + outcome + "!");
		}
		view.printGame(game, 2000);
		view.messageLog.addToLog("");
		view.messageLog.addToLog("Press Enter to return to the main menu.");
		view.printGame(game, 0);
		in.next();
		view.messageLog.clearLog();
	}
	
	public static void divisionsMenu() {
		// list of divisions
		boolean done = false;
		ArrayList<String> divisions = new ArrayList<>();
		Iterator<Sheet> sheetIterator = workbook.sheetIterator();
		while (sheetIterator.hasNext()) {
			String sheetName = sheetIterator.next().getSheetName();
			if (sheetName.endsWith("weights")) {
				divisions.add(sheetName);
			}
		}
		while (!done) {
			view.printDivisions(divisions);
			int i = in.nextInt() - 1;
			if (i > -1 && i <= divisions.size()) {
				done = i < divisions.size() ? firstFighterSelectionMenu(divisions.get(i)) : true;
			}
		}
	}
	
	public static boolean firstFighterSelectionMenu(String division) {
		// list of fighters
		boolean done = false;
		int pageNumber = 1;
		boolean hasPreviousPage = false;
		boolean hasNextPage = true;
		XSSFSheet fighters = workbook.getSheet(division);
		while (!done) {
			ArrayList<Row> fighterGroup = new ArrayList<>();
			int offset = (pageNumber - 1) * 25;
			for (int i = 1 + offset; i <= 25 + offset && i <= fighters.getLastRowNum(); i++) {
				fighterGroup.add(fighters.getRow(i));
			}
			hasNextPage = fighterGroup.size() == 25 && offset + fighterGroup.size() < fighters.getLastRowNum();
			hasPreviousPage = pageNumber > 1;
			view.printFighterSelection(1, fighterGroup, hasNextPage, hasPreviousPage);
			int i = in.nextInt() - 1;
			if (i > -1 && i < fighterGroup.size()) {
				boolean done2 = false;
				while (!done2) {
					Row fighter1 = fighterGroup.get(i);
					view.printFighterConfirmation(fighter1);
					switch (in.nextInt()) {
						case 1:
							done2 = true;
							done = secondFighterSelectionMenu(division, fighter1);
							break;
						case 2:
							done2 = true;
							break;
					}
				}
			} else if (i == fighterGroup.size()) {
				if (hasNextPage) {
					pageNumber++;
				} else if (hasPreviousPage) {
					pageNumber--;
				} else {
					return false;
				}
			} else if (i == fighterGroup.size() + 1) {
				if (hasNextPage) {
					if (hasPreviousPage) {
						pageNumber--;
					} else {
						return false;
					}
				} else if (hasPreviousPage) {
					return false;
				}
			} else if (i == fighterGroup.size() + 2) {
				if (hasNextPage && hasPreviousPage) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean secondFighterSelectionMenu(String division, Row fighter1) {
		// list of fighters
		boolean done = false;
		int pageNumber = 1;
		boolean hasPreviousPage = false;
		boolean hasNextPage = true;
		XSSFSheet fighters = workbook.getSheet(division);
		while (!done) {
			ArrayList<Row> fighterGroup = new ArrayList<>();
			int offset = (pageNumber - 1) * 25;
			for (int i = 1 + offset; i <= 25 + offset && i <= fighters.getLastRowNum(); i++) {
				fighterGroup.add(fighters.getRow(i));
			}
			hasNextPage = fighterGroup.size() == 25 && offset + fighterGroup.size() < fighters.getLastRowNum();
			hasPreviousPage = pageNumber > 1;
			view.printFighterSelection(2, fighterGroup, hasNextPage, hasPreviousPage);
			int i = in.nextInt() - 1;
			if (i > -1 && i < fighterGroup.size()) {
				boolean done2 = false;
				while (!done2) {
					Row fighter2 = fighterGroup.get(i);
					view.printFighterConfirmation(fighter2);
					switch (in.nextInt()) {
						case 1:
							done2 = true;
							if (options.isRatedRefereeEnabled()) {
								done = ratedRefereeSelectionMenu(fighter1, fighter2);
							} else {
								done = standardRefereeSelectionMenu(fighter1, fighter2);
							}
							break;
						case 2:
							done2 = true;
							break;
					}
				}
			} else if (i == fighterGroup.size()) {
				if (hasNextPage) {
					pageNumber++;
				} else if (hasPreviousPage) {
					pageNumber--;
				} else {
					return false;
				}
			} else if (i == fighterGroup.size() + 1) {
				if (hasNextPage) {
					if (hasPreviousPage) {
						pageNumber--;
					} else {
						return false;
					}
				} else if (hasPreviousPage) {
					return false;
				}
			} else if (i == fighterGroup.size() + 2) {
				if (hasNextPage && hasPreviousPage) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean standardRefereeSelectionMenu(Row fighter1, Row fighter2) {
		// list of referee types
		boolean done = false;
		XSSFSheet referees = workbook.getSheet("Standard Referees");
		while (!done) {
			view.printStandardRefereeSelection();
			int i = in.nextInt();
			if (i > 0 && i <= referees.getLastRowNum()) {
				Row referee = referees.getRow(i);
				if (options.isRatedCornerMenEnabled()) {
					done = firstRatedCutManSelectionMenu(fighter1, fighter2, referee);
				} else {
					done = firstStandardCutManSelectionMenu(fighter1, fighter2, referee);
				}
			} else if (i == referees.getLastRowNum() + 1) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean ratedRefereeSelectionMenu(Row fighter1, Row fighter2) {
		// list of rated referees
		// pass proper stats, convert consistency
		boolean done = false;
		int pageNumber = 1;
		boolean hasPreviousPage = false;
		boolean hasNextPage = true;
		XSSFSheet referees = workbook.getSheet("Referees");
		while (!done) {
			ArrayList<Row> refereeGroup = new ArrayList<>();
			int offset = (pageNumber - 1) * 25;
			for (int i = 1 + offset; i <= 25 + offset && i <= referees.getLastRowNum(); i++) {
				refereeGroup.add(referees.getRow(i));
			}
			hasNextPage = refereeGroup.size() == 25 && offset + refereeGroup.size() < referees.getLastRowNum();
			hasPreviousPage = pageNumber > 1;
			view.printRatedRefereeSelection(refereeGroup, hasNextPage, hasPreviousPage);
			int i = in.nextInt() - 1;
			if (i > -1 && i < refereeGroup.size()) {
				boolean done2 = false;
				while (!done2) {
					Row referee = refereeGroup.get(i);
					view.printRatedRefereeConfirmation(referee);
					switch (in.nextInt()) {
						case 1:
							if (options.isRatedCornerMenEnabled()) {
								done = firstRatedCutManSelectionMenu(fighter1, fighter2, referee);
							} else {
								done = firstStandardCutManSelectionMenu(fighter1, fighter2, referee);
							}
							done2 = true;
							break;
						case 2:
							done2 = true;
							break;
					}
				}
			} else if (i == refereeGroup.size()) {
				if (hasNextPage) {
					pageNumber++;
				} else if (hasPreviousPage) {
					pageNumber--;
				} else {
					return false;
				}
			} else if (i == refereeGroup.size() + 1) {
				if (hasNextPage) {
					if (hasPreviousPage) {
						pageNumber--;
					} else {
						return false;
					}
				} else if (hasPreviousPage) {
					return false;
				}
			} else if (i == refereeGroup.size() + 2) {
				if (hasNextPage && hasPreviousPage) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean firstStandardCutManSelectionMenu(Row fighter1, Row fighter2, Row referee) {
		// list of cut man types
		boolean done = false;
		XSSFSheet cutMen = workbook.getSheet("Standard Corner Men");
		while (!done) {
			view.printStandardCutManSelection(fighter1.getCell(0).getStringCellValue(), 1);
			int i = in.nextInt();
			if (i > 0 && i <= cutMen.getLastRowNum()) {
				Row cutMan1 = cutMen.getRow(i);
				done = firstStandardTrainerSelectionMenu(fighter1, fighter2, referee, cutMan1);
			} else if (i == cutMen.getLastRowNum() + 1) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean firstRatedCutManSelectionMenu(Row fighter1, Row fighter2, Row referee) {
		// list of rated cut men
		boolean done = false;
		int pageNumber = 1;
		boolean hasPreviousPage = false;
		boolean hasNextPage = true;
		XSSFSheet cutMen = workbook.getSheet("Cut Men");
		while (!done) {
			ArrayList<Row> cutManGroup = new ArrayList<>();
			int offset = (pageNumber - 1) * 25;
			for (int i = 1 + offset; i <= 25 + offset && i <= cutMen.getLastRowNum(); i++) {
				cutManGroup.add(cutMen.getRow(i));
			}
			hasNextPage = cutManGroup.size() == 25 && offset + cutManGroup.size() < cutMen.getLastRowNum();
			hasPreviousPage = pageNumber > 1;
			view.printRatedCutManSelection(fighter1.getCell(0).getStringCellValue(), 1, cutManGroup, hasNextPage, hasPreviousPage);
			int i = in.nextInt() - 1;
			if (i > -1 && i < cutManGroup.size()) {
				boolean done2 = false;
				while (!done2) {
					Row cutMan1 = cutManGroup.get(i);
					view.printRatedCornerManConfirmation(cutMan1);
					switch (in.nextInt()) {
						case 1:
							done = firstRatedTrainerSelectionMenu(fighter1, fighter2, referee, cutMan1);
							done2 = true;
							break;
						case 2:
							done2 = true;
							break;
					}
				}
			} else if (i == cutManGroup.size()) {
				if (hasNextPage) {
					pageNumber++;
				} else if (hasPreviousPage) {
					pageNumber--;
				} else {
					return false;
				}
			} else if (i == cutManGroup.size() + 1) {
				if (hasNextPage) {
					if (hasPreviousPage) {
						pageNumber--;
					} else {
						return false;
					}
				} else if (hasPreviousPage) {
					return false;
				}
			} else if (i == cutManGroup.size() + 2) {
				if (hasNextPage && hasPreviousPage) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean firstStandardTrainerSelectionMenu(Row fighter1, Row fighter2, Row referee, Row cutMan1) {
		// list of trainer types
		boolean done = false;
		XSSFSheet trainers = workbook.getSheet("Standard Corner Men");
		while (!done) {
			view.printStandardTrainerSelection(fighter1.getCell(0).getStringCellValue(), 1);
			int i = in.nextInt();
			if (i > 0 && i <= trainers.getLastRowNum()) {
				Row trainer1 = trainers.getRow(i);
				done = secondStandardCutManSelectionMenu(fighter1, fighter2, referee, cutMan1, trainer1);
			} else if (i == trainers.getLastRowNum() + 1) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean firstRatedTrainerSelectionMenu(Row fighter1, Row fighter2, Row referee, Row cutMan1) {
		// list of rated cut men
		boolean done = false;
		int pageNumber = 1;
		boolean hasPreviousPage = false;
		boolean hasNextPage = true;
		XSSFSheet trainers = workbook.getSheet("Trainers");
		while (!done) {
			ArrayList<Row> trainerGroup = new ArrayList<>();
			int offset = (pageNumber - 1) * 25;
			for (int i = 1 + offset; i <= 25 + offset && i <= trainers.getLastRowNum(); i++) {
				trainerGroup.add(trainers.getRow(i));
			}
			hasNextPage = trainerGroup.size() == 25 && offset + trainerGroup.size() < trainers.getLastRowNum();
			hasPreviousPage = pageNumber > 1;
			view.printRatedTrainerSelection(fighter1.getCell(0).getStringCellValue(), 1, trainerGroup, hasNextPage, hasPreviousPage);
			int i = in.nextInt() - 1;
			if (i > -1 && i < trainerGroup.size()) {
				boolean done2 = false;
				while (!done2) {
					Row trainer1 = trainerGroup.get(i);
					view.printRatedCornerManConfirmation(trainer1);
					switch (in.nextInt()) {
						case 1:
							done = secondRatedCutManSelectionMenu(fighter1, fighter2, referee, cutMan1, trainer1);
							done2 = true;
							break;
						case 2:
							done2 = true;
							break;
					}
				}
			} else if (i == trainerGroup.size()) {
				if (hasNextPage) {
					pageNumber++;
				} else if (hasPreviousPage) {
					pageNumber--;
				} else {
					return false;
				}
			} else if (i == trainerGroup.size() + 1) {
				if (hasNextPage) {
					if (hasPreviousPage) {
						pageNumber--;
					} else {
						return false;
					}
				} else if (hasPreviousPage) {
					return false;
				}
			} else if (i == trainerGroup.size() + 2) {
				if (hasNextPage && hasPreviousPage) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean secondStandardCutManSelectionMenu(Row fighter1, Row fighter2, Row referee, Row cutMan1, Row trainer1) {
		// list of cut man types
		boolean done = false;
		XSSFSheet cutMen = workbook.getSheet("Standard Corner Men");
		while (!done) {
			view.printStandardCutManSelection(fighter2.getCell(0).getStringCellValue(), 2);
			int i = in.nextInt();
			if (i > 0 && i <= cutMen.getLastRowNum()) {
				Row cutMan2 = cutMen.getRow(i);
				done = secondStandardTrainerSelectionMenu(fighter1, fighter2, referee, cutMan1, trainer1, cutMan2);
			} else if (i == cutMen.getLastRowNum() + 1) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean secondRatedCutManSelectionMenu(Row fighter1, Row fighter2, Row referee, Row cutMan1, Row trainer1) {
		// list of rated cut men
		boolean done = false;
		int pageNumber = 1;
		boolean hasPreviousPage = false;
		boolean hasNextPage = true;
		XSSFSheet cutMen = workbook.getSheet("Cut Men");
		while (!done) {
			ArrayList<Row> cutManGroup = new ArrayList<>();
			int offset = (pageNumber - 1) * 25;
			for (int i = 1 + offset; i <= 25 + offset && i <= cutMen.getLastRowNum(); i++) {
				cutManGroup.add(cutMen.getRow(i));
			}
			hasNextPage = cutManGroup.size() == 25 && offset + cutManGroup.size() < cutMen.getLastRowNum();
			hasPreviousPage = pageNumber > 1;
			view.printRatedCutManSelection(fighter2.getCell(0).getStringCellValue(), 2, cutManGroup, hasNextPage, hasPreviousPage);
			int i = in.nextInt() - 1;
			if (i > -1 && i < cutManGroup.size()) {
				boolean done2 = false;
				while (!done2) {
					Row cutMan2 = cutManGroup.get(i);
					view.printRatedCornerManConfirmation(cutMan2);
					switch (in.nextInt()) {
						case 1:
							done = secondRatedTrainerSelectionMenu(fighter1, fighter2, referee, cutMan1, trainer1, cutMan2);
							done2 = true;
							break;
						case 2:
							done2 = true;
							break;
					}
				}
			} else if (i == cutManGroup.size()) {
				if (hasNextPage) {
					pageNumber++;
				} else if (hasPreviousPage) {
					pageNumber--;
				} else {
					return false;
				}
			} else if (i == cutManGroup.size() + 1) {
				if (hasNextPage) {
					if (hasPreviousPage) {
						pageNumber--;
					} else {
						return false;
					}
				} else if (hasPreviousPage) {
					return false;
				}
			} else if (i == cutManGroup.size() + 2) {
				if (hasNextPage && hasPreviousPage) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean secondStandardTrainerSelectionMenu(Row fighter1, Row fighter2, Row referee, Row cutMan1, Row trainer1, Row cutMan2) {
		// list of trainer types
		boolean done = false;
		XSSFSheet trainers = workbook.getSheet("Standard Corner Men");
		while (!done) {
			view.printStandardTrainerSelection(fighter2.getCell(0).getStringCellValue(), 2);
			int i = in.nextInt();
			if (i > 0 && i <= trainers.getLastRowNum()) {
				Row trainer2 = trainers.getRow(i);
				done = doctorSelectionMenu(fighter1, fighter2, referee, cutMan1, trainer1, cutMan2, trainer2);
			} else if (i == trainers.getLastRowNum() + 1) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean secondRatedTrainerSelectionMenu(Row fighter1, Row fighter2, Row referee, Row cutMan1, Row trainer1, Row cutMan2) {
		// list of rated cut men
		boolean done = false;
		int pageNumber = 1;
		boolean hasPreviousPage = false;
		boolean hasNextPage = true;
		XSSFSheet trainers = workbook.getSheet("Trainers");
		while (!done) {
			ArrayList<Row> trainerGroup = new ArrayList<>();
			int offset = (pageNumber - 1) * 25;
			for (int i = 1 + offset; i <= 25 + offset && i <= trainers.getLastRowNum(); i++) {
				trainerGroup.add(trainers.getRow(i));
			}
			hasNextPage = trainerGroup.size() == 25 && offset + trainerGroup.size() < trainers.getLastRowNum();
			hasPreviousPage = pageNumber > 1;
			view.printRatedTrainerSelection(fighter2.getCell(0).getStringCellValue(), 2, trainerGroup, hasNextPage, hasPreviousPage);
			int i = in.nextInt() - 1;
			if (i > -1 && i < trainerGroup.size()) {
				boolean done2 = false;
				while (!done2) {
					Row trainer2 = trainerGroup.get(i);
					view.printRatedCornerManConfirmation(trainer2);
					switch (in.nextInt()) {
						case 1:
							done = doctorSelectionMenu(fighter1, fighter2, referee, cutMan1, trainer1, cutMan2, trainer2);
							done2 = true;
							break;
						case 2:
							done2 = true;
							break;
					}
				}
			} else if (i == trainerGroup.size()) {
				if (hasNextPage) {
					pageNumber++;
				} else if (hasPreviousPage) {
					pageNumber--;
				} else {
					return false;
				}
			} else if (i == trainerGroup.size() + 1) {
				if (hasNextPage) {
					if (hasPreviousPage) {
						pageNumber--;
					} else {
						return false;
					}
				} else if (hasPreviousPage) {
					return false;
				}
			} else if (i == trainerGroup.size() + 2) {
				if (hasNextPage && hasPreviousPage) {
					return false;
				}
			}
		}
		return true;
	}
	
	public static boolean doctorSelectionMenu(Row fighter1, Row fighter2, Row referee, Row cutMan1, Row trainer1, Row cutMan2, Row trainer2) {
		// list of doctor types
		boolean done = false;
		while (!done) {
			view.printDoctorSelection();
			switch (in.nextInt()) {
				case 1:
					done = startGameConfirmationMenu(fighter1, fighter2, referee, cutMan1, trainer1, cutMan2, trainer2, "Cautious");
					break;
				case 2:
					done = startGameConfirmationMenu(fighter1, fighter2, referee, cutMan1, trainer1, cutMan2, trainer2, "Balanced Judgement");
					break;
				case 3:
					done = startGameConfirmationMenu(fighter1, fighter2, referee, cutMan1, trainer1, cutMan2, trainer2, "Permissive");
					break;
				case 4:
					return false;
			}
		}
		return true;
	}
	
	public static boolean startGameConfirmationMenu(Row fighter1, Row fighter2, Row referee, Row cutMan1, Row trainer1, Row cutMan2, Row trainer2, String dr) {
		// confirmation of game start
		boolean done = false;
		while (!done) {
			view.printStartGameConfirmation();
			switch (in.nextInt()) {
				case 1:
					done = true;
					startGame(fighter1, fighter2, referee, cutMan1, trainer1, cutMan2, trainer2, dr);
					break;
				case 2:
					return false;
			}
		}
		return true;
	}
	
	public static Fighter createFighter(Row fighterRow) {
		Fighter fighter = new Fighter();
		fighter.setName(fighterRow.getCell(0).getStringCellValue());
		if (fighterRow.getCell(1) != null) {
			fighter.setNickname(fighterRow.getCell(1).getStringCellValue());
		}
		fighter.setOR((int) fighterRow.getCell(2).getNumericCellValue());
		fighter.setStyle(fighterRow.getCell(3).getStringCellValue());
		fighter.setCurrentStyle(fighterRow.getCell(3).getStringCellValue());
		String[] specialArr = new String[0];
		if (fighterRow.getCell(4) != null) {
			specialArr = fighterRow.getCell(4).getStringCellValue().split("; ");
		}
		List<String> specialList = new ArrayList<>();
		for (String special : specialArr) {
			specialList.add(special);
		}
		fighter.setSpecial(specialList);
		fighter.setCFBFresh((int) fighterRow.getCell(5).getNumericCellValue());
		fighter.setCFSFresh((int) fighterRow.getCell(6).getNumericCellValue());
		fighter.setDEFFresh((int) fighterRow.getCell(7).getNumericCellValue());
		fighter.setAGGFresh((int) fighterRow.getCell(8).getNumericCellValue());
		fighter.setKD1Fresh((int) fighterRow.getCell(9).getNumericCellValue());
		fighter.setKD2Fresh((int) fighterRow.getCell(10).getNumericCellValue());
		fighter.setKIFresh((int) fighterRow.getCell(11).getNumericCellValue());
		fighter.setKOFresh((int) fighterRow.getCell(12).getNumericCellValue());
		fighter.setEND((int) fighterRow.getCell(13).getNumericCellValue());
		fighter.setCUTFresh((int) fighterRow.getCell(14).getNumericCellValue());
		fighter.setFOULFresh(fighterRow.getCell(15).getStringCellValue());
		fighter.setCPFresh((int) fighterRow.getCell(16).getNumericCellValue());
		fighter.setCFBFatigued((int) fighterRow.getCell(17).getNumericCellValue());
		fighter.setCFSFatigued((int) fighterRow.getCell(18).getNumericCellValue());
		fighter.setDEFFatigued((int) fighterRow.getCell(19).getNumericCellValue());
		fighter.setAGGFatigued((int) fighterRow.getCell(20).getNumericCellValue());
		fighter.setKD1Fatigued((int) fighterRow.getCell(21).getNumericCellValue());
		fighter.setKD2Fatigued((int) fighterRow.getCell(22).getNumericCellValue());
		fighter.setKIFatigued((int) fighterRow.getCell(23).getNumericCellValue());
		fighter.setKOFatigued((int) fighterRow.getCell(24).getNumericCellValue());
		fighter.setCUTFatigued((int) fighterRow.getCell(25).getNumericCellValue());
		fighter.setFOULFatigued(fighterRow.getCell(26).getStringCellValue());
		fighter.setCPFatigued((int) fighterRow.getCell(27).getNumericCellValue());
		fighter.setStratFI((int) fighterRow.getCell(28).getNumericCellValue());
		fighter.setStratFO((int) fighterRow.getCell(29).getNumericCellValue());
		fighter.setStratCU((int) fighterRow.getCell(30).getNumericCellValue());
		fighter.setStratKO((int) fighterRow.getCell(31).getNumericCellValue());
		fighter.setPLFresh((int) fighterRow.getCell(32).getNumericCellValue());
		fighter.setKPFresh((int) fighterRow.getCell(33).getNumericCellValue());
		fighter.setPMFresh((int) fighterRow.getCell(34).getNumericCellValue());
		fighter.setCPDFresh((int) fighterRow.getCell(35).getNumericCellValue());
		fighter.setCFresh((int) fighterRow.getCell(36).getNumericCellValue());
		fighter.setPLFatigued((int) fighterRow.getCell(37).getNumericCellValue());
		fighter.setKPFatigued((int) fighterRow.getCell(38).getNumericCellValue());
		fighter.setPMFatigued((int) fighterRow.getCell(39).getNumericCellValue());
		fighter.setCPDFatigued((int) fighterRow.getCell(40).getNumericCellValue());
		fighter.setCFatigued((int) fighterRow.getCell(41).getNumericCellValue());
		fighter.setJab3((int) fighterRow.getCell(42).getNumericCellValue());
		fighter.setJab2((int) fighterRow.getCell(43).getNumericCellValue());
		fighter.setHook3((int) fighterRow.getCell(44).getNumericCellValue());
		fighter.setHook2((int) fighterRow.getCell(45).getNumericCellValue());
		fighter.setCross3((int) fighterRow.getCell(46).getNumericCellValue());
		fighter.setCross2((int) fighterRow.getCell(47).getNumericCellValue());
		fighter.setCombination3((int) fighterRow.getCell(48).getNumericCellValue());
		fighter.setCombination2((int) fighterRow.getCell(49).getNumericCellValue());
		fighter.setUppercut3((int) fighterRow.getCell(50).getNumericCellValue());
		return fighter;
	}
	
	public static CornerMan createCornerMan(Row cornerManRow) {
		CornerMan cornerMan = new CornerMan();
		cornerMan.setName(cornerManRow.getCell(0).getStringCellValue());
		cornerMan.setTraining(cornerManRow.getCell(1).getStringCellValue());
		cornerMan.setStrategy(cornerManRow.getCell(2).getStringCellValue());
		cornerMan.setAttitude(cornerManRow.getCell(3).getStringCellValue());
		cornerMan.setCutsSwell(cornerManRow.getCell(4).getStringCellValue());
		return cornerMan;
	}
	
	public static Referee createReferee(Row refereeRow) {
		Referee referee = new Referee();
		referee.setName(refereeRow.getCell(0).getStringCellValue());
		referee.setFouls(refereeRow.getCell(1).getStringCellValue());
		referee.setStoppage(refereeRow.getCell(2).getStringCellValue());
		return referee;
	}
	
	public static void startGame(Row fighter1, Row fighter2, Row referee, Row cutMan1, Row trainer1, Row cutMan2, Row trainer2, String dr) {
		Fighter f1 = createFighter(fighter1);
		f1.setCutMan(createCornerMan(cutMan1));
		f1.setTrainer(createCornerMan(trainer1));
		Fighter f2 = createFighter(fighter2);
		f2.setCutMan(createCornerMan(cutMan2));
		f2.setTrainer(createCornerMan(trainer2));
		Referee r = createReferee(referee);
		
		XSSFSheet cards = workbook.getSheet("Cards");
		List<Card> fullDeck = new ArrayList<>();
		for (int i = 1; i <= cards.getLastRowNum(); i++) {
			Card card = new Card();
			card.setCF((int) cards.getRow(i).getCell(0).getNumericCellValue());
			card.setRN((int) cards.getRow(i).getCell(1).getNumericCellValue());
			card.setResult((int) cards.getRow(i).getCell(2).getNumericCellValue());
			card.setCut(cards.getRow(i).getCell(3).getStringCellValue().equals("Y"));
			card.setRingPosition(cards.getRow(i).getCell(4).getStringCellValue());
			card.setKD((int) cards.getRow(i).getCell(5).getNumericCellValue());
			card.setKD2((int) cards.getRow(i).getCell(6).getNumericCellValue());
			card.setKOR((int) cards.getRow(i).getCell(7).getNumericCellValue());
			card.setKDC((int) cards.getRow(i).getCell(8).getNumericCellValue());
			card.setJ1(cards.getRow(i).getCell(9).getStringCellValue());
			card.setJ2(cards.getRow(i).getCell(10).getStringCellValue());
			card.setJ3(cards.getRow(i).getCell(11).getStringCellValue());
			card.setInjury((int) cards.getRow(i).getCell(12).getNumericCellValue());
			card.setSpecialAction((int) cards.getRow(i).getCell(13).getNumericCellValue());
			fullDeck.add(card);
		}
		
		Game game = new Game(f1, f2, fullDeck, r, dr, options, cutsSwellingTable, workbook.getSheet("KD-KO Table"));
		gameLoop(game);
	}
	
	public static void optionsMenu() {
		// list of all options and their current values
		boolean done = false;
		while (!done) {
			view.printOptions(options);
			switch (in.nextInt()) {
				case 1:
					selectOption("NumberOfRounds");
					break;
				case 2:
					selectOption("ERatingSelectionFrequency");
					break;
				case 3:
					selectOption("Injuries");
					break;
				case 4:
					selectOption("CFConversion");
					break;
				case 5:
					selectOption("Aggressiveness");
					break;
				case 6:
					selectOption("FoulDamage");
					break;
				case 7:
					selectOption("FoulTableHeadButt");
					break;
				case 8:
					selectOption("AdvancedTiming");
					break;
				case 9:
					selectOption("CardUsageToExtendTheRound");
					break;
				case 10:
					selectOption("MandatoryEightCount");
					break;
				case 11:
					selectOption("SavedByTheBell");
					break;
				case 12:
					selectOption("ThreeKnockdownRule");
					break;
				case 13:
					selectOption("Southpaw");
					break;
				case 14:
					selectOption("MissingPunchesPenalty");
					break;
				case 15:
					selectOption("KillerInstinctAndRoundTiming");
					break;
				case 16:
					selectOption("AdvancedClinching");
					break;
				case 17:
					selectOption("RatedReferee");
					break;
				case 18:
					selectOption("RatedCornerMen");
					break;
				case 19:
					selectOption("RefereeErrorTable");
					break;
				case 20:
					selectOption("Strategies");
					break;
				case 21:
					selectOption("FighterTraits");
					break;
				case 22:
					selectOption("ThrowInTheTowel");
					break;
				case 23:
					done = true;
					break;
			}
		}
	}
	
	public static void selectOption(String option) {
		// list of all choices for the current option
		boolean done = false;
		while (!done) {
			switch (option) {
				case "NumberOfRounds":
					view.printOptionSelect(option, options.getOption(option), new String[] { "10", "12", "15" });
					switch (in.nextInt()) {
						case 1:
							options.setMaxRounds(10);
							done = true;
							break;
						case 2:
							options.setMaxRounds(12);
							done = true;
							break;
						case 3:
							options.setMaxRounds(15);
							done = true;
							break;
						case 4:
							done = true;
							break;
					}
					break;
				case "ERatingSelectionFrequency":
					view.printOptionSelect(option, options.getOption(option), new String[] { "PerTurn", "PerRound" });
					switch (in.nextInt()) {
						case 1:
							options.setERatingSelectionFrequency("PerTurn");
							done = true;
							break;
						case 2:
							options.setERatingSelectionFrequency("PerRound");
							done = true;
							break;
						case 3:
							done = true;
							break;
					}
					break;
				case "Injuries":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Enabled", "Disabled" });
					switch (in.nextInt()) {
						case 1:
							options.setInjuriesEnabled(true);
							done = true;
							break;
						case 2:
							options.setInjuriesEnabled(false);
							done = true;
							break;
						case 3:
							done = true;
							break;
						}
					break;
				case "CFConversion":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Enabled", "Disabled" });
					switch (in.nextInt()) {
						case 1:
							options.setCFConversionEnabled(true);
							done = true;
							break;
						case 2:
							options.setCFConversionEnabled(false);
							done = true;
							break;
						case 3:
							done = true;
							break;
					}
					break;
				case "Aggressiveness":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Enabled", "Disabled" });
					switch (in.nextInt()) {
						case 1:
							options.setAggressivenessEnabled(true);
							done = true;
							break;
						case 2:
							options.setAggressivenessEnabled(false);
							done = true;
							break;
						case 3:
							done = true;
							break;
					}
					break;
				case "FoulDamage":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Enabled", "Disabled" });
					switch (in.nextInt()) {
						case 1:
							options.setFoulDamageEnabled(true);
							done = true;
							break;
						case 2:
							options.setFoulDamageEnabled(false);
							done = true;
							break;
						case 3:
							done = true;
							break;
					}
					break;
				case "FoulTableHeadButt":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Enabled", "Disabled" });
					switch (in.nextInt()) {
						case 1:
							options.setHeadbuttCutEnabled(true);
							done = true;
							break;
						case 2:
							options.setHeadbuttCutEnabled(false);
							done = true;
							break;
						case 3:
							done = true;
							break;
					}
					break;
				case "AdvancedTiming":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Off", "On", "Alternate" });
					switch (in.nextInt()) {
						case 1:
							options.setAdvancedTimingOption("Off");
							done = true;
							break;
						case 2:
							options.setAdvancedTimingOption("On");
							done = true;
							break;
						case 3:
							options.setAdvancedTimingOption("Alternate");
							done = true;
							break;
						case 4:
							done = true;
							break;
					}
					break;
				case "CardUsageToExtendTheRound":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Enabled", "Disabled" });
					switch (in.nextInt()) {
						case 1:
							options.setExtendRoundEnabled(true);
							done = true;
							break;
						case 2:
							options.setExtendRoundEnabled(false);
							done = true;
							break;
						case 3:
							done = true;
							break;
					}
					break;
				case "MandatoryEightCount":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Enabled", "Disabled" });
					switch (in.nextInt()) {
						case 1:
							options.setMandatoryEightCountEnabled(true);
							done = true;
							break;
						case 2:
							options.setMandatoryEightCountEnabled(false);
							done = true;
							break;
						case 3:
							done = true;
							break;
					}
					break;
				case "SavedByTheBell":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Enabled", "Disabled" });
					switch (in.nextInt()) {
						case 1:
							options.setSavedByTheBellEnabled(true);
							done = true;
							break;
						case 2:
							options.setSavedByTheBellEnabled(false);
							done = true;
							break;
						case 3:
							done = true;
							break;
					}
					break;
				case "ThreeKnockdownRule":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Enabled", "Disabled" });
					switch (in.nextInt()) {
						case 1:
							options.setThreeKnockdownRuleEnabled(true);
							done = true;
							break;
						case 2:
							options.setThreeKnockdownRuleEnabled(false);
							done = true;
							break;
						case 3:
							done = true;
							break;
					}
					break;
				case "Southpaw":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Enabled", "Disabled" });
					switch (in.nextInt()) {
						case 1:
							options.setSouthpawEnabled(true);
							done = true;
							break;
						case 2:
							options.setSouthpawEnabled(false);
							done = true;
							break;
						case 3:
							done = true;
							break;
					}
					break;
				case "MissingPunchesPenalty":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Enabled", "Disabled" });
					switch (in.nextInt()) {
						case 1:
							options.setMissPenaltyEnabled(true);
							done = true;
							break;
						case 2:
							options.setMissPenaltyEnabled(false);
							done = true;
							break;
						case 3:
							done = true;
							break;
					}
					break;
				case "KillerInstinctAndRoundTiming":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Enabled", "Disabled" });
					switch (in.nextInt()) {
						case 1:
							options.setKITimingEnabled(true);
							done = true;
							break;
						case 2:
							options.setKITimingEnabled(false);
							done = true;
							break;
						case 3:
							done = true;
							break;
					}
					break;
				case "AdvancedClinching":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Enabled", "Disabled" });
					switch (in.nextInt()) {
						case 1:
							options.setAdvancedClinchingEnabled(true);
							done = true;
							break;
						case 2:
							options.setAdvancedClinchingEnabled(false);
							done = true;
							break;
						case 3:
							done = true;
							break;
					}
					break;
				case "RatedReferee":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Enabled", "Disabled" });
					switch (in.nextInt()) {
						case 1:
							options.setRatedRefereeEnabled(true);
							done = true;
							break;
						case 2:
							options.setRatedRefereeEnabled(false);
							done = true;
							break;
						case 3:
							done = true;
							break;
					}
					break;
				case "RatedCornerMen":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Enabled", "Disabled" });
					switch (in.nextInt()) {
						case 1:
							options.setRatedCornerMenEnabled(true);
							done = true;
							break;
						case 2:
							options.setRatedCornerMenEnabled(false);
							done = true;
							break;
						case 3:
							done = true;
							break;
					}
					break;
				case "RefereeErrorTable":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Off", "PerFight", "PerRound" });
					switch (in.nextInt()) {
						case 1:
							options.setAdvancedTimingOption("Off");
							done = true;
							break;
						case 2:
							options.setAdvancedTimingOption("PerFight");
							done = true;
							break;
						case 3:
							options.setAdvancedTimingOption("PerRound");
							done = true;
							break;
						case 4:
							done = true;
							break;
					}
					break;
				case "Strategies":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Enabled", "Disabled" });
					switch (in.nextInt()) {
						case 1:
							options.setStrategyEnabled(true);
							done = true;
							break;
						case 2:
							options.setStrategyEnabled(false);
							done = true;
							break;
						case 3:
							done = true;
							break;
					}
					break;
				case "FighterTraits":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Enabled", "Disabled" });
					switch (in.nextInt()) {
						case 1:
							options.setFighterTraitsEnabled(true);
							done = true;
							break;
						case 2:
							options.setFighterTraitsEnabled(false);
							done = true;
							break;
						case 3:
							done = true;
							break;
					}
					break;
				case "ThrowInTheTowel":
					view.printOptionSelect(option, options.getOption(option), new String[] { "Enabled", "Disabled" });
					switch (in.nextInt()) {
						case 1:
							options.setThrowInTheTowelEnabled(true);
							done = true;
							break;
						case 2:
							options.setThrowInTheTowelEnabled(false);
							done = true;
							break;
						case 3:
							done = true;
							break;
					}
					break;
			}
		}
	}
	
	public static void cleanOptions() {
		// if options.ini has invalid options, fixes that
		String[] options = new String[] {
				"NumberOfRounds",
				"ERatingSelectionFrequency",
				"Injuries",
				"CFConversion",
				"Aggressiveness",
				"FoulDamage",
				"FoulTableHeadButt",
				"AdvancedTiming",
				"CardUsageToExtendTheRound",
				"MandatoryEightCount",
				"SavedByTheBell",
				"ThreeKnockdownRule",
				"Southpaw",
				"MissingPunchesPenalty",
				"KillerInstinctAndRoundTiming",
				"AdvancedClinching",
				"RatedReferee",
				"RatedCornerMen",
				"RefereeErrorTable",
				"Strategies",
				"FighterTraits",
				"ThrowInTheTowel",
		};
		try {
			List<String> lines = Files.readAllLines(Paths.get("options.ini"));
			Iterator<String> linesIterator = lines.listIterator();
			while (linesIterator.hasNext()) {
				String line = linesIterator.next();
				boolean found = false;
				for (String option : options) {
					if (line.startsWith(option)) {
						if (found) {
							linesIterator.remove();
						} else {
							found = true;
						}
					}
				}
				if (!found) {
					linesIterator.remove();
				}
			}
			Files.write(Paths.get("options.ini"), lines, StandardCharsets.UTF_8);
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: options.ini not found!");
		} catch (IOException e) {
			System.err.println("ERROR: Unable to read/write options.ini!");
		}
	}
	
	public static void populateOptions() {
		// if options.ini is missing options or has invalid values, fixes that
		Map<String, String> options = new HashMap<>();
		options.put("NumberOfRounds", "12");
		options.put("ERatingSelectionFrequency", "PerTurn");
		options.put("Injuries", "True");
		options.put("CFConversion", "True");
		options.put("Aggressiveness", "False");
		options.put("FoulDamage", "False");
		options.put("FoulTableHeadButt", "False");
		options.put("AdvancedTiming", "Off");
		options.put("CardUsageToExtendTheRound", "False");
		options.put("MandatoryEightCount", "False");
		options.put("SavedByTheBell", "False");
		options.put("ThreeKnockdownRule", "False");
		options.put("Southpaw", "False");
		options.put("MissingPunchesPenalty", "False");
		options.put("KillerInstinctAndRoundTiming", "False");
		options.put("AdvancedClinching", "False");
		options.put("RatedReferee", "False");
		options.put("RatedCornerMen", "False");
		options.put("RefereeErrorTable", "Off");
		options.put("Strategies", "False");
		options.put("FighterTraits", "False");
		options.put("ThrowInTheTowel", "False");
		Map<String, String> missingOptions = new HashMap<>();
		missingOptions.putAll(options);
		try {
			List<String> lines = Files.readAllLines(Paths.get("options.ini"));
			List<String> newLines = new ArrayList<>();
			Iterator<String> linesIterator = lines.listIterator();
			while (linesIterator.hasNext()) {
				String line = linesIterator.next();
				String[] lineArr = line.split("=");
				Iterator<String> optionsIterator = options.keySet().iterator();
				while (optionsIterator.hasNext()) {
					String option = optionsIterator.next();
					if (lineArr[0].equals(option)) {
						missingOptions.remove(option);
						if (lineArr.length != 2) {
							linesIterator.remove();
							newLines.add(option + "=" + options.get(option));
							break;
						}
						if (option.equals("NumberOfRounds") && !lineArr[1].equals("10") && !lineArr[1].equals("12") && !lineArr[1].equals("15")) {
							linesIterator.remove();
							newLines.add(option + "=" + options.get(option));
							break;
						}
						if (option.equals("ERatingSelectionFrequency") && !lineArr[1].equals("PerTurn") && !lineArr[1].equals("PerRound")) {
							linesIterator.remove();
							newLines.add(option + "=" + options.get(option));
							break;
						}
						if (option.equals("AdvancedTiming") && !lineArr[1].equals("Off") && !lineArr[1].equals("On") && !lineArr[1].equals("Alternate")) {
							linesIterator.remove();
							newLines.add(option + "=" + options.get(option));
							break;
						}
						if (option.equals("RefereeErrorTable") && !lineArr[1].equals("Off") && !lineArr[1].equals("PerFight") && !lineArr[1].equals("PerRound")) {
							linesIterator.remove();
							newLines.add(option + "=" + options.get(option));
							break;
						}
						if (!lineArr[1].equals("True") && !lineArr[1].equals("False")) {
							linesIterator.remove();
							newLines.add(option + "=" + options.get(option));
							break;
						}
					}
				}
			}
			newLines.addAll(lines);
			Set<String> keySet = missingOptions.keySet();
			for (String option : keySet) {
				newLines.add(option + "=" + missingOptions.get(option));
			}
			Files.write(Paths.get("options.ini"), newLines, StandardCharsets.UTF_8);
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: options.ini not found!");
		} catch (IOException e) {
			System.err.println("ERROR: Unable to read/write options.ini!");
		}
	}
	
    public static void main(String[] args) {
    	// populate options.ini if needed
    	cleanOptions();
    	populateOptions();
    	options = new Options();
    	
    	// main menu
    	boolean done = false;
		while (!done) {
			view.printMainMenu();
			switch (in.nextInt()) {
				case 1:
					divisionsMenu();
					break;
				case 2:
					optionsMenu();
					break;
				case 3:
					done = true;
					break;
			}
		}
    }
    
}
