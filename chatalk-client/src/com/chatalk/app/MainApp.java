package com.chatalk.app;

import com.chatalk.app.views.ClientController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;


public class MainApp extends Application {
	/**
	 * JavaFX stage for the application
	 * @see MainApp#getPrimaryStage()
	 */
    private Stage primaryStage;
    
    /**
     * MainApp constructor
     */
    public MainApp() {
    }
    
    /**
     * Initializes the application stage
     * @param primaryStage
     * @throws Exception if the FXML file(s) is/are not found
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
    	//Prepare application stage
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("ChaTalk");
        this.primaryStage.setResizable(true);
        this.primaryStage.setFullScreen(false);
        this.primaryStage.setFullScreenExitHint("");
        this.primaryStage.getIcons().add(new Image("file:resources/images/logo.png")); //Application icon
        
        //Load root layout from fxml file.
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(MainApp.class.getResource("views/Client.fxml"));
        VBox rootLayout = (VBox) loader.load();
        
        //Load the controller of the FXML file and sent some parameters to it
	    ClientController clientController = loader.getController();
	    clientController.setMainApp(this);
	    
        
        //Once everything is loaded, display the scene
        Scene scene = new Scene(rootLayout);
        primaryStage.setScene(scene);
        primaryStage.show();
        rootLayout.requestLayout();
    }
    
    /**
     * Used to give access to FXML controllers to the main stage
     * @return the main stage
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
