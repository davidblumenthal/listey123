/**
 * 
 */
package com.blumenthal.listey;

import java.lang.reflect.Type;
import java.util.Map;

import com.blumenthal.listey.ItemInfo.ItemStatus;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class ItemInfoJsonAdapter implements JsonDeserializer<ItemInfo> {
	@Override
	public ItemInfo deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {			
		JsonObject itemJson = json.getAsJsonObject();
		ItemInfo item = new ItemInfo();
		item.uniqueId = itemJson.get("uniqueId").getAsString();
		if (itemJson.has("count")) {
			item.count = itemJson.get("count").getAsInt();
		}
		item.name = itemJson.get("name").getAsString();
		String statusString = itemJson.get("state").getAsString();
		item.status = ItemStatus.valueOf(statusString);

		item.lastUpdate = itemJson.get("lastUpdate").getAsLong();
		if (itemJson.has("categories")) {
			JsonObject categoriesJson = itemJson.get("categories").getAsJsonObject();
			for ( Map.Entry<String,JsonElement> catEntry : categoriesJson.entrySet()){
				ItemCategoryInfo catInfo = context.deserialize(catEntry.getValue(), ItemCategoryInfo.class);
				catInfo.uniqueCategoryId = catEntry.getKey();
				item.categories.put(catEntry.getKey(), catInfo);
			}//for categoriesJson
		}//if has categories

		return item;
	}//ItemInfoSerializer
}//ItemInfoJsonAdapter