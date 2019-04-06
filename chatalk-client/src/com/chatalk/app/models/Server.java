package com.chatalk.app.models;

import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

/**
 * Server entity
 * Adapted for a JavaFX ListView display
 */
public class Server {
	/**
	 * Server name
	 */
	private StringProperty name;
	
	/**
	 * Server IP address
	 */
	private StringProperty IPAddress;
	
	/**
	 * Server port
	 */
	private IntegerProperty port;
	
	/**
	 * Client socket used to communicate with the server
	 */
	private Socket socket;
	
	/**
	 * User's username used to identify with the server
	 */
	private String user;
	
	/**
	 * Server's VBox used to display the logs
	 */
	private VBox logsBox;
	
	/**
	 * Server's logs list
	 */
	private ArrayList<LogMessage> logs;
	
	/**
	 * Stream used to wait for response from server
	 */
	private ObjectInputStream inFromServer;
	
	/**
	 * Stream used to write to server
	 */
	private DataOutputStream outToServer;
	
	/**
	 * Constructor.
	 */
	public Server() {
		name = new SimpleStringProperty();
		IPAddress = new SimpleStringProperty();
		port = new SimpleIntegerProperty();
		logsBox = new VBox();
		logs = new ArrayList<>();
		
		logsBox.setAlignment(Pos.TOP_CENTER);
	}
	
	/**
	 * Returns server name
	 * @return server name
	 */
	public String getName() {
		return name.get();
	}
	
	/**
	 * Updates server name
	 * @param name The new server name
	 * @throws Exception if server name is empty
	 */
	public void setName(String name) throws Exception {
		if(name.isEmpty())
			throw new Exception("serverNameCannotBeEmpty");
		
		this.name.set(name);
	}
	
	/**
	 * Returns server name as property
	 * @return server name property
	 */
	public StringProperty nameProperty() {
		return name;
	}
	
	/**
	 * Returns server IP address
	 * @return server IP address
	 */
	public String getIPAddress() {
		return IPAddress.get();
	}
	
	/**
	 * Updates server IP address
	 * @param IPAddress The new IP address
	 * @throws Exception if invalid IP address format is provided
	 */
	public void setIPAddress(String IPAddress) throws Exception {
		try {
			InetAddress.getByName(IPAddress);
			this.IPAddress.set(IPAddress);
		} catch(Exception e) {
			throw new Exception("invalidIPAddress");
		}
	}
	
	/**
	 * Returns server IP address as property
	 * @return server IP address property
	 */
	public StringProperty IPAddressProperty() {
		return IPAddress;
	}
	
	/**
	 * Returns server port number on which the IRC server application is running
	 * @return port number
	 */
	public int getPort() {
		return port.get();
	}
	
	/**
	 * Updates the server port number on which the IRC server application is running
	 * @param port The new port number
	 * @throws Exception if invalid port number is provided
	 */
	public void setPort(String port) throws Exception {
		try {
			this.port.set(Integer.parseInt(port));
		} catch(Exception e) {
			throw new Exception("invalidPort");
		}
	}
	
	/**
	 * Returns server port as property
	 * @return server port property
	 */
	public IntegerProperty portProperty() {
		return port;
	}
	
	/**
	 * Returns client socket used to communicate with that server
	 * @return client socket
	 */
	public Socket getSocket() {
		return socket;
	}
	
	/**
	 * Updates client socket used to communicate with the server
	 * @param socket The new client socket
	 */
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	
	/**
	 * Returns user's username used to communicate with that server
	 * @return user's username
	 */
	public String getUser() {
		return user;
	}
	
	/**
	 * Updates user's username used to communicate with the server
	 * @param user the new User
	 */
	public void setUser(String user) {
		this.user = user;
	}
	
	public VBox getLogsBox() {
		return logsBox;
	}
	
	public void setLogsBox(VBox logsBox) {
		this.logsBox = logsBox;
	}
	
	public ArrayList<LogMessage> getLogs() {
		return logs;
	}
	
	public void setLogs(ArrayList<LogMessage> logs) {
		this.logs = logs;
	}
	
	public ObjectInputStream getInFromServer() {
		return inFromServer;
	}
	
	public void setInFromServer(ObjectInputStream inFromServer) {
		this.inFromServer = inFromServer;
	}
	
	public DataOutputStream getOutToServer() {
		return outToServer;
	}
	
	public void setOutToServer(DataOutputStream outToServer) {
		this.outToServer = outToServer;
	}
}
