package net.foxavis.kingdoms.entity.taxes;

/**
 * A fixed tax rate that does not change based on the balance.
 * @see TaxRate
 *
 * @author Kyomi
 */
public class FixedTaxRate implements TaxRate {

	private double rate;

	/**
	 * Create a new fixed tax rate with the given rate.
	 * @param rate The flat tax rate
	 */
	public FixedTaxRate(double rate) {
		this.rate = rate;
	}

	/**
	 * Get the flat tax rate.
	 * @return The flat tax rate
	 */
	public double getRate() { return rate; }

	/**
	 * Set the flat tax rate.
	 * @param rate The flat tax rate
	 */
	public void setRate(double rate) { this.rate = rate; }

	@Override public double calculateTax(double balance) {
		return rate;
	}

	@Override public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof FixedTaxRate that)) return false;

		return Double.compare(rate, that.rate) == 0;
	}

	@Override public int hashCode() {
		return Double.hashCode(rate);
	}
}