package com.chatalk.app.models;

import java.io.Serializable;
import java.util.Date;

/**
 * Log message entity
 */
public class LogMessage implements Serializable {
	private static final long serialVersionUID = 4565127925685093204L;

	/**
	 * Message author
	 * Null if type = 0
	 */
	private String author;
	
	/**
	 * Message recipient
	 * Null if type = 0 or type = 2
	 */
	private String recipient;
	
	/**
	 * Message type
	 * 0 -> Server message
	 * 1 -> Private message
	 * 2 -> Broadcast message
	 */
	private int type;
	
	/**
	 * Message content
	 */
	private String content;
	
	/**
	 * Message date
	 */
	private long date;

	
	/**
	 * Returns message author
	 * @return message author
	 */
	public String getAuthor() {
		return author;
	}
	
	/**
	 * Updates message author
	 * @param author The new author
	 */
	public void setAuthor(String author) {
		this.author = author;
	}
	
	/**
	 * Returns message recipient
	 * @return message recipient
	 */
	public String getRecipient() {
		return recipient;
	}
	
	/**
	 * Updates message recipient
	 * @param recipient The new recipient
	 */
	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}
	
	/**
	 * Returns message type
	 * @return message type
	 */
	public int getType() {
		return type;
	}

	/**
	 * Updates message type
	 * @param The new message type
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Returns message content
	 * @return message content
	 */
	public String getContent() {
		return content;
	}

	/**
	 * Updates message content
	 * @param content The new message content
	 */
	public void setContent(String content) {
		this.content = content;
	}

	/**
	 * Returns message date
	 * @return message date
	 */
	public Date getDate() {
		return new Date(date);
	}

	/**
	 * Updates message date
	 * @param date The new message date
	 */
	public void setDate(long date) {
		this.date = date;
	}
}

