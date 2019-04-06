package com.chatalk.app;

import java.net.*;
import java.util.ArrayList;
import java.util.Date;

import com.chatalk.app.models.LogMessage;
import com.chatalk.app.models.LogMessageDAO;
import com.chatalk.app.models.User;
import com.chatalk.app.models.UserDAO;

import java.io.*;

/**
 * Client handler used to handle connection with a client
 * Ran in a background thread to be able to handle multiple connections
 */
public class ClientHandler implements Runnable {
	/**
	 * Socket used to communicate with the client
	 */
	private Socket connectionSocket;
	
	/**
	 * User entity manager
	 */
	private UserDAO userDAO;
	
	/**
	 * LogMessage entity manager
	 */
	private LogMessageDAO logMessageDAO;
	
	/**
	 * BufferedReader used to listen to inputs from the client
	 */
	public BufferedReader inFromClient;
	
	/**
	 * Stream used to send outputs to the client
	 */
	public ObjectOutputStream outToClient;
	
	/**
	 * User with whom the connection is established
	 * Entity is defined after the user's login
	 */
	public User user;
	
	/**
	 * Counter used to keep track of the user's unsuccessfull login attempts
	 */
	private int loginAttempt = 1;
	
	/**
	 * Timestamp used to block the user after 3 unsuccessfull login attempts
	 */
	private long BLOCK_PERIOD;
	
	/**
	 * Timestamp used to keep track of the user's last operation date
	 * Used to logout automatically the user if he has been inactive for more than 15 min
	 */
	private long lastOperation;
	
	
	public ClientHandler(Socket connectionSocket) {
		this.connectionSocket = connectionSocket;
		userDAO = new UserDAO();
		logMessageDAO = new LogMessageDAO();
		user = new User();
		lastOperation = new Date().getTime();
	}	

	/**
	 * Method automatically executed when the thread is started
	 */
	public void run() {
		try {
			//Get I/O Streams
			inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
			outToClient = new ObjectOutputStream(connectionSocket.getOutputStream());
			
			//Run a new thread that will check in background if user's last operation exceeds 15 min
			Runnable task = new Runnable() {
	            public void run() {
	            	while(true) {
		                if((new Date().getTime() - lastOperation) > 900000) {
		                	try {
			                	//Send back the command being processed so the client knows what kind of response to expect
			            		outToClient.writeObject("logout");
			            		outToClient.writeObject("loggedOut");
			            		
			            		if(user.getUsername() != null) {
			            			user.setStatus("offline");
			            			userDAO.update(user);
			            		}
			            		
			            		inFromClient.readLine(); //Wait for client acknowledgement before closing connection
			            		
		            			//Close the connection
		            			connectionSocket.close();
		            			inFromClient.close();
		            			outToClient.close();
		            			
		            			break;
		                	} catch(Exception e) {}
		                }
	            	}
	            }
	        };
	        new Thread(task).start();
			
			while(true) {
				//Get command from client
				String command = inFromClient.readLine();
				String[] commandSplit = command.split(" ");
				
				//Process request
				if(commandSplit[0].equalsIgnoreCase("reg")) //Registration request
					registrationRequest(command);
				else if(commandSplit[0].equalsIgnoreCase("login")) //Login request
					loginRequest(command);
				else if(commandSplit[0].equalsIgnoreCase("message")) //Personal message sending request
					sendPersonalMessageRequest(command);
				else if(commandSplit[0].equalsIgnoreCase("broadcast")) //Broadcast message sending request
					sendBroadcastMessageRequest(command);
				else if(commandSplit[0].equalsIgnoreCase("whoseonline")) //Check online users request
					checkOnlineUsersRequest(command);
				else if(commandSplit[0].equalsIgnoreCase("wholasthr")) //Check online users during last hour request
					checkLastHourOnlineUsersRequest(command);
				else if(commandSplit[0].equalsIgnoreCase("block")) //Block user request
					blockRequest(command);
				else if(commandSplit[0].equalsIgnoreCase("ublock")) //Unblock user request
					unblockRequest(command);
				else if(commandSplit[0].equalsIgnoreCase("logout")) //Logout request
					logoutRequest();
				else //Invalid command
					outToClient.writeObject("invalidCommand");
				
				//Update last operation date
				lastOperation = new Date().getTime();
			}
		} catch(Exception e) {}
	}
	
