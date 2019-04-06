package com.chatalk.app.views;

import java.io.DataOutputStream;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.chatalk.app.MainApp;
import com.chatalk.app.models.LogMessage;
import com.chatalk.app.models.Server;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.util.Pair;

public class ClientController {
    /**
     * Main controller
     * @see MainApp
     */
    private MainApp mainApp;
    
    /**
	 * JavaFX list containing the list of servers
	 */
	@FXML
    private ListView<Server> serversListView;
	
	/**
	 * JavaFX ScrollPane containing the list of chat logs
	 */
	@FXML
	private ScrollPane chatLogs;
	
	@FXML
	private TextField console;
	
	/**
	 * List of servers
	 */
	private ArrayList<Server> servers;
	
	private Socket clientSocket;
	
	/**
	 * Stream used to wait for response from server
	 */
	private ObjectInputStream inFromServer;
	
	/**
	 * Stream used to write to server
	 */
	private DataOutputStream outToServer;
	
	/**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
	public ClientController() {
		servers = new ArrayList<>();
	}
	
	/**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     */
	@FXML
    private void initialize() {
		loadListServers();
		
		//If no server is selected, console field is disabled
		console.disableProperty().bind(Bindings.isEmpty(serversListView.getSelectionModel().getSelectedItems()));
	}
	
    /**
     * Is called by the main application to give a reference back to itself.
     * @param mainApp The application main controller
     */
    public void setMainApp(MainApp mainApp) {
        this.mainApp = mainApp;
    }
    
    public void newServer() {
    	//New dialog
		Dialog<Pair<String, String>> dialog = new Dialog<>();
		dialog.setTitle("New server");
		dialog.setHeaderText("Add new server");
		dialog.initOwner(mainApp.getPrimaryStage());

		//Icon
		ImageView dialogIcon = new ImageView();
		dialogIcon.setImage(new Image("images/dialogs/new.png"));
		dialog.setGraphic(dialogIcon);
		
		//Buttons
		ButtonType submitButtonType = new ButtonType("New", ButtonData.OK_DONE);
		dialog.getDialogPane().getButtonTypes().addAll(submitButtonType, ButtonType.CANCEL);
		
		//Form box
		VBox form = new VBox();
		form.setSpacing(10);
		
		//Server name field
		TextField serverName = new TextField();
		serverName.setPromptText("Name");
		form.getChildren().add(serverName);
		
		//Server IP address field
		TextField serverAddress = new TextField();
		serverAddress.setPromptText("IP address");
		form.getChildren().add(serverAddress);
		
		//Server port where app is running field
		TextField serverPort = new TextField();
		serverPort.setPromptText("Port");
		form.getChildren().add(serverPort);
		
		dialog.getDialogPane().setContent(form);
		
		//Dialog validation
		final Button submitButton = (Button) dialog.getDialogPane().lookupButton(submitButtonType);
        submitButton.addEventFilter(ActionEvent.ACTION, event -> {
			try {
				Server server = new Server();
				server.setName(serverName.getText());
				server.setIPAddress(serverAddress.getText());
				server.setPort(serverPort.getText());
				
				//Verify if server instance already running
				for(Server serverItem : servers) {
					if(serverItem.getName().equals(server.getName()))
						throw new Exception("serverInstanceWithSameNameAlreadyRunning");
					if(serverItem.getIPAddress().equals(server.getIPAddress()) && serverItem.getPort() == server.getPort())
						throw new Exception("serverInstanceAlreadyRunning");
				}
				
				//Initiate server contact
				clientSocket = new Socket(server.getIPAddress(), server.getPort());
				inFromServer = new ObjectInputStream(clientSocket.getInputStream());
		    	outToServer = new DataOutputStream(clientSocket.getOutputStream());
				server.setSocket(clientSocket);
				server.setInFromServer(inFromServer);
				server.setOutToServer(outToServer);
				
				//Display welcome message
				VBox logsBox = server.getLogsBox();
				ArrayList<LogMessage> logs = server.getLogs();
				DateFormat dateFormat = new SimpleDateFormat("EEEE MMMM dd, yyyy");
				Label messageDate = new Label(dateFormat.format(new Date()));
				logsBox.getChildren().add(messageDate);
				logsBox.getChildren().add(new Separator());
				LogMessage welcomeMessage = new LogMessage();
				welcomeMessage.setType(0);
				welcomeMessage.setContent("Connection with server established. Please identify yourself.");
				welcomeMessage.setDate(new Date().getTime());
				HBox logMessage = new HBox();
		    	Label messageTime = new Label(new SimpleDateFormat("[HH:mm:ss]").format(welcomeMessage.getDate()) + "Server: ");
		    	Label messageContent = new Label(welcomeMessage.getContent());
				messageContent.setTextFill(Color.RED);
				logMessage.getChildren().add(messageTime);
				logMessage.getChildren().add(messageContent);
				logsBox.getChildren().add(logMessage);
				logs.add(welcomeMessage);
				server.setLogs(logs);
				server.setLogsBox(logsBox);
				chatLogs.setContent(logsBox);
				chatLogs.vvalueProperty().bind(logsBox.heightProperty());
				
				servers.add(server);
				loadListServers();
				listenToServerResponse();
			} catch(Exception e) {
				event.consume();
				
				Alert alert = new Alert(AlertType.WARNING);
				alert.initOwner(mainApp.getPrimaryStage());
				alert.setTitle("Warning");
				alert.setHeaderText("Oops, something went wrong...");
				alert.setContentText(e.getMessage());
				alert.show();
			}
        });
		
		dialog.showAndWait();
    }
    
