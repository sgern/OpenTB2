package com.sgern.TitleBoutII;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Row;

public class AppView {
	
	MessageLog messageLog;
	
	// probably put messageLog in the constructor
	public AppView() {
		messageLog = new MessageLog(4);
	}
	
	public void clearScreen() {
		String[] cmdarray = {"clear"};
	    try {
	        if (System.getProperty("os.name").contains("Windows")) {
	            new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
	        } else {
	            Runtime.getRuntime().exec(cmdarray);
	        }
	    } catch (IOException | InterruptedException e) {
	    	e.printStackTrace();
	    }
	}
	
	public void printBigTitle() {
		int width = 120, height = 9;
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics graphics = bufferedImage.getGraphics();
		Graphics2D graphics2D = (Graphics2D) graphics;
		graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		graphics2D.drawString("TITLE BOUT II", 22, 9);
		for (int y = 0; y < height; y++) {
		    StringBuilder stringBuilder = new StringBuilder();
		    for (int x = 0; x < width; x++) {
		    	stringBuilder.append(bufferedImage.getRGB(x, y) == -16777216 ? " " : "*");
		    }
		    if (stringBuilder.toString().trim().isEmpty()) {
		        continue;
		    }
		    if (System.getProperty("os.name").contains("Windows")) {
			    System.out.println(stringBuilder);
		    } else {
		    	System.out.print(stringBuilder);
		    }
		}
	}
	
	public void printMainMenu() {
		clearScreen();
		printBigTitle();
		System.out.println("(1) Start Game\n(2) Options\n(3) Exit");
	}
	
	public String formatBoolean(boolean b) {
		return b ? "Enabled" : "Disabled";
	}
	
	public void printOptions(Options options) {
		clearScreen();
		System.out.println("(1) Number of Rounds:\t\t\t" + options.getMaxRounds());
		System.out.println("(2) Injuries:\t\t\t\t" + formatBoolean(options.isInjuriesEnabled()));
		System.out.println("(3) CF Conversion:\t\t\t" + formatBoolean(options.isCFConversionEnabled()));
		System.out.println("(4) Aggressiveness:\t\t\t" + formatBoolean(options.isAggressivenessEnabled()));
		System.out.println("(5) Damage Sustained From a Foul:\t" + formatBoolean(options.isFoulDamageEnabled()));
		System.out.println("(6) Foul Table Head Butt:\t\t" + formatBoolean(options.isHeadbuttCutEnabled()));
		System.out.println("(7) Advanced Timing:\t\t\t" + options.getAdvancedTimingOption());
		System.out.println("(8) Card Usage to Extend the Round:\t" + formatBoolean(options.isExtendRoundEnabled()));
		System.out.println("(9) Mandatory 8-Count:\t\t\t" + formatBoolean(options.isMandatoryEightCountEnabled()));
		System.out.println("(10) Saved by the Bell:\t\t\t" + formatBoolean(options.isSavedByTheBellEnabled()));
		System.out.println("(11) Three-Knockdown Rule:\t\t" + formatBoolean(options.isThreeKnockdownRuleEnabled()));
		System.out.println("(12) Southpaw:\t\t\t\t" + formatBoolean(options.isSouthpawEnabled()));
		System.out.println("(13) Missing Punches Penalty:\t\t" + formatBoolean(options.isMissPenaltyEnabled()));
		System.out.println("(14) Killer Instinct and Round Timing:\t" + formatBoolean(options.isKITimingEnabled()));
		System.out.println("(15) Advanced Clinching:\t\t" + formatBoolean(options.isAdvancedClinchingEnabled()));
		System.out.println("(16) Rated Referee:\t\t\t" + formatBoolean(options.isRatedRefereeEnabled()));
		System.out.println("(17) Rated Corner Men:\t\t\t" + formatBoolean(options.isRatedCornerMenEnabled()));
		System.out.println("(18) Referee Error Table:\t\t" + options.getRefereeErrorOption());
		System.out.println("(19) Strategies:\t\t\t" + formatBoolean(options.isStrategiesEnabled()));
		System.out.println("(20) Fighter Traits:\t\t\t" + formatBoolean(options.isFighterTraitsEnabled()));
		System.out.println("(21) Throw in the Towel:\t\t" + formatBoolean(options.isThrowInTheTowelEnabled()));
		System.out.println("(22) Exit");
	}
	
	public void printOptionSelect(String option, String value, String[] values) {
		clearScreen();
		// System.out.println(option + ": " + value);
		int i = 1;
		while (i <= values.length) {
			System.out.println("(" + i + ") " + values[i - 1]);
			i++;
		}
		System.out.println("(" + i + ") Cancel");
	}
	
	public void printDivisions(ArrayList<String> divisions) {
		clearScreen();
		int i = 1;
		while (i <= divisions.size()) {
			System.out.println("(" + i + ") " + divisions.get(i - 1));
			i++;
		}
		System.out.println("(" + i + ") Cancel");
	}
	
	public void printFighterSelection(int currentFighterNumber, ArrayList<Row> fighterGroup, boolean hasNextPage, boolean hasPreviousPage) {
		clearScreen();
		System.out.println("Select Fighter " + currentFighterNumber + ".");
		int i = 1;
		while (i <= fighterGroup.size()) {
			System.out.println("(" + i + ")\t" + fighterGroup.get(i - 1).getCell(0));
			i++;
		}
		if (hasNextPage) {
			System.out.println("(" + i + ")\tNext");
			i++;
		}
		if (hasPreviousPage) {
			System.out.println("(" + i + ")\tPrevious");
			i++;
		}
		System.out.println("(" + i + ")\tCancel");
	}
	
