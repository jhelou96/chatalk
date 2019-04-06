package com.chatalk.app.models;

import java.util.ArrayList;
import java.util.Collections;

import org.json.JSONArray;
import org.json.JSONObject;

import com.chatalk.app.Config;
import com.chatalk.app.util.ArraySorting;

/**
 * Data manager for the User entity
 */
public class LogMessageDAO {
	
	/**
	 * Retrieves the logs of a user
	 * @return ArrayList of LogMessages
	 * @throws Exception if JSON file not found or if LogMessage entity raises an exception
	 */
	public ArrayList<LogMessage> getLogs(User user) throws Exception {
		//Retrieve all the logs from the JSON file
		JSONObject data = Config.parseDataFile();
		JSONArray logs = data.getJSONArray("logs");

		//Save the retrieved log messages in an array list of user objects
		ArrayList<LogMessage> listLogMessages = new ArrayList<>();
		for(int i = 0; i < logs.length(); i++) {
			//Check if log message is a server message and if recipient is user
			if(logs.getJSONObject(i).getInt("type") == 0 && logs.getJSONObject(i).getString("recipient").equals(user.getUsername())) {
				LogMessage message = new LogMessage();
				message.setRecipient(logs.getJSONObject(i).getString("recipient"));
				message.setType(logs.getJSONObject(i).getInt("type"));
				message.setContent(logs.getJSONObject(i).getString("content"));
				message.setDate(logs.getJSONObject(i).getLong("date"));
				message.setAuthor(null);
				listLogMessages.add(message);
			} else if(logs.getJSONObject(i).getInt("type") == 1 && (logs.getJSONObject(i).getString("author").equals(user.getUsername()) || logs.getJSONObject(i).getString("recipient").equals(user.getUsername()))) { //Check if log message is a private message and if user is recipient or author
				LogMessage message = new LogMessage();
				message.setAuthor(logs.getJSONObject(i).getString("author"));
				message.setType(logs.getJSONObject(i).getInt("type"));
				message.setContent(logs.getJSONObject(i).getString("content"));
				message.setDate(logs.getJSONObject(i).getLong("date"));
				message.setRecipient(logs.getJSONObject(i).getString("recipient"));
				listLogMessages.add(message);
			} else if(logs.getJSONObject(i).getInt("type") == 2) { //If log message is a broadcast message
				LogMessage message = new LogMessage();
				message.setAuthor(logs.getJSONObject(i).getString("author"));
				message.setType(logs.getJSONObject(i).getInt("type"));
				message.setContent(logs.getJSONObject(i).getString("content"));
				message.setDate(logs.getJSONObject(i).getLong("date"));
				message.setRecipient(logs.getJSONObject(i).getString("recipient"));
				listLogMessages.add(message);
			}
		}
		
		Collections.sort(listLogMessages, new ArraySorting()); //Sorts arraylist based on logs date
		return listLogMessages;
	}
	
	/**
	 * Saves a message in the logs
	 * @param logMessage Message to be saved
	 * @throws Exception if JSON file not found or if LogMessage entity raises an exception
	 */
	public void add(LogMessage logMessage) throws Exception {
		//Retrieve JSON array of log messages
		JSONObject data = Config.parseDataFile();
		JSONArray logs = data.getJSONArray("logs");
		
		//Create a JSON object with the new log message data
		JSONObject newLogMessage = new JSONObject();
		
		newLogMessage.put("author", logMessage.getAuthor());
		newLogMessage.put("recipient", logMessage.getRecipient());
		newLogMessage.put("content", logMessage.getContent());
		newLogMessage.put("type", logMessage.getType());
		newLogMessage.put("date", logMessage.getDate().getTime());
		
		logs.put(newLogMessage); //Add the new log message to the JSON array of log messages
		
		//Update logs array
		data.put("logs", logs);
		
		//Save updated data in JSON file
		Config.updateDataFile(data);
	}
}
