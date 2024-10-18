package net.foxavis.kingdoms.util;

/**
 * Represents an exception that occurs within the Kingdoms plugin.
 */
public class KingdomException extends Exception {

	public KingdomException() { }

	public KingdomException(String message) { super(message); }

	public KingdomException(String message, Throwable cause) { super(message, cause); }

}