	public void printFighterConfirmation(Row fighter) {
		clearScreen();
		String output = "";
		
		System.out.println(StringUtils.center(fighter.getCell(0) + " - " + (int) fighter.getCell(2).getNumericCellValue() + fighter.getCell(3), 120));
		
		output = !(fighter.getCell(1) == null) ? fighter.getCell(1).getStringCellValue() : "";
		System.out.println(StringUtils.center(output, 120));
		
		output = "SPECIAL:";
		output += !(fighter.getCell(4) == null) ? " " + fighter.getCell(4).getStringCellValue() : "";
		System.out.println(StringUtils.center(output, 120));
		System.out.println();
		
		System.out.println(StringUtils.center("FRESH", 60) + StringUtils.center("FATIGUED", 60));
		
		output = "                ";
		output += StringUtils.rightPad("CFB/S: ", 7);
		output += StringUtils.center((int) fighter.getCell(5).getNumericCellValue() + "/" + (int) fighter.getCell(6).getNumericCellValue(), 5);
		output += "    ";
		output += StringUtils.rightPad("DEF: ", 7);
		output += StringUtils.center("" + (int) fighter.getCell(7).getNumericCellValue(), 5);
		output += "                                ";
		output += StringUtils.rightPad("CFB/S: ", 7);
		output += StringUtils.center((int) fighter.getCell(17).getNumericCellValue() + "/" + (int) fighter.getCell(18).getNumericCellValue(), 5);
		output += "    ";
		output += StringUtils.rightPad("DEF: ", 7);
		output += StringUtils.center("" + (int) fighter.getCell(19).getNumericCellValue(), 5);
		output += "                ";
		System.out.println(output);
		
		output = "                ";
		output += StringUtils.rightPad("AGG: ", 7);
		output += StringUtils.center("" + (int) fighter.getCell(8).getNumericCellValue(), 5);
		output += "    ";
		output += StringUtils.rightPad("KD1/2: ", 7);
		output += StringUtils.center((int) fighter.getCell(9).getNumericCellValue() + "/" + (int) fighter.getCell(10).getNumericCellValue(), 5);
		output += "                                ";
		output += StringUtils.rightPad("AGG: ", 7);
		output += StringUtils.center("" + (int) fighter.getCell(20).getNumericCellValue(), 5);
		output += "    ";
		output += StringUtils.rightPad("KD1/2: ", 7);
		output += StringUtils.center((int) fighter.getCell(21).getNumericCellValue() + "/" + (int) fighter.getCell(22).getNumericCellValue(), 5);
		output += "                ";
		System.out.println(output);
		
		output = "                ";
		output += StringUtils.rightPad("KI: ", 7);
		output += StringUtils.center("" + (int) fighter.getCell(11).getNumericCellValue(), 5);
		output += "    ";
		output += StringUtils.rightPad("KO: ", 7);
		output += StringUtils.center("" + (int) fighter.getCell(12).getNumericCellValue(), 5);
		output += "                                ";
		output += StringUtils.rightPad("KI: ", 7);
		output += StringUtils.center("" + (int) fighter.getCell(23).getNumericCellValue(), 5);
		output += "    ";
		output += StringUtils.rightPad("KO: ", 7);
		output += StringUtils.center("" + (int) fighter.getCell(24).getNumericCellValue(), 5);
		output += "                ";
		System.out.println(output);
		
		output = "                ";
		output += StringUtils.rightPad("END: ", 7);
		output += StringUtils.center("" + (int) fighter.getCell(13).getNumericCellValue(), 5);
		output += "    ";
		output += StringUtils.rightPad("CUT: ", 7);
		output += StringUtils.center("" + (int) fighter.getCell(14).getNumericCellValue(), 5);
		output += "                                ";
		output += StringUtils.rightPad("END: ", 7);
		output += StringUtils.center("0", 5);
		output += "    ";
		output += StringUtils.rightPad("CUT: ", 7);
		output += StringUtils.center("" + (int) fighter.getCell(25).getNumericCellValue(), 5);
		output += "                ";
		System.out.println(output);
		
		output = "                ";
		output += StringUtils.rightPad("FOUL: ", 7);
		output += StringUtils.center(fighter.getCell(15).getStringCellValue(), 5);
		output += "    ";
		output += StringUtils.rightPad("CP: ", 7);
		output += StringUtils.center("" + (int) fighter.getCell(16).getNumericCellValue(), 5);
		output += "                                ";
		output += StringUtils.rightPad("FOUL: ", 7);
		output += StringUtils.center(fighter.getCell(26).getStringCellValue(), 5);
		output += "    ";
		output += StringUtils.rightPad("CP: ", 7);
		output += StringUtils.center("" + (int) fighter.getCell(27).getNumericCellValue(), 5);
		output += "                ";
		System.out.println(output);
		System.out.println();
		
		output = "                ";
		output += "STRATEGIES: ";
		output += StringUtils.leftPad("FI:      " + (int) fighter.getCell(28).getNumericCellValue(), 19);
		output += StringUtils.leftPad("FO:      " + (int) fighter.getCell(29).getNumericCellValue(), 19);
		output += StringUtils.leftPad("CU:      " + (int) fighter.getCell(30).getNumericCellValue(), 19);
		output += StringUtils.leftPad("KO:      " + (int) fighter.getCell(31).getNumericCellValue(), 19);
		output += "                ";
		System.out.println(output);
		System.out.println();
		
		System.out.println(StringUtils.center("ACTION", 120));
		
		output = "                ";
		output += StringUtils.center("", 30) + StringUtils.center("Normal", 29) + StringUtils.center("Fatigued", 29);
		output += "                ";
		System.out.println(output);

		output = "                ";
		output += StringUtils.rightPad("Punches Landed: ", 30);
		output += StringUtils.center("1-" + (int) fighter.getCell(32).getNumericCellValue() + " (" + (int) fighter.getCell(33).getNumericCellValue() + "KP)", 29);
		output += StringUtils.center("1-" + (int) fighter.getCell(37).getNumericCellValue() + " (" + (int) fighter.getCell(38).getNumericCellValue() + "KP)", 29);
		output += "                ";
		System.out.println(output);

		output = "                ";
		output += StringUtils.rightPad("Punches Missed: ", 30);
		output += StringUtils.center((int) fighter.getCell(32).getNumericCellValue() + 1 + "-" + (int) fighter.getCell(34).getNumericCellValue() + " | CPD " + (int) fighter.getCell(35).getNumericCellValue() + "-" + (int) fighter.getCell(34).getNumericCellValue(), 29);
		output += StringUtils.center((int) fighter.getCell(37).getNumericCellValue() + 1 + "-" + (int) fighter.getCell(39).getNumericCellValue() + " | CPD " + (int) fighter.getCell(40).getNumericCellValue() + "-" + (int) fighter.getCell(39).getNumericCellValue(), 29);
		output += "                ";
		System.out.println(output);

		output = "                ";
		output += StringUtils.rightPad("Clinching: ", 30);
		output += StringUtils.center((int) fighter.getCell(34).getNumericCellValue() + 1 + "-" + (int) fighter.getCell(36).getNumericCellValue(), 29);
		output += StringUtils.center((int) fighter.getCell(39).getNumericCellValue() + 1 + "-" + (int) fighter.getCell(41).getNumericCellValue(), 29);
		output += "                ";
		System.out.println(output);

		output = "                ";
		output += StringUtils.rightPad("Ring Movement: ", 30);
		output += StringUtils.center((int) fighter.getCell(36).getNumericCellValue() + 1 + "-" + 80, 29);
		output += StringUtils.center((int) fighter.getCell(41).getNumericCellValue() + 1 + "-" + 80, 29);
		output += "                ";
		System.out.println(output);
		
		System.out.println();
		output = "                ";
		output += StringUtils.rightPad("Hitting Value: ", 30) + StringUtils.center("3", 29) + StringUtils.center("2", 29);
		output += "                ";
		System.out.println(output);
		
		output = "                ";
		output += StringUtils.rightPad("Jab: ", 30);
		output += StringUtils.center("1-" + (int) fighter.getCell(42).getNumericCellValue(), 29);
		output += StringUtils.center((int) fighter.getCell(42).getNumericCellValue() + 1 + "-" + (int) fighter.getCell(43).getNumericCellValue(), 29);
		output += "                ";
		System.out.println(output);
		
		output = "                ";
		output += StringUtils.rightPad("Hook: ", 30);
		output += StringUtils.center((int) fighter.getCell(43).getNumericCellValue() + 1 + "-" + (int) fighter.getCell(44).getNumericCellValue(), 29);
		output += StringUtils.center((int) fighter.getCell(44).getNumericCellValue() + 1 + "-" + (int) fighter.getCell(45).getNumericCellValue(), 29);
		output += "                ";
		System.out.println(output);
		
		output = "                ";
		output += StringUtils.rightPad("Cross: ", 30);
		output += StringUtils.center((int) fighter.getCell(45).getNumericCellValue() + 1 + "-" + (int) fighter.getCell(46).getNumericCellValue(), 29);
		output += StringUtils.center((int) fighter.getCell(46).getNumericCellValue() + 1 + "-" + (int) fighter.getCell(47).getNumericCellValue(), 29);
		output += "                ";
		System.out.println(output);
		
		output = "                ";
		output += StringUtils.rightPad("Combination: ", 30);
		output += StringUtils.center((int) fighter.getCell(47).getNumericCellValue() + 1 + "-" + (int) fighter.getCell(48).getNumericCellValue(), 29);
		output += StringUtils.center((int) fighter.getCell(48).getNumericCellValue() + 1 + "-" + (int) fighter.getCell(49).getNumericCellValue(), 29);
		output += "                ";
		System.out.println(output);
		
		output = "                ";
		output += StringUtils.rightPad("Uppercut: ", 30);
		output += StringUtils.center((int) fighter.getCell(49).getNumericCellValue() + 1 + "-" + (int) fighter.getCell(50).getNumericCellValue(), 29);
		output += StringUtils.center((int) fighter.getCell(50).getNumericCellValue() + 1 + "-" + 80, 29);
		output += "                ";
		System.out.println(output);
		
		System.out.println();
		System.out.println("(1) Confirm");
		System.out.println("(2) Cancel");
	}
	
