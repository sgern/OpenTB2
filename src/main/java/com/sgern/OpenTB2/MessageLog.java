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
		while (message.length() > 90) {
			messages.add(message.substring(0, 90));
			message = message.substring(90);
		}
		messages.add(message);
		while (messages.size() > maxMessages) {
			messages.remove(0);
		}
	}
	
	
	public void addToCurrentMessage(String message) {
		messages.set(maxMessages - 1, messages.get(maxMessages - 1) + message);
	}
	
	public void clearLog() {
		messages.clear();
	}
	
}
