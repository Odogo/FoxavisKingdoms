package net.foxavis.kingdoms.entity.taxes;

public class PercentTaxRate implements TaxRate {

	private double percentRate;

	/**
	 * Create a new percentage tax rate with the given rate.
	 * @param percentRate The percentage tax rate, as a decimal
	 */
	public PercentTaxRate(double percentRate) {
		this.percentRate = percentRate;
	}

	/**
	 * Get the percentage tax rate.
	 * @return The percentage tax rate, as a decimal
	 */
	public double getPercentRate() { return percentRate; }

	/**
	 * Set the percentage tax rate.
	 * @param percentRate The percentage tax rate, as a decimal
	 */
	public void setPercentRate(double percentRate) { this.percentRate = percentRate; }

	@Override public double calculateTax(double balance) {
		return balance * percentRate;
	}

	@Override public final boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof PercentTaxRate that)) return false;

		return Double.compare(percentRate, that.percentRate) == 0;
	}

	@Override public int hashCode() {
		return Double.hashCode(percentRate);
	}
}