	public void printStandardRefereeSelection() {
		clearScreen();
		System.out.println("Select Referee Type.");
		System.out.println("(1) Very Lenient");
		System.out.println("(2) Lenient");
		System.out.println("(3) Normal");
		System.out.println("(4) Strict");
		System.out.println("(5) Cancel");
	}
	
	public void printRatedRefereeSelection(ArrayList<Row> refereeGroup, boolean hasNextPage, boolean hasPreviousPage) {
		clearScreen();
		System.out.println("Select Rated Referee.");
		int i = 1;
		while (i <= refereeGroup.size()) {
			System.out.println("(" + i + ")\t" + refereeGroup.get(i - 1).getCell(0));
			i++;
		}
		if (hasNextPage) {
			System.out.println("(" + i + ")\tNext");
			i++;
		}
		if (hasPreviousPage) {
			System.out.println("(" + i + ")\tPrevious");
			i++;
		}
		System.out.println("(" + i + ")\tCancel");
	}
	
	public void printRatedRefereeConfirmation(Row referee) {
		clearScreen();
		System.out.println("Name: " + referee.getCell(0).getStringCellValue());
		System.out.println("Fouls: " + referee.getCell(1).getStringCellValue());
		System.out.println("Stoppage: " + referee.getCell(2).getStringCellValue());
		System.out.println("Consistency: 1-" + (int) referee.getCell(3).getNumericCellValue());
		System.out.println("Boxer Rate: " + (int) referee.getCell(4).getNumericCellValue());
		System.out.println("Availability: 1-" + (int) referee.getCell(5).getNumericCellValue());
		System.out.println();
		System.out.println("(1) Confirm");
		System.out.println("(2) Cancel");
	}
	
