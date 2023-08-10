package com.sgern.OpenTB2;

import java.util.ArrayList;
import java.util.List;

public class MessageLog {
	private List<String> messages;
	private int maxMessages;

	public MessageLog(int maxMessages) {
		messages = new ArrayList<>();
		this.maxMessages = maxMessages;
		for (int i = 0; i < maxMessages; i++) {
			messages.add("");
		}
	}
	
	public List<String> getMessages() {
		return messages;
	}
	
	public void addToLog(String message) {
		while (message.length() > 120) {
			messages.add(message.substring(0, 120));
			message = message.substring(120);
		}
		messages.add(message);
		while (messages.size() > maxMessages) {
			messages.remove(0);
		}
	}
	
	public void clearLog() {
		messages.clear();
	}
	
}
