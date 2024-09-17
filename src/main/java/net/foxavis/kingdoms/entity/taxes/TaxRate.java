package net.foxavis.kingdoms.entity.taxes;

/**
 * A tax rate that can be used to calculate the tax for a given balance.
 * @author Kyomi
 */
public interface TaxRate {

	/**
	 * Calculate the tax for a given balance.
	 * @param balance The balance to calculate the tax for
	 * @return The tax for the given balance
	 */
	double calculateTax(double balance);

}