	/**
	 * Handles client registration request
	 * @param command Command executed by the user
	 */
	public void registrationRequest(String command) {
		try {
			//Send back the command being processed so the client knows what kind of response to expect
			outToClient.writeObject("reg");
			
			String[] commandSplit = command.split(" ");
			
			//If command is not in the following format: reg <username> <password>
			if(commandSplit.length != 3) 
				outToClient.writeObject("invalidCommand");
			else if(this.user.getUsername() != null) 
					outToClient.writeObject("userIsLoggedIn");
			else {
				String username = commandSplit[1];
				String password = commandSplit[2];
				
				//Check if user with same username already exists
				ArrayList<User> users = userDAO.getUsers();
				boolean userFound = false;
				for(User user : users) {
					if(user.getUsername().equals(username)) {
						userFound = true;
						break;
					}
				}
				if(userFound)
					outToClient.writeObject("usernameAlreadyExists");
				else {
					//Save user
					User user = new User();
					try {
						user.setUsername(username);
						user.setPassword(password);
						user.setStatus("offline");
						user.setLastConnectionDate(0);
						user.setBlocks(new ArrayList<String>());
						userDAO.add(user);
						
						outToClient.writeObject("userRegistered");
					} catch(Exception e) {
						outToClient.writeObject(e.getMessage()); //If username < 3 characters
					}
				}
			}	
		} catch(Exception e) {}
	}
	
	/**
	 * Handles login request
	 * @param command Command executed by the user
	 */
	public void loginRequest(String command) {
		try {
			//Send back the command being processed so the client knows what kind of response to expect
			outToClient.writeObject("login");
			
			String[] commandSplit = command.split(" ");
			
			//If command is not in the following format: login <username> <password>
			if(commandSplit.length != 3) 
				outToClient.writeObject("invalidCommand");
			else {
				//If user's access is restricted due to too many login attempts
				if(loginAttempt > 3 && (new Date().getTime() - BLOCK_PERIOD) < 120000)
					outToClient.writeObject("accessRestricted");
				else {	
					//Reset login attempts
					if(loginAttempt > 3)
						loginAttempt = 1;
					
					String username = commandSplit[1];
					String password = commandSplit[2];
					
					//Check if user with same credentials exists
					ArrayList<User> users = userDAO.getUsers();
					boolean userFound = false;
					for(User user : users) {
						if(user.getUsername().equals(username) && user.getPassword().equals(password)) {
							if(user.getStatus().equals("online"))
								outToClient.writeObject("userAlreadyLoggedIn");
							else {
								//Get user's logs
								ArrayList<LogMessage> logs = logMessageDAO.getLogs(user);
								outToClient.writeObject("loginSuccessful");
								outToClient.writeObject(logs);
								
								userFound = true;
								this.user = user; //Save user so he can be identified later for future commands
								loginAttempt = 0;
								
								//Update user status
								user.setStatus("online");
								user.setLastConnectionDate(new Date().getTime());
								userDAO.update(user);
							}
							
							break;
						}
					}
					//If no user with provided credentials exist
					if(!userFound) {
						//Block user's access for 2 min at 3rd attempt
						if(loginAttempt == 3)
							BLOCK_PERIOD = new Date().getTime();
						outToClient.writeObject("invalidCredentials");
						loginAttempt++;
					}
				}
			}
		} catch(Exception e) {}
	}
	
