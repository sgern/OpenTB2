package com.sgern.OpenTB2;

import java.util.Set;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Fighter {
	
	private String name;
	private String nickname;
	private int or;
	private String style;
	private String currentStyle;
	private List<String> special;
	private int cfbFresh, cfbFatigued, cfbMod = 0;
	private int cfsFresh, cfsFatigued, cfsMod = 0;
	private int defFresh, defFatigued, defMod = 0;
	private int aggFresh, aggFatigued, aggMod = 0;
	private int kd1Fresh, kd1Fatigued, kd1Mod = 0;
	private int kd2Fresh, kd2Fatigued, kd2Mod = 0;
	private int kiFresh, kiFatigued, kiMod = 0;
	private int koFresh, koFatigued, koMod = 0;
	private int end, endMod = 0, endDrain = 0;
	private int cutFresh, cutFatigued, cutMod = 0;
	private String foulFresh, foulFatigued;
	private int cpFresh, cpFatigued, cpMod = 0;
	private int stratFI, stratFO, stratCU, stratKO;
	private int plFresh, plFatigued, plMod = 0;
	private int kpFresh, kpFatigued, kpMod = 0;
	private int pmFresh, pmFatigued;
	private int cpdFresh, cpdFatigued;
	private int cFresh, cFatigued;
	private int jab3;
	private int jab2;
	private int hook3;
	private int hook2;
	private int cross3;
	private int cross2;
	private int combination3;
	private int combination2;
	private int uppercut3;
	private boolean fresh = true;
	private int points = 0, pointsTwoRounds = 0, pointsThreeRounds = 0;
	private int condition = 0;
	private boolean condition3 = false;
	private int damage = 0;
	private int warnings = 0;
	private int pointsLost = 0;
	private int[][] score = new int[3][15];
	private int knockdowns = 0;
	private int kdc = 0;
	private boolean carryover = false;
	private CornerMan cutMan, trainer;
	private List<Cut> cutList = new ArrayList<>();
	private Set<String> cutTypes = new HashSet<>();
	private Set<Integer> injuries = new HashSet<>();
	private boolean nextRoundCheck = false;
	private boolean resolvedInjury8 = false;
	private boolean resolvedInjury9 = false;
	private int special6 = 0;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	
	public int getOR() {
		return or;
	}
	
	public void setOR(int or) {
		this.or = or;
	}
	
	public String getStyle() {
		return style;
	}
	
	public void setStyle(String style) {
		this.style = style;
	}
	
	public String getCurrentStyle() {
		return currentStyle;
	}

	public void setCurrentStyle(String currentStyle) {
		this.currentStyle = currentStyle;
	}

	public List<String> getSpecial() {
		return special;
	}
	
	public void setSpecial(List<String> special) {
		this.special = special;
	}
	
	public int getCFB() {
		return fresh ? cfbFresh + cfbMod : cfbFatigued + cfbMod;
	}
	
	public void setCFBFresh(int cfbFresh) {
		this.cfbFresh = cfbFresh;
	}
	
	public void setCFBFatigued(int cfbFatigued) {
		this.cfbFatigued = cfbFatigued;
	}
	
	public void modifyCFB(int i) {
		cfbMod += i;
	}
	
	public int getCFS() {
		return fresh ? cfsFresh + cfsMod : cfsFatigued + cfsMod;
	}
	
	public void setCFSFresh(int cfsFresh) {
		this.cfsFresh = cfsFresh;
	}
	
	public void setCFSFatigued(int cfsFatigued) {
		this.cfsFatigued = cfsFatigued;
	}
	
	public void modifyCFS(int i) {
		cfsMod += i;
	}
	
	public int getDEF() {
		return fresh ? defFresh + defMod : defFatigued + defMod;
	}
	
	public void setDEFFresh(int defFresh) {
		this.defFresh = defFresh;
	}
	
	public void setDEFFatigued(int defFatigued) {
		this.defFatigued = defFatigued;
	}
	
	public void modifyDEF(int i) {
		defMod += i;
	}
	
	public int getAGG() {
		return fresh ? aggFresh + aggMod : aggFatigued + aggMod;
	}
	
	public void setAGGFresh(int aggFresh) {
		this.aggFresh = aggFresh;
	}
	
	public void setAGGFatigued(int aggFatigued) {
		this.aggFatigued = aggFatigued;
	}
	
	public void modifyAGG(int i) {
		aggMod += i;
	}
	
	public int getKD1() {
		return fresh ? kd1Fresh + kd1Mod : kd1Fatigued + kd1Mod;
	}
	
	public void setKD1Fresh(int kd1Fresh) {
		this.kd1Fresh = kd1Fresh;
	}
	
	public void setKD1Fatigued(int kd1Fatigued) {
		this.kd1Fatigued = kd1Fatigued;
	}
	
	public void modifyKD1(int i) {
		kd1Mod += i;
	}
	
	public int getKD2() {
		return fresh ? kd2Fresh + kd2Mod : kd2Fatigued + kd2Mod;
	}
	
	public void setKD2Fresh(int kd2Fresh) {
		this.kd2Fresh = kd2Fresh;
	}
	
	public void setKD2Fatigued(int kd2Fatigued) {
		this.kd2Fatigued = kd2Fatigued;
	}
	
	public void modifyKD2(int i) {
		kd2Mod += i;
	}
	
	public int getKI() {
		return fresh ? kiFresh + kiMod : kiFatigued + kiMod;
	}
	
	public void setKIFresh(int kiFresh) {
		this.kiFresh = kiFresh;
	}
	
	public void setKIFatigued(int kiFatigued) {
		this.kiFatigued = kiFatigued;
	}
	
	public void modifyKI(int i) {
		kiMod += i;
	}
	
	public int getKO() {
		return fresh ? koFresh + koMod : koFatigued + koMod;
	}
	
	public void setKOFresh(int koFresh) {
		this.koFresh = koFresh;
	}
	
	public void setKOFatigued(int koFatigued) {
		this.koFatigued = koFatigued;
	}
	
	public void modifyKO(int i) {
		koMod += i;
	}
	
	public int getEND() {
		return end + endMod;
	}
	
	public void setEND(int end) {
		this.end = end;
	}

	public void modifyEND(int i) {
		endMod += i;
	}
	
	public void zeroEND() {
		endMod = -end;
	}
	
	public int getENDDrain() {
		return endDrain;
	}

	public void modifyENDDrain(int i) {
		this.endDrain += i;
	}
	
	public int getCUT() {
		return fresh ? cutFresh + cutMod : cutFatigued + cutMod;
	}
	
	public void setCUTFresh(int cutFresh) {
		this.cutFresh = cutFresh;
	}
	
	public void setCUTFatigued(int cutFatigued) {
		this.cutFatigued = cutFatigued;
	}
	
	public void modifyCUT(int i) {
		cutMod += i;
	}
	
	public String getFOUL() {
		return fresh ? foulFresh : foulFatigued;
	}
	
	public void setFOULFresh(String foulFresh) {
		this.foulFresh = foulFresh;
	}
	
	public void setFOULFatigued(String foulFatigued) {
		this.foulFatigued = foulFatigued;
	}
	
	public int getCP() {
		return fresh ? cpFresh + cpMod : cpFatigued + cpMod;
	}
	
	public void setCPFresh(int cpFresh) {
		this.cpFresh = cpFresh;
	}
	
	public void setCPFatigued(int cpFatigued) {
		this.cpFatigued = cpFatigued;
	}
	
	public void modifyCP(int i) {
		cpMod += i;
	}

	public int getStratFI() {
		return stratFI;
	}

	public void setStratFI(int stratFI) {
		this.stratFI = stratFI;
	}

	public int getStratFO() {
		return stratFO;
	}

	public void setStratFO(int stratFO) {
		this.stratFO = stratFO;
	}

	public int getStratCU() {
		return stratCU;
	}

	public void setStratCU(int stratCU) {
		this.stratCU = stratCU;
	}

	public int getStratKO() {
		return stratKO;
	}

	public void setStratKO(int stratKO) {
		this.stratKO = stratKO;
	}
	
	public int getPL() {
		return fresh ? plFresh + plMod : plFatigued + plMod;
	}
	
	public void setPLFresh(int plFresh) {
		this.plFresh = plFresh;
	}
	
	public void setPLFatigued(int plFatigued) {
		this.plFatigued = plFatigued;
	}
	
	public void modifyPL(int i) {
		plMod += i;
	}
	
	public int getKP() {
		return fresh ? kpFresh + kpMod : kpFatigued + kpMod;
	}
	
	public void setKPFresh(int kpFresh) {
		this.kpFresh = kpFresh;
	}
	
	public void setKPFatigued(int kpFatigued) {
		this.kpFatigued = kpFatigued;
	}
	
	public void modifyKP(int i) {
		kpMod += i;
	}
	
	public int getPM() {
		return fresh ? pmFresh : pmFatigued;
	}
	
	public void setPMFresh(int pmFresh) {
		this.pmFresh = pmFresh;
	}
	
	public void setPMFatigued(int pmFatigued) {
		this.pmFatigued = pmFatigued;
	}
	
	public int getCPD() {
		return fresh ? cpdFresh : cpdFatigued;
	}
	
	public void setCPDFresh(int cpdFresh) {
		this.cpdFresh = cpdFresh;
	}
	
	public void setCPDFatigued(int cpdFatigued) {
		this.cpdFatigued = cpdFatigued;
	}
	
	public int getC() {
		return fresh ? cFresh : cFatigued;
	}
	
	public void setCFresh(int cFresh) {
		this.cFresh = cFresh;
	}
	
	public void setCFatigued(int cFatigued) {
		this.cFatigued = cFatigued;
	}

	public int getJab3() {
		return jab3;
	}

	public void setJab3(int jab3) {
		this.jab3 = jab3;
	}

	public int getJab2() {
		return jab2;
	}

	public void setJab2(int jab2) {
		this.jab2 = jab2;
	}

	public int getHook3() {
		return hook3;
	}

	public void setHook3(int hook3) {
		this.hook3 = hook3;
	}

	public int getHook2() {
		return hook2;
	}

	public void setHook2(int hook2) {
		this.hook2 = hook2;
	}

	public int getCross3() {
		return cross3;
	}

	public void setCross3(int cross3) {
		this.cross3 = cross3;
	}

	public int getCross2() {
		return cross2;
	}

	public void setCross2(int cross2) {
		this.cross2 = cross2;
	}

	public int getCombination3() {
		return combination3;
	}

	public void setCombination3(int combination3) {
		this.combination3 = combination3;
	}

	public int getCombination2() {
		return combination2;
	}

	public void setCombination2(int combination2) {
		this.combination2 = combination2;
	}

	public int getUppercut3() {
		return uppercut3;
	}

	public void setUppercut3(int uppercut3) {
		this.uppercut3 = uppercut3;
	}

	public CornerMan getCutMan() {
		return cutMan;
	}

	public void setCutMan(CornerMan cutMan) {
		this.cutMan = cutMan;
	}

	public CornerMan getTrainer() {
		return trainer;
	}

	public void setTrainer(CornerMan trainer) {
		this.trainer = trainer;
	}

	public boolean isFresh() {
		return fresh;
	}

	public void setFresh(boolean fresh) {
		this.fresh = fresh;
	}

	public int getPoints() {
		return points;
	}

	public int getPointsTwoRounds() {
		return pointsTwoRounds;
	}

	public int getPointsThreeRounds() {
		return pointsThreeRounds;
	}
	
	public void scorePoints(int points) {
		if (injuries.contains(6)) {
			points--;
		} else if ((injuries.contains(5) && points == 2) || (injuries.contains(7) && points == 3)) {
			points--;
		}
		this.points += points;
		pointsTwoRounds += points;
		pointsThreeRounds += points;
	}
	
	public void clearPoints() {
		pointsThreeRounds = pointsTwoRounds;
		pointsTwoRounds = points;
		points = 0;
	}

	public int getCondition() {
		return condition;
	}

	public void setCondition(int condition) {
		this.condition = condition;
	}

	public boolean isCondition3() {
		return condition3;
	}

	public void setCondition3(boolean condition3) {
		this.condition3 = condition3;
	}

	public int getDamage() {
		return damage;
	}
	
	public void modifyDamage(int damage) {
		this.damage += damage;
	}

	public int getWarnings() {
		return warnings;
	}

	public void addWarning() {
		this.warnings++;
	}

	public int getPointsLost() {
		return pointsLost;
	}

	public void addPointLost() {
		this.pointsLost++;
	}

	public void clearPointsLost() {
		this.pointsLost = 0;
	}

	public int[][] getScore() {
		return score;
	}
	
	public void setScore(int judge, int roundNumber, int score) {
		this.score[judge - 1][roundNumber - 1] = score - pointsLost;
	}

	public int getKnockdowns() {
		return knockdowns;
	}

	public void addKnockdown() {
		this.knockdowns++;
	}

	public void clearKnockdowns() {
		this.knockdowns = 0;
	}

	public int getKDC() {
		return kdc;
	}

	public void addKDC(int kdc) {
		this.kdc += kdc;
	}

	public void clearKDC() {
		kdc = 0;
	}

	public boolean isCarryover() {
		return carryover;
	}

	public void setCarryover(boolean carryover) {
		this.carryover = carryover;
	}

	public List<Cut> getCutList() {
		return cutList;
	}
	
	public void addCut(Cut cut) {
		cutList.add(cut);
	}
	
	public void removeCut(Cut cut) {
		cutList.remove(cut);
	}
	
	public void worsenCut(Cut cut) {
		cutList.set(cutList.indexOf(cut), new Cut(cut.getCutType(), cut.getDP() + 1));
	}

	public Set<String> getCutTypes() {
		return cutTypes;
	}

	public void addCutType(String cutType) {
		this.cutTypes.add(cutType);
	}

	public Set<Integer> getInjuries() {
		return injuries;
	}

	public void addInjury(int injury) {
		this.injuries.add(injury);
	}

	public boolean isNextRoundCheck() {
		return nextRoundCheck;
	}

	public void setNextRoundCheck(boolean nextRoundCheck) {
		this.nextRoundCheck = nextRoundCheck;
	}

	public boolean isResolvedInjury8() {
		return resolvedInjury8;
	}

	public void setResolvedInjury8(boolean resolvedInjury8) {
		this.resolvedInjury8 = resolvedInjury8;
	}

	public boolean isResolvedInjury9() {
		return resolvedInjury9;
	}

	public void setResolvedInjury9(boolean resolvedInjury9) {
		this.resolvedInjury9 = resolvedInjury9;
	}

	public int getSpecial6() {
		return special6;
	}

	public void addSpecial6() {
		this.special6++;
	}
	
}
