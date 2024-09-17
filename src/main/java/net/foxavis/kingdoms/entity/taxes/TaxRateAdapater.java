package net.foxavis.kingdoms.entity.taxes;

import com.google.gson.*;

import java.lang.reflect.Type;

public class TaxRateAdapater implements JsonSerializer<TaxRate>, JsonDeserializer<TaxRate> {

	@Override public JsonElement serialize(TaxRate source, Type sourceType, JsonSerializationContext context) {
		JsonObject jsonObject = new JsonObject();

		if (source instanceof FixedTaxRate fixed) {
			jsonObject.addProperty("type", "fixed");
			jsonObject.addProperty("rate", fixed.getRate());
		} else if (source instanceof PercentTaxRate percent) {
			jsonObject.addProperty("type", "percent");
			jsonObject.addProperty("rate", percent.getPercentRate());
		} else {
			throw new JsonParseException("Unknown TaxRate type: " + source.getClass().getName());
		}

		return jsonObject;
	}

	@Override public TaxRate deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
		JsonObject jsonObject = json.getAsJsonObject();
		String typeString = jsonObject.get("type").getAsString();
		double rate = jsonObject.get("rate").getAsDouble();

		return switch (typeString) {
			case "fixed" -> new FixedTaxRate(rate);
			case "percent" -> new PercentTaxRate(rate);
			default -> throw new JsonParseException("Unknown TaxRate type: " + typeString);
		};
	}
}