	public void printStandardCutManSelection(int currentFighterNumber) {
		clearScreen();
		System.out.println("Select Cut Man Type for Fighter " + currentFighterNumber + ".");
		System.out.println("(1) Poor");
		System.out.println("(2) Average");
		System.out.println("(3) Good");
		System.out.println("(4) Very Good");
		System.out.println("(5) Excellent");
		System.out.println("(6) Cancel");
	}
	
	public void printRatedCutManSelection(int currentFighterNumber, ArrayList<Row> cutManGroup, boolean hasNextPage, boolean hasPreviousPage) {
		clearScreen();
		System.out.println("Select Rated Cut Man for Fighter " + currentFighterNumber + ".");
		int i = 1;
		while (i <= cutManGroup.size()) {
			System.out.println("(" + i + ")\t" + cutManGroup.get(i - 1).getCell(0));
			i++;
		}
		if (hasNextPage) {
			System.out.println("(" + i + ")\tNext");
			i++;
		}
		if (hasPreviousPage) {
			System.out.println("(" + i + ")\tPrevious");
			i++;
		}
		System.out.println("(" + i + ")\tCancel");
	}
	
	public void printStandardTrainerSelection(int currentFighterNumber) {
		clearScreen();
		System.out.println("Select Trainer Type for Fighter " + currentFighterNumber + ".");
		System.out.println("(1) Poor");
		System.out.println("(2) Average");
		System.out.println("(3) Good");
		System.out.println("(4) Very Good");
		System.out.println("(5) Excellent");
		System.out.println("(6) Cancel");
	}
	
	public void printRatedTrainerSelection(int currentFighterNumber, ArrayList<Row> trainerGroup, boolean hasNextPage, boolean hasPreviousPage) {
		clearScreen();
		System.out.println("Select Rated Trainer for Fighter " + currentFighterNumber + ".");
		int i = 1;
		while (i <= trainerGroup.size()) {
			System.out.println("(" + i + ")\t" + trainerGroup.get(i - 1).getCell(0));
			i++;
		}
		if (hasNextPage) {
			System.out.println("(" + i + ")\tNext");
			i++;
		}
		if (hasPreviousPage) {
			System.out.println("(" + i + ")\tPrevious");
			i++;
		}
		System.out.println("(" + i + ")\tCancel");
	}
	
	public void printRatedCornerManConfirmation(Row cornerMan) {
		clearScreen();
		System.out.println("Name: " + cornerMan.getCell(0).getStringCellValue());
		System.out.println("Training: " + cornerMan.getCell(1).getStringCellValue());
		System.out.println("Strategy: " + cornerMan.getCell(2).getStringCellValue());
		System.out.println("Attitude: " + cornerMan.getCell(3).getStringCellValue());
		System.out.println("Cuts/Swell: " + cornerMan.getCell(4).getStringCellValue());
		System.out.println("Boxer Rate: " + (int) cornerMan.getCell(5).getNumericCellValue());
		System.out.println("Availability: 1-" + (int) cornerMan.getCell(6).getNumericCellValue());
		System.out.println();
		System.out.println("(1) Confirm");
		System.out.println("(2) Cancel");
	}
	
	public void printDoctorSelection() {
		clearScreen();
		System.out.println("Select Doctor Type.");
		System.out.println("(1) Cautious");
		System.out.println("(2) Balanced Judgement");
		System.out.println("(3) Permissive");
		System.out.println("(4) Cancel");
	}
	
	public void printStartGameConfirmation() {
		clearScreen();
		System.out.println("Start Game?");
		System.out.println("(1) Yes");
		System.out.println("(2) No");
	}
	
