package net.foxavis.kingdoms.entity;

/**
 * An interface to declare that an object has both a name and description.
 * @author Kyomi
 */
public interface Describable {

	/**
	 * Returns the name of the given Describable
	 * @return the name of the Describable
	 */
	String getName();

	/**
	 * Sets the name of the given Describable
	 * @param name the new name to set
	 */
	void setName(String name);

	/**
	 * Returns the description of the given Describable
	 * @return the name of the Describable
	 */
	String getDescription();

	/**
	 * Sets the description of the given Describable
	 * @param description the new description to set
	 */
	void setDescription(String description);
}