    /**
     * Loads list of server instances created in JavaFX list view
     */
    public void loadListServers() {
    	//Default text if no items in the servers list
    	serversListView.setPlaceholder(new Label("No server instance"));
    	
    	//Populate JavaFX list view with list of servers
    	serversListView.setItems(FXCollections.observableList(servers));
    	serversListView.setCellFactory(param -> new ListCell<Server>() {
		    @Override
		    protected void updateItem(Server item, boolean empty) {
		        super.updateItem(item, empty);

		        if(!empty && item != null) {
		        	ImageView imageView = new ImageView();
		        	imageView.setImage(new Image("images/server.png"));
		        	setGraphic(imageView);
		        	setText(item.getName());
		        }
		    }
		});
    }
    
    /**
     * Redirects to sub-methods based on command entered
     * Triggered when user press enter after typing a command
     * @throws Exception if any command raises an exception
     */
    public void handleConsoleCommand() throws Exception {
    	//Server the user is currently communicating with
    	Server server = (Server) serversListView.getSelectionModel().getSelectedItems().get(0);
    	server.getOutToServer().writeBytes(console.getText() + "\n"); //Send command to server
    }
    
    /**
     * Adds a new log message to the logs VBOX
     * Executed everytime the user submits a command through the console line
     * @param message Message to be added
     * @throws Exception if message could not be added
     */
    public void updateVBoxLogsWithNewMessages(LogMessage message) throws Exception {
    	//Server the user is currently communicating with
    	Server server = (Server) serversListView.getSelectionModel().getSelectedItems().get(0);
    	VBox logsBox = server.getLogsBox();
    	ArrayList<LogMessage> logs = server.getLogs();
    	
    	//If this message is the first in the logs list --> Show its date
    	if(logs.size() == 0) {
    		DateFormat dateFormat = new SimpleDateFormat("EEEE MMMM dd, yyyy");
			Label messageDate = new Label(dateFormat.format(message.getDate()));
			logsBox.getChildren().add(messageDate);
			logsBox.getChildren().add(new Separator());
    	} else {
	    	//If this message has a different date than the previous one --> Show its date
	    	DateFormat dateWithoutTimeFormatter = new SimpleDateFormat("dd/MM/yyyy");
			Date newMessageDate = dateWithoutTimeFormatter.parse(dateWithoutTimeFormatter.format(message.getDate()));
			Date prevMessageDate = dateWithoutTimeFormatter.parse(dateWithoutTimeFormatter.format(logs.get(logs.size()-1).getDate()));
			if(newMessageDate.after(prevMessageDate)) {
				DateFormat dateFormat = new SimpleDateFormat("EEEE MMMM dd, yyyy");
				Label messageDate = new Label(dateFormat.format(message.getDate()));
				logsBox.getChildren().add(0, messageDate);
				logsBox.getChildren().add(1, new Separator());
			}
    	}
		
    	//Format the message
    	HBox logMessage = new HBox();
    	Label messageTime = new Label();
    	if(message.getType() == 0)
    		messageTime.setText(new SimpleDateFormat("[HH:mm:ss]").format(message.getDate()) + "Server: ");
    	else if(message.getType() == 1)
    		messageTime.setText(new SimpleDateFormat("[HH:mm:ss]").format(message.getDate()) + message.getAuthor() + " -> " + message.getRecipient() + ": ");
    	else
    		messageTime.setText(new SimpleDateFormat("[HH:mm:ss]").format(message.getDate()) + message.getAuthor() + ": ");
		Label messageContent = new Label(message.getContent());
		if(message.getType() == 0)
			messageContent.setTextFill(Color.RED);
		else if(message.getType() == 1)
			messageContent.setTextFill(Color.BLUE);
		else if(message.getType() == 2)
			messageContent.setTextFill(Color.ORANGE);
		
		//Save the message in the VBox
		logMessage.getChildren().add(messageTime);
		logMessage.getChildren().add(messageContent);
		logsBox.getChildren().add(logMessage);
		
		//Save the logs locally --> This log list is disregarded after the user logs out
		logs.add(message);
		server.setLogs(logs);
		server.setLogsBox(logsBox);
		
		//Update view
		chatLogs.setContent(logsBox);
    }
    