	public void printGame(Game game) {
		clearScreen();
		String output = "";
		// 1
		output = game.getFighter1().getName() + " ";
		if (game.getFighter1().getNickname() != null) {
			output += "(" + game.getFighter1().getNickname() + ") ";
		}
		output += game.getFighter1().getOR() + game.getFighter1().getStyle();
		if (game.getFighter1().getStyle().equals("E")) {
			output += "(" + game.getFighter1().getCurrentStyle() + ")";
		}
		output = StringUtils.center(output, 45);
		System.out.print(output);
		output = game.getFighter2().getName() + " ";
		if (game.getFighter2().getNickname() != null) {
			output += "(" + game.getFighter2().getNickname() + ") ";
		}
		output += game.getFighter2().getOR() + game.getFighter2().getStyle();
		if (game.getFighter2().getStyle().equals("E")) {
			output += "(" + game.getFighter2().getCurrentStyle() + ")";
		}
		output = StringUtils.center(output, 45);
		System.out.print(output);
		if (game.getRingPosition().equals("Left Neutral Corner")) {
			System.out.print("----------                    ");
		} else if (game.getRingPosition().equals("Far Ropes")) {
			System.out.print("          ----------          ");
		} else if (game.getRingPosition().equals("Far Corner")) {
			System.out.print("                    ----------");
		} else {
			System.out.print("                              ");
		}
		System.out.println();
		// 2
		output = "";
		if (!game.getFighter1().getSpecial().isEmpty()) {
			output = "SPECIAL: ";
			for (String trait : game.getFighter1().getSpecial()) {
				output += trait + "; ";
			}
			output = output.substring(0, output.length() - 2);
		}
		System.out.print(StringUtils.center(output, 45));
		output = "";
		if (!game.getFighter2().getSpecial().isEmpty()) {
			output = "SPECIAL: ";
			for (String trait : game.getFighter2().getSpecial()) {
				output += trait + "; ";
			}
			output = output.substring(0, output.length() - 2);
		}
		System.out.print(StringUtils.center(output, 45));
		if (game.getRingPosition().equals("Left Neutral Corner")) {
			System.out.print("|  Left  |   Far       Far    ");
		} else if (game.getRingPosition().equals("Far Ropes")) {
			System.out.print("   Left   |  Far   |   Far    ");
		} else if (game.getRingPosition().equals("Far Corner")) {
			System.out.print("   Left      Far    |  Far   |");
		} else {
			System.out.print("   Left      Far       Far    ");
		}
		System.out.println();
		// 3
		output = game.getFighter1().isFresh() ? "FRESH" : "FATIGUED";
		System.out.print(StringUtils.center(output, 45));
		output = game.getFighter2().isFresh() ? "FRESH" : "FATIGUED";
		System.out.print(StringUtils.center(output, 45));
		if (game.getRingPosition().equals("Left Neutral Corner")) {
			System.out.print("|Neutral |  Ropes     Corner  ");
		} else if (game.getRingPosition().equals("Far Ropes")) {
			System.out.print(" Neutral  | Ropes  |  Corner  ");
		} else if (game.getRingPosition().equals("Far Corner")) {
			System.out.print(" Neutral    Ropes   | Corner |");
		} else {
			System.out.print(" Neutral    Ropes     Corner  ");
		}
		System.out.println();
		// 4
		output = StringUtils.rightPad("CFB/S: ", 7);
		output += StringUtils.center(Integer.toString(game.getFighter1().getCFB()) + "/" + game.getFighter1().getCFS(), 5);
		output += "    ";
		output += StringUtils.rightPad("DEF: ", 7);
		output += StringUtils.center(Integer.toString(game.getFighter1().getDEF()), 5);
		output += StringUtils.center("STRATEGIES", 17);
		System.out.print(output);
		output = StringUtils.rightPad("CFB/S: ", 7);
		output += StringUtils.center(Integer.toString(game.getFighter2().getCFB()) + "/" + game.getFighter2().getCFS(), 5);
		output += "    ";
		output += StringUtils.rightPad("DEF: ", 7);
		output += StringUtils.center(Integer.toString(game.getFighter2().getDEF()), 5);
		output += StringUtils.center("STRATEGIES", 17);
		System.out.print(output);
		if (game.getRingPosition().equals("Left Neutral Corner")) {
			System.out.print("| Corner |                    ");
		} else if (game.getRingPosition().equals("Far Ropes")) {
			System.out.print("  Corner  |        |          ");
		} else if (game.getRingPosition().equals("Far Corner")) {
			System.out.print("  Corner            |        |");
		} else {
			System.out.print("                              ");
		}
		System.out.println();
		// 5
		output = StringUtils.rightPad("AGG: ", 7);
		output += StringUtils.center(Integer.toString(game.getFighter1().getAGG()), 5);
		output += "    ";
		output += StringUtils.rightPad("KD1/2: ", 7);
		output += StringUtils.center(Integer.toString(game.getFighter1().getKD1()) + "/" + Integer.toString(game.getFighter1().getKD2()), 5);
		output += StringUtils.center(StringUtils.rightPad("FI: ", 7) + game.getFighter1().getStratFI(), 17);
		System.out.print(output);
		output = StringUtils.rightPad("AGG: ", 7);
		output += StringUtils.center(Integer.toString(game.getFighter2().getAGG()), 5);
		output += "    ";
		output += StringUtils.rightPad("KD1/2: ", 7);
		output += StringUtils.center(Integer.toString(game.getFighter2().getKD1()) + "/" + Integer.toString(game.getFighter2().getKD2()), 5);
		output += StringUtils.center(StringUtils.rightPad("FI: ", 7) + game.getFighter2().getStratFI(), 17);
		System.out.print(output);
		if (game.getRingPosition().equals("Left Neutral Corner")) {
			System.out.print("----------                    ");
		} else if (game.getRingPosition().equals("Far Ropes")) {
			System.out.print("          ----------          ");
		} else if (game.getRingPosition().equals("Far Corner")) {
			System.out.print("                    ----------");
		} else {
			System.out.print("                              ");
		}
		System.out.println();
		// 6
		output = StringUtils.rightPad("KI: ", 7);
		output += StringUtils.center(Integer.toString(game.getFighter1().getKI()), 5);
		output += "    ";
		output += StringUtils.rightPad("KO: ", 7);
		output += StringUtils.center(Integer.toString(game.getFighter1().getKO()), 5);
		output += StringUtils.center(StringUtils.rightPad("FO: ", 7) + game.getFighter1().getStratFO(), 17);
		System.out.print(output);
		output = StringUtils.rightPad("KI: ", 7);
		output += StringUtils.center(Integer.toString(game.getFighter2().getKI()), 5);
		output += "    ";
		output += StringUtils.rightPad("KO: ", 7);
		output += StringUtils.center(Integer.toString(game.getFighter2().getKO()), 5);
		output += StringUtils.center(StringUtils.rightPad("FO: ", 7) + game.getFighter2().getStratFO(), 17);
		System.out.print(output);
		if (game.getRingPosition().equals("Ring Center")) {
			System.out.print("          ----------          ");
		} else {
			System.out.print("                              ");
		}
		System.out.println();
		// 7
		output = StringUtils.rightPad("END: ", 7);
		output += StringUtils.center(Integer.toString(game.getFighter1().getEND()), 5);
		output += "    ";
		output += StringUtils.rightPad("CUT: ", 7);
		output += StringUtils.center(Integer.toString(game.getFighter1().getCUT()), 5);
		output += StringUtils.center(StringUtils.rightPad("CU: ", 7) + game.getFighter1().getStratCU(), 17);
		System.out.print(output);
		output = StringUtils.rightPad("END: ", 7);
		output += StringUtils.center(Integer.toString(game.getFighter2().getEND()), 5);
		output += "    ";
		output += StringUtils.rightPad("CUT: ", 7);
		output += StringUtils.center(Integer.toString(game.getFighter2().getCUT()), 5);
		output += StringUtils.center(StringUtils.rightPad("CU: ", 7) + game.getFighter2().getStratCU(), 17);
		System.out.print(output);
		if (game.getRingPosition().equals("Ring Center")) {
			System.out.print("          |        |          ");
		} else {
			System.out.print("                              ");
		}
		System.out.println();
		// 8
		output = StringUtils.rightPad("FOUL: ", 7);
		output += StringUtils.center(game.getFighter1().getFOUL(), 5);
		output += "    ";
		output += StringUtils.rightPad("CP: ", 7);
		output += StringUtils.center(Integer.toString(game.getFighter1().getCP()), 5);
		output += StringUtils.center(StringUtils.rightPad("KO: ", 7) + game.getFighter1().getStratKO(), 17);
		System.out.print(output);
		output = StringUtils.rightPad("FOUL: ", 7);
		output += StringUtils.center(game.getFighter2().getFOUL(), 5);
		output += "    ";
		output += StringUtils.rightPad("CP: ", 7);
		output += StringUtils.center(Integer.toString(game.getFighter2().getCP()), 5);
		output += StringUtils.center(StringUtils.rightPad("KO: ", 7) + game.getFighter2().getStratKO(), 17);
		System.out.print(output);
		if (game.getRingPosition().equals("Ring Center")) {
			System.out.print("          |  Ring  |          ");
		} else {
			System.out.print("             Ring             ");
		}
		System.out.println();
		// 9
		output = StringUtils.rightPad("Punches Landed: ", 23);
		output += StringUtils.center(1 + "-" + game.getFighter1().getPL() + " (" + game.getFighter1().getKP() + "KP)", 22);
		System.out.print(output);
		output = StringUtils.rightPad("Punches Landed: ", 23);
		output += StringUtils.center(1 + "-" + game.getFighter2().getPL() + " (" + game.getFighter2().getKP() + "KP)", 22);
		System.out.print(output);
		if (game.getRingPosition().equals("Ring Center")) {
			System.out.print("          | Center |          ");
		} else {
			System.out.print("            Center            ");
		}
		System.out.println();
		// 10
		output = StringUtils.rightPad("Punches Missed: ", 23);
		output += StringUtils.center(game.getFighter1().getPL() + 1 + "-" + game.getFighter1().getPM() + " | CPD " + game.getFighter1().getCPD() + "-" + game.getFighter1().getPM(), 22);
		System.out.print(output);
		output = StringUtils.rightPad("Punches Missed: ", 23);
		output += StringUtils.center(game.getFighter2().getPL() + 1 + "-" + game.getFighter2().getPM() + " | CPD " + game.getFighter2().getCPD() + "-" + game.getFighter2().getPM(), 22);
		System.out.print(output);
		if (game.getRingPosition().equals("Ring Center")) {
			System.out.print("          ----------          ");
		} else {
			System.out.print("                              ");
		}
		System.out.println();
		// 11
		output = StringUtils.rightPad("Clinching: ", 23);
		output += StringUtils.center(game.getFighter1().getPM() + 1 + "-" + game.getFighter1().getC(), 22);
		System.out.print(output);
		output = StringUtils.rightPad("Clinching: ", 23);
		output += StringUtils.center(game.getFighter2().getPM() + 1 + "-" + game.getFighter2().getC(), 22);
		System.out.print(output);
		if (game.getRingPosition().equals("Near Corner")) {
			System.out.print("----------                    ");
		} else if (game.getRingPosition().equals("Near Ropes")) {
			System.out.print("          ----------          ");
		} else if (game.getRingPosition().equals("Right Neutral Corner")) {
			System.out.print("                    ----------");
		} else {
			System.out.print("                              ");
		}
		System.out.println();
		// 12
		output = StringUtils.rightPad("Ring Movement: ", 23);
		output += StringUtils.center(game.getFighter1().getC() + 1 + "-" + 80, 22);
		System.out.print(output);
		output = StringUtils.rightPad("Ring Movement: ", 23);
		output += StringUtils.center(game.getFighter2().getC() + 1 + "-" + 80, 22);
		System.out.print(output);
		if (game.getRingPosition().equals("Near Corner")) {
			System.out.print("|        |            Right   ");
		} else if (game.getRingPosition().equals("Near Ropes")) {
			System.out.print("          |        |  Right   ");
		} else if (game.getRingPosition().equals("Right Neutral Corner")) {
			System.out.print("                    | Right  |");
		} else {
			System.out.print("                      Right   ");
		}
		System.out.println();
		// 13
		output = StringUtils.rightPad("Hitting Value: ", 15);
		output += StringUtils.center("3", 15);
		output += StringUtils.center("2", 15);
		System.out.print(output);
		output = StringUtils.rightPad("Hitting Value: ", 15);
		output += StringUtils.center("3", 15);
		output += StringUtils.center("2", 15);
		System.out.print(output);
		if (game.getRingPosition().equals("Near Corner")) {
			System.out.print("|  Near  |   Near    Neutral  ");
		} else if (game.getRingPosition().equals("Near Ropes")) {
			System.out.print("   Near   |  Near  | Neutral  ");
		} else if (game.getRingPosition().equals("Right Neutral Corner")) {
			System.out.print("   Near      Near   |Neutral |");
		} else {
			System.out.print("   Near      Near    Neutral  ");
		}
		System.out.println();
		// 14
		output = StringUtils.rightPad("Jab: ", 15);
		output += StringUtils.center(1 + "-" + game.getFighter1().getJab3(), 15);
		output += StringUtils.center(game.getFighter1().getJab3() + 1 + "-" + game.getFighter1().getJab2(), 15);
		System.out.print(output);
		output = StringUtils.rightPad("Jab: ", 15);
		output += StringUtils.center(1 + "-" + game.getFighter2().getJab3(), 15);
		output += StringUtils.center(game.getFighter2().getJab3() + 1 + "-" + game.getFighter2().getJab2(), 15);
		System.out.print(output);
		if (game.getRingPosition().equals("Near Corner")) {
			System.out.print("| Corner |  Ropes     Corner  ");
		} else if (game.getRingPosition().equals("Near Ropes")) {
			System.out.print("  Corner  | Ropes  |  Corner  ");
		} else if (game.getRingPosition().equals("Right Neutral Corner")) {
			System.out.print("  Corner    Ropes   | Corner |");
		} else {
			System.out.print("  Corner    Ropes     Corner  ");
		}
		System.out.println();
		// 15
		output = StringUtils.rightPad("Hook: ", 15);
		output += StringUtils.center(game.getFighter1().getJab2() + 1 + "-" + game.getFighter1().getHook3(), 15);
		output += StringUtils.center(game.getFighter1().getHook3() + 1 + "-" + game.getFighter1().getHook2(), 15);
		System.out.print(output);
		output = StringUtils.rightPad("Hook: ", 15);
		output += StringUtils.center(game.getFighter2().getJab2() + 1 + "-" + game.getFighter2().getHook3(), 15);
		output += StringUtils.center(game.getFighter2().getHook3() + 1 + "-" + game.getFighter2().getHook2(), 15);
		System.out.print(output);
		if (game.getRingPosition().equals("Near Corner")) {
			System.out.print("----------                    ");
		} else if (game.getRingPosition().equals("Near Ropes")) {
			System.out.print("          ----------          ");
		} else if (game.getRingPosition().equals("Right Neutral Corner")) {
			System.out.print("                    ----------");
		} else {
			System.out.print("                              ");
		}
		System.out.println();
		// 16
		output = StringUtils.rightPad("Cross: ", 15);
		output += StringUtils.center(game.getFighter1().getHook2() + 1 + "-" + game.getFighter1().getCross3(), 15);
		output += StringUtils.center(game.getFighter1().getCross3() + 1 + "-" + game.getFighter1().getCross2(), 15);
		System.out.print(output);
		output = StringUtils.rightPad("Cross: ", 15);
		output += StringUtils.center(game.getFighter2().getHook2() + 1 + "-" + game.getFighter2().getCross3(), 15);
		output += StringUtils.center(game.getFighter2().getCross3() + 1 + "-" + game.getFighter2().getCross2(), 15);
		System.out.print(output);
		output = StringUtils.center("Referee: " + game.getReferee().getName(), 30);
		System.out.print(output);
		System.out.println();
		// 17
		output = StringUtils.rightPad("Combination: ", 15);
		output += StringUtils.center(game.getFighter1().getCross2() + 1 + "-" + game.getFighter1().getCombination3(), 15);
		output += StringUtils.center(game.getFighter1().getCombination3() + 1 + "-" + game.getFighter1().getCombination2(), 15);
		System.out.print(output);
		output = StringUtils.rightPad("Combination: ", 15);
		output += StringUtils.center(game.getFighter2().getCross2() + 1 + "-" + game.getFighter2().getCombination3(), 15);
		output += StringUtils.center(game.getFighter2().getCombination3() + 1 + "-" + game.getFighter2().getCombination2(), 15);
		System.out.print(output);
		String fouls = game.getReferee().getFouls().equals("Very Lenient") ? "V. L." : game.getReferee().getFouls();
		String tko = game.getReferee().getStoppage().equals("Very Lenient") ? "V. L." : game.getReferee().getStoppage();
		output = StringUtils.center("Fouls: " + fouls + ", TKO: " + tko, 30);
		System.out.print(output);
		System.out.println();
		// 18
		output = StringUtils.rightPad("Uppercut: ", 15);
		output += StringUtils.center(game.getFighter1().getCombination2() + 1 + "-" + game.getFighter1().getUppercut3(), 15);
		output += StringUtils.center(game.getFighter1().getUppercut3() + 1 + "-" + 80, 15);
		System.out.print(output);
		output = StringUtils.rightPad("Uppercut: ", 15);
		output += StringUtils.center(game.getFighter2().getCombination2() + 1 + "-" + game.getFighter2().getUppercut3(), 15);
		output += StringUtils.center(game.getFighter2().getUppercut3() + 1 + "-" + 80, 15);
		System.out.print(output);
		String[] nameWords1 = game.getFighter1().getName().split(" ");
		String firstInitial1 = nameWords1[0].substring(0, 1);
		String lastName1;
		if (nameWords1[nameWords1.length - 1].matches("([IVX])+$") || nameWords1[nameWords1.length - 1].endsWith(".")) {
			lastName1 = nameWords1[nameWords1.length - 2];
		} else {
			lastName1 = nameWords1[nameWords1.length - 1];
		}
		String[] nameWords2 = game.getFighter2().getName().split(" ");
		String firstInitial2 = nameWords2[0].substring(0, 1);
		String lastName2;
		if (nameWords2[nameWords2.length - 1].matches("([IVX])+$") || nameWords2[nameWords2.length - 1].endsWith(".")) {
			lastName2 = nameWords2[nameWords2.length - 2];
		} else {
			lastName2 = nameWords2[nameWords2.length - 1];
		}
		String fighter1ShortName = firstInitial1 + ". " + lastName1;
		String fighter2ShortName = firstInitial2 + ". " + lastName2;
		output = StringUtils.center(fighter1ShortName, 15);
		output += StringUtils.center(fighter2ShortName, 15);
		System.out.print(output);
		System.out.println();
		// 19
		
		output = StringUtils.center("FIGHTER", 22);
		output += StringUtils.center("J#", 4);
		for (int i = 1; i <= 15; i++) {
			output += StringUtils.center(i + "", 4);
		}
		output += StringUtils.center("T", 4);
		System.out.print(output);
		if (game.getAttacker() == null || game.isInControlCheck()) {
			output = StringUtils.center("CONTROL", 30);
		} else {
			output = game.getAttacker().equals(game.getFighter1()) ? "    **    " : "          ";
			output += StringUtils.center("CONTROL", 10);
			output += game.getAttacker().equals(game.getFighter2()) ? "    **    " : "          ";
		}
		System.out.print(output);
		System.out.println();
		// 20
		output = StringUtils.center("", 22);
		output += StringUtils.center("J1", 4);
		int sum = 0;
		for (int i = 0; i <= 14; i++) {
			output += StringUtils.center(game.getFighter1().getScore()[0][i] + "", 4);
			sum += game.getFighter1().getScore()[0][i];
		}
		output += StringUtils.center(sum + "", 4);
		output = output.replace(" 0 ", "   ");
		System.out.print(output);
		output = StringUtils.center(game.getFighter1().getPoints() + "", 10);
		output += StringUtils.center("POINTS", 10);
		output += StringUtils.center(game.getFighter2().getPoints() + "", 10);
		System.out.print(output);
		System.out.println();
		// 21
		output = StringUtils.center(game.getFighter1().getName(), 22);
		output += StringUtils.center("J2", 4);
		sum = 0;
		for (int i = 0; i <= 14; i++) {
			output += StringUtils.center(game.getFighter1().getScore()[1][i] + "", 4);
			sum += game.getFighter1().getScore()[1][i];
		}
		output += StringUtils.center(sum + "", 4);
		output = output.replace(" 0 ", "   ");
		System.out.print(output);
		output = StringUtils.center(game.getFighter1().getKnockdowns() + "", 10);
		output += StringUtils.center("KNOCKDOWNS", 10);
		output += StringUtils.center(game.getFighter2().getKnockdowns() + "", 10);
		System.out.print(output);
		System.out.println();
		// 22
		output = StringUtils.center("", 22);
		output += StringUtils.center("J3", 4);
		sum = 0;
		for (int i = 0; i <= 14; i++) {
			output += StringUtils.center(game.getFighter1().getScore()[2][i] + "", 4);
			sum += game.getFighter1().getScore()[2][i];
		}
		output += StringUtils.center(sum + "", 4);
		output = output.replace(" 0 ", "   ");
		System.out.print(output);
		output = StringUtils.center(game.getFighter1().getDamage() + "", 10);
		output += StringUtils.center("DAMAGE", 10);
		output += StringUtils.center(game.getFighter2().getDamage() + "", 10);
		System.out.print(output);
		System.out.println();
		// 23
		output = StringUtils.center("", 22);
		output += StringUtils.center("J1", 4);
		sum = 0;
		for (int i = 0; i <= 14; i++) {
			output += StringUtils.center(game.getFighter2().getScore()[0][i] + "", 4);
			sum += game.getFighter2().getScore()[0][i];
		}
		output += StringUtils.center(sum + "", 4);
		output = output.replace(" 0 ", "   ");
		System.out.print(output);
		// TODO is this correct?
		output = StringUtils.center(game.getFighter1().getWarnings() + "", 10);
		output += StringUtils.center("FOULS", 10);
		output += StringUtils.center(game.getFighter2().getWarnings() + "", 10);
		System.out.print(output);
		System.out.println();
		// 24
		output = StringUtils.center(game.getFighter2().getName(), 22);
		output += StringUtils.center("J2", 4);
		sum = 0;
		for (int i = 0; i <= 14; i++) {
			output += StringUtils.center(game.getFighter2().getScore()[1][i] + "", 4);
			sum += game.getFighter2().getScore()[1][i];
		}
		output += StringUtils.center(sum + "", 4);
		output = output.replace(" 0 ", "   ");
		System.out.print(output);
		nameWords1 = game.getFighter1().getTrainer().getName().split(" ");
		firstInitial1 = nameWords1[0].substring(0, 1);
		if (nameWords1[nameWords1.length - 1].matches("([IVX])+$") || nameWords1[nameWords1.length - 1].endsWith(".")) {
			lastName1 = nameWords1[nameWords1.length - 2];
		} else {
			lastName1 = nameWords1[nameWords1.length - 1];
		}
		nameWords2 = game.getFighter2().getTrainer().getName().split(" ");
		firstInitial2 = nameWords2[0].substring(0, 1);
		if (nameWords2[nameWords2.length - 1].matches("([IVX])+$") || nameWords2[nameWords2.length - 1].endsWith(".")) {
			lastName2 = nameWords2[nameWords2.length - 2];
		} else {
			lastName2 = nameWords2[nameWords2.length - 1];
		}
		output = StringUtils.center(firstInitial1 + ". " + lastName1, 13);
		output += StringUtils.center("Tr", 4);
		output += StringUtils.center(firstInitial2 + ". " + lastName2, 13);
		System.out.print(output);
		System.out.println();
		// 25
		output = StringUtils.center("", 22);
		output += StringUtils.center("J3", 4);
		sum = 0;
		for (int i = 0; i <= 14; i++) {
			output += StringUtils.center(game.getFighter2().getScore()[2][i] + "", 4);
			sum += game.getFighter2().getScore()[2][i];
		}
		output += StringUtils.center(sum + "", 4);
		output = output.replace(" 0 ", "   ");
		System.out.print(output);
		nameWords1 = game.getFighter1().getCutMan().getName().split(" ");
		firstInitial1 = nameWords1[0].substring(0, 1);
		if (nameWords1[nameWords1.length - 1].matches("([IVX])+$") || nameWords1[nameWords1.length - 1].endsWith(".")) {
			lastName1 = nameWords1[nameWords1.length - 2];
		} else {
			lastName1 = nameWords1[nameWords1.length - 1];
		}
		nameWords2 = game.getFighter2().getCutMan().getName().split(" ");
		firstInitial2 = nameWords2[0].substring(0, 1);
		if (nameWords2[nameWords2.length - 1].matches("([IVX])+$") || nameWords2[nameWords2.length - 1].endsWith(".")) {
			lastName2 = nameWords2[nameWords2.length - 2];
		} else {
			lastName2 = nameWords2[nameWords2.length - 1];
		}
		output = StringUtils.center(firstInitial1 + ". " + lastName1, 13);
		output += StringUtils.center("CM", 4);
		output += StringUtils.center(firstInitial2 + ". " + lastName2, 13);
		System.out.print(output);
		System.out.println();
		// 26-29
		System.out.println(messageLog.getMessages().get(0));
		System.out.println(messageLog.getMessages().get(1));
		System.out.println(messageLog.getMessages().get(2));
		System.out.println(messageLog.getMessages().get(3));
		// 30 is for input, keep blank
		
		// wait 2 seconds before continuing
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
	
}