	/**
	 * Handles message sending request
	 * @param command Command executed by the user
	 */
	public void sendPersonalMessageRequest(String command) {
		try {
			//Send back the command being processed so the client knows what kind of response to expect
			outToClient.writeObject("message");
			
			String[] commandSplit = command.split(" ", 3);
			
			//If command is not in the following format: message <recipient> <message>
			if(commandSplit.length != 3) 
				outToClient.writeObject("invalidCommand");
			else if(user.getUsername() == null) //If user is not logged in
				outToClient.writeObject("notLoggedIn");
			else if(commandSplit[1].equals(this.user.getUsername())) //If author = recipient
				outToClient.writeObject("invalidRecipient");
			else {
				String recipient = commandSplit[1];
				String message = commandSplit[2];
				
				//Check if recipient exists
				ArrayList<User> users = userDAO.getUsers();
				boolean userFound = false;
				boolean authorIsBlocked = false;
				for(User user : users) {
					if(user.getUsername().equals(recipient)) {
						//If recipient is found, check if he has blocked the message author
						ArrayList<String> blocks = user.getBlocks();
						for(String blockedUser : blocks) {
							if(blockedUser.equals(this.user.getUsername())) {
								authorIsBlocked = true;
								break;
							}
						}
						userFound = true;
						break;
					}
				}
				if(authorIsBlocked)
					outToClient.writeObject("authorIsBlocked");
				else if(!userFound)
					outToClient.writeObject("recipientNotFound");
				else {
					//Check if author blocked recipient
					boolean recipientIsBlocked = false;;
					ArrayList<String> blocks = this.user.getBlocks();
					for(String blockedUser : blocks) {
						if(blockedUser.equals(recipient)) {
							recipientIsBlocked = true;
							break;
						}
					}
					if(recipientIsBlocked)
						outToClient.writeObject("recipientIsBlocked");
					else {
						//Save message in the logs
						LogMessage logMessage = new LogMessage();
						logMessage.setAuthor(user.getUsername());
						logMessage.setRecipient(recipient);
						logMessage.setContent(message);
						logMessage.setType(1);
						logMessage.setDate(new Date().getTime());
						
						logMessageDAO.add(logMessage);
						
						outToClient.writeObject("messageSaved");
						
						//If client is online send him the message directly
						for(ClientHandler client : Server.listClients) {
							if(client.user.getUsername().equals(recipient)) {
								client.outToClient.writeObject("messageReceived");
								client.outToClient.writeObject(logMessage);
							}
						}
					}
				}
			}
		} catch(Exception e) {}
	}
	
	/**
	 * Handles broadcast message sending request
	 * @param command Command executed by the user
	 */
	public void sendBroadcastMessageRequest(String command) {
		try {
			//Send back the command being processed so the client knows what kind of response to expect
			outToClient.writeObject("broadcast");
			
			String[] commandSplit = command.split(" ", 2);
			
			//If command is not in the following format: broadcast <message>
			if(commandSplit.length != 2) 
				outToClient.writeObject("invalidCommand");
			else if(user.getUsername() == null) //If user is not logged in
				outToClient.writeObject("notLoggedIn");
			else {
				String message = commandSplit[1];
				
				//Save message in the logs
				LogMessage logMessage = new LogMessage();
				logMessage.setAuthor(user.getUsername());
				logMessage.setRecipient("server");
				logMessage.setContent(message);
				logMessage.setType(2);
				logMessage.setDate(new Date().getTime());
				
				logMessageDAO.add(logMessage);
				
				outToClient.writeObject("messageSaved");
				
				//Send the broadcast message to all online clients directly (Except author of the broadcast)
				for(ClientHandler client : Server.listClients) {
					if(!client.user.getUsername().equals(user.getUsername())) {
						client.outToClient.writeObject("messageReceived");
						client.outToClient.writeObject(logMessage);
					}
				}
			}
		} catch(Exception e) {}
	}
	
	/**
	 * Handles online users checking request
	 * @param command Command to be executed by the user
	 */
	public void checkOnlineUsersRequest(String command) {
		try {
			//Send back the command being processed so the client knows what kind of response to expect
			outToClient.writeObject("whoseonline");
			
			outToClient.writeObject(userDAO.getOnlineUsers());
		} catch(Exception e) {}
	}
	
	/**
	 * Handles online users during last hour checking request
	 * @param command Command executed by the user
	 */
	public void checkLastHourOnlineUsersRequest(String command) {
		try {
			//Send back the command being processed so the client knows what kind of response to expect
			outToClient.writeObject("wholasthr");
			
			outToClient.writeObject(userDAO.getLastHourOnlineUsers());
		} catch(Exception e) {}
	}
	