    /**
     * Adds a previous message to the logs VBox
     * Executed after the user logs in to load the previous chat logs
     * Messages are received in ascending order
     * @param message The message to be added
     * @throws Exception if message could not be added
     */
    public void updateVBoxLogsWithPriorMessages(LogMessage message) throws Exception {
    	//Server the user is currently communicating with
    	Server server = (Server) serversListView.getSelectionModel().getSelectedItems().get(0);
    	VBox logsBox = server.getLogsBox();
    	ArrayList<LogMessage> logs = server.getLogs();
    	
    	int i = 0; //Index used to know where to add the message in the VBox
    	if(logs.size() > 0) {
    		DateFormat dateWithoutTimeFormatter = new SimpleDateFormat("dd/MM/yyyy");
    		Date newMessageDate = dateWithoutTimeFormatter.parse(dateWithoutTimeFormatter.format(message.getDate()));
    		Date nextMessageDate = dateWithoutTimeFormatter.parse(dateWithoutTimeFormatter.format(logs.get(0).getDate()));
    		
    		//If the date of the message to be added comes before the next message date -> Show its date
    		if(newMessageDate.before(nextMessageDate)) {
    			DateFormat dateFormat = new SimpleDateFormat("EEEE MMMM dd, yyyy");
    			Label messageDate = new Label(dateFormat.format(message.getDate()));
    			logsBox.getChildren().add(i, messageDate);
    			logsBox.getChildren().add(i+1, new Separator());
    			
    			i = i+2;
    		} else if(logsBox.getChildren().get(i+1) instanceof Separator) //Otherwise if first message the VBox is the message date
    			i = i+2; //To display the message after the separator
    	}
    	
    	//Format the message
    	HBox logMessage = new HBox();
    	Label messageTime = new Label();
    	if(message.getType() == 0)
    		messageTime.setText(new SimpleDateFormat("[HH:mm:ss]").format(message.getDate()) + "Server: ");
    	else if(message.getType() == 1)
    		messageTime.setText(new SimpleDateFormat("[HH:mm:ss]").format(message.getDate()) + message.getAuthor() + " -> " + message.getRecipient() + ": ");
    	else
    		messageTime.setText(new SimpleDateFormat("[HH:mm:ss]").format(message.getDate()) + message.getAuthor() + ": ");
		Label messageContent = new Label(message.getContent());
		if(message.getType() == 0)
			messageContent.setTextFill(Color.RED);
		else if(message.getType() == 1)
			messageContent.setTextFill(Color.BLUE);
		else if(message.getType() == 2)
			messageContent.setTextFill(Color.ORANGE);
		
		//Save the message in the VBox
		logMessage.getChildren().add(messageTime);
		logMessage.getChildren().add(messageContent);
		logsBox.getChildren().add(i, logMessage);
		
		//Save the logs locally --> This log list is disregarded after the user logs out
		logs.add(0, message);
		server.setLogs(logs);
		server.setLogsBox(logsBox);
		
		//Update view
		chatLogs.setContent(logsBox);
    }
    
