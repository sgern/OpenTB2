package com.sgern.OpenTB2;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Options {
	
	private int maxRounds;
	private boolean injuriesEnabled;
	private boolean cfConversionEnabled;
	private boolean aggressivenessEnabled;
	private boolean foulDamageEnabled;
	private boolean headbuttCutEnabled;
	private String advancedTimingOption;
	private boolean extendRoundEnabled;
	private boolean mandatoryEightCountEnabled;
	private boolean savedByTheBellEnabled;
	private boolean threeKnockdownRuleEnabled;
	private boolean southpawEnabled;
	private boolean missPenaltyEnabled;
	private boolean kiTimingEnabled;
	private boolean advancedClinchingEnabled;
	private boolean ratedRefereeEnabled;
	private boolean ratedCornerMenEnabled;
	private String refereeErrorOption;
	private boolean strategiesEnabled;
	private boolean fighterTraitsEnabled;
	private boolean throwInTheTowelEnabled;
	
	public String getOption(String option) {
		try {
			List<String> lines = Files.readAllLines(Paths.get("options.ini"));
			Iterator<String> linesIterator = lines.listIterator();
			while (linesIterator.hasNext()) {
				String line = linesIterator.next();
				if (line.startsWith(option)) {
					return line.split("=")[1];
				}
			}
			System.err.println("ERROR: Option not found!");
			System.exit(0);
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: options.ini not found!");
		} catch (IOException e) {
			System.err.println("ERROR: Unable to read/write options.ini!");
		}
		return null;
	}
	
	private void setOption(String option, String value) {
		try {
			List<String> lines = Files.readAllLines(Paths.get("options.ini"));
			List<String> newLines = new ArrayList<>();
			Iterator<String> linesIterator = lines.listIterator();
			while (linesIterator.hasNext()) {
				String line = linesIterator.next();
				if (line.startsWith(option)) {
					linesIterator.remove();
					if (value.equals("true")) {
						value = "True";
					} else if (value.equals("false")) {
						value = "False";
					}
					newLines.add(option + "=" + value);
					break;
				}
			}
			newLines.addAll(lines);
			Files.write(Paths.get("options.ini"), newLines, StandardCharsets.UTF_8);
		} catch (FileNotFoundException e) {
			System.err.println("ERROR: options.ini not found!");
		} catch (IOException e) {
			System.err.println("ERROR: Unable to read/write options.ini!");
		}
	}
	
	public Options() {
		maxRounds = Integer.parseInt(getOption("NumberOfRounds"));
		injuriesEnabled = Boolean.parseBoolean(getOption("Injuries"));
		cfConversionEnabled = Boolean.parseBoolean(getOption("CFConversion"));
		aggressivenessEnabled = Boolean.parseBoolean(getOption("Aggressiveness"));
		foulDamageEnabled = Boolean.parseBoolean(getOption("FoulDamage"));
		headbuttCutEnabled = Boolean.parseBoolean(getOption("FoulTableHeadButt"));
		advancedTimingOption = getOption("AdvancedTiming");
		extendRoundEnabled = Boolean.parseBoolean(getOption("CardUsageToExtendTheRound"));
		mandatoryEightCountEnabled = Boolean.parseBoolean(getOption("MandatoryEightCount"));
		savedByTheBellEnabled = Boolean.parseBoolean(getOption("SavedByTheBell"));
		threeKnockdownRuleEnabled = Boolean.parseBoolean(getOption("ThreeKnockdownRule"));
		southpawEnabled = Boolean.parseBoolean(getOption("Southpaw"));
		missPenaltyEnabled = Boolean.parseBoolean(getOption("MissingPunchesPenalty"));
		kiTimingEnabled = Boolean.parseBoolean(getOption("KillerInstinctAndRoundTiming"));
		advancedClinchingEnabled = Boolean.parseBoolean(getOption("AdvancedClinching"));
		ratedRefereeEnabled = Boolean.parseBoolean(getOption("RatedReferee"));
		ratedCornerMenEnabled = Boolean.parseBoolean(getOption("RatedCornerMen"));
		refereeErrorOption = getOption("RefereeErrorTable");
		strategiesEnabled = Boolean.parseBoolean(getOption("Strategies"));
		fighterTraitsEnabled = Boolean.parseBoolean(getOption("FighterTraits"));
		throwInTheTowelEnabled = Boolean.parseBoolean(getOption("ThrowInTheTowel"));
	}
	
	public int getMaxRounds() {
		return maxRounds;
	}

	public void setMaxRounds(int maxRounds) {
		this.maxRounds = maxRounds;
		setOption("NumberOfRounds", Integer.toString(maxRounds));
	}

	public boolean isInjuriesEnabled() {
		return injuriesEnabled;
	}

	public void setInjuriesEnabled(boolean injuriesEnabled) {
		this.injuriesEnabled = injuriesEnabled;
		setOption("Injuries", Boolean.toString(injuriesEnabled));
	}

	public boolean isCFConversionEnabled() {
		return cfConversionEnabled;
	}
	
	public void setCFConversionEnabled(boolean cfConversionEnabled) {
		this.cfConversionEnabled = cfConversionEnabled;
		setOption("CFConversion", Boolean.toString(cfConversionEnabled));
	}

	public boolean isAggressivenessEnabled() {
		return aggressivenessEnabled;
	}

	public void setAggressivenessEnabled(boolean aggressivenessEnabled) {
		this.aggressivenessEnabled = aggressivenessEnabled;
		setOption("Aggressiveness", Boolean.toString(aggressivenessEnabled));
	}

	public boolean isFoulDamageEnabled() {
		return foulDamageEnabled;
	}

	public void setFoulDamageEnabled(boolean foulDamageEnabled) {
		this.foulDamageEnabled = foulDamageEnabled;
		setOption("FoulDamage", Boolean.toString(foulDamageEnabled));
	}

	public boolean isHeadbuttCutEnabled() {
		return headbuttCutEnabled;
	}

	public void setHeadbuttCutEnabled(boolean headbuttCutEnabled) {
		this.headbuttCutEnabled = headbuttCutEnabled;
		setOption("FoulTableHeadButt", Boolean.toString(headbuttCutEnabled));
	}

	public String getAdvancedTimingOption() {
		return advancedTimingOption;
	}

	public void setAdvancedTimingOption(String advancedTimingOption) {
		this.advancedTimingOption = advancedTimingOption;
		setOption("AdvancedTiming", advancedTimingOption);
	}

	public boolean isExtendRoundEnabled() {
		return extendRoundEnabled;
	}

	public void setExtendRoundEnabled(boolean extendRoundEnabled) {
		this.extendRoundEnabled = extendRoundEnabled;
		setOption("CardUsageToExtendTheRound", Boolean.toString(extendRoundEnabled));
	}

	public boolean isMandatoryEightCountEnabled() {
		return mandatoryEightCountEnabled;
	}

	public void setMandatoryEightCountEnabled(boolean mandatoryEightCountEnabled) {
		this.mandatoryEightCountEnabled = mandatoryEightCountEnabled;
		setOption("MandatoryEightCount", Boolean.toString(mandatoryEightCountEnabled));
	}

	public boolean isSavedByTheBellEnabled() {
		return savedByTheBellEnabled;
	}

	public void setSavedByTheBellEnabled(boolean savedByTheBellEnabled) {
		this.savedByTheBellEnabled = savedByTheBellEnabled;
		setOption("SavedByTheBell", Boolean.toString(savedByTheBellEnabled));
	}

	public boolean isThreeKnockdownRuleEnabled() {
		return threeKnockdownRuleEnabled;
	}

	public void setThreeKnockdownRuleEnabled(boolean threeKnockdownRuleEnabled) {
		this.threeKnockdownRuleEnabled = threeKnockdownRuleEnabled;
		setOption("ThreeKnockdownRule", Boolean.toString(threeKnockdownRuleEnabled));
	}

	public boolean isSouthpawEnabled() {
		return southpawEnabled;
	}

	public void setSouthpawEnabled(boolean southpawEnabled) {
		this.southpawEnabled = southpawEnabled;
		setOption("Southpaw", Boolean.toString(southpawEnabled));
	}

	public boolean isMissPenaltyEnabled() {
		return missPenaltyEnabled;
	}

	public void setMissPenaltyEnabled(boolean missPenaltyEnabled) {
		this.missPenaltyEnabled = missPenaltyEnabled;
		setOption("MissingPunchesPenalty", Boolean.toString(missPenaltyEnabled));
	}

	public boolean isKITimingEnabled() {
		return kiTimingEnabled;
	}

	public void setKITimingEnabled(boolean kiTimingEnabled) {
		this.kiTimingEnabled = kiTimingEnabled;
		setOption("KillerInstinctAndRoundTiming", Boolean.toString(kiTimingEnabled));
	}

	public boolean isAdvancedClinchingEnabled() {
		return advancedClinchingEnabled;
	}

	public void setAdvancedClinchingEnabled(boolean advancedClinchingEnabled) {
		this.advancedClinchingEnabled = advancedClinchingEnabled;
		setOption("AdvancedClinching", Boolean.toString(advancedClinchingEnabled));
	}

	public boolean isRatedRefereeEnabled() {
		return ratedRefereeEnabled;
	}

	public void setRatedRefereeEnabled(boolean ratedRefereeEnabled) {
		this.ratedRefereeEnabled = ratedRefereeEnabled;
		setOption("RatedReferee", Boolean.toString(ratedRefereeEnabled));
	}

	public boolean isRatedCornerMenEnabled() {
		return ratedCornerMenEnabled;
	}

	public void setRatedCornerMenEnabled(boolean ratedCornerMenEnabled) {
		this.ratedCornerMenEnabled = ratedCornerMenEnabled;
		setOption("RatedCornerMen", Boolean.toString(ratedCornerMenEnabled));
	}

	public String getRefereeErrorOption() {
		return refereeErrorOption;
	}

	public void setRefereeErrorOption(String refereeErrorOption) {
		this.refereeErrorOption = refereeErrorOption;
		setOption("RefereeErrorTable", refereeErrorOption);
	}

	public boolean isStrategiesEnabled() {
		return strategiesEnabled;
	}

	public void setStrategyEnabled(boolean strategiesEnabled) {
		this.strategiesEnabled = strategiesEnabled;
		setOption("Strategies", Boolean.toString(strategiesEnabled));
	}

	public boolean isFighterTraitsEnabled() {
		return fighterTraitsEnabled;
	}

	public void setFighterTraitsEnabled(boolean fighterTraitsEnabled) {
		this.fighterTraitsEnabled = fighterTraitsEnabled;
		setOption("FighterTraits", Boolean.toString(fighterTraitsEnabled));
	}

	public boolean isThrowInTheTowelEnabled() {
		return throwInTheTowelEnabled;
	}

	public void setThrowInTheTowelEnabled(boolean throwInTheTowelEnabled) {
		this.throwInTheTowelEnabled = throwInTheTowelEnabled;
		setOption("ThrowInTheTowel", Boolean.toString(throwInTheTowelEnabled));
	}
	
	
}
