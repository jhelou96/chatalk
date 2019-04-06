package com.chatalk.app.models;

import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import com.chatalk.app.Config;

/**
 * Data manager for the User entity
 */
public class UserDAO {
	
	/**
	 * Retrieves the list of users
	 * @return ArrayList of users
	 * @throws Exception if JSON file not found or if User entity raises an exception
	 */
	public ArrayList<User> getUsers() throws Exception {
		//Retrieve all the users from the JSON file
		JSONObject data = Config.parseDataFile();
		JSONArray users = data.getJSONArray("users");

		//Save the retrieved users in an array list of user objects
		ArrayList<User> listUsers = new ArrayList<>();
		for(int i = 0; i < users.length(); i++) {
			User user = new User();
			user.setUsername(users.getJSONObject(i).getString("username"));
			user.setPassword(users.getJSONObject(i).getString("password"));
			user.setStatus(users.getJSONObject(i).getString("status"));
			user.setLastConnectionDate(users.getJSONObject(i).getLong("lastConnectionDate"));
			
			//User's list of blocked users
			JSONArray blocks = users.getJSONObject(i).getJSONArray("blocks");
			ArrayList<String> blockedUsersList = new ArrayList<>();
			for(int j = 0; j < blocks.length(); j++)
				blockedUsersList.add(blocks.getJSONObject(j).getString("user"));
			user.setBlocks(blockedUsersList);
			
			listUsers.add(user);
		}
		
		return listUsers;
	}
	
	/**
	 * Saves a user data in the JSON file
	 * @param user User to be saved
	 * @throws Exception if JSON file not found or if User entity raises an exception
	 */
	public void add(User user) throws Exception {
		//Retrieve JSON array of users
		JSONObject data = Config.parseDataFile();
		JSONArray users = data.getJSONArray("users");
		
		//Create a JSON object with the new user data
		JSONObject newUser = new JSONObject();
		newUser.put("username", user.getUsername());
		newUser.put("password", user.getPassword());
		newUser.put("status", user.getStatus());
		newUser.put("lastConnectionDate", user.getLastConnectionDate().getTime());
		JSONArray blocks = new JSONArray();
		ArrayList<String> blockedUsersList = user.getBlocks();
		for(int i = 0; i < blockedUsersList.size(); i++) {
			JSONObject blocked = new JSONObject();
			blocked.put("user", blockedUsersList.get(i));
			blocks.put(blocked);
		}
		newUser.put("blocks", blocks);
		users.put(newUser); //Add the new user to the JSON array of users
		
		//Update users array
		data.put("users", users);
		
		//Save updated data in JSON file
		Config.updateDataFile(data);
	}
	
	/**
	 * Updates the data of a user
	 * @param user User to be updated
	 * @throws Exception if JSON file not found or if User entity raises an exception
	 */
	public void update(User user) throws Exception {
		/*
		 * First, find the index of the user to be updated.
		 * This is done by comparing the user's username with the usernameof each entry from the JSON array of users.
		 */
		int index = 0;
		JSONObject data = Config.parseDataFile();
		JSONArray users = data.getJSONArray("users");
		for(int i = 0; i < users.length(); i++) {
			//Check if usernames are equal, save index and exit loop if they are
			if(users.getJSONObject(i).getString("username").equals(user.getUsername())) {
				index = i;
				break;
			}
		}
		
		//Replace user JSON object by the updated one at the retrieved index
		JSONObject updatedUser = new JSONObject();
		updatedUser.put("username", user.getUsername());
		updatedUser.put("password", user.getPassword());
		updatedUser.put("status", user.getStatus());
		updatedUser.put("lastConnectionDate", user.getLastConnectionDate().getTime());
		JSONArray blocks = new JSONArray();
		ArrayList<String> blockedUsersList = user.getBlocks();
		for(int i = 0; i < blockedUsersList.size(); i++) {
			JSONObject blocked = new JSONObject();
			blocked.put("user", blockedUsersList.get(i));
			blocks.put(blocked);
		}
		updatedUser.put("blocks", blocks);
		users.put(index, updatedUser);
		
		//Update users array and save updated data in the JSON file
		data.put("users", users);
		Config.updateDataFile(data);
	}
	
	/**
	 * Retrieves the list of online users
	 * @return Arraylist of usernames representing the online users
	 * @throws Exception if JSON file not found or if User entity raises an exception
	 */
	public ArrayList<String> getOnlineUsers() throws Exception {
		//Retrieve all the users from the JSON file
		JSONObject data = Config.parseDataFile();
		JSONArray users = data.getJSONArray("users");

		//Save the retrieved users in an array list of user objects
		ArrayList<String> listOnlineUsers = new ArrayList<>();
		for(int i = 0; i < users.length(); i++) {
			if(users.getJSONObject(i).getString("status").equals("online"))
				listOnlineUsers.add(users.getJSONObject(i).getString("username"));
		}
		
		return listOnlineUsers;
	}
	
	/**
	 * Retrieves the list of users that have been connected for the past hour
	 * @return Arraylist of usernames representing the users that logged on during the last hour
	 * @throws Exception if JSON file not found or if User entity raises an exception
	 */
	public ArrayList<String> getLastHourOnlineUsers() throws Exception {
		//Retrieve all the users from the JSON file
		JSONObject data = Config.parseDataFile();
		JSONArray users = data.getJSONArray("users");

		//Save the retrieved users in an array list of user objects
		ArrayList<String> listOnlineUsers = new ArrayList<>();
		for(int i = 0; i < users.length(); i++) {
			long lastConnectionDate = users.getJSONObject(i).getLong("lastConnectionDate");
			long currentTimestamp = new Date().getTime();
			
			//If user's last connection date is less than 1h
			if((currentTimestamp - lastConnectionDate) < 3600000)
				listOnlineUsers.add(users.getJSONObject(i).getString("username"));
		}
		
		return listOnlineUsers;
	}
}