    /**
     * Reloads the server's data (Logs, streams, socket, etc)
     * Executed everytime the user clicks on a new server from the server list
     */
    public void reloadServerData() {
    	//Server the user is currently communicating with
    	Server server = (Server) serversListView.getSelectionModel().getSelectedItems().get(0);
    	
    	clientSocket = server.getSocket();
    	inFromServer = server.getInFromServer();
    	outToServer = server.getOutToServer();
    	
    	VBox logsBox = server.getLogsBox();
    	chatLogs.setContent(logsBox);
    }
    
    /*
     * *************** *
     * SERVER RESPONSE *
     * *************** *
     */
    
    /**
     * Listens to server responses and redirects to sub-methods based on command received from server
     * Is executed in background
     * @throws Exception if communication with server failed
     */
    public void listenToServerResponse() throws Exception {
    	Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				while(true) {
					Object serverResponse = inFromServer.readObject(); //Wait for response from server
					
					if(serverResponse.equals("reg"))
						registrationResponse();
		            else if(serverResponse.equals("login"))
		            	loginResponse();
		            else if(serverResponse.equals("message"))
		            	messageResponse();
		            else if(serverResponse.equals("broadcast"))
		            	broadcastResponse();
		            else if(serverResponse.equals("whoseonline"))
		            	onlineUsersResponse();
		            else if(serverResponse.equals("wholasthr"))
		            	onlineUsersLastHourResponse();
		            else if(serverResponse.equals("block"))
		            	blockResponse();
		            else if(serverResponse.equals("ublock"))
		            	unblockResponse();
		            else if(serverResponse.equals("logout"))
		            	logoutResponse();
		            else if(serverResponse.equals("messageReceived"))
		            	messageReceivedResponse();
		            else if(serverResponse.equals("invalidCommand"))
		            	invalidCommandResponse();
				}
			}
   	 	};
   	 	
   	 	Thread thread = new Thread(task);
   	 	thread.setDaemon(true);
   	 	thread.start();
    }
    
    /**
     * Waits for server response after user's registration attempt
     * @throws Exception if communication with server failed
     */
    public void registrationResponse() throws Exception {
    	//Server the user is currently communicating with
    	Object serverResponse = inFromServer.readObject(); //Wait for response from server
    	
    	//If user submitted invalid command
    	if(serverResponse.equals("invalidCommand")) {
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("Invalid command: " + console.getText());
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                	} catch(Exception e) {}
                }
        	});
    	} else if(serverResponse.equals("userIsLoggedIn")) {
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("You are already logged in.");
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                		
                		console.clear();
                	} catch(Exception e) {}
                }
        	});
    	} else if(serverResponse.equals("usernameCannotBeLessThan3Chars")) {
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("Username cannot be less than 3 characters.");
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                		
                		console.clear();
                	} catch(Exception e) {}
                }
        	});
    	} else if(serverResponse.equals("passwordCannotBeLessThan6Chars")) { 
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("Password cannot be less than 6 characters.");
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                		
                		console.clear();
                	} catch(Exception e) {}
                }
        	});
    	} else if(serverResponse.equals("usernameAlreadyExists")) {
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("Username already exists.");
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                		
                		console.clear();
                	} catch(Exception e) {}
                }
        	});
    	} else if(serverResponse.equals("userRegistered")) {
    		//Display success message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage successMessage = new LogMessage();
                		successMessage.setType(0);
                		successMessage.setContent("Registration successful. You can now login.");
                		successMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(successMessage);
                		
                		console.clear();
                	} catch(Exception e) {}
                }
        	});
    	}
    }
    
    /**
     * Waits for server response after user's login attempt
     * @throws Exception if communication with server failed
     */
    public void loginResponse() throws Exception {
    	Object serverResponse = inFromServer.readObject(); //Wait for response from server

    	//If user submitted invalid command
    	if(serverResponse.equals("invalidCommand")) {
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
		    		try {
		        		LogMessage errorMessage = new LogMessage();
		        		errorMessage.setType(0);
		        		errorMessage.setContent("Invalid command: " + console.getText());
		        		errorMessage.setDate(new Date().getTime());
		        		updateVBoxLogsWithNewMessages(errorMessage);
		        	} catch(Exception e) {}
                }
    		});
    	} else if(serverResponse.equals("invalidCredentials")) { //If user provided invalid credentials
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
		    		try {
		    			LogMessage errorMessage = new LogMessage();
		        		errorMessage.setType(0);
		        		errorMessage.setContent("Access refused: Invalid credentials.");
		        		errorMessage.setDate(new Date().getTime());
		        		updateVBoxLogsWithNewMessages(errorMessage);
		        	} catch(Exception e) {}
                }
    		});
    		
    		console.clear();
    	} else if(serverResponse.equals("userAlreadyLoggedIn")) { //If user is already logged in
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
		    		try {
			    		LogMessage errorMessage = new LogMessage();
			    		errorMessage.setType(0);
			    		errorMessage.setContent("Access refused: User already logged in.");
			    		errorMessage.setDate(new Date().getTime());
			    		updateVBoxLogsWithNewMessages(errorMessage);
		        	} catch(Exception e) {}
                }
    		});
    		
    		console.clear();
    	} else if(serverResponse.equals("accessRestricted")) { //If user is not allowed to login
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
		    		try {
		    			LogMessage errorMessage = new LogMessage();
			    		errorMessage.setType(0);
			    		errorMessage.setContent("Access restricted: Too many attempts. Try again later.");
			    		errorMessage.setDate(new Date().getTime());
			    		updateVBoxLogsWithNewMessages(errorMessage);
		        	} catch(Exception e) {}
                }
    		});
	    
    		console.clear();
    	} else if(serverResponse.equals("loginSuccessful")) { //If user logged in successfully
    		//Save user's username
        	Server server = (Server) serversListView.getSelectionModel().getSelectedItems().get(0); //Server the user is currently communicating with
    		server.setUser(console.getText().split(" ")[1]);
    		
    		//Display success message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
		    		try {
		    			LogMessage successMessage = new LogMessage();
		        		successMessage.setType(0);
		        		successMessage.setContent("Login successful. Welcome back!");
		        		successMessage.setDate(new Date().getTime());
		        		updateVBoxLogsWithNewMessages(successMessage);
		        	} catch(Exception e) {}
                }
    		});
			
    		ArrayList<LogMessage> logs = (ArrayList<LogMessage>) inFromServer.readObject();
    		for(LogMessage message : logs) {
    			Platform.runLater(new Runnable() {
                    @Override public void run() {
    		    		try {
    		    			updateVBoxLogsWithPriorMessages(message);
    		        	} catch(Exception e) {}
                    }
        		});
    		}
    		
    		console.clear();
    	}
    }
    
    /**
     * Waits for server response after user's message sending attempt
     * @throws Exception if communication with server failed
     */
    public void messageResponse() throws Exception {
    	Object serverResponse = inFromServer.readObject(); //Wait for response from server
    	
    	//If user submitted invalid command
    	if(serverResponse.equals("invalidCommand")) {
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("Invalid command: " + console.getText());
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                	} catch(Exception e) {}
                }
        	});
    	} else if(serverResponse.equals("notLoggedIn")) { //Check if user is logged in
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("Access refused: Not logged in.");
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                	} catch(Exception e) {}
                }
        	});
    		
    		console.clear();
    	} else if(serverResponse.equals("invalidRecipient")) { //If user sent message to himself
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("You cannot send a message to yourself.");
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                	} catch(Exception e) {}
                }
        	});
    		
    		console.clear();
    	} else if(serverResponse.equals("recipientNotFound")) { //If recipient does not exist
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("Recipient not found.");
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                	} catch(Exception e) {}
                }
        	});
    		
    		console.clear();
    	} else if(serverResponse.equals("recipientIsBlocked")) { //If user blocked the recipient
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("Message cannot be sent: User is in your blocked list.");
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                	} catch(Exception e) {}
                }
        	});
    		
    		console.clear();
    	} else if(serverResponse.equals("authorIsBlocked")) { //If recipient does not exist
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("Message cannot be sent: Recipient has added you to his blocked list.");
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                	} catch(Exception e) {}
                }
        	});
    		
    		console.clear();
    	} else if(serverResponse.equals("messageSaved")) { //If message saved successfully
    		//Display sent message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                    	Server server = (Server) serversListView.getSelectionModel().getSelectedItems().get(0); //Server the user is currently communicating with
                		
                    	String[] command = console.getText().split(" ", 3);
                		LogMessage message = new LogMessage();
                		message.setType(1);
                		message.setAuthor(server.getUser());
                		message.setContent(command[2]);
                		message.setRecipient(command[1]);
                		message.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(message);
                		
                		console.clear();
                	} catch(Exception e) {}
                }
        	});
    	}
    }
    
    /**
     * Waits for server response after user's broadcast message sending attempt
     * @throws Exception if communication with server failed
     */
    public void broadcastResponse() throws Exception {
    	Object serverResponse = inFromServer.readObject(); //Wait for response from server
    	
    	//If user submitted invalid command
    	if(serverResponse.equals("invalidCommand")) {
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("Invalid command: " + console.getText());
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                	} catch(Exception e) {}
                }
        	});
    	} else if(serverResponse.equals("notLoggedIn")) { //Check if user is logged in
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("Access refused: Not logged in.");
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                		
                		console.clear();
                	} catch(Exception e) {}
                }
        	});
    	} else if(serverResponse.equals("messageSaved")) { //If message saved successfully
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                    	Server server = (Server) serversListView.getSelectionModel().getSelectedItems().get(0); //Server the user is currently communicating with
                    	
                		String[] command = console.getText().split(" ", 2);
                		LogMessage message = new LogMessage();
                		message.setType(2);
                		message.setAuthor(server.getUser());
                		message.setContent(command[1]);
                		message.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(message);
                		
                		console.clear();
                	} catch(Exception e) {}
                }
        	});
    	}
    }

    /**
     * Waits for server response after user's online users retrieval attempt
     * @throws Exception if communication with server failed
     */
    public void onlineUsersResponse() throws Exception {
    	Object serverResponse = inFromServer.readObject(); //Wait for response from server
    	
    	//Display list of online users
		Platform.runLater(new Runnable() {
            @Override public void run() {
            	try {
            		LogMessage message = new LogMessage();
            		message.setType(0);
            		message.setContent("Online users: " + serverResponse.toString());
            		message.setDate(new Date().getTime());
            		updateVBoxLogsWithNewMessages(message);
            		
            		console.clear();
            	} catch(Exception e) {}
            }
    	});
    }

    /**
     * Waits for server response after user's online users during last hour retrieval attempt
     * @throws Exception if communication with server failed
     */
    public void onlineUsersLastHourResponse() throws Exception {
    	Object serverResponse = inFromServer.readObject(); //Wait for response from server
    	
    	//Display list of online users
    	Platform.runLater(new Runnable() {
            @Override public void run() {
            	try {
            		LogMessage message = new LogMessage();
            		message.setType(0);
            		message.setContent("Online users during the past hour: " + serverResponse.toString());
            		message.setDate(new Date().getTime());
            		updateVBoxLogsWithNewMessages(message);
            		
            		console.clear();
            	} catch(Exception e) {}
            }
    	});
    }
    
    /**
     * Waits for server response after user's block attempt
     * @throws Exception if communication with server failed
     */
    public void blockResponse() throws Exception {
    	Object serverResponse = inFromServer.readObject(); //Wait for response from server
    	
    	//If user submitted invalid command
    	if(serverResponse.equals("invalidCommand")) {
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("Invalid command: " + console.getText());
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                	} catch(Exception e) {}
                }
        	});
    	} else if(serverResponse.equals("notLoggedIn")) { //Check if user is logged in
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("Access refused: Not logged in.");
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                		
                		console.clear();
                	} catch(Exception e) {}
                }
        	});
    	} else if(serverResponse.equals("invalidUser")) { //If user tries to block himself
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("You cannot send block yourself.");
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                		
                		console.clear();
                	} catch(Exception e) {}
                }
        	});
    	} else if(serverResponse.equals("userNotFound")) { //If user to be blocked doesn't exist
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("User to be blocked not found.");
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                		
                		console.clear();
                	} catch(Exception e) {}
                }
        	});
    	} else if(serverResponse.equals("userAlreadyBlocked")) {
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("User has already been blocked.");
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                		
                		console.clear();
                	} catch(Exception e) {}
                }
        	});
    	} else if(serverResponse.equals("userBlocked")) { //If user has been blocked
    		//Display success message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		String[] command = console.getText().split(" ", 2);
                		LogMessage message = new LogMessage();
                		message.setType(0);
                		message.setContent("User " + command[1] + " has been blocked.");
                		message.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(message);
                		
                		console.clear();
                	} catch(Exception e) {}
                }
        	});
    	}
    }

    /**
     * Waits for server response after user's unblock attempt
     * @throws Exception if communication with server failed
     */
    public void unblockResponse() throws Exception {
    	Object serverResponse = inFromServer.readObject(); //Wait for response from server
    	
    	//If user submitted invalid command
    	if(serverResponse.equals("invalidCommand")) {
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("Invalid command: " + console.getText());
                		errorMessage.setDate(new Date().getTime());
                		
                		updateVBoxLogsWithNewMessages(errorMessage);
                	} catch(Exception e) {}
                }
        	});
    	} else if(serverResponse.equals("notLoggedIn")) { //Check if user is logged in
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("Access refused: Not logged in.");
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                		
                		console.clear();
                	} catch(Exception e) {}
                }
        	});
    	} else if(serverResponse.equals("userNotBlocked")) { //If user to be unblocked is not blocked
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("User not blocked.");
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                		
                		console.clear();
                	} catch(Exception e) {}
                }
        	});
    	} else if(serverResponse.equals("userUnblocked")) { //If user has been unblocked
    		//Display success message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		String[] command = console.getText().split(" ", 2);
                		LogMessage message = new LogMessage();
                		message.setType(0);
                		message.setContent("User " + command[1] + " has been unblocked.");
                		message.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(message);
                		
                		console.clear();
                	} catch(Exception e) {}
                }
        	});
    	}
    }

    /**
     * Waits for server response after user's logout attempt
     * @throws Exception if communication with server fail
     */
    public void logoutResponse() throws Exception {
    	Object serverResponse = inFromServer.readObject(); //Wait for response from server
    	
    	//If user submitted invalid command
    	if(serverResponse.equals("invalidCommand")) {
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("Invalid command: " + console.getText());
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                	} catch(Exception e) {}
                }
        	});
    	} else if(serverResponse.equals("notLoggedIn")) { //Check if user is logged in
    		//Display error message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage errorMessage = new LogMessage();
                		errorMessage.setType(0);
                		errorMessage.setContent("Access refused: Not logged in.");
                		errorMessage.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(errorMessage);
                		
                		console.clear();
                	} catch(Exception e) {}
                }
        	});
    	} else if(serverResponse.equals("loggedOut")){ //Otherwise, user has been logged out successfully
    		outToServer.writeBytes("clientLoggedOut" + "\n"); //Send acknowledgement back to server
    		
    		//Close the connection
    		clientSocket.close();
    		inFromServer.close();
    		outToServer.close();
    		
    		//Display success message
    		Platform.runLater(new Runnable() {
                @Override public void run() {
                	try {
                		LogMessage message = new LogMessage();
                		message.setType(0);
                		message.setContent("You have been logged out. Bye.");
                		message.setDate(new Date().getTime());
                		updateVBoxLogsWithNewMessages(message);
                		
                		console.clear();
                		
            	    	//Remove the server from the list of servers and reload the list
            	    	Server server = (Server) serversListView.getSelectionModel().getSelectedItems().get(0); //Server the user is currently communicating with
            	    	servers.remove(server);
            	    	loadListServers();
                	} catch(Exception e) {}
                }
        	});
    	}
    }

    /**
     * If server responds with an invalidCommand statement
     * @throws Exception if communication with server failed
     */
    public void invalidCommandResponse() throws Exception {
		Platform.runLater(new Runnable() {
            @Override public void run() {
            	try {
            		LogMessage message = new LogMessage();
            		message.setType(0);
            		message.setContent("Invalid command: " + console.getText());
            		message.setDate(new Date().getTime());
            		updateVBoxLogsWithNewMessages(message);
            		
            		console.clear();
            	} catch(Exception e) {}
            }
    	});
    }

    /**
     * If server sends a message from a client directly
     * @throws Exception if communication with server failed
     */
    public void messageReceivedResponse() throws Exception {
    	LogMessage serverResponse = (LogMessage) inFromServer.readObject(); //Wait for response from server
    	
    	Platform.runLater(new Runnable() {
            @Override public void run() {
            	try {
            		updateVBoxLogsWithNewMessages(serverResponse);
            		
            		//Play notification sound
                	Media media = new Media(getClass().getResource("/notification.mp3").toURI().toString());
                    MediaPlayer player = new MediaPlayer(media); 
                    player.play();
            	} catch(Exception e) {}
            }
    	});
    }
}