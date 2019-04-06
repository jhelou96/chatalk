package com.chatalk.app;

import java.net.*;
import java.util.ArrayList;

import org.json.JSONObject;

public class Server {
	public static ArrayList<ClientHandler> listClients = new ArrayList<>();
	
	public static void main(String[] args) throws Exception {
		//Load server configurations
		JSONObject config = Config.parseConfigFile();
		int port = config.getInt("port");
		
		ServerSocket welcomeSocket = new ServerSocket(port);
		Socket connectionSocket;

		while(true) {
			// Wait for contact
			connectionSocket = welcomeSocket.accept();
			
			//Establish connection with server in a new thread
			ClientHandler client = new ClientHandler(connectionSocket);
			listClients.add(client);
			new Thread(client).start();
		}
	}
}