	/**
	 * Handles block request
	 * @param command Command executed by the user
	 */
	public void blockRequest(String command) {
		try {
			//Send back the command being processed so the client knows what kind of response to expect
			outToClient.writeObject("block");
			
			String[] commandSplit = command.split(" ", 2);
			
			//If command is not in the following format: block <user>
			if(commandSplit.length != 2) 
				outToClient.writeObject("invalidCommand");
			else if(user.getUsername() == null) //If user is not logged in
				outToClient.writeObject("notLoggedIn");
			else if(user.getUsername().equals(commandSplit[1])) //If user is trying to block himself
					outToClient.writeObject("invalidUser");
			else {
				String userToBeBlocked = commandSplit[1];
				
				//Check if user to be blocked exists
				ArrayList<User> users = userDAO.getUsers();
				boolean userFound = false;
				for(User user : users) {
					if(user.getUsername().equals(userToBeBlocked)) 
						userFound = true;
				}
				if(userFound == false)
					outToClient.writeObject("userNotFound");
				else {
					//Check if user has already been blocked
					ArrayList<String> blocks = new ArrayList<>();
					blocks = user.getBlocks();
					boolean userBlocked = false;
					for(String blockedUser : blocks) {
						if(blockedUser.equals(userToBeBlocked))
							userBlocked = true;
					}
					if(userBlocked)
						outToClient.writeObject("userAlreadyBlocked");
					else {
						//Save message in the logs
						LogMessage logMessage = new LogMessage();
						logMessage.setAuthor("server");
						logMessage.setRecipient(user.getUsername());
						logMessage.setContent("User " + userToBeBlocked + " has been blocked.");
						logMessage.setType(0);
						logMessage.setDate(new Date().getTime());
						logMessageDAO.add(logMessage);
						
						//Add blocked user to the user's list of blocked user
						blocks.add(userToBeBlocked);
						user.setBlocks(blocks);
						userDAO.update(user);
						
						outToClient.writeObject("userBlocked");
					}
				}
			}
		} catch(Exception e) {}
	}
	
	/**
	 * Handles unblock request
	 * @param command Command executed by the user
	 */
	public void unblockRequest(String command) {
		try {
			//Send back the command being processed so the client knows what kind of response to expect
			outToClient.writeObject("ublock");
			
			String[] commandSplit = command.split(" ", 2);
			
			//If command is not in the following format: ublock <user>
			if(commandSplit.length != 2) 
				outToClient.writeObject("invalidCommand");
			else if(user.getUsername() == null) //If user is not logged in
				outToClient.writeObject("notLoggedIn");
			else {
				String userToBeUnblocked = commandSplit[1];
				
				//Check if user is in blocked list
				ArrayList<String> blocks = new ArrayList<>();
				blocks = user.getBlocks();
				boolean userBlocked = false;
				int i = 0;
				for(String blockedUser : blocks) {
					if(blockedUser.equals(userToBeUnblocked)) {
						userBlocked = true;
						break;
					}
					i++;
				}
				if(!userBlocked)
					outToClient.writeObject("userNotBlocked");
				else {
					//Save message in the logs
					LogMessage logMessage = new LogMessage();
					logMessage.setAuthor("server");
					logMessage.setRecipient(user.getUsername());
					logMessage.setContent("User " + userToBeUnblocked + " has been unblocked.");
					logMessage.setType(0);
					logMessage.setDate(new Date().getTime());
					logMessageDAO.add(logMessage);
					
					//Removes user from blocked list
					blocks.remove(i);
					user.setBlocks(blocks);
					userDAO.update(user);
					
					outToClient.writeObject("userUnblocked");
				}
			}
		} catch(Exception e) {}
	}
	
	/**
	 * Handles logout request
	 * @throws Exception Command executed by the user
	 */
	public void logoutRequest() throws Exception {
		//Send back the command being processed so the client knows what kind of response to expect
		outToClient.writeObject("logout");
		
		if(this.user.getUsername() == null)
			outToClient.writeObject("notLoggedIn");
		else {
			outToClient.writeObject("loggedOut");
			
			user.setStatus("offline");
			userDAO.update(user);
			
			inFromClient.readLine(); //Wait for client acknowledgement before closing connection
			
			//Close the connection
			connectionSocket.close();
			inFromClient.close();
			outToClient.close();
		}
	}
}