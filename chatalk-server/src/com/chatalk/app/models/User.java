package com.chatalk.app.models;

import java.util.ArrayList;
import java.util.Date;

/**
 * User entity as it is saved in the JSON data file
 */
public class User {
	/**
	 * User's username
	 */
	private String username;
	
	/**
	 * User's password
	 */
	private String password;
	
	/**
	 * User status
	 */
	private String status;
	
	/**
	 * User's last connection date
	 */
	private long lastConnectionDate;
	
	/**
	 * User's list of blocked users
	 */
	private ArrayList<String> blocks;
	
	
	public User() {
		blocks = new ArrayList<>();
	}
	
	/**
	 * Returns user's username
	 * @return user's username
	 */
	public String getUsername() {
		return username;
	}
	
	/**
	 * Updates user's username
	 * @param username The new username
	 * @throws Exception if username is less than 3 characters
	 */
	public void setUsername(String username) throws Exception {
		if(username.length() < 3)
			throw new Exception("usernameCannotBeLessThan3Chars");
			
		this.username = username;
	}
	
	/**
	 * Returns user's password
	 * @return user's password
	 */
	public String getPassword() {
		return password;
	}
	
	/**
	 * Updates user's password
	 * @param password The new password
	 * @throws Exception if password is less than 6 characters
	 */
	public void setPassword(String password) throws Exception {
		if(password.length() < 6)
			throw new Exception("passwordCannotBeLessThan6Chars");
		
		this.password = password;
	}
	
	/**
	 * Returns user status
	 * @return user status
	 */
	public String getStatus() {
		return status;
	}
	
	/**
	 * Updates user status
	 * @param status The new status
	 */
	public void setStatus(String status) {
		this.status = status;
	}
	
	/**
	 * Returns user's last connection date
	 * @return last connection date
	 */
	public Date getLastConnectionDate() {
		return new Date(lastConnectionDate);
	}

	/**
	 * Updates user's last connection date date
	 * @param date The new last connection date date
	 */
	public void setLastConnectionDate(long date) {
		this.lastConnectionDate = date;
	} 
	
	/**
	 * Returns user's list of blocked users
	 * @return list of blocked users
	 */
	public ArrayList<String> getBlocks() {
		return blocks;
	}
	
	/**
	 * Updates the user's list of blocked users
	 * @param blocks The new list of blocked users
	 */
	public void setBlocks(ArrayList<String> blocks) {
		this.blocks = blocks;
	}
}
