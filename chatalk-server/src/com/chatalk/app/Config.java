package com.chatalk.app;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Config class containing some useful methods
 * Static methods can be called without instanciating the class
 */
public class Config {
	/**
	 * Reads JSON config file
	 * @return Config data
	 * @throws FileNotFoundException if JSON file not found
	 */
	public static JSONObject parseConfigFile() throws FileNotFoundException {
		JSONTokener parser = new JSONTokener(new FileReader("resources/config.json"));
		JSONObject data = new JSONObject(parser);
		
		return data;
	}
	
	/**
	 * Updates JSON config file
	 * @param data Config updated
	 * @throws IOException if JSON file could not be updated
	 */
	public static void updateConfigFile(JSONObject data) throws IOException {
		FileWriter file = new FileWriter("resources/config.json");
		file.write(data.toString());
		file.close();
	}
	
	/**
	 * Reads JSON data file
	 * @return User data
	 * @throws FileNotFoundException if JSON file not found
	 */
	public static JSONObject parseDataFile() throws FileNotFoundException {
		JSONTokener parser = new JSONTokener(new FileReader("resources/data.json"));
		JSONObject data = new JSONObject(parser);
		
		return data;
	}
	
	/**
	 * Updates JSON data file
	 * @param data Data updated
	 * @throws IOException if JSON file could not be updated
	 */
	public static void updateDataFile(JSONObject data) throws IOException {
		FileWriter file = new FileWriter("resources/data.json");
		file.write(data.toString());
		file.close();
	}
}
